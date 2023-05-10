/*
 * Copyright © 2012-2023 Jesse Gallagher
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
package api.rss20.model;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="link", namespace=Rss.NS_ATOM)
public class AtomLink {
	private String href;
	private String rel;
	private String type;

	@XmlAttribute
	public String getHref() {
		return href;
	}
	public void setHref(final String href) {
		this.href = href;
	}
	@XmlAttribute
	public String getRel() {
		return rel;
	}
	public void setRel(final String rel) {
		this.rel = rel;
	}
	@XmlAttribute
	public String getType() {
		return type;
	}
	public void setType(final String type) {
		this.type = type;
	}
}
