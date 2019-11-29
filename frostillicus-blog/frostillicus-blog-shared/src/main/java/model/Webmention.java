package model;

import java.time.OffsetDateTime;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.darwino.jnosql.artemis.extension.converter.ISOOffsetDateTimeConverter;

import jakarta.nosql.mapping.Column;
import jakarta.nosql.mapping.Convert;
import jakarta.nosql.mapping.Entity;
import jakarta.nosql.mapping.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity @Data @NoArgsConstructor
public class Webmention {
	public enum Type {
		Post
	}

	@Id @Column private String id;
	@Column @NotNull private Type type;
	@Column @NotEmpty private String targetId;
	@Column @NotEmpty private String source;
	@Column @NotNull @Convert(ISOOffsetDateTimeConverter.class) private OffsetDateTime posted;
	@Column private boolean verified;
	@Column private boolean approved;
	@Column private String problemCause;
	@Column private String sourceTitle;
	@Column("http_referer") private String httpReferer;
	@Column("http_user_agent") private String httpUserAgent;
	@Column("remote_addr") private String httpRemoteAddr;
	@Column private boolean isConflict;
	
}
