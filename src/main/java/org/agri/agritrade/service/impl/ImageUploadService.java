

package org.agri.agritrade.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.agri.agritrade.dto.ResponseStructure;
import org.agri.agritrade.service.ImageUploadServicePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.Map;

@Service
@Slf4j
public class ImageUploadService implements ImageUploadServicePort {

    private static final String IMAGEKIT_UPLOAD_URL = "https://upload.imagekit.io/api/v1/files/upload";

    @Value("${imagekit.private-key}")
    private String privateKey;

    private final RestTemplate restTemplate = new RestTemplate();
    @Override
    public ResponseStructure<Map<String, String>> uploadImage(MultipartFile file, String folder) {
        try {
            if (file.isEmpty()) {
                return new ResponseStructure<>(HttpStatus.BAD_REQUEST.value(), "File is empty", null);
            }

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return new ResponseStructure<>(HttpStatus.BAD_REQUEST.value(), "Only image files are allowed", null);
            }

            // Validate file size (max 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                return new ResponseStructure<>(HttpStatus.BAD_REQUEST.value(), "File size must not exceed 5MB", null);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            String auth = Base64.getEncoder().encodeToString((privateKey + ":").getBytes());
            headers.set("Authorization", "Basic " + auth);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            String sanitizedName = file.getOriginalFilename()
                .replaceAll("[^a-zA-Z0-9._-]", "_");
            body.add("file", file.getResource());
            body.add("fileName", sanitizedName);
            body.add("folder", folder);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    IMAGEKIT_UPLOAD_URL, HttpMethod.POST, requestEntity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String url = (String) response.getBody().get("url");
                String fileId = (String) response.getBody().get("fileId");
                Map<String, String> result = Map.of("url", url, "fileId", fileId);
                log.info("Image uploaded successfully: {}", url);
                return new ResponseStructure<>(HttpStatus.OK.value(), "Image uploaded successfully", result);
            }

            return new ResponseStructure<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Upload failed", null);
        } catch (Exception e) {
            log.error("Error uploading image: {}", e.getMessage(), e);
            return new ResponseStructure<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to upload image", null);
        }
    }
}