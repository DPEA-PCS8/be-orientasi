package com.pcs8.orientasi.service;

import com.pcs8.orientasi.domain.entity.MstUser;
import com.pcs8.orientasi.domain.dto.response.LoginResponse.UserInfo;

public interface UserService {

    /**
     * Save atau update user dari SSO userinfo.
     * Upsert by username; set fullName/email/department/title dan update lastLoginAt.
     * Role TIDAK di-set dari SSO (tetap dari DB existing).
     */
    MstUser saveOrUpdateFromSso(UserInfo ssoUserInfo);

    /**
     * Get user by username.
     */
    MstUser getByUsername(String username);
}