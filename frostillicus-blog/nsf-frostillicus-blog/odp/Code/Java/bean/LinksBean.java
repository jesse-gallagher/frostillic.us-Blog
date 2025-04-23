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
package bean;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import model.Link;

@ApplicationScoped @Named("links")
public class LinksBean {
	@Inject
	Link.LinkRepository links;

	private static Comparator<Link> linkComparator = Comparator.comparing(Link::name);

	public Map<String, Collection<Link>> getByCategory() {
		Map<String, Collection<Link>> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		// TODO seems like this could be done with a map collector or a Darwino query
		links.findAll()
			.filter(l -> l.name() != null && !l.name().isEmpty())
			.filter(Link::visible)
			.forEach(link -> {
				var l = result.computeIfAbsent(link.category(), cat -> new TreeSet<>(linkComparator));
				l.add(link);
			});

		return result;
	}

	public Collection<Link> getAll() {
		return links.findAllByCategoryAndName();
	}
}
