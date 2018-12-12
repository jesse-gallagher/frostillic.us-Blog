package model;

import com.darwino.commons.json.JsonException;
import com.darwino.commons.json.JsonObject;
import com.darwino.jsonstore.Database;
import com.darwino.jsonstore.Store;
import frostillicus.blog.app.AppDatabaseDef;

import javax.enterprise.inject.spi.CDI;

public enum PostUtil {
    ;
    public static final int PAGE_LENGTH = 10;

    public static int getPostCount() throws JsonException {
        Database database = CDI.current().select(Database.class).get();
        Store store = database.getStore(AppDatabaseDef.STORE_POSTS);
        return store.openCursor()
                .query(JsonObject.of("form", Post.class.getSimpleName())) //$NON-NLS-1$
                .count();
    }
}
