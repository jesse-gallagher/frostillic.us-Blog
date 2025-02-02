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
package controller;

import static model.util.PostUtil.PAGE_LENGTH;

import com.darwino.commons.json.JsonException;

import jakarta.inject.Inject;
import jakarta.mvc.Models;
import model.PostRepository;
import model.util.PostUtil;

public abstract class AbstractPostListController {
    @Inject
    Models models;

    @Inject
    PostRepository posts;

    protected String maybeList(final String startParam) throws JsonException {
        var start = PostUtil.parseStartParam(startParam);
        if(start > -1) {
        		var homeList = posts.homeList(start, PAGE_LENGTH);
            models.put("posts", homeList); //$NON-NLS-1$
            models.put("start", start); //$NON-NLS-1$
            models.put("pageSize", PAGE_LENGTH); //$NON-NLS-1$

            var total = start + PAGE_LENGTH;
            if(total >= PostUtil.getPostCount()) {
                models.put("endOfLine", true); //$NON-NLS-1$
            } else {
                models.put("endOfLine", false); //$NON-NLS-1$
            }
            return "home.jsp"; //$NON-NLS-1$
        } else {
           return null;
        }
    }

}
