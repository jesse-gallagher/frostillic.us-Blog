package model.event;

import lombok.Value;
import model.Post;

@Value
public class PostEvent {
	public enum Type {
		PUBLISH, UPDATE, DELETE
	}
	
	Post post;
	Type type;
}
