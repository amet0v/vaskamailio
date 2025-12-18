package com.nurtel.vaskamailio.db.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "databases")
public class DbEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String ip;
    String name;
    String login;
    String password;
    String mscSocket;
    String asteriskSocket;

    @Override
    public String toString() {
        return "DbEntity{" +
                "id=" + id +
                ", ip='" + ip + '\'' +
                ", name='" + name + '\'' +
                ", login='" + login + '\'' +
                ", password='" + password + '\'' +
                ", mscSocket='" + mscSocket + '\'' +
                ", asteriskSocket='" + asteriskSocket + '\'' +
                '}';
    }
}
