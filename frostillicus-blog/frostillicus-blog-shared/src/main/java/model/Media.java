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

import java.util.Date;
import java.util.List;

import org.darwino.jnosql.diana.driver.EntityConverter;
import org.eclipse.jnosql.communication.driver.attachment.EntityAttachment;

import com.darwino.jsonstore.Document;

import jakarta.nosql.mapping.Column;
import jakarta.nosql.mapping.Entity;
import jakarta.nosql.mapping.Id;

@Entity
public class Media {
	@Id @Column private String id;
	@Column private String name;
	@Column(EntityConverter.ATTACHMENT_FIELD) private List<EntityAttachment> attachments;
	@Column(Document.SYSTEM_META_MDATE) private Date lastModificationDate;
	@Column(Document.SYSTEM_META_CUSER) private String creationUser;
	@Column private boolean isConflict;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<EntityAttachment> getAttachments() {
		return attachments;
	}
	public void setAttachments(List<EntityAttachment> attachments) {
		this.attachments = attachments;
	}
	public Date getLastModificationDate() {
		return lastModificationDate;
	}
	public void setLastModificationDate(Date lastModificationDate) {
		this.lastModificationDate = lastModificationDate;
	}
	public String getCreationUser() {
		return creationUser;
	}
	public void setCreationUser(String creationUser) {
		this.creationUser = creationUser;
	}
	public boolean isConflict() {
		return isConflict;
	}
	public void setConflict(boolean isConflict) {
		this.isConflict = isConflict;
	}
}
