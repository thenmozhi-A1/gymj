package com.example.gym.repository;

import com.example.gym.entity.WebAuthnCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WebAuthnCredentialRepository extends JpaRepository<WebAuthnCredential, Long> {
    Optional<WebAuthnCredential> findByCredentialId(String credentialId);
    List<WebAuthnCredential> findByUserId(Long userId);
    List<WebAuthnCredential> findByUserIdAndActiveTrue(Long userId);
    boolean existsByCredentialId(String credentialId);
}
