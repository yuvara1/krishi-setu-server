// File: server/agritrade/src/main/java/org/agri/agritrade/controller/ImageUploadController.java

package org.agri.agritrade.controller;

import lombok.RequiredArgsConstructor;
import org.agri.agritrade.dto.response.ResponseStructure;
import org.agri.agritrade.service.impl.ImageUploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class ImageUploadController {

    private final ImageUploadService imageUploadService;

    @PostMapping("/image")
    public ResponseEntity<ResponseStructure<Map<String, String>>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "/crops") String folder) {
        ResponseStructure<Map<String, String>> response = imageUploadService.uploadImage(file, folder);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}