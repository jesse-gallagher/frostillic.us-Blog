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

import org.darwino.jnosql.artemis.extension.converter.ISOOffsetDateTimeConverter;

import jakarta.nosql.mapping.Column;
import jakarta.nosql.mapping.Convert;
import jakarta.nosql.mapping.Entity;
import jakarta.nosql.mapping.Id;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Entity
public class Webmention {
	public enum Type {
		Post
	}

	@Id @Column private String id;
	@Column @NotNull private Type type;
	@Column @NotEmpty private String targetId;
	@Column @NotEmpty private String source;
	@Column @NotNull @Convert(ISOOffsetDateTimeConverter.class) private OffsetDateTime posted;
	@Column private boolean verified;
	@Column private boolean approved;
	@Column private String problemCause;
	@Column private String sourceTitle;
	@Column("http_referer") private String httpReferer;
	@Column("http_user_agent") private String httpUserAgent;
	@Column("remote_addr") private String httpRemoteAddr;
	@Column private boolean isConflict;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}
	public String getTargetId() {
		return targetId;
	}
	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public OffsetDateTime getPosted() {
		return posted;
	}
	public void setPosted(OffsetDateTime posted) {
		this.posted = posted;
	}
	public boolean isVerified() {
		return verified;
	}
	public void setVerified(boolean verified) {
		this.verified = verified;
	}
	public boolean isApproved() {
		return approved;
	}
	public void setApproved(boolean approved) {
		this.approved = approved;
	}
	public String getProblemCause() {
		return problemCause;
	}
	public void setProblemCause(String problemCause) {
		this.problemCause = problemCause;
	}
	public String getSourceTitle() {
		return sourceTitle;
	}
	public void setSourceTitle(String sourceTitle) {
		this.sourceTitle = sourceTitle;
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
	public boolean isConflict() {
		return isConflict;
	}
	public void setConflict(boolean isConflict) {
		this.isConflict = isConflict;
	}

}
