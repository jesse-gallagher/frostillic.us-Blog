package model;

import java.util.Date;

import javax.validation.constraints.NotEmpty;

import org.darwino.jnosql.artemis.extension.ISODateConverter;
import org.jnosql.artemis.Column;
import org.jnosql.artemis.Convert;
import org.jnosql.artemis.Entity;
import org.jnosql.artemis.Id;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity @Data @NoArgsConstructor
public class Comment {
	@Id @Column @NotEmpty private String id;
	@Column @NotEmpty @Convert(ISODateConverter.class) private Date posted;
	@Column @NotEmpty private String postedBy;
	@Column private String bodyMarkdown;
	@Column @NotEmpty private String bodyHtml;
}
