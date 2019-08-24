package model;

import java.util.List;

import org.darwino.jnosql.artemis.extension.DarwinoRepository;
import org.darwino.jnosql.artemis.extension.JSQL;
import org.darwino.jnosql.artemis.extension.RepositoryProvider;

import darwino.AppDatabaseDef;

@RepositoryProvider(AppDatabaseDef.STORE_MICROPOSTS)
public interface MicroPostRepository extends DarwinoRepository<MicroPost, String> {
	@JSQL("select unid from microposts where $.form='MicroPost' order by $.posted desc")
	List<MicroPost> findAll();
}
