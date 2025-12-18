package com.nurtel.vaskamailio.router.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ht_router")
public class RouterEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "key_name", nullable = false)
    private String did;

    @Column(name = "key_type", nullable = false)
    private Integer keyType = 0;

    @Column(name = "value_type", nullable = false)
    private Integer valueType = 0;

    @Column(name = "key_value")
    private String setid;

    @Column(name = "description")
    private String description;

    @Override
    public String toString() {
        return "RouterEntity{" +
                "id=" + id +
                ", did='" + did + '\'' +
                ", keyType=" + keyType +
                ", valueType=" + valueType +
                ", setid='" + setid + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}