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
package api.rss20.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlRootElement(name="channel")
@XmlType(propOrder = { "title", "link", "description", "image", "items", "links" })
public class Channel {
	private String title;
	private String link;
	private String description;
	private Image image = new Image();
	private List<RssItem> items = new ArrayList<>();
	private List<AtomLink> links = new ArrayList<>();

	@XmlElement
	public String getTitle() {
		return title;
	}
	public void setTitle(final String title) {
		this.title = title;
	}
	@XmlElement
	public String getLink() {
		return link;
	}
	public void setLink(final String link) {
		this.link = link;
	}
	@XmlElement
	public String getDescription() {
		return description;
	}
	public void setDescription(final String description) {
		this.description = description;
	}
	@XmlElementRef
	public Image getImage() {
		return image;
	}
	@XmlElementRef
	public List<RssItem> getItems() {
		return items;
	}
	@XmlElementRef
	public List<AtomLink> getLinks() {
		return links;
	}
}