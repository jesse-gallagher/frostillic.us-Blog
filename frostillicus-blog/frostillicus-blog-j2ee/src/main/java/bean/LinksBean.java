package bean;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Collection;
import java.util.Comparator;

import model.Link;
import model.LinkRepository;

@ApplicationScoped @Named("links")
public class LinksBean {
	@Inject
	LinkRepository links;
	
	private static Comparator<Link> linkComparator = (o1, o2) -> String.valueOf(o1.getName()).compareTo(o2.getName());
	
	public Map<String, Collection<Link>> getByCategory() {
		Map<String, Collection<Link>> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		
		// TODO seems like this could be done with a map collector or a Darwino query
		links.findAll()
			.filter(l -> l.getName() != null && !l.getName().isEmpty())
			.filter(Link::isVisible)
			.forEach(link -> {
				Collection<Link> l = result.computeIfAbsent(link.getCategory(), cat -> new TreeSet<Link>(linkComparator));
				l.add(link);
			});
		
		return result;
	}
}
