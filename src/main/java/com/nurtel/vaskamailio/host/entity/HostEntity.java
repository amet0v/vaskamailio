package com.nurtel.vaskamailio.host.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ht_hosts")
public class HostEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "key_name", nullable = false)
    private String ip;

    @Column(name = "key_type", nullable = false)
    private Integer keyType = 0;

    @Column(name = "value_type", nullable = false)
    private Integer valueType = 0;

    @Column(name = "key_value")
    private String isActive;

    @Column(name = "description")
    private String description;
}
