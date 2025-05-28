package com.nurtel.vaskamailio.dispatcher.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "dispatcher")
public class DispatcherEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer setid;

    @Column(length = 192, nullable = false)
    private String destination;

    @Column(nullable = false)
    private Integer flags;

    @Column(nullable = false)
    private Integer priority;

    @Column(length = 128, nullable = false)
    private String attrs;

    @Column(length = 64, nullable = false)
    private String description;
}
