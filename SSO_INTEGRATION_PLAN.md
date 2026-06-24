# SSO Integration — Executable Spec (BE + FE)

Spec ini ditulis agar bisa dieksekusi langsung oleh AI coding agent.
Tujuan: ganti **total** login `LDAP + RSA password` → **SSO OIDC** pola **Backend-for-Frontend (BFF)**.

> Catatan AI agent: kerjakan task berurutan (T1 → Tn). Setiap task punya **Files**, **Action**,
> dan **Done when**. Jangan hapus file legacy sebelum task cleanup (T13) dan sebelum cek referensi.

---

## 0. Konstanta & Kredensial (dev/local)

```
SSO_CLIENT_ID         = P1VHAIF287355UAN2UTG
SSO_CLIENT_SECRET     = REDACTED
SSO_REDIRECT_URI      = https://localhost:5174/signin-oidc   (terdaftar di SSO; ini FE)
SSO_SCOPES            = openid email profile roles
SSO_BASE_URL (dev)    = ${SSO_BASE_URL:http://auth-web.sso-engine-dev.svc.cluster.local}
BE                    = http://localhost:8080, context-path /api
FE (vite)             = https://localhost:5174   (HARUS https + port 5174, lihat T11)
```

> ⚠️ Secret di atas = nilai dev untuk testing lokal. JANGAN commit ke repo public / prod.
> Di `application.yaml` taruh sebagai default `${SSO_CLIENT_SECRET:...}` yang bisa di-override env.

OIDC endpoints (relatif ke `SSO_BASE_URL`):
`/connect/authorize`, `/connect/token`, `/connect/userinfo`, `/connect/logout`.

---

## 0b. EXECUTION & RESUME PROTOCOL (baca duluan)

Branch kerja: **`feat/marvel/refactor-login-sso`** (BE & FE, nama sama di dua repo).

**Aturan eksekusi tiap agent:**
1. Kerjakan task berurutan dalam range yang diberikan.
2. Setelah satu task **Done when** terpenuhi + build/compile ijo → **commit** dgn pesan
   `feat(sso): T<n> <judul singkat>` (mis. `feat(sso): T5 SsoAuthService`).
3. **Centang** kotak task di bagian CHECKLIST doc ini (`[ ]` → `[x]`) di commit yang sama.
4. Kalau **blocked**: tulis baris di section "RESUME STATE" bawah (`BLOCKED T<n>: <alasan>`), commit, stop, lapor.
5. **Jangan** kerjakan task cleanup (T14–T15) tanpa instruksi mandor.

**Cara RESUME kalau sesi terputus (mandor / agent baru):**
1. `git log --oneline` di branch `feat/marvel/refactor-login-sso` → lihat task terakhir yang ke-commit.
2. Baca CHECKLIST di doc ini → task pertama yang masih `[ ]` = titik lanjut.
3. `git status` → cek ada kerjaan setengah jalan yang belum di-commit (task in-progress).
4. Lanjut dari task `[ ]` pertama. Spec tiap task self-contained → tak perlu context percakapan sebelumnya.
5. Cek section "RESUME STATE" di bawah untuk catatan/blocker terakhir.

### RESUME STATE
> Agent update baris ini tiap selesai/blocked. Format bebas, ringkas.
```
T2 done; next T3. DEVIATION: catalog SsoTokenClient reuses sso.client-id/secret (protected), so login OIDC client bound from sso.login-client-id/login-client-secret instead of overwriting.
```

Claims dari `/connect/userinfo`: `sub` (=username), `name`, `email`, `organization`, `jabatan`, `user_type`.
**Tidak ada role app** → role di-resolve dari DB BE.

---

## 1. Arsitektur & Flow

Pola **BFF**: confidential client, `client_secret` HANYA di BE. `code` di-relay FE → BE.
BE tetap menerbitkan **App JWT** sendiri (HS256, format klaim SAMA seperti sekarang),
sehingga `AuthorizationInterceptor`, `@RequiresRole`, `apiClient`, `ProtectedRoute` **tidak berubah**.

```
1. FE LoginPage: window.location.href = "/api/auth/sso/login"
2. BE GET /api/auth/sso/login:
     - gen state (random url-safe), code_verifier, code_challenge = BASE64URL(SHA256(verifier))
     - simpan {state -> code_verifier} di PkceStateStore (TTL 5 menit)
     - 302 → {SSO}/connect/authorize
         ?response_type=code
         &client_id=P1VHAIF287355UAN2UTG
         &redirect_uri=https://localhost:5174/signin-oidc
         &scope=openid email profile roles
         &state={state}
         &code_challenge={challenge}
         &code_challenge_method=S256
3. User login di portal SSO (LDAP/MFA ditangani SSO)
4. SSO 302 → https://localhost:5174/signin-oidc?code={code}&state={state}
5. FE route /signin-oidc (SsoCallback): baca code+state, POST /api/auth/sso/exchange {code, state}
6. BE POST /api/auth/sso/exchange:
     - ambil code_verifier by state (tidak ada → 400)
     - POST {SSO}/connect/token  (grant_type=authorization_code, code, redirect_uri, client_id,
                                   client_secret, code_verifier)
     - GET {SSO}/connect/userinfo  (Authorization: Bearer {access_token})
     - userService.saveOrUpdateFromSso(userInfo) → upsert MstUser + resolve roles dari DB
     - jwtConfig.generateToken(username, claims{uuid,full_name,email,department,title,has_role,roles})
     - return JSON BaseResponse(200, "Login successful", LoginResponse{token,tokenType,expiresIn,userInfo})
7. FE: storeAuthData(token, userInfo) → navigate "/"
8. Request berikutnya: apiClient pasang Bearer {appJwt} + APIKey (TIDAK berubah)
```

`redirect_uri` WAJIB identik di langkah 2 (authorize), langkah 6 (token), dan registrasi SSO.

---

## BACKEND TASKS

### T1 — SSO config properties
**Files:** `src/main/java/com/pcs8/orientasi/config/SsoOAuthProperties.java` (baru), `src/main/resources/application.yaml`
**Action:**
- Buat `@ConfigurationProperties(prefix="sso")` dengan field:
  `baseUrl, clientId, clientSecret, redirectUri, frontendCallbackUrl, scopes (String), tokenBufferSeconds`.
- Di `application.yaml` lengkapi blok `sso:` (sudah ada `base-url`, `client-id`, `client-secret`, `token-buffer-seconds`):
  ```yaml
  sso:
    base-url: ${SSO_BASE_URL:http://auth-web.sso-engine-dev.svc.cluster.local}
    client-id: ${SSO_CLIENT_ID:P1VHAIF287355UAN2UTG}
    client-secret: ${SSO_CLIENT_SECRET:REDACTED}
    redirect-uri: ${SSO_REDIRECT_URI:https://localhost:5174/signin-oidc}
    frontend-callback-url: ${SSO_FE_CALLBACK:https://localhost:5174/signin-oidc}
    scopes: ${SSO_SCOPES:openid email profile roles}
    token-buffer-seconds: ${SSO_TOKEN_BUFFER_SECONDS:50}
  ```
**Done when:** properties bean ter-bind, app start tanpa error.

### T2 — PKCE util
**Files:** `src/main/java/com/pcs8/orientasi/service/sso/PkceUtil.java` (baru)
**Action:** static helper:
- `generateCodeVerifier()` → 32 random bytes → BASE64URL no-pad.
- `codeChallenge(verifier)` → BASE64URL(SHA-256(verifier)) no-pad.
- `generateState()` → random url-safe.
**Done when:** unit-testable, output sesuai RFC 7636 (S256).

### T3 — PKCE/state store
**Files:** `src/main/java/com/pcs8/orientasi/service/sso/PkceStateStore.java` (baru)
**Action:** in-memory `ConcurrentHashMap<String state, Entry{verifier, Instant expiresAt}>` dgn TTL 5 menit + cleanup.
- `put(state, verifier)`, `consume(state) -> Optional<verifier>` (hapus setelah ambil; one-time use).
**Note:** single-instance OK untuk dev. Multi-instance → ganti Redis (lihat Risiko). Tandai TODO.
**Done when:** state bisa disimpan & dikonsumsi sekali; expired ditolak.

### T4 — SSO DTOs
**Files:**
- `src/main/java/com/pcs8/orientasi/client/sso/SsoTokenResponse.java` (sudah ada — pastikan ada `access_token`, `id_token`, `refresh_token`, `expires_in`, `token_type`).
- `src/main/java/com/pcs8/orientasi/client/sso/dto/SsoUserInfoResponse.java` (baru): `sub, name, email, organization, jabatan, user_type` (pakai `@JsonProperty`).
**Done when:** Jackson bisa deserialize response token & userinfo.

### T5 — SsoAuthService
**Files:** `src/main/java/com/pcs8/orientasi/service/sso/SsoAuthService.java` + `impl/SsoAuthServiceImpl.java` (baru)
**Action:** method:
- `String buildAuthorizeUrl(String state, String codeChallenge)` → susun URL `/connect/authorize` (URL-encode params).
- `SsoTokenResponse exchangeCode(String code, String codeVerifier)` → POST `/connect/token`
  form: `grant_type=authorization_code, code, redirect_uri={sso.redirectUri}, client_id, client_secret, code_verifier`.
- `UserInfo fetchUserInfo(String accessToken)` → GET `/connect/userinfo` (Bearer), map ke `LoginResponse.UserInfo`
  (`sub→username`, `name→fullName/displayName`, `email→email`, `jabatan→title`, `organization→department`).
- Gunakan `@Qualifier("ssoRestTemplate") RestTemplate`.
**Done when:** ketiga method jalan terhadap SSO dev (atau mock di test).

### T6 — saveOrUpdateFromSso
**Files:** `service/UserService.java` + `service/impl/UserServiceImpl.java`
**Action:** tambah `MstUser saveOrUpdateFromSso(UserInfo ssoUser)` — analog `saveOrUpdateFromLdap`:
upsert by `username`, set fullName/email/department/title, update `lastLoginAt`, persist. Role TIDAK di-set dari SSO (tetap dari DB existing).
**Done when:** user baru SSO ter-create dgn `has_role=false`; user existing ter-update.

### T7 — SsoAuthController
**Files:** `src/main/java/com/pcs8/orientasi/controller/SsoAuthController.java` (baru), `@RequestMapping("/auth/sso")`
**Action:**
- `@PublicAccess GET /login` → `state=PkceUtil.generateState()`, `verifier=...`, `challenge=...`;
  `pkceStateStore.put(state, verifier)`; `response.sendRedirect(ssoAuthService.buildAuthorizeUrl(state, challenge))`.
- `@PublicAccess POST /exchange` body `{ code, state }` →
  `verifier = pkceStateStore.consume(state)` (kosong → 400 `BaseResponse(400,"Invalid state",null)`);
  `token = ssoAuthService.exchangeCode(code, verifier)`;
  `userInfo = ssoAuthService.fetchUserInfo(token.accessToken)`;
  `saved = userService.saveOrUpdateFromSso(userInfo)`;
  build claims (uuid, full_name, email, department, title, has_role, roles) + `jwtConfig.generateToken(...)`;
  return `BaseResponse(200,"Login successful", LoginResponse{token,"Bearer",expiresIn,userInfo})`.
  (Bentuk respons SAMA dgn `AuthController.login` lama — lihat `controller/AuthController.java`.)
**Done when:** `/api/auth/sso/login` redirect ke SSO; `/api/auth/sso/exchange` balikin App JWT valid.

### T8 — Endpoint exchange harus public di interceptor
**Files:** `config/AuthorizationInterceptor.java` (cek), DTO request `domain/dto/request/SsoExchangeRequest.java` (baru: `code`, `state`).
**Action:** pastikan `/auth/sso/**` lolos auth (`@PublicAccess` di controller sudah cukup — verifikasi tidak ke-block AuthHeaderFilter/APIKey filter).
**Done when:** kedua endpoint bisa diakses tanpa Bearer.

### T9 — CORS / APIKey untuk endpoint SSO
**Files:** `config/CorsConfig.java`, `config/AuthHeaderFilter.java`, `config/SecurityHeadersFilter.java`
**Action:** izinkan origin FE `https://localhost:5174`; pastikan `/auth/sso/exchange` (POST dari FE fetch) tidak ditolak APIKey filter (atau FE tetap kirim `APIKey` — keputusan: **FE tetap kirim APIKey** di `/exchange`, jadi filter tak perlu diubah; verifikasi).
**Done when:** FE bisa POST `/api/auth/sso/exchange` lintas origin tanpa CORS error.

### T10 — Dev TLS trust (kalau SSO https self-signed)
**Files:** config `ssoRestTemplate`
**Action:** jika `SSO_BASE_URL` https dgn cert self-signed (mis. `https://localhost:7283`), buat `ssoRestTemplate` dev yang trust-all **HANYA bila profil dev**. Kalau SSO dev pakai http biasa, skip.
**Done when:** BE bisa call token/userinfo tanpa SSL handshake error di lokal.

---

## FRONTEND TASKS

### T11 — Vite https + port 5174
**Files:** `vite.config.ts`
**Action:** set `server.port = 5174` dan `server.https` (pakai cert lokal, mis. `@vitejs/plugin-basic-ssl` atau `server: { https: true }`). Redirect URI terdaftar `https://localhost:5174/signin-oidc` HARUS match.
**Done when:** `npm run dev` serve di `https://localhost:5174`.

### T12 — Login pakai tombol SSO + callback route
**Files:**
- `src/pages/Login/LoginPage.tsx` → buang form username/password, ganti tombol **"Login dengan SSO"**:
  `onClick = () => { window.location.href = '/api/auth/sso/login'; }` (lewat vite proxy `/api`→8080).
- `src/pages/Login/SsoCallback.tsx` (baru) → onMount:
  baca `code`,`state` dari `window.location.search`; jika `error` param → tampilkan error;
  `POST /api/auth/sso/exchange {code, state}` (sertakan header `APIKey`);
  `storeAuthData(data.token, data.userInfo)`; `navigate('/')`.
- `src/App.tsx` → tambah route `<Route path="/signin-oidc" element={<SsoCallback/>} />` (public, di luar ProtectedRoute).
**Done when:** klik tombol → portal SSO → balik `/signin-oidc` → tersimpan token → masuk `/`.

### T13 — Buang RSA & login lama (FE)
**Files:** `src/api/authApi.ts`, `src/hooks/useLoginForm.ts`, `src/types/auth.types.ts`
**Action:**
- Hapus `RSA_PUBLIC_KEY_BASE64`, `importPublicKey`, `encryptPassword`, `login()`.
- Pertahankan: `storeAuthData, getAuthToken, getUserInfo, clearAuthData, handleLogout, isAuthenticated`, semua role helper.
- Hapus/sederhanakan `useLoginForm` (tak ada form lagi).
**Done when:** tidak ada referensi RSA/`login()` tersisa; build FE sukses.

---

## CLEANUP TASKS (jalankan paling akhir, setelah end-to-end OK)

### T14 — Cek referensi sebelum hapus legacy BE
**Action (WAJIB cek dulu):** `grep -rn "LdapService\|PasswordEncryptionService\|LoginRequest" src/main/java`.
- Jika TIDAK dipakai modul lain → lanjut T15.
- Jika dipakai → laporkan, jangan hapus, minta keputusan.

### T15 — Hapus legacy BE
**Files (hapus jika T14 aman):**
- `controller/AuthController.java`
- `service/LdapService.java` + `service/impl/LdapServiceImpl.java`
- `service/PasswordEncryptionService.java` + `service/impl/PasswordEncryptionServiceImpl.java`
- `domain/dto/request/LoginRequest.java`
- `application.yaml`: blok `rsa.encryption.*` + config LDAP (`spring.ldap.*`) bila tak dipakai.
**Done when:** app compile & start tanpa referensi nyangkut.

> **TIDAK DIHAPUS / TIDAK DIUBAH:** `AuthorizationInterceptor`, `@RequiresRole`, `JwtConfig`,
> `UserContext`, `AuthHeaderFilter`, role management, controller bisnis, `apiClient.ts`,
> `ProtectedRoute.tsx`, `usePermissions.ts`, `SsoTokenClient` (catalog client_credentials — beda concern).

---

## VERIFIKASI END-TO-END

1. Start SSO dev, BE (`:8080`), FE (`https://localhost:5174`).
2. Buka FE → klik "Login dengan SSO" → harus redirect ke portal SSO.
3. Login di SSO → harus balik ke `https://localhost:5174/signin-oidc?code=...&state=...`.
4. FE auto POST `/api/auth/sso/exchange` → dapat App JWT → masuk dashboard `/`.
5. Cek `localStorage/sessionStorage` ada `auth_token` (JWT, decode → ada `roles`).
6. Panggil endpoint ber-`@RequiresRole` → 200 (jika user punya role) / 403 (jika belum di-assign).
7. Manual test exchange (opsional):
   ```bash
   curl -k -X POST http://localhost:8080/api/auth/sso/exchange \
     -H 'Content-Type: application/json' -H 'APIKey: REDACTED' \
     -d '{"code":"<code>","state":"<state>"}'
   ```

---

## RISIKO & CATATAN

- **redirect_uri mismatch** = error OIDC paling umum. Harus identik 3 tempat (authorize, token, registrasi). Saat ini `https://localhost:5174/signin-oidc`.
- **PkceStateStore in-memory** → hanya single-instance BE. Multi-instance/dev-restart hilang. Untuk prod: Redis. (TODO di T3)
- **scope `openid`** → diperlukan OIDC. SSO seed scopes: email/profile/roles. Jika `/connect/authorize` tolak `openid`, hapus dari scope param & retesting.
- **`user_type` ≠ role app.** Role tetap dari DB. User SSO baru → `has_role=false` (flow "belum di-assign role" existing menangani).
- **FE wajib https:5174** agar match redirect URI terdaftar. Cert lokal self-signed → browser warning (terima manual saat dev).
- **Secret di yaml** = dev only. Jangan commit ke prod; isu secret existing di `application.yaml` belum dibenahi (atas keputusan user, ditunda).
- **Confidential client** → `client_secret` tidak boleh bocor ke FE. Exchange WAJIB di BE.

---

## CHECKLIST

- [x] T1 SsoOAuthProperties + yaml
- [x] T2 PkceUtil
- [ ] T3 PkceStateStore
- [ ] T4 SSO DTOs (token + userinfo)
- [ ] T5 SsoAuthService (authorize/token/userinfo)
- [ ] T6 saveOrUpdateFromSso
- [ ] T7 SsoAuthController (/login, /exchange)
- [ ] T8 public access + SsoExchangeRequest
- [ ] T9 CORS / APIKey FE origin
- [ ] T10 dev TLS trust (jika perlu)
- [ ] T11 vite https:5174
- [ ] T12 LoginPage tombol + SsoCallback + route
- [ ] T13 buang RSA/login lama FE
- [ ] T14 cek referensi legacy BE
- [ ] T15 hapus legacy BE
- [ ] Verifikasi end-to-end (8 langkah)
