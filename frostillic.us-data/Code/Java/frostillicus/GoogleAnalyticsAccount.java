package frostillicus;

import java.io.Serializable;
import lotus.domino.*;

public class GoogleAnalyticsAccount implements Serializable {
	private String id = null;

	public String getId() throws NotesException {
		if(this.id == null) {
			Database database = JSFUtil.getDatabase();
			View config = database.getView("Configuration");
			Document doc = config.getDocumentByKey("Google Analytics", true);
			this.id = doc.getItemValueString("GoogleAnalyticsAccountID");
			doc.recycle();
			config.recycle();
		}
		return this.id;
	}

	private static final long serialVersionUID = -4136127532904522488L;
}
