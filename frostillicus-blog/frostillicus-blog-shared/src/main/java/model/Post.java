/**
 * Copyright © 2016-2018 Jesse Gallagher
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

import lombok.Data;
import lombok.NoArgsConstructor;
import org.darwino.jnosql.artemis.extension.converter.ISOOffsetDateTimeConverter;
import org.jnosql.artemis.Column;
import org.jnosql.artemis.Convert;
import org.jnosql.artemis.Entity;
import org.jnosql.artemis.Id;

import javax.enterprise.inject.spi.CDI;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.List;

@Entity @Data @NoArgsConstructor
public class Post {
	public enum Status {
		Posted, Draft
	}
	
	@Id @Column private String id;
	@Column private int postIdInt;
	@Column private String postId;
	@Column private String title;
	@Column @NotNull @Convert(ISOOffsetDateTimeConverter.class) private OffsetDateTime posted;
	@Column private String postedBy;
	@Column private String bodyMarkdown;
	@Column private String bodyHtml;
	@Column("_tags") private List<String> tags;
	@Column private String thread;
	@Column private Status status;
	@Column private String name;
	@Column @Convert(ISOOffsetDateTimeConverter.class) private OffsetDateTime modified;
	@Column private String modifiedBy;
	
	public int getCommentCount() {
		return CDI.current().select(CommentRepository.class).get().findByPostId(getPostId()).size();
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
	
	public List<Post> getThreadInfo() {
		return CDI.current().select(PostRepository.class).get().findByThread(getThread());
	}
}
