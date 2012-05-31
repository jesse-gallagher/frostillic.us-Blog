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

package frostillicus.ruby;

import java.io.*;
import javax.faces.component.StateHolder;
import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.PropertyNotFoundException;

import com.ibm.xsp.binding.ValueBindingEx;
import javax.script.*;
import org.jruby.embed.ScriptingContainer;

public class RubyValueBinding extends ValueBindingEx implements Serializable, StateHolder {
	private static final long serialVersionUID = 3164174793723541283L;
	
	String content;
	private boolean _transient = false;
	
	// Empty constructor for restoreState()
	public RubyValueBinding() { super(); }
	
	public RubyValueBinding(String content) { this.content = content; }
	
	@SuppressWarnings("unchecked")
	@Override
	public Class getType(FacesContext arg0) throws EvaluationException, PropertyNotFoundException {
		return Object.class;
	}

	@Override
	public Object getValue(FacesContext context) throws EvaluationException, PropertyNotFoundException {
		Object result = null;
		try {
			ScriptingContainer engine = RubyBindingFactory.getScriptEngine(context);
			result = engine.runScriptlet(this.content);
		}
		catch(ScriptException se) { throw new EvaluationException(se); }
		catch(IOException ioe) { throw new EvaluationException(ioe); }
		
//		if(result instanceof org.jruby.RubyArray) {
//			Vector<Object> vResult = new Vector<Object>();
//			vResult.addAll((List<Object>)result);
//			return vResult;
//		}
		return result;
	}

	@Override public boolean isReadOnly(FacesContext arg0) throws EvaluationException, PropertyNotFoundException { return true; }
	@Override public void setValue(FacesContext arg0, Object arg1) throws EvaluationException, PropertyNotFoundException { }

	@Override public boolean isTransient() { return this._transient; }
	@Override public void setTransient(boolean arg0) { this._transient = arg0; }
	
	@Override
	public void restoreState(FacesContext context, Object paramObject) {
		Object[] arrayOfObject = (Object[]) paramObject;
		super.restoreState(context, arrayOfObject[0]);
		this.content = (String)arrayOfObject[1];
	}

	@Override
	public Object saveState(FacesContext context) {
		Object[] arrayOfObject = new Object[2];
		arrayOfObject[0] = super.saveState(context);
		arrayOfObject[1] = this.content;
		return arrayOfObject;
	}
}
