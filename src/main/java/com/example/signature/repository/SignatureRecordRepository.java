package com.example.signature.repository;

import com.example.signature.entity.SignatureRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SignatureRecordRepository extends JpaRepository<SignatureRecord, Long> {

    Optional<SignatureRecord> findByAppIdAndTimestampAndNonce(String appId, String timestamp, String nonce);

    Optional<SignatureRecord> findBySignature(String signature);

}
