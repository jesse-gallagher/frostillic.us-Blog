/*
 * Copyright (c) 2012-2023 Jesse Gallagher
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
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity @Data @NoArgsConstructor
public class Media {
	@Id @Column private String id;
	@Column private String name;
	@Column(EntityConverter.ATTACHMENT_FIELD) private List<EntityAttachment> attachments;
	@Column(Document.SYSTEM_META_MDATE) private Date lastModificationDate;
	@Column(Document.SYSTEM_META_CUSER) private String creationUser;
	@Column private boolean isConflict;
}
