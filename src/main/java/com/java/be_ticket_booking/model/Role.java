package com.java.be_ticket_booking.model;

import com.java.be_ticket_booking.model.enumModel.ERole;
import jakarta.persistence.*;

@Entity
@Table(name = "Role",
		uniqueConstraints = { @UniqueConstraint(columnNames = { "name" })
	})
public class Role {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO )
	@Column(name = "id")
	private Long id;
	
	@Column(name = "name")
	private String name;
	
	public Role() {}
	
	public Role(Long id, ERole role) {
		this.id = id;
		this.name = role.name();
	}
	
	public Role(ERole role) {
		this.name = role.name();
	}
	
	public Long getId() {
		return this.id;
	}
	
	public String getRole() {
		return this.name;
	}
	
	public void setRole(ERole role) {
		this.name = role.name();
	}
	
}
