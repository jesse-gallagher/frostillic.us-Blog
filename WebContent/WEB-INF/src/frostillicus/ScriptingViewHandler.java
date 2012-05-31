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

import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
//import javax.script.ScriptEngineFactory;
//import javax.script.ScriptEngineManager;

import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.component.UIViewRootEx2;
import com.ibm.xsp.factory.FactoryLookup;

import frostillicus.ruby.RubyBindingFactory;
import org.jruby.*;
import org.jruby.embed.*;

public class ScriptingViewHandler extends com.ibm.xsp.application.ViewHandlerExImpl {

	public ScriptingViewHandler(ViewHandler arg0) {
		super(arg0);
	}
	
	@SuppressWarnings({ "deprecation" })
	@Override
	public UIViewRoot createView(FacesContext context, String paramString) {
		ApplicationEx app = (ApplicationEx)context.getApplication();
		FactoryLookup facts = app.getFactoryLookup();
		
		// Add the Ruby bindings
		RubyBindingFactory rfac = new frostillicus.ruby.RubyBindingFactory();
		if(facts.getFactory(rfac.getPrefix()) == null) {
			facts.setFactory(rfac.getPrefix(), rfac);
		}
		
		// Add any remaining languages, without special support
		/*ScriptEngineManager manager = new ScriptEngineManager();
		for(ScriptEngineFactory scriptFactory : manager.getEngineFactories()) {
			GenericBindingFactory fac = new GenericBindingFactory(scriptFactory.getLanguageName());
			if(facts.getFactory(fac.getPrefix()) == null) {
				facts.setFactory(fac.getPrefix(), fac);
			}
		}*/
		
		return super.createView(context, paramString);
	}

	@Override
	public UIViewRoot restoreView(FacesContext context, String paramString) {
		try {
			ScriptingContainer container = RubyBindingFactory.getScriptEngine(context);
			Ruby.setThreadLocalRuntime(container.getProvider().getRuntime());
		} catch(Exception e) { }
		
		UIViewRootEx2 result = (UIViewRootEx2) super.restoreView(context, paramString);
		return result;
	}
	
}
