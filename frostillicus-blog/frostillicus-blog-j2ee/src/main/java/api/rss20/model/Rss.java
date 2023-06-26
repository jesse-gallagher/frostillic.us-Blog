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
package api.rss20.model;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="rss")
public class Rss {
	public static final String NS_CONTENT = "http://purl.org/rss/1.0/modules/content/"; //$NON-NLS-1$
	public static final String NS_DC = "http://purl.org/dc/elements/1.1/"; //$NON-NLS-1$
	public static final String NS_ATOM = "http://www.w3.org/2005/Atom"; //$NON-NLS-1$

	private String version = "2.0"; //$NON-NLS-1$
	private String base;
	private Channel channel = new Channel();

	@XmlElementRef
	public Channel getChannel() {
		return channel;
	}
	@XmlAttribute(name="version")
	public String getVersion() {
		return version;
	}
	@XmlAttribute(name="xml:base")
	public String getBase() {
		return base;
	}
	public void setBase(final String base) {
		this.base = base;
	}
}
