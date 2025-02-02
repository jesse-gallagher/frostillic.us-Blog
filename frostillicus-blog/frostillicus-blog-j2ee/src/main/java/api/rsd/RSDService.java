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
package api.rsd;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.enterprise.inject.Stereotype;

/**
 * This annotation should be applied to any class that should contribute to
 * the services list in the RSD manifest.
 *
 * @author Jesse Gallagher
 * @since 2.3.0
 */
@Retention(RUNTIME)
@Target(TYPE)
@Stereotype
public @interface RSDService {
	String name();
	boolean preferred();
	String basePath();

	boolean microblog() default false;
	boolean blog() default true;
}
