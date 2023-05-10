/*
 * Copyright © 2012-2023 Jesse Gallagher
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
import jakarta.nosql.mapping.Column;
import jakarta.nosql.mapping.Convert;
import jakarta.nosql.mapping.Entity;
import jakarta.nosql.mapping.Id;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.Date;

@Entity @Data @NoArgsConstructor
public class Comment {
	@Id @Column private String id;
	@Column("commentId") @NotEmpty private String commentId;
	@Column("postId") @NotEmpty private String postId;
	@Column @NotNull @Convert(ISOOffsetDateTimeConverter.class) private OffsetDateTime posted;
	@Column("postedBy") @NotEmpty private String postedBy;
	@Column("postedByEmail") @Email private String postedByEmail;
	@Column("postedByUrl") private String postedByUrl;
	@Column("bodyMarkdown") private String bodyMarkdown;
	@Column("bodyHtml") @NotEmpty private String bodyHtml;
	@Column("http_referer") private String httpReferer;
	@Column("http_user_agent") private String httpUserAgent;
	@Column("remote_addr") private String httpRemoteAddr;
	@Column("akismetspam") private boolean akismetSpam;
	@Column private boolean isConflict;

	public Date getPostedDate() {
		return Date.from(posted.toInstant());
	}
}
