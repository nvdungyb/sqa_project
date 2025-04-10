package com.java.be_ticket_booking.model;

import com.java.be_ticket_booking.model.enumModel.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Entity
@Table(name = "Account", uniqueConstraints = {@UniqueConstraint(columnNames = {"username", "email"})
})
public class Account implements UserDetails {

    private static final long serialVersionUID = -7242669578209869683L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", unique = true, nullable = false, length = 26, insertable = false)
    private String id;

    @NotBlank
    @NotNull
    @Column(name = "username", length = 128, nullable = false)
    private String username;

    @NotBlank
    @NotNull
    @Column(name = "password", nullable = false)
    private String password;

    @NotBlank
    @NotNull
    @Column(name = "fullname", nullable = false)
    private String fullname;

    @NotBlank
    @NotNull
    @Column(name = "address")
    private String address;

    @NotBlank
    @NotNull
    @Email
    @Column(name = "email", length = 128, nullable = false)
    private String email;

    @NotBlank
    @NotNull
    @Column(name = "phone")
    private String phone;

    @NotBlank
    @NotNull
    @Column(name = "ip")
    private String ip;

    @CreationTimestamp
    @Column(name = "create_at", nullable = false, updatable = false)
    private Date create_at;

    @UpdateTimestamp
    @Column(name = "update_at", nullable = true, updatable = true)
    private Date update_at;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @ManyToMany(fetch = FetchType.EAGER)
    private Collection<Role> roles = new ArrayList<>();

    public Account() {
    }

    public Account(String id, String fullname,
                   String username, String password,
                   String phone, String address,
                   String email, String ip, UserStatus status) {
        this.id = id;
        this.fullname = fullname;
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.address = address;
        this.email = email;
        this.ip = ip;
        this.status = status;
    }

    public Account(AccountTemp tmp) {
        this.id = null;
        this.fullname = tmp.getFullname();
        this.username = tmp.getUsername();
        this.password = tmp.getPassword();
        this.phone = tmp.getPhone();
        this.address = tmp.getAddress();
        this.email = tmp.getEmail();
        this.ip = tmp.getIp();
        this.status = UserStatus.ACTIVE;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullname() {
        return this.fullname;
    }

    public void setFullname(String name) {
        this.fullname = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStatus() {
        return status.name();
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public void setRoles(Collection<Role> roles) {
        this.roles = roles;
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public Collection<Role> getRoles() {
        return this.roles;
    }

    public Date getCreateAt() {
        return this.create_at;
    }

    public Date getUpdateAt() {
        return this.update_at;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getIp() {
        return this.ip;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map(r -> new SimpleGrantedAuthority(r.getRole())).collect(Collectors.toList());
        // Collection<SimpleGrantedAuthority> auths = new ArrayList<>();
        // roles.stream().map(r -> new SimpleGrantedAuthority(r.getRole()));
        // return List.of(new SimpleGrantedAuthority(auths.toString()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}