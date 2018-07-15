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

import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotEmpty;

@Entity @Data @NoArgsConstructor
public class Post {
	public enum Status {
		Posted, Draft
	}
	
	@Id @Column @NotEmpty private String id;
	@Column @NotEmpty private String title;
	@Column @NotEmpty @Convert(ISODateConverter.class) private Date posted;
	@Column @NotEmpty private String postedBy;
	@Column private String bodyMarkdown;
	@Column @NotEmpty private String bodyHtml;
	@Column private List<String> tags;
	@Column private String thread;
	@Column private Status status;
}