package model;

import java.util.stream.Stream;

import org.darwino.jnosql.artemis.extension.DarwinoRepository;
import org.darwino.jnosql.artemis.extension.RepositoryProvider;

import frostillicus.blog.app.AppDatabaseDef;

@RepositoryProvider(AppDatabaseDef.STORE_CONFIG)
public interface LinkRepository extends DarwinoRepository<Link, String> {
	Stream<Link> findAll();
}
