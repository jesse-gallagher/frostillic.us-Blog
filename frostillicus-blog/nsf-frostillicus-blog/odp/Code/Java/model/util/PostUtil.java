/*
 * Copyright (c) 2012-2025 Jesse Gallagher
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

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;

import com.ibm.commons.util.StringUtil;

import jakarta.enterprise.inject.spi.CDI;
import model.Post;

public enum PostUtil {
    ;
    public static final int PAGE_LENGTH = 10;

    public static long getPostCount() {
        return CDI.current().select(Post.PostRepository.class)
        	.get()
        	.countBy();
    }

    public static Collection<String> getPostMonths() {
    	return CDI.current().select(Post.PostRepository.class)
    		.get()
    		.postMonths()
    		.map(Post::getPostMonth)
    		.toList();
    }

    public static Post createPost() {
        var post = new Post();
        post.setPosted(OffsetDateTime.now());
        post.setPostId(UUID.randomUUID().toString());

        return post;
    }

    public static Stream<String> getCategories() {
    	return CDI.current().select(Post.PostRepository.class)
    		.get()
    		.postTags()
    		.map(Post::getTags)
    		.filter(Objects::nonNull)
    		.flatMap(List::stream);
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
				var name = new LdapName(dn.replace('/', ','));
				for(var i = name.size()-1; i >= 0; i--) {
					var bit = name.get(i);
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
