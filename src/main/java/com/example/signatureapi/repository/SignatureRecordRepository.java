package com.example.signatureapi.repository;

import com.example.signatureapi.entity.SignatureRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SignatureRecordRepository extends JpaRepository<SignatureRecord, Long> {

    Optional<SignatureRecord> findByRequestId(String requestId);

    Optional<SignatureRecord> findBySignatureValue(String signatureValue);

    boolean existsByRequestId(String requestId);

    boolean existsBySignatureValue(String signatureValue);
}
