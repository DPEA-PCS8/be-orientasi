package com.pcs8.orientasi.controller;

import com.pcs8.orientasi.config.JwtConfig;
import com.pcs8.orientasi.domain.dto.request.LoginRequest;
import com.pcs8.orientasi.domain.dto.response.BaseResponse;
import com.pcs8.orientasi.domain.dto.response.LoginResponse;
import com.pcs8.orientasi.domain.dto.response.LoginResponse.UserInfo;
import com.pcs8.orientasi.domain.entity.MstUser;
import com.pcs8.orientasi.service.LdapService;
import com.pcs8.orientasi.service.PasswordEncryptionService;
import com.pcs8.orientasi.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final LdapService ldapService;
    private final UserService userService;
    private final PasswordEncryptionService passwordEncryptionService;
    private final JwtConfig jwtConfig;

    /**
     * Login endpoint dengan RSA password decryption.
     */
    @PostMapping("/login")
    public ResponseEntity<BaseResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());

        try {
            // Step 1: Decrypt password (RSA decrypt)
            String decryptedPassword = passwordEncryptionService.decrypt(request.getPassword());
            log.debug("Password decrypted successfully for user: {}", request.getUsername());

            // Step 2: Authenticate via LDAP
            UserInfo ldapUserInfo = ldapService.authenticate(request.getUsername(), decryptedPassword);
            log.info("LDAP authentication successful for user: {}", ldapUserInfo.getUsername());

            // Step 3: Save or update user in database
            MstUser savedUser = userService.saveOrUpdateFromLdap(ldapUserInfo);
            log.info("User saved/updated in database: {} with UUID: {}", savedUser.getUsername(), savedUser.getUuid());

            // Step 3.5: Get user's roles
            boolean hasRole = savedUser.hasRole();
            Set<String> roles = savedUser.getRoleNames();
            log.info("User {} has {} role(s)", savedUser.getUsername(), roles.size());

            // Step 4: Build UserInfo response from saved user (combine LDAP + DB data)
            UserInfo responseUserInfo = UserInfo.builder()
                    .uuid(savedUser.getUuid().toString())
                    .username(savedUser.getUsername())
                    .fullName(savedUser.getFullName())
                    .displayName(savedUser.getFullName())
                    .email(savedUser.getEmail())
                    .department(savedUser.getDepartment())
                    .title(savedUser.getTitle())
                    .distinguishedName(ldapUserInfo.getDistinguishedName())
                    .lastLoginAt(savedUser.getLastLoginAt())
                    .hasRole(hasRole)
                    .roles(roles)
                    .build();

            // Step 5: Generate JWT token with user claims (including roles)
            Map<String, Object> claims = new HashMap<>();
            claims.put("uuid", savedUser.getUuid().toString());
            claims.put("full_name", savedUser.getFullName());
            claims.put("email", savedUser.getEmail());
            claims.put("department", savedUser.getDepartment());
            claims.put("title", savedUser.getTitle());
            claims.put("has_role", hasRole);
            claims.put("roles", roles);

            String token = jwtConfig.generateToken(savedUser.getUsername(), claims);

            // Step 6: Build response
            LoginResponse loginResponse = LoginResponse.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .expiresIn(jwtConfig.getJwtExpiration() / 1000)
                    .userInfo(responseUserInfo)
                    .build();

            log.info("Login successful for user: {}", savedUser.getUsername());

            return ResponseEntity.ok(new BaseResponse(200, "Login successful", loginResponse));

        } catch (Exception e) {
            log.error("Login failed for user: {}. Error: {}", request.getUsername(), e.getMessage());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new BaseResponse(401, "Invalid credentials", null));
        }
    }
}