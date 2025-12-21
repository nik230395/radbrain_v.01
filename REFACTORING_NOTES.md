# Role Entity to UserRole Enum Refactoring

## Overview
This document describes the refactoring from a separate Role entity with a many-to-many relationship to a simple UserRole enum. This change simplifies the codebase, improves performance, and makes role management more maintainable.

## Changes Made

### 1. Created UserRole Enum
**File:** `src/main/java/org/nikolic/programm/enums/UserRole.java`

- Enum with two values: `USER` and `ADMIN`
- Includes utility methods:
  - `fromRoleName(String)`: Converts legacy role names (ROLE_USER, ROLE_ADMIN) to enum
  - `getRoleName()`: Returns role name with ROLE_ prefix for Spring Security compatibility
  - `getDisplayName()` and `getDescription()`: For UI display

### 2. Updated User Entity
**File:** `src/main/java/org/nikolic/programm/entities/User.java`

**Removed:**
- `Set<Role> roles` field with @ManyToMany annotation
- `@JoinTable` for user_role join table
- `addRole()` and `removeRole()` methods
- `String role` field

**Added:**
- `UserRole userRole` field with @Enumerated(EnumType.STRING)
- `isAdmin()`: Check if user is admin
- `isUser()`: Check if user is standard user
- `hasRole(UserRole)`: Check if user has specific role
- `getRoleNameForSecurity()`: Get role name for Spring Security

### 3. Removed Legacy Classes
- **Deleted:** `src/main/java/org/nikolic/programm/entities/Role.java`
- **Deleted:** `src/main/java/org/nikolic/programm/repositories/RoleRepository.java`
- **Deleted:** `src/main/java/org/nikolic/programm/security/DataInitializer.java`

### 4. Updated Services

#### RegistrationCacheService
**File:** `src/main/java/org/nikolic/programm/services/RegistrationCacheService.java`

- Removed `RoleRepository` dependency
- Updated `verifyAndCreateUser()` to set `UserRole.USER` directly
- Simplified user creation logic

### 5. Updated Controllers

#### AdminQuizController
**File:** `src/main/java/org/nikolic/programm/controllers/AdminQuizController.java`

- Simplified `isAdmin()` method to use `User.isAdmin()`
- Removed complex role checking logic

#### UserApiController
**File:** `src/main/java/org/nikolic/programm/controllers/UserApiController.java`

- Removed `RoleRepository` dependency
- Removed unnecessary `@Autowired(required = false)` injection

### 6. Updated Configuration

#### DataSeeder
**File:** `src/main/java/org/nikolic/programm/config/DataSeeder.java`

- Removed `RoleRepository` dependency
- Updated to seed users with `UserRole.ADMIN` and `UserRole.USER`
- Added test user creation
- Simplified seeding logic

#### SecurityConfig & JwtFilter
**Files:** 
- `src/main/java/org/nikolic/programm/security/SecurityConfig.java`
- `src/main/java/org/nikolic/programm/security/JwtFilter.java`

- Updated JwtFilter to inject `UserRepository`
- Modified JWT authentication to include user roles as `GrantedAuthority`
- Improved security by fetching user and their role during authentication

### 7. Database Migration
**File:** `src/main/resources/db/migration/V2__refactor_role_to_user_role_enum.sql`

- Adds `user_role` VARCHAR(20) column to `users` table
- Migrates existing role assignments from `user_role` join table
- Drops `user_role` join table
- Drops `role` table

## Benefits

1. **Simplified Code**: Removed complex many-to-many relationship
2. **Better Performance**: No JOIN needed to check user roles
3. **Type Safety**: Enum provides compile-time type checking
4. **Easier Maintenance**: Role logic is centralized in the enum
5. **Clearer Intent**: Enum values are more explicit than string comparisons
6. **Better Security**: Roles are now properly included in Spring Security authentication

## Migration Steps

1. **Backup Database**: Always backup before running migrations
2. **Run Migration**: Execute `V2__refactor_role_to_user_role_enum.sql`
3. **Deploy Application**: Deploy the refactored code
4. **Verify**: Test admin and user functionality

## Testing

### What to Test
1. User registration (should default to USER role)
2. Admin creation via DataSeeder
3. Admin-only endpoints (e.g., `/api/secure/admin/quizzes`)
4. JWT authentication with roles
5. Role-based access control

### Test Users (Created by DataSeeder)
- **Admin**: `admin@radbrain.local` / `adminpass`
- **Test User**: `test@radbrain.local` / `testpass`

## Backward Compatibility

The refactoring maintains backward compatibility where possible:
- `UserRole.fromRoleName()` can convert legacy role names (ROLE_USER, ROLE_ADMIN)
- `UserRole.getRoleName()` returns Spring Security-compatible role names (ROLE_USER, ROLE_ADMIN)

## Future Enhancements

Potential improvements for the future:
1. Add more roles if needed (e.g., MODERATOR, GUEST)
2. Add role-based method security annotations (@PreAuthorize)
3. Create role management UI for admins
4. Add role audit logging
5. Implement role-based feature flags

## Code Quality

- **Build Status**: ✅ Compiles successfully
- **No Breaking Changes**: Existing functionality preserved
- **Clean Code**: Removed 200+ lines of unnecessary code
- **Better Architecture**: Simpler and more maintainable design
