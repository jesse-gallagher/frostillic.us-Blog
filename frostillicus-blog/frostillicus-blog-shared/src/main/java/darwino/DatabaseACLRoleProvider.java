package darwino;

import com.darwino.commons.json.JsonArray;
import com.darwino.commons.json.JsonException;
import com.darwino.commons.json.JsonObject;
import com.darwino.commons.security.acl.UserException;
import com.darwino.commons.security.acl.impl.UserImpl;
import com.darwino.commons.security.acl.impl.UserProviderImpl;
import com.darwino.commons.util.StringUtil;
import com.darwino.jsonstore.Design;
import com.darwino.jsonstore.LocalJsonDBServer;
import com.darwino.jsonstore.meta._DatabaseACL;
import com.darwino.platform.DarwinoApplication;

public class DatabaseACLRoleProvider extends UserProviderImpl {
	public static final String PROVIDER_ID = "databaseAcl";

	public DatabaseACLRoleProvider() {
		super(PROVIDER_ID);
	}
	
	@Override
	public void contribute(UserImpl user) throws UserException {
		DarwinoApplication app = DarwinoApplication.get();
		try {
			LocalJsonDBServer server = app.getLocalJsonDBServer();
			// TODO support instances? I'm not sure we can at this point
			JsonObject json = (JsonObject)server.loadDesignDocument(app.getManifest().getMainDatabase(), null, Design.UNID_ACL_DOCUMENT);
			if(json != null) {
				JsonArray entries = json.getArray("entries");
				JsonArray roles = findRoles(entries, user);
				if(roles != null) {
					for(Object role : roles) {
						user.addRole((String)role);
					}
				}
			}
		} catch (JsonException e) {
			throw new UserException(e);
		}
	}
	
	private JsonArray findRoles(JsonArray entries, UserImpl user) {
		JsonArray closestRoles = null;
		for(int i = 0; i < entries.size(); i++) {
			JsonObject entry = entries.getAsObject(i);
			switch(entry.getAsInt("type")) {
			case _DatabaseACL.TYPE_PEOPLE:
				// This is inherently the closest if it matches
				if(StringUtil.equalsIgnoreCase(entry.getAsString("id"), user.getDn())) {
					return entry.getArray("roles");
				}
			case _DatabaseACL.TYPE_GROUP:
				// TODO support globs
				if(user.getGroups().contains(entry.getAsString("id"))) {
					closestRoles = entry.getArray("roles");
				}
				break;
			case _DatabaseACL.TYPE_AUTHENTICATED:
				if(closestRoles == null) {
					closestRoles = entry.getArray("roles");
				}
				break;
			}
		}
		return closestRoles;
	}

}
