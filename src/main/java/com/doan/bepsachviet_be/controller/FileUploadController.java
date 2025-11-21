package com.doan.bepsachviet_be.controller;

import com.doan.bepsachviet_be.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class FileUploadController {

  private final FileUploadService fileUploadService;

  // API này dùng để upload ảnh trước, sau đó lấy link trả về
  @PostMapping("/upload")
  public String uploadFile(@RequestParam("file") MultipartFile file) {
    return fileUploadService.uploadFile(file);
  }
}