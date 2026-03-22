package org.agri.agritrade.service;

import org.agri.agritrade.dto.ResponseStructure;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface ImageUploadServicePort {
    ResponseStructure<Map<String, String>> uploadImage(MultipartFile file, String folder);
}
