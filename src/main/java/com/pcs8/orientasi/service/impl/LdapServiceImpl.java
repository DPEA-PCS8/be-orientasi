package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.domain.dto.response.LoginResponse.UserInfo;
import com.pcs8.orientasi.service.LdapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.util.Hashtable;

@Service
public class LdapServiceImpl implements LdapService {

    private static final Logger log = LoggerFactory.getLogger(LdapServiceImpl.class);

    @Value("${spring.ldap.urls}")
    private String ldapUrl;

    @Value("${spring.ldap.base}")
    private String ldapBase;

    @Override
    public UserInfo authenticate(String username, String password) {
        String cleanUsername = extractUsername(username);
        String userPrincipalName = cleanUsername + "@devojk.go.id";

        log.info("Attempting LDAP authentication for user: {}", cleanUsername);

        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapUrl);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, userPrincipalName);
        env.put(Context.SECURITY_CREDENTIALS, password);

        DirContext ctx = null;
        try {
            ctx = new InitialDirContext(env);
            log.info("LDAP authentication successful for user: {}", cleanUsername);

            UserInfo userInfo = fetchUserAttributes(ctx, cleanUsername);
            return userInfo;

        } catch (NamingException e) {
            log.error("LDAP authentication failed for user: {}. Error: {}", cleanUsername, e.getMessage());
            throw new RuntimeException("Invalid credentials or LDAP connection error", e);
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (NamingException e) {
                    log.warn("Error closing LDAP context: {}", e.getMessage());
                }
            }
        }
    }

    private String extractUsername(String username) {
        // Handle corp/username format
        if (username.contains("/")) {
            return username.substring(username.lastIndexOf("/") + 1);
        }
        // Handle corp\'username format
        if (username.contains("\\")) {
            return username.substring(username.lastIndexOf("\\") + 1);
        }
        return username;
    }

    private UserInfo fetchUserAttributes(DirContext ctx, String username) {
        try {
            SearchControls searchControls = new SearchControls();
            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            searchControls.setReturningAttributes(new String[]{
                    "sAMAccountName",
                    "displayName",
                    "mail",
                    "department",
                    "title",
                    "distinguishedName",
                    "cn",
                    "givenName",
                    "sn",
                    "memberOf",
                    "telephoneNumber",
                    "physicalDeliveryOfficeName"
            });

            String searchFilter = "(sAMAccountName={0})";
            NamingEnumeration<SearchResult> results = ctx.search(ldapBase, searchFilter, new Object[]{username}, searchControls);

            if (results.hasMore()) {
                SearchResult result = results.next();
                Attributes attrs = result.getAttributes();

                logAllAttributes(attrs, username);

                String displayName = getAttributeValue(attrs, "displayName");

                return UserInfo.builder()
                        .username(getAttributeValue(attrs, "sAMAccountName"))
                        .displayName(displayName)
                        .fullName(displayName)
                        .email(getAttributeValue(attrs, "mail"))
                        .department(getAttributeValue(attrs, "department"))
                        .title(getAttributeValue(attrs, "title"))
                        .distinguishedName(getAttributeValue(attrs, "distinguishedName"))
                        .build();
            }

            log.warn("User attributes not found for: {}", username);
            return UserInfo.builder()
                    .username(username)
                    .build();

        } catch (NamingException e) {
            log.error("Error fetching user attributes for: {}. Error: {}", username, e.getMessage());
            return UserInfo.builder()
                    .username(username)
                    .build();
        }
    }

    private void logAllAttributes(Attributes attrs, String username) {
        log.info("=== LDAP User Attributes for: {} ===", username);
        try {
            NamingEnumeration<? extends Attribute> allAttrs = attrs.getAll();
            while (allAttrs.hasMore()) {
                Attribute attr = allAttrs.next();
                StringBuilder values = new StringBuilder();
                NamingEnumeration<?> vals = attr.getAll();
                while (vals.hasMore()) {
                    if (values.length() > 0) values.append(", ");
                    values.append(vals.next().toString());
                }
                log.info("  {} = {}", attr.getID(), values.toString());
            }
        } catch (NamingException e) {
            log.warn("Error logging attributes: {}", e.getMessage());
        }
        log.info("=== End of LDAP Attributes ===");
    }

    private String getAttributeValue(Attributes attrs, String attributeName) {
        try {
            Attribute attr = attrs.get(attributeName);
            if (attr != null) {
                return attr.get().toString();
            }
        } catch (NamingException e) {
            log.warn("Error getting attribute {}: {}", attributeName, e.getMessage());
        }
        return null;
    }
}