package bean;

import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

@RequestScoped
public class ConfigBean {

	@Inject
	@ConfigProperty(name = "microblog-key")
	private Optional<String> microBlogKey;

	@Inject
	@ConfigProperty(name = "rss-request-urls")
	private boolean rssRequestUrls;

	public String getMicroBlogKey() {
		return this.microBlogKey.orElse(null);
	}

	public boolean isRssRequestUrls() {
		return this.rssRequestUrls;
	}
}
