/*
 * Copyright (c) 2012-2025 Jesse Gallagher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package model;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.jnosql.mapping.EntityPrePersist;
import org.openntf.xsp.jakarta.nosql.communication.driver.DominoConstants;
import org.openntf.xsp.jakarta.nosql.mapping.extension.DominoRepository;
import org.openntf.xsp.jakarta.nosql.mapping.extension.ItemFlags;
import org.openntf.xsp.jakarta.nosql.mapping.extension.ItemStorage;
import org.openntf.xsp.jakarta.nosql.mapping.extension.ViewDocuments;
import org.openntf.xsp.jakarta.nosql.mapping.extension.ViewEntries;
import org.openntf.xsp.jakarta.nosql.mapping.extension.ViewQuery;

import com.ibm.commons.util.StringUtil;

import bean.MarkdownBean;
import jakarta.data.page.PageRequest;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;
import jakarta.validation.constraints.NotNull;
import model.event.PostEvent;
import model.event.PostEvent.Type;

@Entity
public class Post {
	public interface PostRepository extends DominoRepository<Post, String> {
		Optional<Post> findPostByKey(String key);
		
		@ViewDocuments("PostsByPostID")
		Optional<Post> findByPostId(ViewQuery query);

		List<Post> findByTag(String tag);

		@ViewDocuments("PostsByMonth")
		List<Post> findByMonth(ViewQuery query);

		@ViewEntries(value="PostsByMonth", maxLevel=0)
		Stream<Post> postMonths();

		@ViewDocuments("PostsDescending")
		List<Post> search(ViewQuery query);

		@ViewDocuments("PostsDescending")
		List<Post> homeList(PageRequest page);

		@ViewDocuments("PostsByThread")
		List<Post> findByThread(ViewQuery query);

		Optional<Post> findByName(String name);
		
		@ViewEntries(value="PostsByTag", maxLevel=0)
		Stream<Post> postTags();
	}
	
	public enum Status {
		Posted, Draft;

		public static Status valueFor(final String optionalName) {
			for(Status status : values()) {
				if(StringUtil.equalsIgnoreCase(status.name(), optionalName)) {
					return status;
				}
			}
			return Draft;
		}
	}

	@Id @Column private String id;
	@Column private int postIdInt;
	@Column private String postId;
	@Column("$$TITLE") private String title;
	@Column @NotNull private OffsetDateTime posted;
	@Column("$$Creator") private String postedBy;
	@Column private String bodyMarkdown;
	@Column("Body") @ItemStorage(type = ItemStorage.Type.MIME) private String bodyHtml;
	@Column("Tags") private List<String> tags;
	@Column private String thread;
	@Column private Status status;
	@Column private String name;
	@Column(DominoConstants.FIELD_MDATE) private OffsetDateTime modified;
	@Column private String modifiedBy;
	@Column private boolean hasGoneLive;
	@Column private String summary;
	@Column("$PostMonth") @ItemStorage(insertable = false) private String postMonth;
	@Column @ItemFlags(readers = true) private List<String> readers;

	@Inject private Event<PostEvent> postEvent;

	void querySave(@Observes final EntityPrePersist entity) {
		if(!(entity.get() instanceof Post)) {
			return;
		}
		Post post = (Post)entity.get();

		// Auto-generate a slug if not already present
		if(StringUtil.isEmpty(post.getName()) && post.getStatus() == Status.Posted) {
			PostRepository posts = CDI.current().select(PostRepository.class).get();

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
		MarkdownBean markdown = CDI.current().select(MarkdownBean.class).get();
		post.setBodyHtml(markdown.toHtml(StringUtil.toString(post.getBodyMarkdown())));

		// Set the posted time if this is the first time it's posted or has gone live
		if(post.posted == null || post.status == Status.Posted && !post.hasGoneLive) {
			post.setPosted(OffsetDateTime.now());
		}
		if(post.status == Status.Posted && !post.hasGoneLive) {
			post.setHasGoneLive(true);

			postEvent.fire(new PostEvent(this, Type.PUBLISH));
		} else if(post.status == Status.Posted) {
			postEvent.fire(new PostEvent(this, Type.UPDATE));
		}
	}

	// *******************************************************************************
	// * Utility getters
	// *******************************************************************************

	public int getCommentCount() {
		Comment.CommentRepository comments = CDI.current().select(Comment.CommentRepository.class).get();
		return comments.findByPostId(ViewQuery.query().category(this.postId)).size();
	}

	public int getPostedYear() {
		return posted.get(ChronoField.YEAR);
	}
	public int getPostedMonth() {
		return posted.get(ChronoField.MONTH_OF_YEAR);
	}
	public int getPostedDay() {
		return posted.get(ChronoField.DAY_OF_MONTH);
	}

	public Date getPostedDate() {
		return Date.from(posted.toInstant());
	}

	public boolean matchesPostedDate(final int year, final int month, final int day) {
		return year == getPostedYear() && month == getPostedMonth() && day == getPostedDay();
	}

	public String getSlug() {
		if(StringUtil.isEmpty(name)) {
			return postId;
		} else {
			return name;
		}
	}

	public List<Post> getThreadInfo() {
		return CDI.current().select(PostRepository.class).get().findByThread(ViewQuery.query().category(getThread()));
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getPostIdInt() {
		return postIdInt;
	}

	public void setPostIdInt(int postIdInt) {
		this.postIdInt = postIdInt;
	}

	public String getPostId() {
		return postId;
	}

	public void setPostId(String postId) {
		this.postId = postId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public OffsetDateTime getPosted() {
		return posted;
	}

	public void setPosted(OffsetDateTime posted) {
		this.posted = posted;
	}

	public String getPostedBy() {
		return postedBy;
	}

	public void setPostedBy(String postedBy) {
		this.postedBy = postedBy;
	}

	public String getBodyMarkdown() {
		return bodyMarkdown;
	}

	public void setBodyMarkdown(String bodyMarkdown) {
		this.bodyMarkdown = bodyMarkdown;
	}

	public String getBodyHtml() {
		return bodyHtml;
	}

	public void setBodyHtml(String bodyHtml) {
		this.bodyHtml = bodyHtml;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public String getThread() {
		return thread;
	}

	public void setThread(String thread) {
		this.thread = thread;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public OffsetDateTime getModified() {
		return modified;
	}

	public void setModified(OffsetDateTime modified) {
		this.modified = modified;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public boolean isHasGoneLive() {
		return hasGoneLive;
	}

	public void setHasGoneLive(boolean hasGoneLive) {
		this.hasGoneLive = hasGoneLive;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}
	
	public String getPostMonth() {
		return postMonth;
	}
	public void setPostMonth(String postMonth) {
		this.postMonth = postMonth;
	}
	
	public List<String> getReaders() {
		return readers;
	}
	public void setReaders(List<String> readers) {
		this.readers = readers;
	}
}
