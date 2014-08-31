package frostillicus.event;

import java.io.Serializable;

public class SimpleXPagesEvent implements XPagesEvent, Serializable {
	private static final long serialVersionUID = 1L;

	private String eventName;
	private Object[] eventPayload;

	public SimpleXPagesEvent(final String eventName, final Object... eventPayload) {
		this.eventName = eventName;
		this.eventPayload = eventPayload;
	}

	public String getEventName() {
		return eventName;
	}
	public void setEventName(final String eventName) {
		this.eventName = eventName;
	}

	public Object[] getEventPayload() {
		return eventPayload;
	}
}
