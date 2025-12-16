# User Lock/Unlock Feature Documentation

## Overview
This feature allows administrators to lock and unlock user accounts. When a user account is locked, they cannot log in until an administrator unlocks their account.

## API Endpoints

### Lock User
**POST** `/admin/users/{userId}/lock`

Locks a user account. Only accessible by administrators.

**Request Body (Optional):**
```json
{
  "reason": "Reason for locking the account"
}
```

If no reason is provided, default message "Account locked by administrator" will be used.

**Response:**
```json
{
  "userId": "uuid-string",
  "email": "user@example.com",
  "name": "User Name",
  "phoneNumber": "0123456789",
  "address": "User Address",
  "createdAt": "2025-12-16T10:00:00.000+00:00",
  "updatedAt": "2025-12-16T10:00:00.000+00:00",
  "role": "USER",
  "isLocked": true,
  "lockedAt": "2025-12-16T10:00:00.000+00:00",
  "lockReason": "Reason for locking the account"
}
```

**Error Responses:**
- `400 Bad Request` - Cannot lock admin accounts
- `404 Not Found` - User not found with the given ID

---

### Unlock User
**POST** `/admin/users/{userId}/unlock`

Unlocks a previously locked user account. Only accessible by administrators.

**Response:**
```json
{
  "userId": "uuid-string",
  "email": "user@example.com",
  "name": "User Name",
  "phoneNumber": "0123456789",
  "address": "User Address",
  "createdAt": "2025-12-16T10:00:00.000+00:00",
  "updatedAt": "2025-12-16T10:00:00.000+00:00",
  "role": "USER",
  "isLocked": false,
  "lockedAt": null,
  "lockReason": null
}
```

**Error Responses:**
- `404 Not Found` - User not found with the given ID

---

### Get All Users (Updated)
**GET** `/admin/users`

The response now includes lock status for each user:

```json
[
  {
    "userId": "uuid-string",
    "email": "user@example.com",
    "name": "User Name",
    "role": "USER",
    "isLocked": false,
    "lockedAt": null,
    "lockReason": null,
    ...
  }
]
```

---

## Login Behavior for Locked Users

When a locked user attempts to log in:

**Response:**
- **Status Code:** `403 Forbidden`
- **Message:** "Account is locked: [lock reason]" or "Account is locked" if no reason was specified

---

## Database Changes

New columns added to `users` table:
- `is_locked` (BOOLEAN, NOT NULL, DEFAULT FALSE)
- `locked_at` (TIMESTAMP, NULL)
- `lock_reason` (VARCHAR(500), NULL)

Run the migration script in `Documents/USER_LOCK_MIGRATION.sql` to add these columns.

---

## Business Rules

1. **Admin Protection:** Admin accounts cannot be locked
2. **Default Lock Reason:** If no reason is provided, "Account locked by administrator" is used
3. **Unlock Clears Data:** When a user is unlocked, `lockedAt` and `lockReason` are cleared

---

## Files Modified

1. `entity/UserEntity.java` - Added isLocked, lockedAt, lockReason fields
2. `io/Response/UserResponse.java` - Added isLocked, lockedAt, lockReason fields
3. `io/Request/LockUserRequest.java` - New request class for lock operation
4. `service/UserService.java` - Added lockUser, unlockUser, isUserLocked methods
5. `service/Impl/UserServiceImpl.java` - Implemented lock/unlock methods
6. `service/Impl/AppUserDetailServiceImpl.java` - Added check for locked users during authentication
7. `controller/UserController.java` - Added lock/unlock endpoints
8. `controller/AuthController.java` - Added LockedException handling

---

## Commit Message

```
feat: Add lock/unlock user account functionality

- Add isLocked, lockedAt, lockReason fields to UserEntity
- Create LockUserRequest for lock operation with optional reason
- Add lock/unlock methods to UserService and UserServiceImpl
- Add POST /admin/users/{userId}/lock endpoint (admin only)
- Add POST /admin/users/{userId}/unlock endpoint (admin only)
- Prevent locked users from logging in with 403 Forbidden response
- Prevent locking admin accounts
- Include lock status in UserResponse for user listings
```

