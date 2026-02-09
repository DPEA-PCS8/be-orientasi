package com.pcs8.orientasi.service;

import com.pcs8.orientasi.domain.dto.response.LoginResponse.UserInfo;

public interface LdapService {
    
    UserInfo authenticate(String username, String password);
}