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

import java.time.Instant;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name="item")
public class RssItem {
	
	private String title;
	private String link;
	private String contentEncoded;
	private String guid;
	private String creator;
	private Instant date;
	private String description;

	@XmlElement
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	@XmlElement
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	@XmlElement(name="encoded", namespace=Rss.NS_CONTENT)
	public String getContentEncoded() {
		return contentEncoded;
	}
	public void setContentEncoded(String contentEncoded) {
		this.contentEncoded = contentEncoded;
		this.description = contentEncoded;
	}
	@XmlElement
	public String getGuid() {
		return guid;
	}
	public void setGuid(String guid) {
		this.guid = guid;
	}
	@XmlElement(name="creator", namespace=Rss.NS_DC)
	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}
	@XmlElement(name="date", namespace=Rss.NS_DC)
	@XmlJavaTypeAdapter(InstantXmlAdapter.class)
	public Instant getDate() {
		return date;
	}
	public void setDate(Instant date) {
		this.date = date;
	}
	@XmlElement
	public String getDescription() {
		return description;
	}
	
}
