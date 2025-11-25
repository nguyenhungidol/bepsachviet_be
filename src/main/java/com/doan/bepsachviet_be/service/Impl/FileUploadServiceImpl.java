package com.doan.bepsachviet_be.service.Impl;

import com.doan.bepsachviet_be.service.FileUploadService;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadServiceImpl implements FileUploadService {
  @Value("${aws.bucket.name}")
  private String bucketName;
  private final S3Client s3Client;

  @Override
  public String uploadFile(MultipartFile file) {
    String originalFilename = file.getOriginalFilename();
    String extension = "";
    if (originalFilename != null && originalFilename.contains(".")) {
      extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1);
    }
    String key = UUID.randomUUID().toString();
    if (!extension.isBlank()) {
      key = key + "." + extension;
    }

    try {
      PutObjectRequest putObjectRequest = PutObjectRequest
          .builder()
          .bucket(bucketName)
          .key(key)
//          .acl(ObjectCannedACL.PUBLIC_READ)
          .contentType(file.getContentType())
          .build();

      PutObjectResponse response = s3Client.putObject(putObjectRequest,
          RequestBody.fromBytes(file.getBytes()));
      if(response.sdkHttpResponse().isSuccessful()){
        return s3Client.utilities()
            .getUrl(GetUrlRequest.builder().bucket(bucketName).key(key).build())
            .toExternalForm();
      }else{
        throw  new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occured while upload image!!!");
      }
    }catch (IOException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occured while upload file!!!");
    }
  }

  @Override
  public boolean deleteFile(String imgUrl) {
    Optional<String> objectKey = resolveObjectKey(imgUrl);
    if (objectKey.isEmpty()) {
      log.warn("Could not resolve S3 object key from URL: {}", imgUrl);
      return false;
    }

    String key = objectKey.get();
    try {
      log.info("Attempting to delete S3 object with key '{}' from bucket '{}'", key, bucketName);
      DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
          .bucket(bucketName)
          .key(key)
          .build();

      DeleteObjectResponse resp = s3Client.deleteObject(deleteObjectRequest);
      boolean success = resp.sdkHttpResponse() != null && resp.sdkHttpResponse().isSuccessful();
      if (!success) {
        log.warn("DeleteObject returned non-successful response for key {}: {}", key, resp);
      } else {
        log.info("Successfully deleted S3 object '{}' from bucket '{}'", key, bucketName);
      }
      return success;
    } catch (S3Exception exception) {
      log.warn("Failed to delete S3 object {} in bucket {}: {}", key, bucketName, exception.awsErrorDetails() != null ? exception.awsErrorDetails().errorMessage() : exception.getMessage());
      return false;
    }
  }

  private Optional<String> resolveObjectKey(String imgUrl) {
    if (imgUrl == null || imgUrl.isBlank()) {
      return Optional.empty();
    }

    try {
      URI uri = URI.create(imgUrl.trim());
      String host = uri.getHost();
      String path = uri.getPath();
      if (path == null || path.isBlank()) {
        return Optional.empty();
      }

      String normalizedPath = path.startsWith("/") ? path.substring(1) : path;
      // 1) virtual-hosted style: bucketName.s3.amazonaws.com/key or bucketName.s3.region.amazonaws.com/key
      if (host != null && host.startsWith(bucketName + ".")) {
        return Optional.of(normalizedPath);
      }

      // 2) path-style: s3.amazonaws.com/bucketName/key or s3.region.amazonaws.com/bucketName/key
      if (host != null && (host.startsWith("s3.") || "s3.amazonaws.com".equals(host))) {
        if (normalizedPath.startsWith(bucketName + "/")) {
          return Optional.of(normalizedPath.substring(bucketName.length() + 1));
        }
      }

      // 3) regional/alternate patterns: host like bucketName.s3-REGION.amazonaws.com
      if (host != null && host.contains(bucketName + ".s3")) {
        return Optional.of(normalizedPath);
      }

      // 4) fallback: if path contains /{bucketName}/..., extract after bucketName/
      Pattern p = Pattern.compile("(^|/)" + Pattern.quote(bucketName) + "/(.+)$");
      Matcher m = p.matcher(normalizedPath);
      if (m.find()) {
        return Optional.of(m.group(2));
      }

      // 5) if host contains the bucket name in some custom way and normalizedPath looks like a key, try using it
      if (host != null && host.contains(bucketName) && !normalizedPath.isBlank()) {
        log.debug("Host contains bucket name but none of the known patterns matched. Using full path as key: {}", normalizedPath);
        return Optional.of(normalizedPath);
      }

      // Not resolvable (e.g., custom CDN domain). Best practice: store the object key at upload time and delete by that key.
      log.debug("Unable to resolve object key from URL host='{}' path='{}'", host, path);
      return Optional.empty();
    } catch (IllegalArgumentException exception) {
      log.debug("Skip deleting unmanaged url {}", imgUrl, exception);
      return Optional.empty();
    }
  }

  // Note: Best practice — when uploading, persist the generated object key (e.g., UUID + extension) to your DB.
  // Use that stored key to delete the object later instead of parsing the public URL (avoids CDN/custom domain issues).
}