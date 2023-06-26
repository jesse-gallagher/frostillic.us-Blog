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
// TODO figure out why JAX-B doesn't pick up on these
@XmlSchema(
	xmlns = {
		@XmlNs(namespaceURI=Rss.NS_CONTENT, prefix="content"),
		@XmlNs(namespaceURI=Rss.NS_DC, prefix="dc"),
		@XmlNs(namespaceURI=Rss.NS_ATOM, prefix="atom")
	}
)
package api.rss20.model;

import jakarta.xml.bind.annotation.XmlNs;
import jakarta.xml.bind.annotation.XmlSchema;