/*
 * © Copyright Jesse Gallagher 2012
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */

package frostillicus;


import com.ibm.xsp.FacesExceptionEx;
import com.ibm.xsp.extlib.component.dynamicview.UIDynamicViewPanel;
import com.ibm.xsp.extlib.component.dynamicview.ViewDesign;
import com.ibm.xsp.extlib.component.dynamicview.ViewDesign.*;
import com.ibm.xsp.extlib.component.dynamicview.ViewColumnConverter;
import com.ibm.xsp.extlib.component.dynamicview.DominoDynamicColumnBuilder.DominoViewCustomizer;
import com.ibm.commons.util.SystemCache;
import com.ibm.xsp.extlib.builder.ControlBuilder.IControl;
import com.ibm.xsp.model.domino.wrapped.DominoViewEntry;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import lotus.domino.*;

import com.raidomatic.xml.*;


public class DynamicViewCustomizer extends DominoViewCustomizer {
	UIDynamicViewPanel panel;
	
	public ViewFactory getViewFactory() {
		return new DynamicViewFactory();
	}

	public class DynamicViewFactory extends DefaultViewFactory {
		private static final long serialVersionUID = 123034173761337005L;
		private SystemCache views = new SystemCache("View Definition", 16, "xsp.extlib.viewdefsize");
		
		public ViewDef getViewDef(View view) {
			if(view == null) return null; 
			try {
				String viewKey = ViewDesign.getViewKey(view);
				DefaultViewDef viewDef = (DefaultViewDef)views.get(viewKey);
				if(viewDef == null) {
					// Read the view
					viewDef = new DefaultViewDef();
					if(view.isHierarchical()) viewDef.flags |= DefaultViewDef.FLAG_HIERARCHICAL;
					if(view.isCategorized()) viewDef.flags |= DefaultViewDef.FLAG_CATEGORIZED;
					
					viewDef.columns.addAll(this.getViewColumnInformation(view));
				}
				return viewDef;
			} catch(Exception ex) {
				throw new FacesExceptionEx(ex, "Error while accessing view {0}", view.toString());
			}
		}
		
		@SuppressWarnings("unchecked")
		private List<ColumnDef> getViewColumnInformation(View view) throws Exception {
			Database database = view.getParent();
			
			/* Generate the DXL */
			Document viewDoc = database.getDocumentByUNID(view.getUniversalID());
			String dxl = viewDoc.generateXML();
			InputStream is = new ByteArrayInputStream(dxl.getBytes(Charset.forName("UTF-8")));
			XMLDocument dxlDoc = new XMLDocument();
			dxlDoc.loadInputStream(is);
			viewDoc.recycle();
			
			/* Fetch both types of column information since some properties are much easier to deal
			 * with via the standard API */
			List<ViewColumn> viewColumns = view.getColumns();
			List<XMLNode> dxlColumns = dxlDoc.selectNodes("//column");
			
			// Figure out if we're going to extend the last column
			boolean extendLastColumn = dxlDoc.selectSingleNode("//view").getAttribute("extendlastcolumn").equals("true");
			
			Document contextDoc = database.createDocument();
			List<ColumnDef> columns = new Vector<ColumnDef>();
			String activeColorColumn = "";
			for(int i = 0; i < dxlColumns.size(); i++) {
				XMLNode columnNode = dxlColumns.get(i);
				ViewColumn viewColumn = viewColumns.get(i);
				
				ExtendedColumnDef column = new ExtendedColumnDef();
				
				if(columnNode.getAttribute("hidden").equals("true")) {
					column.flags |= DefaultColumnDef.FLAG_HIDDEN;
				} else {
					// Check to see if it's hidden by a hide-when formula
					XMLNode hideWhen = columnNode.selectSingleNode("code[@event='hidewhen']");
					if(hideWhen != null) {
						if(hideWhen.getAttribute("enabled") == null || !hideWhen.getAttribute("enabled").equals("false")) {
							String hideWhenFormula = hideWhen.getText();
							if(hideWhenFormula.length() > 0) {
								List<Object> evalResult = JSFUtil.getSession().evaluate(hideWhenFormula, contextDoc);
								if(evalResult.size() > 0 && evalResult.get(0) instanceof Double && (Double)evalResult.get(0) == 1) {
									column.flags |= DefaultColumnDef.FLAG_HIDDEN;
								}
							}
						}
					}
				}
				
				
				column.name = columnNode.getAttribute("itemname");
				
				if(columnNode.getAttribute("showascolor").equals("true")) {
					activeColorColumn = column.name;
				}
				column.colorColumn = activeColorColumn;
				
				// Get the header information
				XMLNode header = columnNode.selectSingleNode("columnheader");
				column.title = columnNode.selectSingleNode("columnheader").getAttribute("title");
				if(header.getAttribute("align").equals("center")) column.flags |= DefaultColumnDef.FLAG_HALIGNCENTER;
				else if(header.getAttribute("align").equals("right")) column.flags |= DefaultColumnDef.FLAG_HALIGNRIGHT;
				
				column.width = new Float(columnNode.getAttribute("width")).intValue();

				if(columnNode.getAttribute("responsesonly").equals(true)) column.flags |= DefaultColumnDef.FLAG_RESPONSE;
				if(columnNode.getAttribute("categorized").equals("true")) column.flags |= DefaultColumnDef.FLAG_CATEGORIZED;
				if(columnNode.getAttribute("sort").length() > 0) column.flags |= DefaultColumnDef.FLAG_SORTED;
				if(columnNode.getAttribute("resort").equals("ascending") || columnNode.getAttribute("resort").equals("both")) column.flags |= DefaultColumnDef.FLAG_RESORTASC;
				if(columnNode.getAttribute("resort").equals("descending") || columnNode.getAttribute("resort").equals("both")) column.flags |= DefaultColumnDef.FLAG_RESORTDESC;
				//if(columnNode.getAttribute("align").equals("center")) column.flags |= DefaultColumnDef.FLAG_ALIGNCENTER;
				//else if(columnNode.getAttribute("align").equals("right")) column.flags |= DefaultColumnDef.FLAG_ALIGNRIGHT;
				if(columnNode.getAttribute("showaslinks").equals("true")) column.flags |= DefaultColumnDef.FLAG_LINK | DefaultColumnDef.FLAG_ONCLICK | DefaultColumnDef.FLAG_CHECKBOX;
				
				column.numberFmt = viewColumn.getNumberFormat();
				column.numberDigits = viewColumn.getNumberDigits();
				column.numberAttrib = viewColumn.getNumberAttrib();
				if(viewColumn.isNumberAttribParens()) column.flags |= DefaultColumnDef.FLAG_ATTRIBPARENS;
				if(viewColumn.isNumberAttribPercent()) column.flags |= DefaultColumnDef.FLAG_ATTRIBPERCENT;
				if(viewColumn.isNumberAttribPunctuated()) column.flags |= DefaultColumnDef.FLAG_ATTRIBPUNC;
				column.timeDateFmt = viewColumn.getTimeDateFmt();
				column.dateFmt = viewColumn.getDateFmt();
				column.timeFmt = viewColumn.getTimeFmt();
				column.timeZoneFmt = viewColumn.getTimeZoneFmt();
				column.listSep = viewColumn.getListSep();

				if(columnNode.getAttribute("showasicons").equals("true")) column.flags |= DefaultColumnDef.FLAG_ICON;
				if(columnNode.getAttribute("twisties").equals("true")) column.flags |= DefaultColumnDef.FLAG_INDENTRESP;
				
				// Find any twistie image
				XMLNode twistieImage = columnNode.selectSingleNode("twistieimage/imageref");
				if(twistieImage != null) {
					if(twistieImage.getAttribute("database").equals("0000000000000000")) {
						column.twistieReplicaId = database.getReplicaID();
					} else {
						column.twistieReplicaId = twistieImage.getAttribute("database");
					}
					
					// Make sure that the referenced database is available on the current server
					boolean setTwistie = true;
					if(!column.twistieReplicaId.equalsIgnoreCase(database.getReplicaID())) {
						Database twistieDB = JSFUtil.getSession().getDatabase("", "");
						twistieDB.openByReplicaID("", column.twistieReplicaId);
						if(!twistieDB.isOpen()) {
							setTwistie = false;
						}
						twistieDB.recycle();
					}
					if(setTwistie) column.twistieImage = twistieImage.getAttribute("name");
					
				}
				
				// Support extending the column width to the full window.
				// In the client, "extend last column" takes priority
				if(extendLastColumn && i == dxlColumns.size()-1) {
					column.extendColumn = true;
				} else if(!extendLastColumn && columnNode.getAttribute("extwindowwidth").equals("true")) {
					column.extendColumn = true;
				}
				
				columns.add(column);
				
				viewColumn.recycle();
			}
			contextDoc.recycle();
			
			database.recycle();
			
			return columns;
		}
		
		public class ExtendedColumnDef extends DefaultColumnDef {
			public String colorColumn;
			public String twistieImage = "";
			public String twistieReplicaId = "";
			public boolean extendColumn = false;
		}
	}
	
	public boolean createColumns(FacesContext context, UIDynamicViewPanel panel, ViewFactory f) {
		// All I care about here is getting the panel for later
		this.panel = panel;
		return false;
	}
	
	public void afterCreateColumn(FacesContext context, int index, ColumnDef colDef, IControl column) {
		// Patch in a converter to handle special text
		UIDynamicViewPanel.DynamicColumn col = (UIDynamicViewPanel.DynamicColumn)column.getComponent();
		col.setConverter(new ExtendedViewColumnConverter(null, colDef, panel));
		
		// We'll handle escaping the HTML manually, to support [<b>Notes-style</b>] pass-through-HTML
		col.setContentType("html");
		
		// Deal with any twistie images and color columns
		if(colDef instanceof DynamicViewFactory.ExtendedColumnDef) {
			DynamicViewFactory.ExtendedColumnDef extColDef = (DynamicViewFactory.ExtendedColumnDef)colDef;
			
			if(extColDef.twistieImage.length() > 0) {
				// Assume that it's a multi-image well for now
				col.setCollapsedImage("/.ibmxspres/domino/__" + extColDef.twistieReplicaId + ".nsf/" + extColDef.twistieImage.replaceAll("\\\\", "/") + "?Open&ImgIndex=2");
				col.setExpandedImage("/.ibmxspres/domino/__" + extColDef.twistieReplicaId + ".nsf/" + extColDef.twistieImage.replaceAll("\\\\", "/") + "?Open&ImgIndex=1");
			}
			
			// The style applies to the contents of the column as well as the column itself, which messes with icon columns
			// For now, don't apply it at all to those columns
			String widthStyle = "";
			if((extColDef.flags & DefaultColumnDef.FLAG_ICON) == 0 && !extColDef.extendColumn && !extColDef.isCategorized()) {
				widthStyle = "max-width: " + (extColDef.width * 10) + "px; min-width: " + (extColDef.width * 10) + "px";
			} else if(extColDef.extendColumn && !extColDef.isCategorized()) {
				widthStyle = "width: auto";
			}
			
			if(extColDef.colorColumn.length() > 0) {
				String styleFormula = "#{javascript:'" + widthStyle + ";' + " + getClass().getName() + ".colorColumnToStyle(" + panel.getVar() + ".getColumnValue('" + extColDef.colorColumn + "'))}";
				ValueBinding style = context.getApplication().createValueBinding(styleFormula);
				col.setValueBinding("style", style);
			} else {
				col.setStyle(widthStyle);
			}
		}
      }
	public class ExtendedViewColumnConverter extends ViewColumnConverter {
		ColumnDef colDef;
		UIDynamicViewPanel panel;
		
		public ExtendedViewColumnConverter(ViewDef viewDef, ColumnDef colDef, UIDynamicViewPanel panel) {
			super(viewDef, colDef);
			this.colDef = colDef;
			this.panel = panel;
		}
		
		@Override
		public String getValueAsString(FacesContext context, UIComponent component, Object value) {
			// First, apply any color-color info needed
			DominoViewEntry entry = resolveViewEntry(context, panel.getVar());
			try {
				if(value instanceof DateTime) {
					return getValueDateTimeAsString(context, component, ((DateTime)value).toJavaDate());
				}
				if(value instanceof Date) {
					return getValueDateTimeAsString(context, component, (Date)value);
				}
				if(value instanceof Number) {
					return getValueNumberAsString(context, component, (Number)value);
				}
			} catch(NotesException ex) { }
			
			String stringValue = value.toString();
			
			
			try {
				stringValue = JSFUtil.specialTextDecode(stringValue, entry);
			} catch(NotesException ne) { }
			
			// Process the entry as Notes-style pass-through-HTML
			stringValue = handlePassThroughHTML(stringValue);
			
			
			// Add in some text for empty categories
			if(entry.isCategory() && stringValue.length() == 0) {
				stringValue = "(Not Categorized)";
			}
			
			// Include a &nbsp; to avoid weird styling problems when the content itself is empty or not visible
			return stringValue;
		}
		
		private String handlePassThroughHTML(String cellData) {
			if(cellData.contains("[<") && cellData.contains(">]")) {
				String[] cellChunks = cellData.split("\\[\\<", -2);
				cellData = "";
				for(String chunk : cellChunks) {
					if(chunk.contains(">]")) {
						String[] smallChunks = chunk.split(">]", -2);
						cellData += "<" + smallChunks[0] + ">" + JSFUtil.xmlEncode(smallChunks[1]);
					} else {
						cellData += JSFUtil.xmlEncode(chunk);
					}
				}
			}
			return cellData;
		}
		
		private DominoViewEntry resolveViewEntry(FacesContext context, String var) {
			return (DominoViewEntry)context.getApplication().getVariableResolver().resolveVariable(context, var);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static String colorColumnToStyle(Object colorValuesObj) {
		String cellStyle = "";
		if(colorValuesObj instanceof List) {
			List<Double> colorValues = (List<Double>)colorValuesObj;
			if(colorValues.size() > 3) {
				// Then we have a background color
				if(colorValues.get(0) != -1) {
					// Then the background is not pass-through
					cellStyle = "background-color: rgb(" + colorValues.get(0).intValue() + ", " + colorValues.get(1).intValue() + ", " + colorValues.get(2).intValue() + ");";
				} else {
					cellStyle = "";
				}
				if(colorValues.get(3) != -1) {
					// Then main color is not pass-through
					cellStyle += "color: rgb(" + colorValues.get(3).intValue() + ", " + colorValues.get(4).intValue() + ", " + colorValues.get(5).intValue() + ");";
				}
			} else {
				// Then it's just a text color
				if(colorValues.get(0) != -1) {
					cellStyle += "color: rgb(" + colorValues.get(0).intValue() + ", " + colorValues.get(1).intValue() + ", " + colorValues.get(2).intValue() + ");";
				}
			}
		}
		return cellStyle;
	}
}
