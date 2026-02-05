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

import org.darwino.jnosql.artemis.extension.converter.ISOOffsetDateTimeConverter;

import jakarta.nosql.mapping.Column;
import jakarta.nosql.mapping.Convert;
import jakarta.nosql.mapping.Entity;
import jakarta.nosql.mapping.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Entity
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
	
	public Comment() {
		
	}

	public Date getPostedDate() {
		return Date.from(posted.toInstant());
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCommentId() {
		return commentId;
	}

	public void setCommentId(String commentId) {
		this.commentId = commentId;
	}

	public String getPostId() {
		return postId;
	}

	public void setPostId(String postId) {
		this.postId = postId;
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

	public String getPostedByEmail() {
		return postedByEmail;
	}

	public void setPostedByEmail(String postedByEmail) {
		this.postedByEmail = postedByEmail;
	}

	public String getPostedByUrl() {
		return postedByUrl;
	}

	public void setPostedByUrl(String postedByUrl) {
		this.postedByUrl = postedByUrl;
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

	public String getHttpReferer() {
		return httpReferer;
	}

	public void setHttpReferer(String httpReferer) {
		this.httpReferer = httpReferer;
	}

	public String getHttpUserAgent() {
		return httpUserAgent;
	}

	public void setHttpUserAgent(String httpUserAgent) {
		this.httpUserAgent = httpUserAgent;
	}

	public String getHttpRemoteAddr() {
		return httpRemoteAddr;
	}

	public void setHttpRemoteAddr(String httpRemoteAddr) {
		this.httpRemoteAddr = httpRemoteAddr;
	}

	public boolean isAkismetSpam() {
		return akismetSpam;
	}

	public void setAkismetSpam(boolean akismetSpam) {
		this.akismetSpam = akismetSpam;
	}

	public boolean isConflict() {
		return isConflict;
	}

	public void setConflict(boolean isConflict) {
		this.isConflict = isConflict;
	}
}
