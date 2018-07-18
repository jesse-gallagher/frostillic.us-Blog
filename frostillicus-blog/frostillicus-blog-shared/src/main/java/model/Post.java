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

import org.darwino.jnosql.artemis.extension.ISODateConverter;
import org.jnosql.artemis.Column;
import org.jnosql.artemis.Convert;
import org.jnosql.artemis.Entity;
import org.jnosql.artemis.Id;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.enterprise.inject.spi.CDI;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Entity @Data @NoArgsConstructor
public class Post {
	public enum Status {
		Posted, Draft
	}
	
	@Id @Column private String id;
	@Column("postId") private String postId;
	@Column @NotEmpty private String title;
	@Column @NotNull @Convert(ISODateConverter.class) private Date posted;
	@Column("postedBy") @NotEmpty private String postedBy;
	@Column("bodyMarkdown") private String bodyMarkdown;
	@Column("bodyHtml") @NotEmpty private String bodyHtml;
	@Column("_tags") private List<String> tags;
	@Column private String thread;
	@Column private Status status;
	
	public int getCommentCount() {
		return CDI.current().select(CommentRepository.class).get().findByPostId(getPostId()).size();
	}
	
	public int getPostedYear() {
		return getCalendar().get(Calendar.YEAR);
	}
	public int getPostedMonth() {
		return getCalendar().get(Calendar.MONTH);
	}
	public int getPostedDay() {
		return getCalendar().get(Calendar.DAY_OF_MONTH);
	}
	
	public List<Post> getThreadInfo() {
		return CDI.current().select(PostRepository.class).get().findByThread(getThread());
	}
	
	// *******************************************************************************
	// * Internal utility methods
	// *******************************************************************************
	
	private Calendar getCalendar() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(getPosted());
		return cal;
	}
}
