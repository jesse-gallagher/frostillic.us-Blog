package model;

import java.util.stream.Stream;

import org.darwino.jnosql.artemis.extension.DarwinoRepository;
import org.darwino.jnosql.artemis.extension.RepositoryProvider;

import darwino.AppDatabaseDef;

@RepositoryProvider(AppDatabaseDef.STORE_CONFIG)
public interface MetaTagRepository extends DarwinoRepository<MetaTag, String> {
	Stream<MetaTag> findAll();
}
