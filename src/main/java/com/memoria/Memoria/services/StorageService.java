package com.memoria.Memoria.services;

import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;

public interface StorageService {
    String uploadFile(MultipartFile file);
    InputStream downloadFile(String fileKey);
    void deleteFile(String fileKey);
}
