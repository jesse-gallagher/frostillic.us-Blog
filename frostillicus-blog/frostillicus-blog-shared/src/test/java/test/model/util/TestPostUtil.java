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
package test.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import model.util.PostUtil;

@SuppressWarnings("nls")
public class TestPostUtil {
	@Test
	public void testToCn() {
		assertEquals("Foo Fooson", PostUtil.toCn("cn=Foo Fooson,o=SomeOrg"));
		assertEquals("Bar Barson", PostUtil.toCn("CN=Bar Barson,O=SomeOrg"));
		assertEquals("", PostUtil.toCn("o=SomeOrg"));
		assertEquals("", PostUtil.toCn("O=SomeOrg"));
		assertEquals("Baz Bazson", PostUtil.toCn("Baz Bazson"));
	}
}
