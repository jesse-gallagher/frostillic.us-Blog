package frostillicus.ruby;


import java.util.Map;
import javax.faces.context.*;
import javax.faces.event.*;
import javax.servlet.http.HttpServletRequest;

import org.jruby.embed.ScriptingContainer;

@SuppressWarnings("serial")
public class RubyPhaseListener implements PhaseListener {
	
	@SuppressWarnings("unchecked")
	public void afterPhase(PhaseEvent event) {
		FacesContext facesContext = event.getFacesContext();
		
		/*Map requestScope = facesContext.getExternalContext().getRequestMap();
		if(requestScope.containsKey("_RubyScriptEngine")) {
			ScriptingContainer engine = (ScriptingContainer)requestScope.get("_RubyScriptEngine");
			engine.terminate();
			requestScope.remove("_RubyScriptEngine");
		}*/
	}
	public void beforePhase(PhaseEvent event) { }
	public PhaseId getPhaseId() { return PhaseId.RENDER_RESPONSE; }
}
