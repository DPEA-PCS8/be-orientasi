package com.pcs8.orientasi.service.sso;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory store mapping {@code state -> code_verifier} for the PKCE flow, with a 5-minute TTL.
 *
 * <p>Entries are one-time use: {@link #consume(String)} removes the entry on read.
 *
 * <p>TODO(redis): This store is single-instance only. For multi-instance BE (or to survive
 * restarts) replace with a shared store such as Redis (key=state, value=verifier, TTL=5m).
 * See "RISIKO & CATATAN" in SSO_INTEGRATION_PLAN.md.
 */
@Component
public class PkceStateStore {

    private static final Logger log = LoggerFactory.getLogger(PkceStateStore.class);

    private static final long TTL_SECONDS = 5 * 60L;

    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    /**
     * Store a verifier keyed by state. Opportunistically evicts expired entries.
     */
    public void put(String state, String verifier) {
        cleanupExpired();
        store.put(state, new Entry(verifier, Instant.now().plusSeconds(TTL_SECONDS)));
        log.debug("PKCE state stored (active entries: {})", store.size());
    }

    /**
     * Atomically remove and return the verifier for the given state if present and not expired.
     * Returns empty if the state is unknown or has expired (one-time use).
     */
    public Optional<String> consume(String state) {
        if (state == null) {
            return Optional.empty();
        }
        Entry entry = store.remove(state);
        if (entry == null) {
            return Optional.empty();
        }
        if (Instant.now().isAfter(entry.expiresAt)) {
            log.debug("PKCE state expired on consume");
            return Optional.empty();
        }
        return Optional.of(entry.verifier);
    }

    private void cleanupExpired() {
        Instant now = Instant.now();
        store.entrySet().removeIf(e -> now.isAfter(e.getValue().expiresAt));
    }

    private static final class Entry {
        private final String verifier;
        private final Instant expiresAt;

        private Entry(String verifier, Instant expiresAt) {
            this.verifier = verifier;
            this.expiresAt = expiresAt;
        }
    }
}
