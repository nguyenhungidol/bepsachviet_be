# Password Reset and User Profile Management API Documentation

## Overview
This document describes the implementation of password reset (forgot password), change password, and user profile management features for the BepSachViet application.

## Features Implemented

### 1. Forgot Password
Allows users to request a password reset link via email.

### 2. Reset Password
Allows users to reset their password using the token sent to their email.

### 3. Change Password
Allows authenticated users to change their password by providing their current password.

### 4. Update User Profile
Allows authenticated users to update their profile information (name, phone number, address).

### 5. Get User Profile
Allows authenticated users to retrieve their profile information.

---

## API Endpoints

### 1. Forgot Password
**Endpoint:** `POST /api/v1.0/forgot-password`  
**Authentication:** Not required  
**Description:** Sends a password reset email to the user.

**Request Body:**
```json
{
  "email": "user@example.com"
}
```

**Response (Success - 200 OK):**
```json
{
  "message": "Password reset link has been sent to your email"
}
```

**Response (Error - 400 Bad Request):**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "User not found with email: user@example.com"
}
```

**Validation:**
- Email is required and must be in valid email format

---

### 2. Reset Password
**Endpoint:** `POST /api/v1.0/reset-password`  
**Authentication:** Not required  
**Description:** Resets the user's password using the token from the email.

**Request Body:**
```json
{
  "resetToken": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "newPassword": "newSecurePassword123"
}
```

**Response (Success - 200 OK):**
```json
{
  "message": "Password has been reset successfully"
}
```

**Response (Error - 400 Bad Request):**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid or expired reset token"
}
```

**Validation:**
- Reset token is required
- New password is required and must be at least 6 characters

**Token Expiry:** Reset tokens expire after 1 hour

---

### 3. Change Password
**Endpoint:** `POST /api/v1.0/change-password`  
**Authentication:** Required (JWT Token)  
**Description:** Changes the password for the authenticated user.

**Request Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Request Body:**
```json
{
  "currentPassword": "oldPassword123",
  "newPassword": "newSecurePassword456"
}
```

**Response (Success - 200 OK):**
```json
{
  "message": "Password has been changed successfully"
}
```

**Response (Error - 400 Bad Request):**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Current password is incorrect"
}
```

**Validation:**
- Current password is required
- New password is required and must be at least 6 characters

---

### 4. Get User Profile
**Endpoint:** `GET /api/v1.0/user/profile`  
**Authentication:** Required (JWT Token)  
**Description:** Retrieves the profile information of the authenticated user.

**Request Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Response (Success - 200 OK):**
```json
{
  "userId": "abc123-def456-ghi789",
  "email": "user@example.com",
  "name": "John Doe",
  "phoneNumber": "+84123456789",
  "address": "123 Main St, Hanoi, Vietnam",
  "role": "USER",
  "createdAt": "2025-01-15T10:30:00",
  "updatedAt": "2025-01-20T15:45:00"
}
```

**Response (Error - 404 Not Found):**
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "User not found"
}
```

---

### 5. Update User Profile
**Endpoint:** `PUT /api/v1.0/user/profile`  
**Authentication:** Required (JWT Token)  
**Description:** Updates the profile information of the authenticated user.

**Request Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Request Body:**
```json
{
  "name": "John Smith",
  "phoneNumber": "+84987654321",
  "address": "456 New Street, HCMC, Vietnam"
}
```

**Note:** All fields are optional. Only provided fields will be updated.

**Response (Success - 200 OK):**
```json
{
  "userId": "abc123-def456-ghi789",
  "email": "user@example.com",
  "name": "John Smith",
  "phoneNumber": "+84987654321",
  "address": "456 New Street, HCMC, Vietnam",
  "role": "USER",
  "createdAt": "2025-01-15T10:30:00",
  "updatedAt": "2025-01-20T16:00:00"
}
```

**Response (Error - 400 Bad Request):**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Unable to update profile"
}
```

---

## Database Schema Changes

### UserEntity Table Updates
The following fields were added to the `users` table:

| Column Name | Type | Description |
|------------|------|-------------|
| phone_number | VARCHAR(255) | User's phone number |
| address | VARCHAR(255) | User's address |
| reset_token | VARCHAR(255) | Token for password reset |
| reset_token_expiry | TIMESTAMP | Expiry time for reset token |

---

## Email Configuration

### application.properties
Ensure the following email configuration is set in `src/main/resources/application.properties`:

```properties
# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Frontend URL (for password reset link)
app.frontend.url=http://localhost:5173
```

**Important:** For Gmail, you need to use an **App Password** instead of your regular password:
1. Enable 2-Factor Authentication on your Google Account
2. Generate an App Password: https://myaccount.google.com/apppasswords
3. Use the generated App Password in the configuration

---

## Implementation Details

### New Files Created

#### Request DTOs:
- `ForgotPasswordRequest.java` - For forgot password requests
- `ResetPasswordRequest.java` - For password reset requests
- `ChangePasswordRequest.java` - For change password requests
- `UpdateUserInfoRequest.java` - For updating user profile

#### Response DTOs:
- `MessageResponse.java` - Generic message response

#### Services:
- `EmailService.java` - Interface for email operations
- `EmailServiceImpl.java` - Implementation for sending emails

### Modified Files:
- `UserEntity.java` - Added new fields for password reset and user profile
- `UserRepository.java` - Added findByResetToken method
- `UserService.java` - Added new method signatures
- `UserServiceImpl.java` - Implemented new methods
- `UserController.java` - Added new endpoints
- `SecurityConfig.java` - Updated to allow access to password reset endpoints
- `UserResponse.java` - Added phoneNumber and address fields
- `pom.xml` - Added spring-boot-starter-mail dependency

---

## Security Considerations

1. **Reset Token Generation:** Uses UUID.randomUUID() for secure token generation
2. **Token Expiry:** Reset tokens expire after 1 hour
3. **Password Hashing:** All passwords are hashed using BCrypt
4. **Authentication:** User profile endpoints require JWT authentication
5. **Authorization:** Users can only access and modify their own profile

---

## Testing the APIs

### 1. Test Forgot Password
```bash
curl -X POST http://localhost:8080/api/v1.0/forgot-password \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com"
  }'
```

### 2. Test Reset Password
```bash
curl -X POST http://localhost:8080/api/v1.0/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "resetToken": "your-reset-token-here",
    "newPassword": "newPassword123"
  }'
```

### 3. Test Change Password (Requires Authentication)
```bash
curl -X POST http://localhost:8080/api/v1.0/change-password \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "currentPassword": "oldPassword123",
    "newPassword": "newPassword456"
  }'
```

### 4. Test Get User Profile (Requires Authentication)
```bash
curl -X GET http://localhost:8080/api/v1.0/user/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 5. Test Update User Profile (Requires Authentication)
```bash
curl -X PUT http://localhost:8080/api/v1.0/user/profile \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "John Smith",
    "phoneNumber": "+84987654321",
    "address": "456 New Street, HCMC, Vietnam"
  }'
```

---

## Error Handling

All endpoints return appropriate HTTP status codes:
- **200 OK** - Request successful
- **201 CREATED** - Resource created successfully
- **400 BAD REQUEST** - Invalid request data or business logic error
- **401 UNAUTHORIZED** - Missing or invalid JWT token
- **404 NOT FOUND** - Resource not found

Error responses follow this format:
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Detailed error message here"
}
```

---

## Frontend Integration Notes

### Password Reset Flow:
1. User clicks "Forgot Password" and enters their email
2. Frontend calls `/forgot-password` endpoint
3. User receives email with reset link containing the token
4. User clicks the link, which should navigate to a reset password page with the token in the URL
5. Frontend extracts the token from URL and displays a form for new password
6. Frontend calls `/reset-password` endpoint with token and new password

### Profile Management Flow:
1. User must be logged in (have JWT token)
2. Frontend includes JWT token in Authorization header for all requests
3. User can view profile using `/user/profile` GET endpoint
4. User can update profile using `/user/profile` PUT endpoint
5. User can change password using `/change-password` POST endpoint

---

## Notes

1. **Email Service:** The application uses Spring Boot's JavaMailSender. Ensure email configuration is correct before testing.
2. **Token Security:** Reset tokens are single-use and expire after 1 hour.
3. **Password Validation:** Currently requires minimum 6 characters. Can be enhanced with additional validation rules.
4. **Profile Fields:** Name, phone number, and address are optional and can be updated independently.
5. **Existing Users:** Existing users in the database will have null values for new fields (phoneNumber, address) until they update their profile.

---

## Future Enhancements

1. Add email verification for new registrations
2. Add stronger password validation (uppercase, lowercase, numbers, special characters)
3. Add rate limiting for password reset requests
4. Add email templates with HTML formatting
5. Add user profile picture upload
6. Add additional profile fields (date of birth, gender, etc.)
7. Add password history to prevent reusing recent passwords
8. Add account lockout after multiple failed password attempts

