package model;

import java.util.Date;
import java.util.List;

import org.darwino.jnosql.diana.attachment.EntityAttachment;
import org.darwino.jnosql.diana.driver.EntityConverter;
import org.jnosql.artemis.Column;
import org.jnosql.artemis.Entity;
import org.jnosql.artemis.Id;

import com.darwino.jsonstore.Document;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity @Data @NoArgsConstructor
public class Media {
	@Id @Column private String id;
	@Column private String name;
	@Column(EntityConverter.ATTACHMENT_FIELD) private List<EntityAttachment> attachments;
	@Column(Document.SYSTEM_META_MDATE) private Date lastModificationDate;
	@Column(Document.SYSTEM_META_CUSER) private String creationUser;
}
