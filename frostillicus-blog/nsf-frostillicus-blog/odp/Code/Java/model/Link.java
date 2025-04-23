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

import java.util.List;
import java.util.stream.Stream;

import org.openntf.xsp.jakarta.nosql.mapping.extension.DominoRepository;
import org.openntf.xsp.jakarta.nosql.mapping.extension.ViewDocuments;

import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;
import jakarta.validation.constraints.NotEmpty;

@Entity
public record Link(
	@Id @Column String id,
	@Column String category,
	@Column("link_url") @NotEmpty String url,
	@Column("link_name") @NotEmpty String name,
	@Column("link_visible") boolean visible,
	@Column String rel,
	@Column String classes
) {
	public interface LinkRepository extends DominoRepository<Link, String> {
		Stream<Link> findAll();

		// TODO implement view
		@ViewDocuments("LinksByCategoryAndName")
		List<Link> findAllByCategoryAndName();
	}
}
