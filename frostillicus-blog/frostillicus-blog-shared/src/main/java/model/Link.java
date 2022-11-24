/*
 * Copyright © 2012-2022 Jesse Gallagher
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

import jakarta.validation.constraints.NotEmpty;

import jakarta.nosql.mapping.Column;
import jakarta.nosql.mapping.Convert;
import jakarta.nosql.mapping.Entity;
import jakarta.nosql.mapping.Id;

import lombok.Data;
import lombok.NoArgsConstructor;
import model.util.BooleanYNConverter;

@Entity @Data @NoArgsConstructor
public class Link {
	@Id @Column private String id;
	@Column private String category;
	@Column("link_url") @NotEmpty private String url;
	@Column("link_name") @NotEmpty private String name;
	@Column("link_visible") @Convert(BooleanYNConverter.class) private boolean visible;
	@Column private String rel;
	@Column private boolean isConflict;
}
