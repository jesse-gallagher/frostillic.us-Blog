package bean;

import java.security.Principal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.eclipse.jnosql.mapping.EntityPrePersist;

import com.ibm.commons.util.StringUtil;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import model.Post;
import model.Post.Status;
import model.event.PostEvent;
import model.event.PostEvent.Type;

/**
 * This bean provides query-save processing for {@link Post} instances.
 */
@ApplicationScoped
public class PostProcessorBean {
	@Inject
	private Event<PostEvent> postEvent;
	
	@Inject
	private Post.PostRepository posts;
	
	@Inject
	private MarkdownBean markdown;

	public void querySave(@Observes final EntityPrePersist entity) {
		if(entity.get() instanceof Post post) {
			var status = post.getStatus();
			
			// Auto-generate a slug if not already present
			if(StringUtil.isEmpty(post.getName()) && status == Status.Posted) {
				String baseName = StringUtil.toString(post.getTitle()).toLowerCase()
						.replaceAll("[^\\w]", "-") //$NON-NLS-1$ //$NON-NLS-2$
						.replaceAll("--+", "-"); //$NON-NLS-1$ //$NON-NLS-2$
				int dedupe = 1;
				String name = baseName;

				Optional<Post> existing = posts.findByName(name);
				String id = post.getId();
				while(existing.isPresent() && (StringUtil.isEmpty(id) || !StringUtil.equals(id, existing.get().getId()))) {
					name = baseName + ++dedupe;
					existing = posts.findByName(name);
				}

				post.setName(name);
			}

			// Update the calculated HTML body
			post.setBodyHtml(markdown.toHtml(StringUtil.toString(post.getBodyMarkdown())));
			
			// Set reader fields based on the status
			if(status == Status.Draft) {
				post.setReaders(List.of("[Admin]", post.getPostedBy()));
			} else {
				post.setReaders(null);
			}

			// Set the posted time if this is the first time it's posted or has gone live
			if(status == null || status == Status.Posted && !post.isHasGoneLive()) {
				post.setPosted(OffsetDateTime.now());
			}
			if(status == Status.Posted && !post.isHasGoneLive()) {
				post.setHasGoneLive(true);

				postEvent.fire(new PostEvent(post, Type.PUBLISH));
			} else if(status == Status.Posted) {
				postEvent.fire(new PostEvent(post, Type.UPDATE));
			}
		}
	}
}
