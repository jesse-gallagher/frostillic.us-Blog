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
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.jnosql.communication.driver.attachment.EntityAttachment;
import org.openntf.xsp.jakarta.nosql.communication.driver.DominoConstants;
import org.openntf.xsp.jakarta.nosql.mapping.extension.DominoRepository;
import org.openntf.xsp.jakarta.nosql.mapping.extension.ViewDocuments;
import org.openntf.xsp.jakarta.nosql.mapping.extension.ViewQuery;

import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;


@Entity
public class Media {
	public interface MediaRepository extends DominoRepository<Media, String> {
		Optional<Media> findByName(String name);

		Stream<Media> findAll();
		
		@ViewDocuments("MediaByID")
		Optional<Media> findByMediaId(ViewQuery query);
	}

	@Id @Column
	private String id;
	@Column
	private String name;
	@Column(DominoConstants.FIELD_ATTACHMENTS)
	private List<EntityAttachment> attachments;
	@Column(DominoConstants.FIELD_MDATE)
	private OffsetDateTime lastModificationDate;
	// TODO migrate $UpdatedBy
	@Column("CreatedBy")
	private String creationUser;
	
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
	public OffsetDateTime getLastModificationDate() {
		return lastModificationDate;
	}
	public void setLastModificationDate(OffsetDateTime lastModificationDate) {
		this.lastModificationDate = lastModificationDate;
	}
	public String getCreationUser() {
		return creationUser;
	}
	public void setCreationUser(String creationUser) {
		this.creationUser = creationUser;
	}
}
