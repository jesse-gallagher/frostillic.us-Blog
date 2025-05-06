package bean;

import java.util.Optional;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import model.ConfigEntry;

@RequestScoped
public class ConfigBean {

	@Inject
	private ConfigEntry.Repository configRepository;
	
	public Optional<String> getConfig(String key) {
		return configRepository.findByKey(key)
			.map(ConfigEntry::value);
	}
}
