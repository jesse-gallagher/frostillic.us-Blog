package model;

import org.jnosql.artemis.Column;
import org.jnosql.artemis.Entity;
import org.jnosql.artemis.Id;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity @Data @NoArgsConstructor
public class Post {
	@Id @Column private String id;
	@Column private String title;
	@Column private Date posted;
	@Column private String postedBy;
	@Column private String bodyMarkdown;
	@Column private String bodyHtml;
}
