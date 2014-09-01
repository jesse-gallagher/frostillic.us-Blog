package util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.model.SelectItem;

import org.openntf.domino.utils.DominoUtils;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.extlib.util.ExtLibUtil;

import frostillicus.xsp.util.FrameworkUtils;

public enum UserUtil {
	;

	// TODO Re-OpenNTF-ify when */O=whatever is fixed
	public static String toAbbreviated(final String value) {
		try {
			lotus.domino.Session session = ExtLibUtil.getCurrentSession();
			lotus.domino.Name name = session.createName(value);
			String result = name.getAbbreviated();
			name.recycle();
			return result;
		} catch(lotus.domino.NotesException ne) {
			return ne.toString();
		}
	}
	public static String toCanonical(final String value) {
		try {
			lotus.domino.Session session = ExtLibUtil.getCurrentSession();
			lotus.domino.Name name = session.createName(value);
			String result = name.getCanonical();
			name.recycle();
			return result;
		} catch(lotus.domino.NotesException ne) {
			return ne.toString();
		}
	}

	public static String toEmailAddress(final Object value) {
		if(value == null || "".equals(value)) {
			return "";
		}

		List<String> incoming = new ArrayList<String>();
		if(value instanceof Collection) {
			for(Object val : (Collection<?>)value) {
				incoming.add(String.valueOf(val));
			}
		} else {
			incoming.add(String.valueOf(value));
		}
		List<String> result = new ArrayList<String>(incoming.size());
		for(String val : incoming) {
			if(val.contains("@")) {
				result.add(val);
			} else {
				List<?> names = FrameworkUtils.getSession().evaluate(" name := \"" + DominoUtils.escapeForFormulaString(val) + "\"; @NameLookup([NoUpdate]; name; 'InternetAddress')[1] ");
				if(names.size() > 0) {
					result.add(String.valueOf(names.get(0)));
				} else {
					result.add(val);
				}
			}
		}
		return StringUtil.concatStrings(result.toArray(new String[] { }), ',', false);
	}

	@SuppressWarnings("unchecked")
	public static List<SelectItem> namesToItems(final Object value) {
		Set<String> includedNames = new HashSet<String>();
		List<SelectItem> result = new ArrayList<SelectItem>();

		if(value instanceof String) {
			String user = (String)value;
			if(StringUtil.isNotEmpty(user) && !includedNames.contains(user)) {
				includedNames.add(user);
				SelectItem item = new SelectItem();
				if(user.contains("@")) {
					item.setLabel(user);
					item.setValue(user);
				} else {
					item.setLabel(UserUtil.toAbbreviated(user));
					item.setValue(UserUtil.toCanonical(user));
				}
				result.add(item);
			}
		} else if(value instanceof List) {
			for(String user : (List<String>)value) {
				if(StringUtil.isNotEmpty(user) && !includedNames.contains(user)) {
					includedNames.add(user);
					SelectItem item = new SelectItem();
					if(user.contains("@")) {
						item.setLabel(user);
						item.setValue(user);
					} else {
						item.setLabel(UserUtil.toAbbreviated(user));
						item.setValue(UserUtil.toCanonical(user));
					}
					result.add(item);
				}
			}
		}

		return result;
	}
}
