package com.java.be_ticket_booking.response;

import com.java.be_ticket_booking.model.Account;

public class AccountSummaryResponse {
    String id;
    String username;
    String fullname;
    String email;
    String address;
    String status;
    String createAt;
    String updateAt;
    String[] roles;

    public AccountSummaryResponse(Account user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.fullname = user.getFullname();
        this.email = user.getEmail();
        this.address = user.getAddress();
        this.status = user.getStatus();
        this.roles = user.getRoles().toArray(new String[0]);
        this.createAt = user.getCreateAt().toString();
        this.updateAt = user.getUpdateAt().toString();
    }

    public String getId() {
        return this.id;
    }

    public String getUsername() {
        return this.username;
    }

    public String getFullname() {
        return this.fullname;
    }

    public String getEmail() {
        return this.email;
    }

    public String getStatus() {
        return this.status;
    }

    public String getAddress() {
        return this.address;
    }

    public String getCreateAt() {
        return this.createAt;
    }

    public String getUpdateAt() {
        return this.updateAt;
    }

    public String[] getRoles() {
        return this.roles;
    }
}
