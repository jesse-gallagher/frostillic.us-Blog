package controller;

import com.darwino.commons.json.JsonException;
import com.darwino.commons.util.StringUtil;
import model.PostRepository;
import model.PostUtil;

import javax.inject.Inject;
import javax.mvc.Models;

import static model.PostUtil.PAGE_LENGTH;

public abstract class AbstractPostListController {
    @Inject
    Models models;

    @Inject
    PostRepository posts;

    protected String maybeList(String startParam) throws JsonException {
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
        if(start > -1) {
            models.put("posts", posts.homeList(start, PAGE_LENGTH));
            models.put("start", start);
            models.put("pageSize", PAGE_LENGTH);

            int total = start + PAGE_LENGTH;
            if(total >= PostUtil.getPostCount()) {
                models.put("endOfLine", true);
            } else {
                models.put("endOfLine", false);
            }
            return "home.jsp"; //$NON-NLS-1$
        } else {
           return null;
        }
    }
}
