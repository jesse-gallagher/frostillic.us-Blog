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

import java.util.List;
import java.util.Optional;

import jakarta.nosql.mapping.Param;
import jakarta.nosql.mapping.Repository;

public interface PostRepository extends Repository<Post, String> {
//	@StoredCursor("FindPost")
	Optional<Post> findPost(@Param("key") String key);

//	@JSQL("select unid from posts where $.postIdInt::int=:postIdInt")
	Optional<Post> findByPostIdInt(@Param("postIdInt") int postIdInt);

//	@StoredCursor("PostsByTag")
	List<Post> findByTag(@Param("tag") String tag);

//	@StoredCursor("PostsByMonth")
	List<Post> findByMonth(@Param("monthQuery") String monthQuery);

//	@Search(orderBy="posted desc")
	List<Post> search(String query);

//	@JSQL("select unid from posts where $.form='Post' order by $.posted desc limit 10")
	List<Post> homeList();

//	@JSQL("select unid from posts where $.form='Post' order by $.posted desc")
	List<Post> homeList(@Param("skip") int skip, @Param("limit") int limit);

//	@JSQL("select unid from posts where $.form='Post' and $.thread=:thread order by $.posted")
	List<Post> findByThread(@Param("thread") String thread);

	Optional<Post> findByName(String name);
}