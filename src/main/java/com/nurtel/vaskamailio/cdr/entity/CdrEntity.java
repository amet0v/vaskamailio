package com.nurtel.vaskamailio.cdr.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "cdr")
public class CdrEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "call_time", nullable = false)
    private LocalDateTime callTime;

    @Column(name = "source")
    private String source;

    @Column(name = "cid")
    private String cid;

    @Column(name = "did")
    private String did;

    @Column(name = "setid")
    private Integer setid;
}
