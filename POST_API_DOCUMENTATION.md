# News/Post API Documentation

## Overview
This API provides complete CRUD operations for managing news/blog posts with support for pagination, filtering, featured posts, and related posts functionality.

## Database Setup

### SQL Schema
```sql
CREATE TABLE posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id VARCHAR(255) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    short_description VARCHAR(500),
    content TEXT,
    thumbnail_url VARCHAR(500),
    author VARCHAR(255),
    category_id BIGINT NOT NULL,
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE INDEX idx_posts_slug ON posts(slug);
CREATE INDEX idx_posts_status ON posts(status);
CREATE INDEX idx_posts_category ON posts(category_id);
CREATE INDEX idx_posts_featured ON posts(is_featured);
CREATE INDEX idx_posts_created_at ON posts(created_at);
```

## API Endpoints

### 1. GET /posts - List Posts with Pagination
**Description**: Get paginated list of published posts with optional category filter

**Parameters**:
- `page` (optional, default: 0): Page number
- `size` (optional, default: 10): Page size
- `categoryId` (optional): Filter by category
- `sortBy` (optional, default: "createdAt"): Sort field
- `sortDirection` (optional, default: "DESC"): Sort direction (ASC/DESC)

**Example Request**:
```http
GET /posts?page=0&size=10&categoryId=cat-123&sortBy=createdAt&sortDirection=DESC
```

**Example Response**:
```json
{
  "content": [
    {
      "postId": "550e8400-e29b-41d4-a716-446655440000",
      "title": "Tại Sao Nên Mua Gà Ủ Muối",
      "slug": "tai-sao-nen-mua-ga-u-muoi",
      "shortDescription": "Gà ủ muối là món ăn truyền thống...",
      "thumbnailUrl": "https://s3.amazonaws.com/bucket/image.jpg",
      "author": "Hoàng Trung Hưng",
      "categoryName": "Sản phẩm từ gà",
      "isFeatured": true,
      "status": "PUBLISHED",
      "createdAt": "2025-11-24T10:00:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalPages": 5,
  "totalElements": 50,
  "last": false,
  "first": true
}
```

---

### 2. GET /posts/{slug} - Get Post by Slug
**Description**: Get detailed information of a post using its SEO-friendly slug

**Path Parameters**:
- `slug`: URL-friendly post identifier (e.g., "tai-sao-nen-mua-ga-u-muoi")

**Example Request**:
```http
GET /posts/tai-sao-nen-mua-ga-u-muoi
```

**Example Response**:
```json
{
  "postId": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Tại Sao Nên Mua Gà Ủ Muối",
  "slug": "tai-sao-nen-mua-ga-u-muoi",
  "shortDescription": "Gà ủ muối là món ăn truyền thống...",
  "content": "<h1>Gà Ủ Muối</h1><p>Nội dung chi tiết...</p>",
  "thumbnailUrl": "https://s3.amazonaws.com/bucket/image.jpg",
  "author": "Hoàng Trung Hưng",
  "categoryId": "cat-123",
  "categoryName": "Sản phẩm từ gà",
  "isFeatured": true,
  "status": "PUBLISHED",
  "createdAt": "2025-11-24T10:00:00",
  "updatedAt": "2025-11-24T10:30:00"
}
```

---

### 3. GET /posts/featured - Get Featured Posts
**Description**: Get paginated list of featured posts (isFeatured = true)

**Parameters**:
- `page` (optional, default: 0): Page number
- `size` (optional, default: 10): Page size

**Example Request**:
```http
GET /posts/featured?page=0&size=5
```

**Example Response**: Same as list posts response

---

### 4. GET /posts/related - Get Related Posts
**Description**: Get posts from the same category (excluding current post)

**Parameters**:
- `slug` (required): Current post slug
- `limit` (optional, default: 5): Maximum number of related posts

**Example Request**:
```http
GET /posts/related?slug=tai-sao-nen-mua-ga-u-muoi&limit=5
```

**Example Response**:
```json
[
  {
    "postId": "550e8400-e29b-41d4-a716-446655440001",
    "title": "Cách Chế Biến Gà Ngon",
    "slug": "cach-che-bien-ga-ngon",
    "shortDescription": "Hướng dẫn chi tiết...",
    "thumbnailUrl": "https://s3.amazonaws.com/bucket/image2.jpg",
    "author": "Nguyễn Văn A",
    "categoryName": "Sản phẩm từ gà",
    "isFeatured": false,
    "status": "PUBLISHED",
    "createdAt": "2025-11-23T10:00:00"
  }
]
```

---

### 5. POST /admin/posts - Create Post (Admin Only)
**Description**: Create a new post

**Headers**:
```
Authorization: Bearer {jwt_token}
Content-Type: application/json
```

**Request Body**:
```json
{
  "title": "Tại Sao Nên Mua Gà Ủ Muối",
  "slug": "tai-sao-nen-mua-ga-u-muoi",
  "shortDescription": "Gà ủ muối là món ăn truyền thống...",
  "content": "<h1>Gà Ủ Muối</h1><p>Nội dung chi tiết...</p>",
  "thumbnailUrl": "https://s3.amazonaws.com/bucket/image.jpg",
  "author": "Hoàng Trung Hưng",
  "categoryId": "cat-123",
  "isFeatured": true,
  "status": "PUBLISHED"
}
```

**Response**: 201 Created with full post object

**Notes**:
- `title` is required
- `categoryId` is required
- `status` is required (DRAFT or PUBLISHED)
- If `slug` is not provided, it will be auto-generated from `title`
- If `slug` already exists, a unique suffix will be added
- `isFeatured` defaults to false if not provided

---

### 6. PUT /admin/posts/{postId} - Update Post (Admin Only)
**Description**: Update an existing post

**Path Parameters**:
- `postId`: UUID of the post

**Headers**:
```
Authorization: Bearer {jwt_token}
Content-Type: application/json
```

**Request Body**: Same as create, all fields are optional
```json
{
  "title": "Updated Title",
  "status": "DRAFT"
}
```

**Response**: 200 OK with updated post object

**Notes**:
- Only provided fields will be updated
- If updating thumbnail, old thumbnail will be deleted from S3
- If changing slug, uniqueness will be validated

---

### 7. DELETE /admin/posts/{postId} - Delete Post (Admin Only)
**Description**: Delete a post and its thumbnail from S3

**Path Parameters**:
- `postId`: UUID of the post

**Headers**:
```
Authorization: Bearer {jwt_token}
```

**Response**: 204 No Content

**Notes**:
- Thumbnail image will be automatically deleted from S3
- This operation cannot be undone

---

## Features

### 1. Automatic Slug Generation
- Vietnamese text is automatically converted to URL-friendly slug
- Diacritics are removed (ả → a, đ → d)
- Special characters are removed
- Spaces are converted to hyphens
- If slug exists, a numeric suffix is added

**Examples**:
- "Tại Sao Nên Mua Gà Ủ Muối" → "tai-sao-nen-mua-ga-u-muoi"
- "Sản Phẩm Từ Heo" → "san-pham-tu-heo"
- "Rượu Ngon 100%" → "ruou-ngon-100"

### 2. Image Management
- Thumbnails are uploaded to AWS S3
- When updating thumbnail, old image is automatically deleted
- When deleting post, thumbnail is automatically removed from S3

### 3. Status Management
- **DRAFT**: Post is not visible to public
- **PUBLISHED**: Post is visible in public endpoints

Only PUBLISHED posts appear in:
- `/posts` list
- `/posts/{slug}` detail
- `/posts/featured` featured list
- `/posts/related` related posts

### 4. Security
- **Public Endpoints**: GET /posts, /posts/{slug}, /posts/featured, /posts/related
- **Admin Only**: POST, PUT, DELETE /admin/posts/**
- Admin endpoints require JWT token with ROLE_ADMIN

## Error Responses

### 400 Bad Request
```json
{
  "timestamp": "2025-11-24T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Post title is required",
  "path": "/admin/posts"
}
```

### 404 Not Found
```json
{
  "timestamp": "2025-11-24T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Post not found",
  "path": "/posts/invalid-slug"
}
```

### 401 Unauthorized
```json
{
  "timestamp": "2025-11-24T10:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required",
  "path": "/admin/posts"
}
```

## Testing with cURL

### Create a post
```bash
curl -X POST http://localhost:8080/admin/posts \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Post",
    "shortDescription": "This is a test",
    "content": "<p>Content here</p>",
    "categoryId": "cat-123",
    "status": "PUBLISHED"
  }'
```

### Get posts
```bash
curl http://localhost:8080/posts?page=0&size=10
```

### Get post by slug
```bash
curl http://localhost:8080/posts/test-post
```

### Update post
```bash
curl -X PUT http://localhost:8080/admin/posts/POST_ID \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "DRAFT"
  }'
```

### Delete post
```bash
curl -X DELETE http://localhost:8080/admin/posts/POST_ID \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Integration with Frontend

### Display post list
```javascript
const fetchPosts = async (page = 0, categoryId = null) => {
  const params = new URLSearchParams({
    page: page,
    size: 10,
    ...(categoryId && { categoryId })
  });
  
  const response = await fetch(`/posts?${params}`);
  const data = await response.json();
  return data;
};
```

### Display post detail
```javascript
const fetchPost = async (slug) => {
  const response = await fetch(`/posts/${slug}`);
  const post = await response.json();
  return post;
};
```

### Display featured posts in sidebar
```javascript
const fetchFeaturedPosts = async () => {
  const response = await fetch('/posts/featured?page=0&size=5');
  const data = await response.json();
  return data.content;
};
```

### Display related posts
```javascript
const fetchRelatedPosts = async (currentSlug) => {
  const response = await fetch(`/posts/related?slug=${currentSlug}&limit=5`);
  const posts = await response.json();
  return posts;
};
```

## Best Practices

1. **Always use slugs in URLs** instead of IDs for better SEO
2. **Upload images to S3 first** using `/upload` endpoint, then use returned URL
3. **Use DRAFT status** for posts that are not ready to be published
4. **Set isFeatured** for important posts to display in sidebar
5. **Provide shortDescription** for better list display and SEO
6. **Use proper HTML or Markdown** in content field
7. **Include author name** for credibility

## Files Created

1. **Entity**: `PostEntity.java` - Database entity with all fields
2. **Enum**: `PostStatus.java` - DRAFT/PUBLISHED status enum
3. **Request DTO**: `PostRequest.java` - For creating/updating posts
4. **Response DTOs**: 
   - `PostResponse.java` - Full post details
   - `PostListItem.java` - Simplified for list views
5. **Repository**: `PostRepository.java` - Data access with custom queries
6. **Service**: `PostService.java` + `PostServiceImpl.java` - Business logic
7. **Controller**: `PostController.java` - REST endpoints
8. **Security**: Updated `SecurityConfig.java` - Public GET access configured

All files follow the existing project patterns and integrate seamlessly with your current architecture.

