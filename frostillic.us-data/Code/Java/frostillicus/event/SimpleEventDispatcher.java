package frostillicus.event;

import java.util.*;

public class SimpleEventDispatcher implements XPagesEventDispatcher {
	private final Set<XPagesEventListener> listeners = new HashSet<XPagesEventListener>();

	@SuppressWarnings("unchecked")
	public void setListenerClasses(List<String> listenerClassNames) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
		for(String className : listenerClassNames) {
			Class<XPagesEventListener> listenerClass = (Class<XPagesEventListener>)Class.forName(className);
			this.addListener(listenerClass.newInstance());
		}
	}

	public void addListener(XPagesEventListener listener) {
		this.listeners.add(listener);
	}

	public void dispatchEvent(XPagesEvent event) {
		for(XPagesEventListener listener : this.listeners) {
			listener.receiveEvent(event);
		}
	}

	public void dispatch(String eventName) {
		this.dispatch(eventName, new Object[] { });
	}
	public void dispatch(String eventName, Object... eventPayload) {
		this.dispatchEvent(new SimpleXPagesEvent(eventName, eventPayload));
	}
}
