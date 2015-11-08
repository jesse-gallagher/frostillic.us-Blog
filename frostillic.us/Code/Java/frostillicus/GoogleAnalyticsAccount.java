package frostillicus;

import java.io.Serializable;
import org.openntf.domino.*;
import org.openntf.domino.utils.*;

public class GoogleAnalyticsAccount implements Serializable {
	private String id = null;

	public String getId() {
		if(this.id == null) {
			Database database = XSPUtil.getCurrentDatabase();
			View config = database.getView("Configuration");
			Document doc = config.getDocumentByKey("Google Analytics", true);
			this.id = doc.getItemValueString("GoogleAnalyticsAccountID");
		}
		return this.id;
	}

	private static final long serialVersionUID = -4136127532904522488L;
}
