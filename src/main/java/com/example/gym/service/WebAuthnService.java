package com.example.gym.service;

import com.example.gym.entity.User;
import com.example.gym.entity.WebAuthnCredential;
import com.example.gym.repository.UserRepository;
import com.example.gym.repository.WebAuthnCredentialRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.FinishAssertionOptions;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartAssertionOptions;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.AuthenticatorAssertionResponse;
import com.yubico.webauthn.data.AuthenticatorAttestationResponse;
import com.yubico.webauthn.data.AuthenticatorAttachment;
import com.yubico.webauthn.data.AuthenticatorSelectionCriteria;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import com.yubico.webauthn.data.ResidentKeyRequirement;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.data.UserVerificationRequirement;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class WebAuthnService {

    private final WebAuthnCredentialRepository credentialRepository;
    private final UserRepository userRepository;
    private final RelyingParty relyingParty;
    private final ObjectMapper mapper = new ObjectMapper();

    public WebAuthnService(
            WebAuthnCredentialRepository credentialRepository,
            UserRepository userRepository,
            @Value("${webauthn.rp.id}") String rpId,
            @Value("${webauthn.rp.name}") String rpName,
            @Value("${webauthn.rp.origin}") String rpOrigin) {

        this.credentialRepository = credentialRepository;
        this.userRepository = userRepository;

        RelyingPartyIdentity rpIdentity = RelyingPartyIdentity.builder()
                .id(rpId)
                .name(rpName)
                .build();

        this.relyingParty = RelyingParty.builder()
                .identity(rpIdentity)
                .credentialRepository(new com.yubico.webauthn.CredentialRepository() {
                    @Override
                    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
                        return Set.of(); // Not strictly needed for this flow
                    }

                    @Override
                    public Optional<ByteArray> getUserHandleForUsername(String username) {
                        return Optional.empty(); // Not strictly needed
                    }

                    @Override
                    public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
                        return Optional.empty(); // Not strictly needed
                    }

                    @Override
                    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
                        Optional<WebAuthnCredential> credOpt = credentialRepository.findByCredentialId(credentialId.getBase64Url());
                        if (credOpt.isEmpty()) return Optional.empty();
                        WebAuthnCredential cred = credOpt.get();
                        
                        return Optional.of(RegisteredCredential.builder()
                                .credentialId(credentialId)
                                .userHandle(new ByteArray(String.valueOf(cred.getUserId()).getBytes()))
                                .publicKeyCose(new ByteArray(cred.getPublicKeyCose()))
                                .signatureCount(cred.getSignCount())
                                .build());
                    }

                    @Override
                    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
                        return lookup(credentialId, null).map(Set::of).orElse(Set.of());
                    }
                })
                .origins(Set.of(rpOrigin))
                .build();
    }

    public String startRegistration(Long userId, String username, HttpSession session) throws JsonProcessingException {
        UserIdentity userIdentity = UserIdentity.builder()
                .name(username)
                .displayName(username)
                .id(new ByteArray(String.valueOf(userId).getBytes()))
                .build();

        PublicKeyCredentialCreationOptions options = relyingParty.startRegistration(
                StartRegistrationOptions.builder()
                        .user(userIdentity)
                        .authenticatorSelection(AuthenticatorSelectionCriteria.builder()
                                .authenticatorAttachment(AuthenticatorAttachment.PLATFORM)
                                .userVerification(UserVerificationRequirement.REQUIRED)
                                .residentKey(ResidentKeyRequirement.PREFERRED)
                                .build())
                        .build()
        );

        String optionsJson = options.toCredentialsCreateJson();
        session.setAttribute("webauthn_reg_options_" + userId, optionsJson);
        return optionsJson;
    }

    public Map<String, Object> finishRegistration(Long userId, String responseJson, HttpSession session) {
        try {
            String optionsJson = (String) session.getAttribute("webauthn_reg_options_" + userId);
            if (optionsJson == null) {
                throw new RuntimeException("Registration session expired or invalid");
            }

            PublicKeyCredentialCreationOptions options = PublicKeyCredentialCreationOptions.fromJson(optionsJson);
            PublicKeyCredential<AuthenticatorAttestationResponse, com.yubico.webauthn.data.ClientRegistrationExtensionOutputs> pkc =
                    PublicKeyCredential.parseRegistrationResponseJson(responseJson);

            com.yubico.webauthn.RegistrationResult result = relyingParty.finishRegistration(
                    FinishRegistrationOptions.builder()
                            .request(options)
                            .response(pkc)
                            .build()
            );

            WebAuthnCredential credential = new WebAuthnCredential();
            credential.setUserId(userId);
            credential.setCredentialId(result.getKeyId().getId().getBase64Url());
            credential.setPublicKeyCose(result.getPublicKeyCose().getBytes());
            credential.setSignCount(0); // Initial sign count
            
            // Extract aaguid if present
            if (result.getAaguid() != null) {
                credential.setAaguid(result.getAaguid().toString());
            }
            
            credential.setDeviceType("Platform Device"); // Simple fallback

            credentialRepository.save(credential);
            session.removeAttribute("webauthn_reg_options_" + userId);

            User user = userRepository.findById(userId).orElseThrow();
            user.setFingerprintEnrolled(true);
            userRepository.save(user);

            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("credentialId", credential.getCredentialId());
            return res;

        } catch (Exception e) {
            throw new RuntimeException("Registration failed: " + e.getMessage(), e);
        }
    }

    public String startAuthentication(Long userId, HttpSession session) throws JsonProcessingException {
        List<WebAuthnCredential> credentials = credentialRepository.findByUserIdAndActiveTrue(userId);
        if (credentials.isEmpty()) {
            throw new RuntimeException("No fingerprint registered for this user");
        }

        AssertionRequest request = relyingParty.startAssertion(
                StartAssertionOptions.builder()
                        .username(Optional.empty()) // Using explicit allowCredentials instead
                        .userVerification(UserVerificationRequirement.REQUIRED)
                        .build()
        );

        // Map existing credentials to allowCredentials list
        request = request.toBuilder().publicKeyCredentialRequestOptions(
                request.getPublicKeyCredentialRequestOptions().toBuilder()
                        .allowCredentials(credentials.stream()
                                .map(c -> {
                                    try {
                                        return PublicKeyCredentialDescriptor.builder()
                                                .id(ByteArray.fromBase64Url(c.getCredentialId()))
                                                .build();
                                    } catch (com.yubico.webauthn.data.exception.Base64UrlException e) {
                                        throw new RuntimeException(e);
                                    }
                                })
                                .collect(Collectors.toList()))
                        .build()
        ).build();

        String requestJson = request.toCredentialsGetJson();
        session.setAttribute("webauthn_auth_options_" + userId, requestJson);
        return requestJson;
    }

    public Map<String, Object> finishAuthentication(Long userId, String responseJson, HttpSession session) {
        try {
            String requestJson = (String) session.getAttribute("webauthn_auth_options_" + userId);
            if (requestJson == null) {
                throw new RuntimeException("Authentication session expired or invalid");
            }

            AssertionRequest request = AssertionRequest.fromJson(requestJson);
            PublicKeyCredential<AuthenticatorAssertionResponse, com.yubico.webauthn.data.ClientAssertionExtensionOutputs> pkc =
                    PublicKeyCredential.parseAssertionResponseJson(responseJson);

            com.yubico.webauthn.AssertionResult result = relyingParty.finishAssertion(
                    FinishAssertionOptions.builder()
                            .request(request)
                            .response(pkc)
                            .build()
            );

            if (!result.isSuccess()) {
                throw new RuntimeException("Assertion signature verification failed");
            }

            // Verify sign count for replay attacks
            WebAuthnCredential credential = credentialRepository.findByCredentialId(result.getCredential().getCredentialId().getBase64Url())
                    .orElseThrow(() -> new RuntimeException("Credential not found"));

            long newSignCount = result.getSignatureCount();
            if (newSignCount != 0 || credential.getSignCount() != 0) {
                if (newSignCount <= credential.getSignCount()) {
                    throw new SecurityException("Cloned authenticator detected or replay attack! Sign count did not increase.");
                }
            }

            credential.setSignCount(newSignCount);
            credential.setLastUsedAt(LocalDateTime.now());
            credentialRepository.save(credential);

            session.removeAttribute("webauthn_auth_options_" + userId);

            User user = userRepository.findById(userId).orElseThrow();

            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("action", "CHECK_IN"); // Mock logic: in reality, fetch latest attendance to determine IN/OUT
            res.put("userName", user.getFullName());
            res.put("role", user.getRole());
            res.put("timestamp", LocalDateTime.now());

            return res;

        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Authentication failed: " + e.getMessage(), e);
        }
    }
}
