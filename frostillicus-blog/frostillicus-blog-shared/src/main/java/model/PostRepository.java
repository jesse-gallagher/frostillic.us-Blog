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

import java.util.List;
import java.util.Optional;

import org.darwino.jnosql.artemis.extension.DarwinoRepository;
import org.darwino.jnosql.artemis.extension.RepositoryProvider;
import org.jnosql.artemis.Query;

import frostillicus.blog.app.AppDatabaseDef;

@RepositoryProvider(AppDatabaseDef.STORE_POSTS)
public interface PostRepository extends DarwinoRepository<Post, String> {
	Optional<Post> findByPostId(String postId);

	@Query("select * from Post order by posted desc")
	List<Post> findAll();
	
	@Query("select * from Post limit 20 order by posted desc")
	List<Post> homeList();
}