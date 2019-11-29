package model;

import jakarta.nosql.mapping.Column;
import jakarta.nosql.mapping.Entity;
import jakarta.nosql.mapping.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity @Data @NoArgsConstructor
public class AccessToken {
	@Id @Column private String id;
	@Column private String userName;
	@Column private String name;
	@Column private String token;
	@Column private boolean isConflict;
}
