package com.theforceprotocol.bbd.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@Entity
@NoArgsConstructor
public class User implements Serializable {
    private static final long serialVersionUID = -3384799989400395455L;
    @Id
    @GeneratedValue
    @JsonIgnore
    private Long id;
    @Column(nullable = false, updatable = false)
    private Integer countryCode;
    @Column(length = 11, unique = true, nullable = false, updatable = false)
    private String phone;
    @JsonIgnore
    private boolean locked = false;
    @JsonIgnore
    @OneToMany(mappedBy = "initiator", cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<Order> initOrders = new ArrayList<>();
    @JsonIgnore
    @OneToMany(mappedBy = "recipient", cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<Order> rectOrders = new ArrayList<>();
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<OrderLog> orderLogs = new ArrayList<>();

    public User(Integer countryCode, String phone) {
        this.countryCode = countryCode;
        this.phone = phone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        User user = (User) o;
        return countryCode.equals(user.countryCode) &&
                phone.equals(user.phone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(countryCode, phone);
    }

    @Override
    public String toString() {
        return "User{" +
                "countryCode=" + countryCode +
                ", phone='" + phone + '\'' +
                '}';
    }
}
