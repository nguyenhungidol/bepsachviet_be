package com.doan.bepsachviet_be.service.Impl;

import com.doan.bepsachviet_be.service.FileUploadService;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;
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
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
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
      return false;
    }

    try {
      DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
          .bucket(bucketName)
          .key(objectKey.get())
          .build();

      s3Client.deleteObject(deleteObjectRequest);
      return true;
    } catch (S3Exception exception) {
      log.warn("Failed to delete S3 object {} in bucket {}", objectKey.get(), bucketName, exception);
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
      if (host == null || path == null || path.isBlank()) {
        return Optional.empty();
      }

      String normalizedPath = path.startsWith("/") ? path.substring(1) : path;
      if (host.startsWith(bucketName + ".")) {
        return Optional.of(normalizedPath);
      }
      if ((host.startsWith("s3.") || "s3.amazonaws.com".equals(host)) && normalizedPath.startsWith(bucketName + "/")) {
        return Optional.of(normalizedPath.substring(bucketName.length() + 1));
      }
      return Optional.empty();
    } catch (IllegalArgumentException exception) {
      log.debug("Skip deleting unmanaged url {}", imgUrl, exception);
      return Optional.empty();
    }
  }
}