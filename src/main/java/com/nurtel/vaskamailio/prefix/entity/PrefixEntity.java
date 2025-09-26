package com.nurtel.vaskamailio.prefix.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "prefixes")
public class PrefixEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "regex")
    private String regex;

    @Column(name = "setid")
    private Integer setid;

    @Column(name = "strip")
    private Boolean strip = false;

    @Column(name = "description")
    private String description;
}
