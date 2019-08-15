package com.theforceprotocol.bbd.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.time.Instant;

@Data
@MappedSuperclass
public class BaseEntity implements Serializable {
    public static final String CREATED_DATE = "createdDate";
    public static final String LAST_MODIFIED_DATE = "lastModifiedDate";
    private static final long serialVersionUID = 5045090219740705631L;
    @JsonIgnore
    @CreationTimestamp
    protected Instant createdDate;
    @JsonIgnore
    @UpdateTimestamp
    protected Instant lastModifiedDate;
}
