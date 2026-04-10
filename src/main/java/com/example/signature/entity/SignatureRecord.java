package com.example.signature.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "signature_record")
public class SignatureRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "signature_id", length = 64, unique = true, nullable = false)
    private String signatureId;

    @Column(name = "original_data", columnDefinition = "TEXT", nullable = false)
    private String originalData;

    @Column(name = "signature_result", length = 512, nullable = false)
    private String signatureResult;

    @Column(name = "algorithm", length = 32, nullable = false)
    private String algorithm;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "expire_time")
    private LocalDateTime expireTime;

    @Column(name = "status", length = 16)
    private String status;

    @Column(name = "verification_count")
    private Integer verificationCount;

    @Column(name = "last_verification_time")
    private LocalDateTime lastVerificationTime;

    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
        if (this.status == null) {
            this.status = "ACTIVE";
        }
        if (this.verificationCount == null) {
            this.verificationCount = 0;
        }
    }
}
