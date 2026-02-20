# RBAC Implementation Guide - Backend Orientasi

## Overview

This document provides comprehensive guidance on the Role-Based Access Control (RBAC) system implemented in the Backend Orientasi application.

## Architecture

The RBAC system consists of the following components:

### 1. Database Schema

```
mst_role
├── id (UUID, PK)
├── role_name (String, unique)
├── description (String)
├── created_at
└── updated_at

mst_permission
├── id (UUID, PK)
├── permission_name (String, unique)
├── description (String)
├── resource_type (String)
├── action (String)
├── created_at
└── updated_at

mst_user_role (Many-to-Many)
├── id (UUID, PK)
├── user_id (FK to mst_user)
├── role_id (FK to mst_role)
├── assigned_by (UUID)
├── created_at
└── updated_at

mst_role_permission (Many-to-Many)
├── role_id (FK to mst_role, PK)
└── permission_id (FK to mst_permission, PK)
```

### 2. Default Roles

Three roles are pre-configured:

#### SKPA
- **Description**: Can submit PKSI proposals (upload and edit) and view monitoring (read-only)
- **Permissions**:
  - `pksi.create` - Create PKSI proposals
  - `pksi.read` - View PKSI proposals
  - `pksi.update` - Edit PKSI proposals
  - `monitoring.read` - View monitoring data

#### Pengembang
- **Description**: Same as SKPA but can also edit monitoring
- **Permissions**: All SKPA permissions plus:
  - `monitoring.update` - Edit monitoring data

#### Admin
- **Description**: Full access including role and user management
- **Permissions**: All Pengembang permissions plus:
  - `pksi.delete` - Delete PKSI proposals
  - `user.read` - View users
  - `user.update.role` - Assign roles to users
  - `role.create` - Create roles
  - `role.read` - View roles
  - `role.update` - Update roles
  - `role.delete` - Delete roles
  - `permission.create` - Create permissions
  - `permission.read` - View permissions
  - `permission.update` - Update permissions
  - `permission.delete` - Delete permissions

## API Endpoints

### Base URL
```
http://host:8080/api
```

### Authentication
All endpoints require:
- **APIKey** header: `da39b92f-a1b8-46d5-a10c-d08b1cc92218`
- **Authorization** header: `Bearer <JWT_TOKEN>`
- **Content-Type** header: `application/json` (for POST/PUT/PATCH)

### Role Management

#### 1. Create Role (Admin only)
```http
POST /api/roles
Content-Type: application/json
Authorization: Bearer <token>

{
  "roleName": "NewRole",
  "description": "Description of the role",
  "permissionIds": ["uuid-1", "uuid-2"]
}
```

**Response**:
```json
{
  "status": 201,
  "message": "Role created successfully",
  "data": {
    "id": "uuid",
    "roleName": "NewRole",
    "description": "Description",
    "permissions": [...],
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  }
}
```

#### 2. Get All Roles
```http
GET /api/roles
Authorization: Bearer <token>
```

#### 3. Get Role by ID
```http
GET /api/roles/{roleId}
Authorization: Bearer <token>
```

#### 4. Update Role (Admin only)
```http
PUT /api/roles/{roleId}
Content-Type: application/json
Authorization: Bearer <token>

{
  "roleName": "UpdatedRole",
  "description": "Updated description",
  "permissionIds": ["uuid-1", "uuid-2", "uuid-3"]
}
```

#### 5. Delete Role (Admin only)
```http
DELETE /api/roles/{roleId}
Authorization: Bearer <token>
```

#### 6. Add Permissions to Role (Admin only)
```http
POST /api/roles/{roleId}/permissions
Content-Type: application/json
Authorization: Bearer <token>

["permission-uuid-1", "permission-uuid-2"]
```

#### 7. Remove Permissions from Role (Admin only)
```http
DELETE /api/roles/{roleId}/permissions
Content-Type: application/json
Authorization: Bearer <token>

["permission-uuid-1", "permission-uuid-2"]
```

### Permission Management

#### 1. Create Permission (Admin only)
```http
POST /api/roles/permissions
Content-Type: application/json
Authorization: Bearer <token>

{
  "permissionName": "resource.action",
  "description": "Description",
  "resourceType": "RESOURCE",
  "action": "ACTION"
}
```

#### 2. Get All Permissions
```http
GET /api/roles/permissions
Authorization: Bearer <token>
```

#### 3. Get Permission by ID
```http
GET /api/roles/permissions/{permissionId}
Authorization: Bearer <token>
```

#### 4. Update Permission (Admin only)
```http
PUT /api/roles/permissions/{permissionId}
Content-Type: application/json
Authorization: Bearer <token>

{
  "permissionName": "resource.action",
  "description": "Updated description",
  "resourceType": "RESOURCE",
  "action": "ACTION"
}
```

#### 5. Delete Permission (Admin only)
```http
DELETE /api/roles/permissions/{permissionId}
Authorization: Bearer <token>
```

### User-Role Management

#### 1. Assign Roles to User (Admin only)
```http
POST /api/roles/assign
Content-Type: application/json
Authorization: Bearer <token>

{
  "userUuid": "user-uuid",
  "roleIds": ["role-uuid-1", "role-uuid-2"]
}
```

**Response**:
```json
{
  "status": 200,
  "message": "Roles assigned to user successfully",
  "data": {
    "uuid": "user-uuid",
    "username": "username",
    "fullName": "Full Name",
    "email": "email@example.com",
    "roles": [...],
    "hasRole": true
  }
}
```

#### 2. Remove Role from User (Admin only)
```http
DELETE /api/roles/users/{userUuid}/roles/{roleId}
Authorization: Bearer <token>
```

#### 3. Get All Users with Roles (Admin only)
```http
GET /api/roles/users
Authorization: Bearer <token>
```

#### 4. Get User with Roles
```http
GET /api/roles/users/{userUuid}
Authorization: Bearer <token>
```

### Login Response

The login endpoint now returns user role information:

```json
{
  "status": 200,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGc...",
    "token_type": "Bearer",
    "expires_in": 86400,
    "user_info": {
      "uuid": "user-uuid",
      "username": "username",
      "full_name": "Full Name",
      "email": "email@example.com",
      "has_role": true,
      "roles": ["Admin"],
      "permissions": ["pksi.create", "pksi.read", ...]
    }
  }
}
```

**Note**: If `has_role` is `false`, the user has not been assigned a role and should contact an administrator.

## Using Authorization Annotations in Controllers

### @RequiresRole

Restrict endpoints to specific roles:

```java
@RestController
@RequestMapping("/api/admin")
@RequiresRole("Admin")  // All methods in this controller require Admin role
public class AdminController {

    @GetMapping("/dashboard")
    public ResponseEntity<BaseResponse> getDashboard() {
        // Only accessible by users with Admin role
    }
}
```

**With multiple roles** (user needs ONE of them):
```java
@RequiresRole({"Admin", "Pengembang"})
@GetMapping("/monitoring")
public ResponseEntity<BaseResponse> getMonitoring() {
    // Accessible by Admin OR Pengembang
}
```

**Require ALL roles**:
```java
@RequiresRole(value = {"Admin", "Pengembang"}, requireAll = true)
@GetMapping("/special")
public ResponseEntity<BaseResponse> getSpecial() {
    // User must have BOTH Admin AND Pengembang roles
}
```

### @RequiresPermission

Restrict endpoints to specific permissions:

```java
@RestController
@RequestMapping("/api/pksi")
public class PksiController {

    @RequiresPermission("pksi.create")
    @PostMapping
    public ResponseEntity<BaseResponse> createPksi() {
        // Only accessible by users with pksi.create permission
    }

    @RequiresPermission("pksi.read")
    @GetMapping
    public ResponseEntity<BaseResponse> getAllPksi() {
        // Only accessible by users with pksi.read permission
    }

    @RequiresPermission(value = {"pksi.update", "pksi.delete"}, requireAll = true)
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse> deletePksi(@PathVariable UUID id) {
        // User must have BOTH pksi.update AND pksi.delete permissions
    }
}
```

### @PublicAccess

Mark endpoints as publicly accessible (no authentication required):

```java
@RestController
@RequestMapping("/api/public")
public class PublicController {

    @PublicAccess
    @GetMapping("/health")
    public ResponseEntity<BaseResponse> health() {
        // Accessible without authentication
    }
}
```

## Security Best Practices

### 1. Principle of Least Privilege
- Assign only the permissions necessary for a user's role
- Regularly review and audit role assignments

### 2. Role Hierarchy
The system follows this hierarchy:
```
Admin > Pengembang > SKPA
```

### 3. Permission Naming Convention
Use the format: `resource.action`
- Examples: `pksi.create`, `user.read`, `monitoring.update`

### 4. First-Time User Flow
1. User logs in via LDAP for the first time
2. System creates user record but assigns no role
3. Login response includes `has_role: false`
4. Frontend should display message: "You have not been assigned a role. Please contact an administrator."
5. Admin assigns appropriate role via `/api/roles/assign` endpoint
6. User logs in again and receives proper access

### 5. JWT Token Security
- Tokens include `roles` and `permissions` claims
- Tokens expire after 24 hours
- Always validate token on each request (handled by AuthorizationInterceptor)

## Database Initialization

The system automatically creates default roles and permissions on first startup via `DataInitializer.java`.

Manual initialization can be done using the SQL script:
```sql
-- Run this script on your database
src/main/resources/db/migration/V001__create_rbac_tables.sql
```

## Troubleshooting

### Issue: User cannot access endpoints after role assignment
**Solution**: User needs to log in again to get a new JWT token with updated roles/permissions.

### Issue: "You have not been assigned a role" error
**Solution**: Contact an administrator to assign a role via the `/api/roles/assign` endpoint.

### Issue: Role cannot be deleted
**Solution**: Remove all users from the role first, then delete the role.

### Issue: Permission cannot be deleted
**Solution**: Remove the permission from all roles first, then delete the permission.

## Example Workflows

### Workflow 1: Creating a New User with SKPA Role

1. User logs in (first time):
```bash
POST /api/auth/login
{
  "username": "newuser",
  "password": "encrypted_password"
}
```

Response includes `has_role: false`

2. Admin assigns SKPA role:
```bash
POST /api/roles/assign
{
  "userUuid": "newuser-uuid",
  "roleIds": ["skpa-role-uuid"]
}
```

3. User logs in again and can now access SKPA endpoints

### Workflow 2: Creating Custom Role

1. Admin creates custom permission:
```bash
POST /api/roles/permissions
{
  "permissionName": "report.generate",
  "description": "Generate reports",
  "resourceType": "REPORT",
  "action": "GENERATE"
}
```

2. Admin creates custom role with permission:
```bash
POST /api/roles
{
  "roleName": "Reporter",
  "description": "Can generate reports",
  "permissionIds": ["report-permission-uuid"]
}
```

3. Admin assigns role to user:
```bash
POST /api/roles/assign
{
  "userUuid": "user-uuid",
  "roleIds": ["reporter-role-uuid"]
}
```

## API Response Formats

### Success Response
```json
{
  "status": 200,
  "message": "Operation successful",
  "data": { /* payload */ }
}
```

### Error Responses

**401 Unauthorized**:
```json
{
  "status": 401,
  "message": "Authentication required",
  "data": null
}
```

**403 Forbidden**:
```json
{
  "status": 403,
  "message": "Insufficient permissions. Required role(s): [Admin]",
  "data": null
}
```

**404 Not Found**:
```json
{
  "status": 404,
  "message": "Resource not found",
  "data": null
}
```

**400 Bad Request**:
```json
{
  "status": 400,
  "message": "Invalid request data",
  "data": null
}
```

---

## Contact

For questions or issues with the RBAC system, please contact the development team or create an issue in the project repository.
