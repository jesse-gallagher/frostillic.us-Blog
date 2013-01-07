package controller;

import frostillicus.JSFUtil;
import frostillicus.controller.BasicXPageController;
import java.util.*;

import lotus.domino.NotesException;

import com.ibm.xsp.extlib.util.ExtLibUtil;

public class Month extends BasicXPageController {
	private static final long serialVersionUID = 1L;

	public Object getSearchKeys() throws NotesException {
		Map<String, String> param = JSFUtil.getParam();
		return ExtLibUtil.getCurrentSession().createDateTime(param.get("month") + "-15");
	}
}
