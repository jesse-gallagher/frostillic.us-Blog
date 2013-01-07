package frostillicus.event;

import java.io.Serializable;
import lombok.*;

@ToString
public class SimpleXPagesEvent implements XPagesEvent, Serializable {
	private @Getter @Setter String eventName;
	private @Getter Object[] eventPayload;

	public SimpleXPagesEvent(String eventName, Object... eventPayload) {
		this.eventName = eventName;
		this.eventPayload = eventPayload;
	}

	private static final long serialVersionUID = 2494192698824356379L;
}
