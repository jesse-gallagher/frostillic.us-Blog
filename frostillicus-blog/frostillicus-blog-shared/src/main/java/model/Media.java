package model;

import java.util.Date;
import java.util.List;

import org.darwino.jnosql.diana.driver.EntityConverter;
import jakarta.nosql.mapping.Column;
import jakarta.nosql.mapping.Entity;
import jakarta.nosql.mapping.Id;
import org.jnosql.diana.driver.attachment.EntityAttachment;

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
