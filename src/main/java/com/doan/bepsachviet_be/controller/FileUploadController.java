package com.doan.bepsachviet_be.controller;

import com.doan.bepsachviet_be.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class FileUploadController {

  private final FileUploadService fileUploadService;

  @PostMapping("/upload")
  public String uploadFile(@RequestParam("file") MultipartFile file) {
    return fileUploadService.uploadFile(file);
  }

  // new: endpoint to attempt delete by URL (useful for debugging)
  @DeleteMapping("/delete")
  public boolean deleteFile(@RequestParam("url") String url) {
    // returns true if deletion was attempted and succeeded, false otherwise
    return fileUploadService.deleteFile(url);
  }
}