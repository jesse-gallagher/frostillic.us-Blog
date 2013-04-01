package frostillicus;


import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import sun.misc.BASE64Decoder;
import com.ibm.xsp.component.UIViewRootEx2;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.extlib.util.JdbcUtil;

import java.sql.Connection;
import java.sql.SQLException;
import org.openntf.domino.*;
import org.openntf.domino.utils.XSPUtil;
import org.openntf.domino.utils.Factory;
import lotus.notes.addins.DominoServer;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import javax.servlet.http.Cookie;

public class JSFUtil {

	private JSFUtil() { }

	public static Object getBindingValue(String ref) {
		FacesContext context = FacesContext.getCurrentInstance();
		Application application = context.getApplication();
		return application.createValueBinding(ref).getValue(context);
	}
	public static void setBindingValue(String ref, Object newObject) {
		FacesContext context = FacesContext.getCurrentInstance();
		Application application = context.getApplication();
		ValueBinding binding = application.createValueBinding(ref);
		binding.setValue(context, newObject);
	}

	public static Object getVariableValue(String varName) {
		FacesContext context = FacesContext.getCurrentInstance();
		return context.getApplication().getVariableResolver().resolveVariable(context, varName);
	}

	public static String getUserName() {
		return (String)getBindingValue("#{context.user.name}");
	}
	public static Session getSession() {
		return XSPUtil.getCurrentSession();
	}
	public static Session getSessionAsSigner() {
		return XSPUtil.getCurrentSessionAsSigner();
	}
	public static Database getDatabase() {
		return XSPUtil.getCurrentDatabase();
	}
	public static UIViewRootEx2 getViewRoot() {
		return (UIViewRootEx2)getVariableValue("view");
	}

	public static Connection getConnection(String connectionName) throws SQLException {
		return JdbcUtil.getConnection(FacesContext.getCurrentInstance(), connectionName);
	}

	public static String pluralize(String input) {
		if(input.endsWith("s")) {
			return input + "es";
		} else if(input.endsWith("y")) {
			return input.substring(0, input.length()-2) + "ies";
		}
		return input + "s";
	}
	public static String singularize(String input) {
		if(input.endsWith("ses")) {
			return input.substring(0, input.length()-2);
		} else if(input.endsWith("ies")) {
			return input.substring(0, input.length()-3) + "y";
		} else if(input.endsWith("s")) {
			return input.substring(0, input.length()-1);
		}
		return input;
	}

	public String fetchURL(String urlString) throws Exception {
		URL url = new URL(urlString);
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setRequestProperty("User-Agent", "Firefox/2.0");
		//conn.setRequestProperty("Cookie", cookie);

		BufferedReader in = new BufferedReader(new InputStreamReader((InputStream)conn.getContent()));
		StringWriter resultWriter = new StringWriter();
		String inputLine;
		while((inputLine = in.readLine()) != null) {
			resultWriter.write(inputLine);
		}
		in.close();

		return resultWriter.toString().replace("<HTTP-EQUIV", "<meta http-equiv");

	}

	public static byte[] getFileResourceData(Document fileResource) throws IOException {
		String dxl = fileResource.generateXML();
		int fileDataPos = dxl.indexOf("<filedata>");
		int fileDataEnd = dxl.indexOf("</filedata>");
		String fileDataB64 = dxl.substring(fileDataPos + 10, fileDataEnd-1);
		return new BASE64Decoder().decodeBuffer(fileDataB64);
	}

	@SuppressWarnings("unchecked")
	public static String xor(String input, Vector key) {
		StringBuilder output = new StringBuilder();

		for(int i = 0; i < input.length(); i++) {
			int character = input.codePointAt(i);
			int keyNode = ((Double)key.get(i % key.size())).intValue();

			int onePass = character ^ keyNode;

			output.append((char)onePass);
		}

		return output.toString();
	}

	public static String xmlEncode(String text) {
		StringBuilder result = new StringBuilder();

		for(int i = 0; i < text.length(); i++) {
			char currentChar = text.charAt(i);
			if(!((currentChar >= 'a' && currentChar <= 'z') || (currentChar >= 'A' && currentChar <= 'Z') || (currentChar >= '0' && currentChar <= '9'))) {
				result.append("&#" + (int)currentChar + ";");
			} else {
				result.append(currentChar);
			}
		}

		return result.toString();
	}

	public static String strLeft(String input, String delimiter) {
		return input.substring(0, input.indexOf(delimiter));
	}
	public static String strRight(String input, String delimiter) {
		return input.substring(input.indexOf(delimiter) + delimiter.length());
	}
	public static String strLeftBack(String input, String delimiter) {
		return input.substring(0, input.lastIndexOf(delimiter));
	}
	public static String strLeftBack(String input, int chars) {
		return input.substring(0, input.length() - chars);
	}
	public static String strRightBack(String input, String delimiter) {
		return input.substring(input.lastIndexOf(delimiter) + delimiter.length());
	}
	public static String strRightBack(String input, int chars) {
		return input.substring(input.length() - chars);
	}

	@SuppressWarnings("unchecked")
	public static List<String> toStringList(Object columnValue) {
		List<String> result = new Vector<String>();
		if(columnValue.getClass().getName().equals("java.util.Vector")) {
			for(Object reader : (Vector)columnValue) {
				result.add((String)reader);
			}
		} else if(((String)columnValue).length() > 0) {
			result.add((String)columnValue);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public static List<Integer> toIntegerList(Object columnValue) {
		List<Integer> result = new Vector<Integer>();
		if(columnValue.getClass().getName().equals("java.util.Vector")) {
			for(Object element : (Vector)columnValue) {
				result.add(((Double)element).intValue());
			}
		} else {
			result.add(((Double)columnValue).intValue());
		}
		return result;
	}

	public static int toInteger(Object columnValue) {
		int result = 0;
		if(columnValue.getClass().getName().equals("java.lang.String")) {
			result = 0;
		} else {
			result = ((Double)columnValue).intValue();
		}
		return result;
	}
	public static Date toDate(Object columnValue) {
		DateTime dt;
		if(columnValue instanceof org.openntf.domino.DateTime) {
			dt = (DateTime)columnValue;
		} else {
			dt = Factory.fromLotus((lotus.domino.DateTime)columnValue, DateTime.class, XSPUtil.getCurrentSession());
		}
		return ((DateTime)columnValue).toJavaDate();
	}

	@SuppressWarnings("unchecked")
	public static String iterableToSQL(Iterable input) {
		// This isn't a particularly safe implementation, but I don't have the desire to
		//  look for the "right" way to do it with PreparedStatements
		StringBuilder result = new StringBuilder();
		for(Object element : input) {
			if(result.length() > 0) { result.append(", "); }
			result.append("'" + element.toString().replaceAll("'", "\\'") + "'");
		}
		return result.toString();
	}

	public static boolean isSpecialText(String specialText) {
		return specialText.contains("");
	}
	public static String specialTextDecode(String specialText, ViewEntry viewEntry) {
		String result = specialText;
		//if(true) return result;

		String specialStart = "";
		String specialEnd = "�";

		// First, find the start and end of the special text
		int start_pos = result.indexOf(specialStart);
		int end_pos = result.indexOf(specialEnd);

		// This is just in case things get out of hand - no need to have broken code
		//	result in an infinite loop on the server
		int loopStopper = 1;
		while(start_pos > -1 && end_pos > start_pos && loopStopper < 100) {
			loopStopper++;

			// "working" holds the text we're going to replace, minus the delimiters
			// "result" holds the text we're going to replace working and the delimiters with
			String working = result.substring(start_pos+1, end_pos);
			String midResult = "";
			String[] choices;
			int offset, length, parameterCount;

			switch(working.charAt(0)) {
			case 'C':
				// @DocChildren
				parameterCount = Integer.parseInt(working.substring(1, 2));
				switch(parameterCount) {
				case 0:
					midResult = viewEntry.getChildCount() + "";
					break;
				case 1:
					midResult = strRight(working, "=").replaceAll("%", viewEntry.getChildCount() + "");
					break;
				case 2:
					// For convenience, I'll break the string into each option, even if I only use one
					choices = new String[] { "", "" };

					// I can cheat a bit on the first one to find the length
					offset = 0;
					length = Integer.parseInt(strLeft(strRight(working, ";"), "="));
					choices[0] = working.substring(working.indexOf("=", offset)+1, working.indexOf("=", offset)+1+length);

					offset = working.indexOf("=", offset) + 1 + length;
					choices[1] = working.substring(working.indexOf("=", offset)+1, working.indexOf("=", offset)+1+length);

					if(viewEntry.getChildCount() == 0) {
						midResult = choices[0].replaceAll("%", "0");
					} else {
						midResult = choices[1].replaceAll("%", viewEntry.getChildCount() + "");
					}

					break;
				case 3:
					// For convenience, I'll break the string into each option, even if I only use one
					choices = new String[] { "", "", "" };

					// I can cheat a bit on the first one to find the length
					offset = 0;
					length = Integer.parseInt(strLeft(strRight(working, ";"), "="));
					choices[0] = working.substring(working.indexOf("=", offset)+1, working.indexOf("=", offset)+1+length);

					offset = working.indexOf("=", offset) + 2 + length;
					length = Integer.parseInt(working.substring(offset, working.indexOf("=", offset)));
					choices[1] = working.substring(working.indexOf("=", offset)+1, working.indexOf("=", offset)+1+length);

					offset = working.indexOf("=", offset) + 2 + length;
					length = Integer.parseInt(working.substring(offset, working.indexOf("=", offset)));
					choices[2] = working.substring(working.indexOf("=", offset)+1, working.indexOf("=", offset)+1+length);

					if(viewEntry.getChildCount() == 0) {
						midResult = choices[0].replaceAll("%", "0");
					} else if(viewEntry.getChildCount() == 1) {
						midResult = choices[1].replaceAll("%", "1");
					} else {
						midResult = choices[2].replaceAll("%", viewEntry.getChildCount() + "");
					}

					break;
				}
				break;
			case 'D':
				// @DocDescendants
				parameterCount = Integer.parseInt(working.substring(1, 2));
				switch(parameterCount) {
				case 0:
					midResult = viewEntry.getDescendantCount() + "";
					break;
				case 1:
					midResult = strRight(working, "=").replaceAll("%", viewEntry.getDescendantCount() + "");
					break;
				case 2:
					// For convenience, I'll break the string into each option, even if I only use one
					choices = new String[] { "", "" };

					// I can cheat a bit on the first one to find the length
					offset = 0;
					length = Integer.parseInt(strLeft(strRight(working, ";"), "="));
					choices[0] = working.substring(working.indexOf("=", offset)+1, working.indexOf("=", offset)+1+length);

					offset = working.indexOf("=", offset) + 1 + length;
					choices[1] = working.substring(working.indexOf("=", offset)+1, working.indexOf("=", offset)+1+length);

					if(viewEntry.getDescendantCount() == 0) {
						midResult = choices[0].replaceAll("%", "0");
					} else {
						midResult = choices[1].replaceAll("%", viewEntry.getDescendantCount() + "");
					}

					break;
				case 3:
					// For convenience, I'll break the string into each option, even if I only use one
					choices = new String[] { "", "", "" };

					// I can cheat a bit on the first one to find the length
					offset = 0;
					length = Integer.parseInt(strLeft(strRight(working, ";"), "="));
					choices[0] = working.substring(working.indexOf("=", offset)+1, working.indexOf("=", offset)+1+length);

					offset = working.indexOf("=", offset) + 2 + length;
					length = Integer.parseInt(working.substring(offset, working.indexOf("=", offset)));
					choices[1] = working.substring(working.indexOf("=", offset)+1, working.indexOf("=", offset)+1+length);

					offset = working.indexOf("=", offset) + 2 + length;
					length = Integer.parseInt(working.substring(offset, working.indexOf("=", offset)));
					choices[2] = working.substring(working.indexOf("=", offset)+1, working.indexOf("=", offset)+1+length);

					if(viewEntry.getDescendantCount() == 0) {
						midResult = choices[0].replaceAll("%", "0");
					} else if(viewEntry.getDescendantCount() == 1) {
						midResult = choices[1].replaceAll("%", "1");
					} else {
						midResult = choices[2].replaceAll("%", viewEntry.getDescendantCount() + "");
					}

					break;
				}
				break;
			case 'H':
				// @DocLevel
				midResult = (viewEntry.getIndentLevel()+1) + ""; 
				break;
			case 'A':
				// @DocNumber
				/* Three forms:
				 * @DocNumber - all levels separated by "."
				 * @DocNumber("") - only the least significant level
				 * @DocNumber(char) - all levels separated by char. Note: the formula accepts a multi-character string, but
				 * 	displays it as just the string, not the doc level
				 */
				parameterCount = Integer.parseInt(working.substring(1, 2));
				switch(parameterCount) {
				case 0:
					midResult = viewEntry.getPosition('.');
					break;
				case 1:
					String delimiter = strRight(working, "=");
					if(delimiter.length() == 0) {
						midResult = strRightBack(viewEntry.getPosition('.'), ".");
					} else if(delimiter.length() > 1) {
						// Mimic formula's weird behavior for multi-character strings
						midResult = delimiter;
					} else {
						midResult = viewEntry.getPosition(delimiter.charAt(0));
					}
					break;
				}
				break;
			case 'J':
				// @DocParentNumber
				// Same as above, just for the parent, so do the same thing and chomp off the last bit
				if(viewEntry.getIndentLevel() == 0) {
					midResult = "";
				} else {
					parameterCount = Integer.parseInt(working.substring(1, 2));
					switch(parameterCount) {
					case 0:
						midResult = strLeftBack(viewEntry.getPosition('.'), ".");
						break;
					case 1:
						String delimiter = strRight(working, "=");
						if(delimiter.length() == 0) {
							midResult = strRightBack(strLeftBack(viewEntry.getPosition('.'), "."), ".");
						} else if(delimiter.length() > 1) {
							// Mimic formula's weird behavior for multi-character strings
							midResult = delimiter;
						} else {
							midResult = strLeftBack(viewEntry.getPosition(delimiter.charAt(0)), delimiter);
						}
						break;
					}
				}
				break;
			case 'B':
				// @DocSiblings
				midResult = (viewEntry.getSiblingCount()) + "";
				break;
			case 'I':
				// @IsCategory
				/* Three forms:
				 * @IsCategory - "*" if it's a category, "" otherwise
				 * @IsCategory(string) - string if it's a category, "" otherwise
				 * @IsCategory(string1, string 2) - string1 if it's a category, string2 otherwise
				 */
				parameterCount = Integer.parseInt(working.substring(1, 2));
				switch(parameterCount) {
				case 0:
					midResult = viewEntry.isCategory() ? "*" : "";
					break;
				case 1:
					midResult = viewEntry.isCategory() ? strRight(working, "=") : "";
					break;
				case 2:
					// For convenience, I'll break the string into each option, even if I only use one
					choices = new String[] { "", "" };
					offset = 0;
					length = Integer.parseInt(strLeft(strRight(working, ";"), "="));
					choices[0] = working.substring(working.indexOf("=", offset)+1, working.indexOf("=", offset)+1+length);

					offset = working.indexOf("=", offset) + 2 + length;
					length = Integer.parseInt(working.substring(offset, working.indexOf("=", offset)));
					choices[1] = working.substring(working.indexOf("=", offset)+1, working.indexOf("=", offset)+1+length);

					midResult = viewEntry.isCategory() ? choices[0] : choices[1];

					break;
				}

				break;
			case 'G':
				// @IsExpandable
				// This is a UI function that changes based on the expanded/collapsed state of the entry in
				//	the Notes client. This kind of behavior could be better done without @functions on the web,
				//	so it's not really worth implementing
				midResult = "";
				break;
			default:
				midResult = working;
			break;
			}

			result = result.replaceAll(specialStart + working + specialEnd, midResult);

			start_pos = result.indexOf(specialStart);
			end_pos = result.indexOf(specialEnd);
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	public static Map<Object, Object> getFlashScope() {
		return (Map<Object, Object>)getVariableValue("flashScope");
	}
	@SuppressWarnings("unchecked")
	public static void addMessage(String type, String message) {
		Map<Object, Object> flashScope = getFlashScope();
		List<Object> messages = (List<Object>)flashScope.get(type + "Messages");
		if(messages == null) {
			messages = new ArrayList<Object>();
			flashScope.put(type + "Messages", messages);
		}
		messages.add(message);
	}
	@SuppressWarnings("unchecked")
	public static Map<String, String> getParam() {
		return (Map<String, String>)getVariableValue("param");
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Cookie> getCookie() {
		return (Map<String, Cookie>)getVariableValue("cookie");
	}

	@SuppressWarnings("unchecked")
	public static boolean isDocEditableBy(Document doc, String userName) throws lotus.domino.NotesException {
		Database database = XSPUtil.getCurrentDatabase();

		// Easy check first
		int level = database.queryAccess(userName);
		if(level >= ACL.LEVEL_EDITOR) {
			return true;
		} else if(level < ACL.LEVEL_AUTHOR) {
			return false;
		}

		// Gather the effective user names and roles
		DominoServer server = new DominoServer(database.getServer());
		Collection<String> rawNames = (Collection<String>)server.getNamesList(userName);
		Set<String> lowerNames = new HashSet<String>();
		for(String name : rawNames) { lowerNames.add(name.toLowerCase()); }
		for(String role : (List<String>)database.queryAccessRoles(userName)) { lowerNames.add(role.toLowerCase()); }

		// Now look through all the items in the doc to see if they're an Authors field
		// If so, check to see if any of the names or roles are in them
		for(Item item : (List<Item>)doc.getItems()) {
			if(item.getType() == Item.AUTHORS) {
				Set<String> itemLowerNames = new HashSet<String>();
				for(Object name : (List<Object>)item.getValues()) { itemLowerNames.add(name.toString().toLowerCase()); }
				itemLowerNames.retainAll(lowerNames);
				if(itemLowerNames.size() > 0) {
					// Then at least one name is common between them
					return true;
				}
			}
		}

		return false;
	}
}