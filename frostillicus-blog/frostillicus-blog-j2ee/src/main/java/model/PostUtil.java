/**
 * Copyright © 2016-2018 Jesse Gallagher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package model;

import com.darwino.commons.json.JsonArray;
import com.darwino.commons.json.JsonException;
import com.darwino.commons.json.JsonObject;
import com.darwino.commons.util.StringUtil;
import com.darwino.jsonstore.Database;
import com.darwino.jsonstore.Store;
import darwino.AppDatabaseDef;

import javax.enterprise.inject.spi.CDI;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
        post.setPosted(OffsetDateTime.now());
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

    public static Collection<String> getCategories() throws JsonException {
        Database database = CDI.current().select(Database.class).get();
        JsonArray tags = (JsonArray)database.getStore(AppDatabaseDef.STORE_POSTS).getTags(Integer.MAX_VALUE, true);
        return tags.stream().map(StringUtil::toString).collect(Collectors.toList());
    }

    public static int parseStartParam(String startParam) {
        int start;
        if(StringUtil.isNotEmpty(startParam)) {
            try {
                start = Integer.parseInt(startParam);
            } catch(NumberFormatException e) {
                start = -1;
            }
        } else {
            start = -1;
        }
        return start;
    }
}
