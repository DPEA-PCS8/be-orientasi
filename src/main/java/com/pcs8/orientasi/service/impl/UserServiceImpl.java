package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.domain.entity.MstUser;
import com.pcs8.orientasi.domain.dto.response.LoginResponse.UserInfo;
import com.pcs8.orientasi.repository.MstUserRepository;
import com.pcs8.orientasi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final MstUserRepository mstUserRepository;

    @Override
    @Transactional
    public MstUser saveOrUpdateFromLdap(UserInfo ldapUserInfo) {
        String username = ldapUserInfo.getUsername();
        log.info("Saving/updating user from LDAP: {}", username);

        Optional<MstUser> existingUser = mstUserRepository.findByUsername(username);

        MstUser savedUser;
        if (existingUser.isPresent()) {
            MstUser user = existingUser.get();
            user.setFullName(ldapUserInfo.getDisplayName());
            user.setEmail(ldapUserInfo.getEmail());
            user.setDepartment(ldapUserInfo.getDepartment());
            user.setTitle(ldapUserInfo.getTitle());
            user.setLastLoginAt(LocalDateTime.now());

            savedUser = mstUserRepository.save(user);
            savedUser = mstUserRepository.save(user);
            log.info("Updated existing user: {} with UUID: {}", username, savedUser.getUuid());
        } else {
            MstUser newUser = MstUser.builder()
                    .username(username)
                    .fullName(ldapUserInfo.getDisplayName())
                    .email(ldapUserInfo.getEmail())
                    .department(ldapUserInfo.getDepartment())
                    .title(ldapUserInfo.getTitle())
                    .lastLoginAt(LocalDateTime.now())
                    .build();

            savedUser = mstUserRepository.save(newUser);
            savedUser = mstUserRepository.save(newUser);
            log.info("Created new user: {} with UUID: {}", username, savedUser.getUuid());
        }


        // Re-fetch user with roles eagerly loaded
        MstUser userWithRoles = mstUserRepository.findByUsernameWithRoles(username).orElse(savedUser);
        log.info("User {} has {} role(s) after re-fetch", username, userWithRoles.getUserRoles().size());

        return userWithRoles;
    }

    @Override
    public MstUser getByUsername(String username) {
        return mstUserRepository.findByUsername(username).orElse(null);
    }
}
