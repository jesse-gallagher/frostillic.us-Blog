package model.event;

import lombok.Value;
import model.MicroPost;

@Value
public class MicroPostEvent {
	MicroPost post;
}
