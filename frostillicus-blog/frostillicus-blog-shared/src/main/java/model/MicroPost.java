package model;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.UUID;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.darwino.jnosql.artemis.extension.converter.ISOOffsetDateTimeConverter;

import com.darwino.commons.util.StringUtil;

import jakarta.nosql.mapping.Column;
import jakarta.nosql.mapping.Convert;
import jakarta.nosql.mapping.Entity;
import jakarta.nosql.mapping.EntityPostPersit;
import jakarta.nosql.mapping.EntityPrePersist;
import jakarta.nosql.mapping.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.event.MicroPostEvent;

@Entity @Data @NoArgsConstructor
public class MicroPost {
	@Id @Column private String id;
	@Column @NotEmpty private String postId;
	@Column private String name;
	@Column @NotEmpty private String content;
	@Column @NotNull @Convert(ISOOffsetDateTimeConverter.class) private OffsetDateTime posted;
	@Column private boolean isConflict;
	
	// TODO attachments
	
	@Inject
	Event<MicroPostEvent> microPostEvent;
	
	void querySave(@Observes EntityPrePersist event) {
		if(!(event.getValue() instanceof MicroPost)) {
			return;
		}
		MicroPost post = (MicroPost)event.getValue();
		
		if(StringUtil.isEmpty(post.getPostId())) {
			post.setPostId(UUID.randomUUID().toString());
		}
		if(post.getPosted() == null) {
			post.setPosted(OffsetDateTime.now());
		}
	}
	
	void postSave(@Observes EntityPostPersit event) {
		if(!(event.getValue() instanceof MicroPost)) {
			return;
		}
		MicroPost post = (MicroPost)event.getValue();
		microPostEvent.fire(new MicroPostEvent(post));
	}
	
	public Date getPostedDate() {
		return Date.from(posted.toInstant());
	}
}
