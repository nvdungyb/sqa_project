package com.java.be_ticket_booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.be_ticket_booking.model.Role;

public interface RoleRepository extends JpaRepository<Role, Long>{
	Role findByName(String name);
}
