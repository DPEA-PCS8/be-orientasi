# RBAC System - Quick Start Guide

## What Was Created

A complete RBAC system has been implemented with the following components:

### 1. Database Entities (4 new tables)
- `MstRole` - Stores role information
- `MstPermission` - Stores permission definitions
- `MstUserRole` - Links users to roles (Many-to-Many)
- `MstRolePermission` - Links roles to permissions (Many-to-Many)

### 2. Three Pre-configured Roles
- **SKPA**: Can submit and edit PKSI proposals, view monitoring (read-only)
- **Pengembang**: Same as SKPA + can edit monitoring
- **Admin**: Full access including user and role management

### 3. REST API Endpoints
All endpoints are at `/api/roles`:
- Role CRUD operations
- Permission CRUD operations
- User-role assignment
- List users with their roles

### 4. Security Features
- JWT tokens include role and permission information
- Login response indicates if user has a role (`has_role` flag)
- Custom annotations for endpoint protection: `@RequiresRole`, `@RequiresPermission`
- Automatic permission checking via `AuthorizationInterceptor`

## Getting Started

### Step 1: Run Database Migration
Execute the SQL script to create RBAC tables:
```bash
# Script location:
src/main/resources/db/migration/V001__create_rbac_tables.sql
```

Or let Hibernate auto-create the tables by starting the application.

### Step 2: Start the Application
When the application starts, `DataInitializer` will automatically create:
- 16 default permissions
- 3 default roles (SKPA, Pengembang, Admin)

Check the logs for:
```
INFO  - Initializing default roles and permissions...
INFO  - Created role: SKPA with 4 permissions
INFO  - Created role: Pengembang with 5 permissions
INFO  - Created role: Admin with 16 permissions
```

### Step 3: Assign Admin Role to First User

1. A user logs in for the first time:
```bash
POST http://localhost:8080/api/auth/login
Headers:
  APIKey: da39b92f-a1b8-46d5-a10c-d08b1cc92218
  Content-Type: application/json
Body:
{
  "username": "admin_user",
  "password": "encrypted_rsa_password"
}
```

Response will include:
```json
{
  "user_info": {
    "uuid": "user-uuid-here",
    "has_role": false,
    "roles": [],
    "permissions": []
  }
}
```

2. **Manually assign Admin role via database** (first time only):
```sql
-- Get role ID
SELECT id FROM mst_role WHERE role_name = 'Admin';

-- Get user UUID
SELECT uuid FROM mst_user WHERE username = 'admin_user';

-- Assign role
INSERT INTO mst_user_role (id, user_id, role_id, assigned_by, created_at, updated_at)
VALUES (NEWID(), '<user-uuid>', '<admin-role-id>', NULL, GETDATE(), GETDATE());
```

3. User logs in again and will now have Admin access

### Step 4: Use Admin to Assign Roles to Other Users

Once you have an admin user, use the API to assign roles:

```bash
POST http://localhost:8080/api/roles/assign
Headers:
  APIKey: da39b92f-a1b8-46d5-a10c-d08b1cc92218
  Authorization: Bearer <admin-jwt-token>
  Content-Type: application/json
Body:
{
  "userUuid": "target-user-uuid",
  "roleIds": ["skpa-role-uuid-or-pengembang-role-uuid"]
}
```

## API Examples

### Get All Roles
```bash
GET http://localhost:8080/api/roles
Headers:
  APIKey: da39b92f-a1b8-46d5-a10c-d08b1cc92218
  Authorization: Bearer <token>
```

### Get All Users with Roles (Admin only)
```bash
GET http://localhost:8080/api/roles/users
Headers:
  APIKey: da39b92f-a1b8-46d5-a10c-d08b1cc92218
  Authorization: Bearer <admin-token>
```

### Get Specific User's Roles
```bash
GET http://localhost:8080/api/roles/users/{userUuid}
Headers:
  APIKey: da39b92f-a1b8-46d5-a10c-d08b1cc92218
  Authorization: Bearer <token>
```

## Using Authorization in Your Controllers

### Example 1: Require Admin Role
```java
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @RequiresRole("Admin")
    @DeleteMapping("/sensitive-operation")
    public ResponseEntity<BaseResponse> dangerousOperation() {
        // Only Admin users can access this
    }
}
```

### Example 2: Require Specific Permission
```java
@RestController
@RequestMapping("/api/monitoring")
public class MonitoringController {

    @RequiresPermission("monitoring.update")
    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse> updateMonitoring(@PathVariable UUID id) {
        // Only users with monitoring.update permission can access
    }
}
```

### Example 3: Multiple Roles (OR logic)
```java
@RequiresRole({"Admin", "Pengembang"})
@GetMapping("/reports")
public ResponseEntity<BaseResponse> getReports() {
    // Admin OR Pengembang can access
}
```

## Login Flow Diagram

```
User Login (LDAP)
    ↓
System creates/updates MstUser
    ↓
Check if user has role assigned?
    ↓
YES: Generate JWT with roles/permissions → User can access protected endpoints
NO: Generate JWT without roles → User sees "Contact admin" message
```

## Testing the System

### Test 1: User Without Role
1. Login as new user
2. Try to access `/api/roles/users` (Admin endpoint)
3. Expected: 403 Forbidden - "You have not been assigned a role"

### Test 2: User With SKPA Role
1. Assign SKPA role to user
2. User logs in again
3. User can access:
   - PKSI create/read/update endpoints
   - Monitoring read endpoints
4. User CANNOT access:
   - Admin endpoints
   - Monitoring update endpoints

### Test 3: User With Admin Role
1. Assign Admin role to user
2. User logs in again
3. User can access ALL endpoints

## Troubleshooting

### Problem: "Missing or invalid Authorization header"
- Make sure you include the Bearer token in Authorization header
- Format: `Authorization: Bearer eyJhbGc...`

### Problem: "Insufficient permissions"
- User needs to log in again after role assignment to get new JWT token
- Check that user has the correct role/permission

### Problem: "You have not been assigned a role"
- Contact administrator to assign a role via `/api/roles/assign`

## Default Permissions List

| Permission Name | Description | Resource Type | Action |
|-----------------|-------------|---------------|--------|
| pksi.create | Create PKSI proposals | PKSI | CREATE |
| pksi.read | View PKSI proposals | PKSI | READ |
| pksi.update | Edit PKSI proposals | PKSI | UPDATE |
| pksi.delete | Delete PKSI proposals | PKSI | DELETE |
| monitoring.read | View monitoring data | MONITORING | READ |
| monitoring.update | Edit monitoring data | MONITORING | UPDATE |
| user.read | View users | USER | READ |
| user.update.role | Assign roles to users | USER | UPDATE |
| role.create | Create roles | ROLE | CREATE |
| role.read | View roles | ROLE | READ |
| role.update | Update roles | ROLE | UPDATE |
| role.delete | Delete roles | ROLE | DELETE |
| permission.create | Create permissions | PERMISSION | CREATE |
| permission.read | View permissions | PERMISSION | READ |
| permission.update | Update permissions | PERMISSION | UPDATE |
| permission.delete | Delete permissions | PERMISSION | DELETE |

## Next Steps

1. ✅ Start the application
2. ✅ Verify default roles and permissions are created
3. ✅ Manually assign Admin role to first admin user
4. ✅ Use admin account to assign roles to other users
5. ✅ Apply `@RequiresRole` or `@RequiresPermission` annotations to your existing controllers
6. ✅ Test the authorization system

## Additional Resources

- Full documentation: `RBAC_GUIDE.md`
- Requirements: `/Users/marvelkrent/Developer/projects/orientasi/workspace/docs-requirement/backend/rbac.md`

---

**Important Notes:**
- All users must re-login after role assignment to get updated JWT tokens
- Users without roles will see `has_role: false` in login response
- Admin users can manage all roles and permissions through the API
- The system follows the principle of least privilege - assign minimum necessary permissions
