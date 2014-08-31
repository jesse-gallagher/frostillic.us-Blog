package frostillicus.event;

public interface XPagesEventDispatcher {
	public void dispatchEvent(XPagesEvent event);
	public void addListener(XPagesEventListener listener);
}