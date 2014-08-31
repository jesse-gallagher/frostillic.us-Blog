package frostillicus.event;

import java.util.EventListener;

public interface XPagesEventListener extends EventListener {
	public void receiveEvent(XPagesEvent event);
}
