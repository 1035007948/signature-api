package com.example.signatureapi.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "signature_record")
public class SignatureRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", unique = true, nullable = false, length = 64)
    private String requestId;

    @Column(name = "signer", nullable = false, length = 128)
    private String signer;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "signature_value", nullable = false, length = 512)
    private String signatureValue;

    @Column(name = "algorithm", length = 32)
    private String algorithm;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "expire_time")
    private LocalDateTime expireTime;

    @Column(name = "verified_count")
    private Integer verifiedCount = 0;

    @Column(name = "status", length = 16)
    private String status = "VALID";

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
