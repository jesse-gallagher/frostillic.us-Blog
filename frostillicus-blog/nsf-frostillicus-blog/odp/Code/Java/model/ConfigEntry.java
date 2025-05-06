package model;

import java.util.Optional;

import org.openntf.xsp.jakarta.nosql.mapping.extension.DominoRepository;

import jakarta.nosql.Column;
import jakarta.nosql.Entity;

@Entity
public record ConfigEntry(
	@Column String key,
	@Column String value
) {
	public interface Repository extends DominoRepository<ConfigEntry, String> {
		Optional<ConfigEntry> findByKey(String key);
	}
}
