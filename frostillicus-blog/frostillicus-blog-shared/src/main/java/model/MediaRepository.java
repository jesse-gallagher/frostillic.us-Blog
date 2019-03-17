package model;

import java.util.Optional;
import java.util.stream.Stream;

import org.darwino.jnosql.artemis.extension.DarwinoRepository;
import org.darwino.jnosql.artemis.extension.RepositoryProvider;

import darwino.AppDatabaseDef;

@RepositoryProvider(AppDatabaseDef.STORE_MEDIA)
public interface MediaRepository extends DarwinoRepository<Media, String> {
	Optional<Media> findByName(String name);
	
	Stream<Media> findAll();
}
