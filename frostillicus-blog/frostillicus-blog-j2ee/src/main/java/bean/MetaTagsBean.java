package bean;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import model.MetaTag;
import model.MetaTagRepository;

@RequestScoped
@Named("metaTags")
public class MetaTagsBean {
	@Inject
	private MetaTagRepository metaTags;
	
	public List<MetaTag> getAll() {
		return metaTags.findAll().collect(Collectors.toList());
	}
}
