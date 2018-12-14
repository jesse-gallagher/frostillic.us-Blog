package model;

import com.darwino.commons.json.JsonException;
import com.darwino.commons.json.JsonObject;
import com.darwino.jsonstore.Database;
import com.darwino.jsonstore.Store;
import frostillicus.blog.app.AppDatabaseDef;

import javax.enterprise.inject.spi.CDI;
import java.util.*;

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

    public static Collection<String> getPostMonths() throws JsonException {
        Collection<String> months = new TreeSet<>();

        Database database = CDI.current().select(Database.class).get();
        Store store = database.getStore(AppDatabaseDef.STORE_POSTS);
        store.openCursor()
                .query(JsonObject.of("form", Post.class.getSimpleName())) //$NON-NLS-1$
                .findDocuments(doc -> {
                    String posted = doc.getString("posted"); //$NON-NLS-1$
                    if(posted != null && posted.length() >= 7) {
                        months.add(posted.substring(0, 7));
                    }
                    return true;
                });

        return months;
    }

    public static Post createPost() {
        Post post = new Post();
        post.setPosted(new Date());
        post.setPostId(UUID.randomUUID().toString());

        // It's not pretty, but it's CLOSER to replication-safe
        Random random = new Random();
        PostRepository posts = CDI.current().select(PostRepository.class).get();
        int postIdInt;
        do {
            postIdInt = random.nextInt();
        } while(posts.findByPostIdInt(postIdInt).isPresent());
        post.setPostIdInt(postIdInt);

        return post;
    }

}
