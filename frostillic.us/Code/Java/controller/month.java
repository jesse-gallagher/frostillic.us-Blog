package controller;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.*;

import config.Translation;

import model.Post;

import frostillicus.xsp.controller.BasicXPageController;
import frostillicus.xsp.util.FrameworkUtils;

public class month extends BasicXPageController {
	private static final long serialVersionUID = 1L;

	public List<Post> getPosts() throws ParseException {
		Map<String, String> param = FrameworkUtils.getParam();
		String month = param.get("month");
		return Post.Manager.get().getNamedCollection("By Month", month);
	}

	public String getPageTitle() throws ParseException {
		String displayMonth = getDisplayMonth();
		Translation translation = Translation.get();
		String pageTitle = translation.getValue("monthTitle");
		return MessageFormat.format(pageTitle, displayMonth);
	}

	public String getDisplayMonth() throws ParseException {
		Map<String, String> param = FrameworkUtils.getParam();
		String month = param.get("month");
		Date monthDate = Post.Manager.MONTH_CONVERT_FORMAT.get().parse(month);
		return Post.Manager.MONTH_LABEL_FORMAT.get().format(monthDate);
	}
}
