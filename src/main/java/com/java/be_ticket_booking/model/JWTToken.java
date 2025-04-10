package com.java.be_ticket_booking.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "Token")
public class JWTToken {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", unique = true, nullable = false, length = 26, insertable = false)
    private String id;
	
	@OneToOne
	private Account user;
	
	@Column(name = "refresh_token", length = 3000)
	@NotBlank
	@NotNull
	private String refreshToken;
	
	@Column(name = "access_token", length = 3000)
	@NotBlank
	@NotNull
	private String accessToken;
	
	public JWTToken() {}
	
	public JWTToken(Account user, String accessToken, String refreshToken) {
		this.user = user;
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
	}
	
	public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
	
	public Account getUser() {
		return this.user;
	}
	
	public void setUser(Account user) {
		this.user = user;
	}
	
	public String getAccessToken() {
		return this.accessToken;
	}
	
	public void setAccessToken(String token) {
		this.accessToken = token;
	}
	
	public String getRefreshToken() {
		return this.refreshToken;
	}
	
	public void setRefreshToken(String token) {
		this.refreshToken = token;
	}
}



