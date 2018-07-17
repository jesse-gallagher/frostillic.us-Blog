package model;

import javax.validation.constraints.NotEmpty;

import org.jnosql.artemis.Column;
import org.jnosql.artemis.Convert;
import org.jnosql.artemis.Entity;
import org.jnosql.artemis.Id;

import lombok.Data;
import lombok.NoArgsConstructor;
import util.BooleanYNConverter;

@Entity @Data @NoArgsConstructor
public class Link {
	@Id @Column private String id;
	@Column private String category;
	@Column("link_url") @NotEmpty private String url;
	@Column("link_name") @NotEmpty private String name;
	@Column("link_visible") @Convert(BooleanYNConverter.class) private boolean visible;
}
