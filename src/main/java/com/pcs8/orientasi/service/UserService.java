package com.pcs8.orientasi.service;

import com.pcs8.orientasi.domain.entity.MstUser;
import com.pcs8.orientasi.domain.dto.response.LoginResponse.UserInfo;

public interface UserService {

    /**
     * Save atau update user dari LDAP info.
     * Jika user sudah ada (by username), update info-nya.
     * Jika belum ada, create baru.
     */
    MstUser saveOrUpdateFromLdap(UserInfo ldapUserInfo);

    /**
     * Get user by username.
     */
    MstUser getByUsername(String username);
}