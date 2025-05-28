package com.nurtel.vaskamailio.router.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "router")
public class RouterEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;
    protected String cid;
    protected String did;
    protected Integer setid;
    protected String description;
}
