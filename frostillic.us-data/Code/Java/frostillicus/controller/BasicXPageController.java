package frostillicus.controller;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import java.util.*;
import lotus.domino.*;

public class BasicXPageController implements XPageController {
	private static final long serialVersionUID = 1L;

	public BasicXPageController() { }

	public void beforePageLoad() throws Exception { }
	public void afterPageLoad() throws Exception { }

	public void beforeRenderResponse(PhaseEvent event) throws Exception { }
	public void afterRenderResponse(PhaseEvent event) throws Exception { }

	public void afterRestoreView(PhaseEvent event) throws Exception { }

	protected static Object resolveVariable(String varName) {
		return ExtLibUtil.resolveVariable(FacesContext.getCurrentInstance(), varName);
	}

	public boolean isEditable() { return false; }


	@SuppressWarnings("unchecked")
	public List<Date> getArchiveMonths() throws NotesException {
		List<Date> result = new ArrayList<Date>();

		Database database = ExtLibUtil.getCurrentDatabase();
		View posts = database.getView("Posts by Month");
		List<DateTime> months = (List<DateTime>)posts.getColumnValues(0);
		for(DateTime month : months) {
			result.add(month.toJavaDate());
			month.recycle();
		}
		posts.recycle();

		return result;
	}

	private Map<String, List<Map<String, String>>> linksData;
	@SuppressWarnings("unchecked")
	public Map<String, List<Map<String, String>>> getLinksData() throws NotesException {
		if(this.linksData == null) {
			Map<String, List<Map<String, String>>> result = new TreeMap<String, List<Map<String, String>>>();
			Database database = ExtLibUtil.getCurrentDatabase();
			View links = database.getView("Links");
			ViewNavigator nav = links.createViewNav();
			ViewEntry entry = nav.getFirst();
			while(entry != null) {
				entry.setPreferJavaDates(true);
				List<Object> columnValues = entry.getColumnValues();
				String category = String.valueOf(columnValues.get(0));

				if(entry.isCategory()) {
					result.put(category, new ArrayList<Map<String, String>>());
				} else {
					Map<String, String> thisLink = new HashMap<String, String>();
					thisLink.put("name", String.valueOf(columnValues.get(1)));
					thisLink.put("link", String.valueOf(columnValues.get(2)));
					result.get(category).add(thisLink);
				}

				ViewEntry tempEntry = entry;
				entry = nav.getNext();
				tempEntry.recycle();
			}
			this.linksData = result;
		}
		return this.linksData;
	}
}
