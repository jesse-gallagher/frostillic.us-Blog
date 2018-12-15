package api.atompub;

import api.AtomPubAPI;
import bean.MarkdownBean;
import com.darwino.commons.json.JsonException;
import com.darwino.commons.util.PathUtil;
import com.darwino.commons.util.StringUtil;
import com.darwino.commons.xml.DomUtil;
import com.darwino.commons.xml.XPathUtil;
import com.darwino.jsonstore.Session;
import com.rometools.rome.feed.synd.*;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedOutput;
import model.Post;
import model.PostRepository;
import model.PostUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.xpath.XPathExpressionException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Path(AtomPubAPI.BASE_PATH + "/{blogId}")
@RolesAllowed("admin")
public class BlogResource {
    public static final int PAGE_LENGTH = 100;

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
    public String get(@QueryParam("start") String startParam) throws FeedException, JsonException {
        SyndFeed feed = new SyndFeedImpl();
        feed.setFeedType("atom_1.0"); //$NON-NLS-1$
        feed.setTitle(translation.getString("appTitle")); //$NON-NLS-1$
        feed.setDescription(translation.getString("appDescription")); //$NON-NLS-1$
        feed.setLink(translation.getString("baseUrl")); //$NON-NLS-1$
        feed.setUri(resolveUrl(AtomPubAPI.BLOG_ID));

        // Figure out the starting point
        int start = Math.max(PostUtil.parseStartParam(startParam), 0);
        List<Post> result = posts.homeList(start, PAGE_LENGTH);

        // Add a nav link
        List<SyndLink> links = new ArrayList<>();
        SyndLink first = new SyndLinkImpl();
        first.setRel("first");
        first.setHref(resolveUrl(AtomPubAPI.BLOG_ID));
        links.add(first);

        if(start + PAGE_LENGTH < PostUtil.getPostCount()) {
            // Then add nav links
            SyndLink next = new SyndLinkImpl();
            next.setRel("next");
            next.setHref(resolveUrl(AtomPubAPI.BLOG_ID) + "?start=" + (start + PAGE_LENGTH));
            links.add(next);
        }
        feed.setLinks(links);


        feed.setEntries(result.stream()
                .map(this::toEntry)
                .collect(Collectors.toList()));

        return new SyndFeedOutput().outputString(feed);
    }

    @POST
    @Produces("application/atom+xml")
    public Response post(Document xml) throws XPathExpressionException, JsonException, URISyntaxException, FeedException {

        Post post = PostUtil.createPost();
        post.setPostedBy(darwinoSession.getUser().getDn());
        updatePost(post, xml);

        return Response.created(new URI(resolveUrl(AtomPubAPI.BLOG_ID, post.getPostId()))).entity(toAtomXml(post)).build();
    }

    @GET
    @Path("{entryId}")
    @Produces("application/atom+xml")
    public String getEntry(@PathParam("entryId") String postId) throws FeedException, XPathExpressionException {
        Post post = posts.findPost(postId).orElseThrow(() -> new IllegalArgumentException("Unable to find post matching ID " + postId)); //$NON-NLS-1$
        return toAtomXml(post);
    }

    @PUT
    @Path("{entryId}")
    public Response updateEntry(@PathParam("entryId") String postId, Document xml) throws XPathExpressionException {
        Post post = posts.findPost(postId).orElseThrow(() -> new IllegalArgumentException("Unable to find post matching ID " + postId)); //$NON-NLS-1$
        updatePost(post, xml);
        return Response.ok().build();
    }

    @DELETE
    @Path("{entryId}")
    public Response deleteEntry(@PathParam("entryId") String postId) {
        // TODO figure out why this doesn't work with existing posts. I imagine it's to do with Darwino's treatment of editors
        Post post = posts.findPost(postId).orElseThrow(() -> new IllegalArgumentException("Unable to find post matching ID " + postId)); //$NON-NLS-1$
        posts.deleteById(post.getId());
        return Response.ok().build();
    }

    private SyndEntry toEntry(Post post) {
        SyndEntry entry = new SyndEntryImpl();
        entry.setAuthor(post.getPostedBy());
        entry.setTitle(post.getTitle());
        entry.setPublishedDate(post.getPosted());
        Date mod = post.getModified();
        entry.setUpdatedDate(mod == null ? post.getPosted() : mod);

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
        edit.setHref(resolveUrl("posts", post.getId()));
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

    private void updatePost(Post post, Document xml) throws XPathExpressionException {
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
