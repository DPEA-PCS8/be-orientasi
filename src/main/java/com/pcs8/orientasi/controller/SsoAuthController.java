package com.pcs8.orientasi.controller;

import com.pcs8.orientasi.client.sso.SsoTokenResponse;
import com.pcs8.orientasi.config.JwtConfig;
import com.pcs8.orientasi.config.annotation.PublicAccess;
import com.pcs8.orientasi.domain.dto.request.SsoExchangeRequest;
import com.pcs8.orientasi.domain.dto.response.BaseResponse;
import com.pcs8.orientasi.domain.dto.response.LoginResponse;
import com.pcs8.orientasi.domain.dto.response.LoginResponse.UserInfo;
import com.pcs8.orientasi.domain.entity.MstUser;
import com.pcs8.orientasi.service.UserService;
import com.pcs8.orientasi.service.sso.PkceStateStore;
import com.pcs8.orientasi.service.sso.PkceUtil;
import com.pcs8.orientasi.service.sso.SsoAuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * SSO OIDC (BFF) login endpoints.
 *
 * <ul>
 *   <li>{@code GET /auth/sso/login} — start the flow: generate state + PKCE, redirect to SSO authorize.</li>
 *   <li>{@code POST /auth/sso/exchange} — FE relays {code,state}; BE exchanges code, fetches userinfo,
 *       upserts the user, and issues the existing App JWT (same shape as the legacy login).</li>
 * </ul>
 */
@RestController
@RequestMapping("/auth/sso")
@RequiredArgsConstructor
@PublicAccess
public class SsoAuthController {

    private static final Logger log = LoggerFactory.getLogger(SsoAuthController.class);

    private final SsoAuthService ssoAuthService;
    private final PkceStateStore pkceStateStore;
    private final UserService userService;
    private final JwtConfig jwtConfig;

    /**
     * Start the SSO login: store {state -> verifier} and 302 to the SSO authorize endpoint.
     */
    @PublicAccess
    @GetMapping("/login")
    public void login(HttpServletResponse response) throws IOException {
        String state = PkceUtil.generateState();
        String verifier = PkceUtil.generateCodeVerifier();
        String challenge = PkceUtil.codeChallenge(verifier);

        pkceStateStore.put(state, verifier);

        String authorizeUrl = ssoAuthService.buildAuthorizeUrl(state, challenge);
        log.info("Redirecting to SSO authorize endpoint");
        response.sendRedirect(authorizeUrl);
    }

    /**
     * Exchange the relayed {code,state} for the App JWT.
     */
    @PublicAccess
    @PostMapping("/exchange")
    public ResponseEntity<BaseResponse> exchange(@Valid @RequestBody SsoExchangeRequest request) {
        Optional<String> verifierOpt = pkceStateStore.consume(request.getState());
        if (verifierOpt.isEmpty()) {
            log.warn("SSO exchange rejected: invalid or expired state");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new BaseResponse(400, "Invalid state", null));
        }

        try {
            SsoTokenResponse token = ssoAuthService.exchangeCode(request.getCode(), verifierOpt.get());
            UserInfo ssoUserInfo = ssoAuthService.fetchUserInfo(token.getAccessToken());

            MstUser savedUser = userService.saveOrUpdateFromSso(ssoUserInfo);
            log.info("SSO user saved/updated: {} with UUID: {}", savedUser.getUsername(), savedUser.getUuid());

            boolean hasRole = savedUser.hasRole();
            Set<String> roles = savedUser.getRoleNames();

            UserInfo responseUserInfo = UserInfo.builder()
                    .uuid(savedUser.getUuid().toString())
                    .username(savedUser.getUsername())
                    .fullName(savedUser.getFullName())
                    .displayName(savedUser.getFullName())
                    .email(savedUser.getEmail())
                    .department(savedUser.getDepartment())
                    .title(savedUser.getTitle())
                    .lastLoginAt(savedUser.getLastLoginAt())
                    .hasRole(hasRole)
                    .roles(roles)
                    .build();

            Map<String, Object> claims = new HashMap<>();
            claims.put("uuid", savedUser.getUuid().toString());
            claims.put("full_name", savedUser.getFullName());
            claims.put("email", savedUser.getEmail());
            claims.put("department", savedUser.getDepartment());
            claims.put("title", savedUser.getTitle());
            claims.put("has_role", hasRole);
            claims.put("roles", roles);

            String appJwt = jwtConfig.generateToken(savedUser.getUsername(), claims);

            LoginResponse loginResponse = LoginResponse.builder()
                    .token(appJwt)
                    .tokenType("Bearer")
                    .expiresIn(jwtConfig.getJwtExpiration() / 1000)
                    .userInfo(responseUserInfo)
                    .build();

            log.info("SSO login successful for user: {}", savedUser.getUsername());
            return ResponseEntity.ok(new BaseResponse(200, "Login successful", loginResponse));

        } catch (RuntimeException e) {
            log.error("SSO exchange failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new BaseResponse(401, "SSO login failed", null));
        }
    }
}
