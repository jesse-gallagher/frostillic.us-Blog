package api.atompub;

import api.AtomPubAPI;
import bean.MarkdownBean;
import com.darwino.commons.json.JsonException;
import com.darwino.commons.util.PathUtil;
import com.darwino.commons.util.StringUtil;
import com.darwino.commons.xml.DomUtil;
import com.darwino.commons.xml.XPathUtil;
import com.darwino.jsonstore.Session;
import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.synd.*;
import com.rometools.rome.feed.synd.impl.ConverterForAtom10;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedOutput;
import com.rometools.rome.io.impl.Atom10Generator;
import lombok.SneakyThrows;
import model.Post;
import model.PostRepository;
import model.PostUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Path(AtomPubAPI.BASE_PATH + "/{blogId}")
public class BlogResource {


    @Inject
    @Named("translation")
    ResourceBundle translation;

    @Context
    UriInfo uriInfo;

    @Inject
    PostRepository posts;

    @Inject
    Session darwinoSession;

    @Inject
    MarkdownBean markdown;

    @GET
    @Produces("application/atom+xml")
    public String get() throws FeedException {
        SyndFeed feed = new SyndFeedImpl();
        feed.setFeedType("atom_1.0"); //$NON-NLS-1$
        feed.setTitle(translation.getString("appTitle")); //$NON-NLS-1$
        feed.setDescription(translation.getString("appDescription")); //$NON-NLS-1$
        feed.setLink(translation.getString("baseUrl")); //$NON-NLS-1$
        feed.setUri(resolveUrl());

        feed.setEntries(posts.homeList().stream()
                .map(this::toEntry)
                .collect(Collectors.toList()));

        return new SyndFeedOutput().outputString(feed);
    }

    @POST
    @Produces("application/atom+xml")
    public Response post(Document xml) throws XPathExpressionException, JsonException, URISyntaxException, FeedException {
        // TODO convert to ROME

        String title = XPathUtil.node(xml,"/*[name()='entry']/*[name()='title']").getTextContent();
        String body = XPathUtil.node(xml, "/*[name()='entry']/*[name()='content']").getTextContent();
        NodeList tagsNodes = XPathUtil.nodes(xml,"/*[name()='entry']/*[name()='category']");
        List<String> tags = IntStream.range(0, tagsNodes.getLength())
            .mapToObj(tagsNodes::item)
            .map(Element.class::cast)
            .map(el -> el.getAttribute("term"))
            .collect(Collectors.toList());

        boolean posted = !"no".equals(XPathUtil.node(xml, "*[name()='entry']/*[name()='app:control']/*[name()='app:draft']").getTextContent());

        Post post = PostUtil.createPost();
        post.setPostedBy(darwinoSession.getUser().getDn());

        if(StringUtil.isEmpty(post.getName())) {
            post.setName(StringUtil.toString(title).toLowerCase().replaceAll("\\s+", "-"));
        }
        post.setTitle(title);
        post.setBodyMarkdown(body);
        post.setBodyHtml(markdown.toHtml(body));
        post.setTags(tags);
        post.setStatus(posted ? Post.Status.Posted : Post.Status.Draft);
        post.setPosted(new Date());
        posts.save(post);

        return Response.created(new URI(resolveUrl(AtomPubAPI.BLOG_ID, post.getPostId()))).entity(toAtomXml(post)).build();
    }

    @GET
    @Path("{entryId}")
    @Produces("application/atom+xml")
    public String getEntry(@PathParam("entryId") String postId) throws FeedException, XPathExpressionException {
        Post post = posts.findPost(postId).orElseThrow(() -> new IllegalArgumentException("Unable to find post matching ID " + postId)); //$NON-NLS-1$
        return toAtomXml(post);
    }

    private SyndEntry toEntry(Post post) {
        SyndEntry entry = new SyndEntryImpl();
        entry.setAuthor(post.getPostedBy());
        entry.setTitle(post.getTitle());
        entry.setPublishedDate(post.getPosted());

        List<SyndContent> contents = new ArrayList<>();

        String bodyMarkdown = post.getBodyMarkdown();

        if(StringUtil.isNotEmpty(bodyMarkdown)) {
            SyndContent markdown = new SyndContentImpl();
            markdown.setType("text/markdown"); //$NON-NLS-1$
            markdown.setValue(bodyMarkdown);
            contents.add(markdown);
        } else {
            SyndContent content = new SyndContentImpl();
            content.setType(MediaType.TEXT_HTML);
            content.setValue(post.getBodyHtml());
            contents.add(content);
        }

        entry.setContents(contents);

        entry.setCategories(post.getTags().stream().map(this::toCategory).collect(Collectors.toList()));

        // Add links
        SyndLink read = new SyndLinkImpl();
        read.setHref(resolveUrl("posts", post.getId()));
        SyndLink edit = new SyndLinkImpl();
        edit.setHref(resolveUrl("posts", post.getId(), "edit"));
        edit.setRel("edit"); //$NON-NLS-1$
        entry.setLinks(Arrays.asList(read, edit));


        return entry;
    }

    private String toAtomXml(Post post) throws FeedException, XPathExpressionException {
        SyndEntry entry = toEntry(post);
        SyndFeed feed = new SyndFeedImpl();
        feed.setFeedType("atom_1.0"); //$NON-NLS-1$
        feed.setEntries(Arrays.asList(entry));
        String feedXml = new SyndFeedOutput().outputString(feed);
        Document feedDoc = DomUtil.createDocument(feedXml);
        Node entryElement = XPathUtil.node(feedDoc, "/*[name()='feed']/*[name()='entry']");

        return DomUtil.getXMLString(entryElement, false, true);
    }

    private SyndCategory toCategory(String tag) {
        SyndCategory cat = new SyndCategoryImpl();
        cat.setName(tag);
        return cat;
    }

    private String resolveUrl(String... parts) {
        URI baseUri = uriInfo.getBaseUri();
        String uri = PathUtil.concat(baseUri.toString(), AtomPubAPI.BASE_PATH);
        for(String part : parts) {
            uri = PathUtil.concat(uri, part);
        }
        return uri;
    }
}
