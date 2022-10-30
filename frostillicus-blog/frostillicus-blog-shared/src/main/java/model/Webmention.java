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

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import org.darwino.jnosql.artemis.extension.converter.ISOOffsetDateTimeConverter;

import jakarta.nosql.mapping.Column;
import jakarta.nosql.mapping.Convert;
import jakarta.nosql.mapping.Entity;
import jakarta.nosql.mapping.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity @Data @NoArgsConstructor
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

}
