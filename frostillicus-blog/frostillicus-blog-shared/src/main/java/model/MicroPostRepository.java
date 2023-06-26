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

import java.util.List;

import org.darwino.jnosql.artemis.extension.DarwinoRepository;
import org.darwino.jnosql.artemis.extension.JSQL;
import org.darwino.jnosql.artemis.extension.RepositoryProvider;

import darwino.AppDatabaseDef;

@RepositoryProvider(AppDatabaseDef.STORE_MICROPOSTS)
public interface MicroPostRepository extends DarwinoRepository<MicroPost, String> {
	@JSQL("select unid from microposts where $.form='MicroPost' order by $.posted desc")
	List<MicroPost> findAll();
}
