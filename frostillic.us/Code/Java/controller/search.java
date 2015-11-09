package controller;

import java.text.MessageFormat;
import java.util.Map;

import config.Translation;
import frostillicus.xsp.controller.BasicXPageController;
import frostillicus.xsp.util.FrameworkUtils;

public class search extends BasicXPageController {
	private static final long serialVersionUID = 1L;

	public String getMessage() {
		Map<String, String> param = FrameworkUtils.getParam();
		String thread = param.get("q"); //$NON-NLS-1$

		Translation translation = Translation.get();
		return MessageFormat.format(translation.getValue("searchHeader"), thread); //$NON-NLS-1$
	}
}
