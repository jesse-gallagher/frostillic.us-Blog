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
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.eclipse.jnosql.mapping.EntityPostPersist;
import org.eclipse.jnosql.mapping.EntityPrePersist;
import org.openntf.xsp.jakarta.nosql.mapping.extension.DominoRepository;
import org.openntf.xsp.jakarta.nosql.mapping.extension.ViewDocuments;

import com.ibm.commons.util.StringUtil;

import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import model.event.MicroPostEvent;

@Entity
public class MicroPost {
	public interface MicroPostRepository extends DominoRepository<MicroPost, String> {
		@ViewDocuments("MicropostsByPosted")
		List<MicroPost> findAllByPosted();
	}


	@Id @Column private String id;
	@Column @NotEmpty private String postId;
	@Column private String name;
	@Column @NotEmpty private String content;
	@Column @NotNull private OffsetDateTime posted;
	// TODO attachments

	@Inject
	Event<MicroPostEvent> microPostEvent;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPostId() {
		return postId;
	}

	public void setPostId(String postId) {
		this.postId = postId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public OffsetDateTime getPosted() {
		return posted;
	}

	public void setPosted(OffsetDateTime posted) {
		this.posted = posted;
	}

	void querySave(@Observes final EntityPrePersist event) {
		if(!(event.get() instanceof MicroPost)) {
			return;
		}
		var post = (MicroPost)event.get();

		if(StringUtil.isEmpty(post.getPostId())) {
			post.setPostId(UUID.randomUUID().toString());
		}
		if(post.getPosted() == null) {
			post.setPosted(OffsetDateTime.now());
		}
	}

	void postSave(@Observes final EntityPostPersist event) {
		if(!(event.get() instanceof MicroPost)) {
			return;
		}
		var post = (MicroPost)event.get();
		microPostEvent.fire(new MicroPostEvent(post));
	}

	public Date getPostedDate() {
		return Date.from(posted.toInstant());
	}
}
