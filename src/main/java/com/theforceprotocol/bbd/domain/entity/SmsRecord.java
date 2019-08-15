package com.theforceprotocol.bbd.domain.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity
@NoArgsConstructor
public class SmsRecord extends BaseEntity {
    private static final long serialVersionUID = -8010919142049816940L;
    @Id
    @GeneratedValue
    private Long id;
    @Column(nullable = false, updatable = false)
    private Integer countryCode;
    @Column(nullable = false, updatable = false)
    private String phone;
    private String source = "bbd";
    private String status;
    private String message;
    private String info;
    private Integer remaining;
    private String taskId;
    private Integer successCount;
}
