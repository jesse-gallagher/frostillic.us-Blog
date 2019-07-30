/**
 * Copyright © 2016-2019 Jesse Gallagher
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

import com.darwino.commons.util.StringUtil;

import bean.UserInfoBean;
import jakarta.nosql.mapping.Column;
import jakarta.nosql.mapping.Convert;
import jakarta.nosql.mapping.Entity;
import jakarta.nosql.mapping.Id;

import javax.enterprise.inject.spi.CDI;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.List;

@Entity @Data @NoArgsConstructor
public class Post {
	public enum Status {
		Posted, Draft;
		
		public static Status valueFor(String optionalName) {
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
		CommentRepository comments = CDI.current().select(CommentRepository.class).get();
		UserInfoBean userInfo = CDI.current().select(UserInfoBean.class).get();
		if(userInfo.isAdmin()) {
			return comments.findByPostIdAdmin(getPostId()).size();
		} else {
			return comments.findByPostId(getPostId()).size();
		}
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
	
	public boolean matchesPostedDate(int year, int month, int day) {
		return year == getPostedYear() && month == getPostedMonth() && day == getPostedDay();
	}
	
	public String getSlug() {
		if(StringUtil.isEmpty(name)) {
			return id;
		} else {
			return name;
		}
	}
	
	public List<Post> getThreadInfo() {
		return CDI.current().select(PostRepository.class).get().findByThread(getThread());
	}
}
