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

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.script.ScriptException;

import org.jruby.embed.*;

import com.ibm.xsp.resource.Resource;
import com.ibm.xsp.resource.ScriptResource;
import com.ibm.xsp.util.ValueBindingUtil;
import com.ibm.xsp.binding.BindingFactory;
import com.ibm.xsp.component.UIViewRootEx2;
import com.ibm.xsp.context.FacesContextEx;

public class RubyBindingFactory implements BindingFactory {
	
	@SuppressWarnings("unchecked")
	public MethodBinding createMethodBinding(Application arg0, String arg1, Class[] arg2) {
		String script = ValueBindingUtil.parseSimpleExpression(arg1);
		return new RubyMethodBinding(script);
	}

	public ValueBinding createValueBinding(Application arg0, String arg1) {
		String script = ValueBindingUtil.parseSimpleExpression(arg1);
		return new RubyValueBinding(script);
	}

	public String getPrefix() {
		return "ruby";
	}
	
	/* Utility functions for the binding classes */
	@SuppressWarnings("unchecked")
	public static ScriptingContainer getScriptEngine(FacesContext facesContext) throws ScriptException, IOException {
		// Check to see if there's an engine in the current request; if so, use that, and otherwise generate a new one
		//ScriptingContainer engine = (ScriptingContainer)facesContext.getExternalContext().getRequestMap().get("_RubyScriptEngine");
		Map<Object, Object> applicationScope = facesContext.getExternalContext().getApplicationMap();
		ScriptingContainer engine = (ScriptingContainer)applicationScope.get("_RubyScriptEngine");
		if(engine == null) {
			engine = new ScriptingContainer(LocalContextScope.SINGLETHREAD);
			engine.setClassLoader(facesContext.getContextClassLoader());
			
			
			//facesContext.getExternalContext().getRequestMap().put("_RubyScriptEngine", engine);
			applicationScope.put("_RubyScriptEngine", engine);
			if(applicationScope.containsKey("_RubyCompiledLibraries")) {
				applicationScope.remove("_RubyCompiledLibraries");
			}
			applicationScope.put("_RubyCompiledLibraries", new HashMap<String, EmbedEvalUnit>());
		}
		engine.runScriptlet("require \"java\"\n" +
				"def method_missing(name, *args)\n" +
				"	if args.size == 0\n" +
				"		context = javax.faces.context.FacesContext.current_instance\n" +
				"		val = context.application.variable_resolver.resolve_variable(context, name.to_s)\n" +
				"		if val != nil\n" +
				"			return val\n" +
				"		end\n" +
				"	end\n" +
				"	super\n" +
				"end");
		RubyBindingFactory.includeScriptLibraries(engine, facesContext);
		return engine;
	}
	
	@SuppressWarnings("unchecked")
	protected static void includeScriptLibraries(ScriptingContainer engine, FacesContext context) throws IOException, ScriptException {
		
		// First, find or create the request-specific list of already-included libraries
		// Libraries can be added after the initial page load, either via themes or via code, so this check is necessary
		// 	on every binding. The set will make sure it doesn't do more work than necessary
		Set<String> includedLibraries = (Set<String>)context.getExternalContext().getRequestMap().get("_RubyIncludedLibraries");
		if(includedLibraries == null) {
			includedLibraries = new HashSet<String>();
			context.getExternalContext().getRequestMap().put("_RubyIncludedLibraries", includedLibraries);
		}
		
		Map<String, EmbedEvalUnit> compiledLibraries = (Map<String, EmbedEvalUnit>)context.getExternalContext().getApplicationMap().get("_RubyCompiledLibraries");
		
		// Now look through the view's resources for appropriate scripts and load them
		UIViewRootEx2 view = (UIViewRootEx2)context.getViewRoot();
		if(view != null) {
			for(Resource res : view.getResources()) {
				if(res instanceof ScriptResource) {
					ScriptResource script = (ScriptResource)res;
					if(script.getType() != null && (script.getType().equalsIgnoreCase("text/x-ruby") || script.getType().equalsIgnoreCase("text/ruby"))) {
						// Then we have a Ruby script - find its contents and run it
						
						String properName = (script.getSrc().charAt(0) == '/' ? "" : "/") + script.getSrc();
						if(!includedLibraries.contains(properName)) {
							// If we haven't compiled the library before, do so now
							if(!compiledLibraries.containsKey(properName)) {
								InputStream is = FacesContextEx.getCurrentInstance().getExternalContext().getResourceAsStream("/WEB-INF/ruby" + properName);
								if(is == null) {
									// The SSJS interpreter throws an exception when the library can't be loaded, so match that behavior
									throw new ScriptException("Couldn't load library: " + properName);
								} else {
									//engine.runScriptlet(is, properName);
									compiledLibraries.put(properName, engine.parse(is, properName));
									try { is.close(); } catch(IOException ioe) { }
								}
							}
							compiledLibraries.get(properName).run();
							
							includedLibraries.add(properName);
						}
					}
				}
			}
		}
		
	}
}
