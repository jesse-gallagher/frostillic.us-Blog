/*
 * Copyright © 2012-2020 Jesse Gallagher
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
import java.util.UUID;

import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import org.darwino.jnosql.artemis.extension.converter.ISOOffsetDateTimeConverter;

import com.darwino.commons.util.StringUtil;

import jakarta.nosql.mapping.Column;
import jakarta.nosql.mapping.Convert;
import jakarta.nosql.mapping.Entity;
import jakarta.nosql.mapping.EntityPostPersit;
import jakarta.nosql.mapping.EntityPrePersist;
import jakarta.nosql.mapping.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.event.MicroPostEvent;

@Entity @Data @NoArgsConstructor
public class MicroPost {
	@Id @Column private String id;
	@Column @NotEmpty private String postId;
	@Column private String name;
	@Column @NotEmpty private String content;
	@Column @NotNull @Convert(ISOOffsetDateTimeConverter.class) private OffsetDateTime posted;
	@Column private boolean isConflict;

	// TODO attachments

	@Inject
	Event<MicroPostEvent> microPostEvent;

	void querySave(@Observes final EntityPrePersist event) {
		if(!(event.getValue() instanceof MicroPost)) {
			return;
		}
		MicroPost post = (MicroPost)event.getValue();

		if(StringUtil.isEmpty(post.getPostId())) {
			post.setPostId(UUID.randomUUID().toString());
		}
		if(post.getPosted() == null) {
			post.setPosted(OffsetDateTime.now());
		}
	}

	void postSave(@Observes final EntityPostPersit event) {
		if(!(event.getValue() instanceof MicroPost)) {
			return;
		}
		MicroPost post = (MicroPost)event.getValue();
		microPostEvent.fire(new MicroPostEvent(post));
	}

	public Date getPostedDate() {
		return Date.from(posted.toInstant());
	}
}
