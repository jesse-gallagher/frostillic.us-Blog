/*
 * Copyright © 2012-2022 Jesse Gallagher
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
package model.util;

import com.darwino.commons.json.JsonArray;
import com.darwino.commons.json.JsonException;
import com.darwino.commons.json.JsonObject;
import com.darwino.commons.util.StringUtil;
import com.darwino.jsonstore.Database;
import com.darwino.jsonstore.Store;
import darwino.AppDatabaseDef;
import model.Post;
import model.PostRepository;

import jakarta.enterprise.inject.spi.CDI;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;

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
                .extract(JsonObject.of("posted", "posted")) //$NON-NLS-1$ //$NON-NLS-2$
                .find(entry -> {
                    String posted = entry.getString("posted"); //$NON-NLS-1$
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
        return tags.stream()
                .map(JsonObject.class::cast)
                .map(tag -> tag.getAsString("name")) //$NON-NLS-1$
                .map(StringUtil::toString)
                .collect(Collectors.toList());
    }

    public static int parseStartParam(final String startParam) {
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

    /**
     * Extracts the common name from the provided distinguished name, or returns
     * the original value if the argument is not a valid DN.
     *
     * @param dn the LDAP-format distinguished name
     * @return the common name component
     * @since 2.2.0
     */
    public static String toCn(final String dn) {
		if(StringUtil.isNotEmpty(dn)) {
			try {
				LdapName name = new LdapName(dn);
				for(int i = name.size()-1; i >= 0; i--) {
					String bit = name.get(i);
					if(bit.toLowerCase().startsWith("cn=")) { //$NON-NLS-1$
						return bit.substring(3);
					}
				}
			} catch(InvalidNameException e) {
				return dn;
			}
		}
		return StringUtil.EMPTY_STRING;
    }
}
