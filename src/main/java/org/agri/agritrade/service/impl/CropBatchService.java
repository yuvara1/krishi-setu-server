package org.agri.agritrade.service.impl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.agri.agritrade.dto.CropBatchDTO;
import org.agri.agritrade.dto.PagedResponse;
import org.agri.agritrade.dto.ResponseStructure;
import org.agri.agritrade.entity.CropBatch;
import org.agri.agritrade.entity.User;
import org.agri.agritrade.entity.enums.CropStatus;
import org.agri.agritrade.mapper.CropBatchMapper;
import org.agri.agritrade.repository.CropBatchRepository;
import org.agri.agritrade.repository.UserRepository;
import org.agri.agritrade.service.CropBatchServicePort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CropBatchService implements CropBatchServicePort {

    private final CropBatchRepository cropBatchRepository;
    private final UserRepository userRepository;


    @Override
    @Transactional
    public ResponseStructure<CropBatchDTO> createCropBatch(CropBatchDTO cropBatchDTO) {
        try {
            if (cropBatchDTO.getFarmerId() == null) {
                return new ResponseStructure<>(HttpStatus.BAD_REQUEST.value(), "Farmer ID is required", null);
            }

            Optional<User> farmer = userRepository.findById(cropBatchDTO.getFarmerId());
            if (farmer.isEmpty()) {
                return new ResponseStructure<>(HttpStatus.NOT_FOUND.value(), "Farmer not found", null);
            }

            CropBatch cropBatch = CropBatchMapper.toEntity(cropBatchDTO);
            cropBatch.setFarmer(farmer.get());
            cropBatch.setStatus(CropStatus.AVAILABLE);
            cropBatch.setCreatedAt(LocalDateTime.now());
            cropBatch.setUpdatedAt(LocalDateTime.now());

            CropBatch savedCropBatch = cropBatchRepository.save(cropBatch);
            CropBatchDTO responseDTO = CropBatchMapper.toDTO(savedCropBatch);

            log.info("Crop batch created with ID: {}", savedCropBatch.getId());
            return new ResponseStructure<>(HttpStatus.CREATED.value(), "Crop batch created successfully", responseDTO);
        } catch (Exception e) {
            log.error("Error creating crop batch: {}", e.getMessage(), e);
            return new ResponseStructure<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to create crop batch", null);
        }
    }
    @Override
    public ResponseStructure<List<CropBatchDTO>> getAllCropBatches() {
        try {
            List<CropBatch> cropBatches = cropBatchRepository.findAll();
            List<CropBatchDTO> cropBatchDTOs = cropBatches.stream()
                    .map(CropBatchMapper::toDTO)
                    .toList();
            return new ResponseStructure<>(HttpStatus.OK.value(), "Crop batches retrieved successfully", cropBatchDTOs);
        } catch (Exception e) {
            log.error("Error retrieving crop batches: {}", e.getMessage(), e);
            return new ResponseStructure<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve crop batches", null);
        }
    }
    @Override
    public ResponseStructure<PagedResponse<CropBatchDTO>> getAllCropBatchesPaged(int page, int size) {
        try {
            Page<CropBatch> cropPage = cropBatchRepository.findAll(
                    PageRequest.of(page, size,
                            Sort.by("createdAt").descending()));
            List<CropBatchDTO> dtos = cropPage.getContent().stream()
                    .map(CropBatchMapper::toDTO).toList();
            PagedResponse<CropBatchDTO> paged = new PagedResponse<>(
                    dtos, cropPage.getNumber(), cropPage.getSize(),
                    cropPage.getTotalElements(), cropPage.getTotalPages(), cropPage.isLast());
            return new ResponseStructure<>(HttpStatus.OK.value(), "Crop batches retrieved successfully", paged);
        } catch (Exception e) {
            log.error("Error retrieving paged crop batches: {}", e.getMessage(), e);
            return new ResponseStructure<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve crop batches", null);
        }
    }
    @Override
    public ResponseStructure<CropBatchDTO> getCropBatchById(Long id) {
        try {
            Optional<CropBatch> cropBatch = cropBatchRepository.findById(id);
            if (cropBatch.isEmpty()) {
                return new ResponseStructure<>(HttpStatus.NOT_FOUND.value(), "Crop batch not found", null);
            }
            CropBatchDTO cropBatchDTO = CropBatchMapper.toDTO(cropBatch.get());
            return new ResponseStructure<>(HttpStatus.OK.value(), "Crop batch retrieved successfully", cropBatchDTO);
        } catch (Exception e) {
            log.error("Error retrieving crop batch with ID {}: {}", id, e.getMessage(), e);
            return new ResponseStructure<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve crop batch", null);
        }
    }
    @Override
    @Transactional
    public ResponseStructure<CropBatchDTO> updateCropBatch(Long id, @Valid CropBatchDTO updatedCropBatch) {
        try {
            Optional<CropBatch> existingCropBatch = cropBatchRepository.findById(id);
            if (existingCropBatch.isEmpty()) {
                return new ResponseStructure<>(HttpStatus.NOT_FOUND.value(), "Crop batch not found", null);
            }

            CropBatch cropBatch = existingCropBatch.get();

            // Prevent updates on sold crops
            if (cropBatch.getStatus() == CropStatus.SOLD) {
                return new ResponseStructure<>(HttpStatus.BAD_REQUEST.value(), "Cannot update a sold crop", null);
            }

            // Update fields
            if (updatedCropBatch.getCropName() != null) {
                cropBatch.setCropName(updatedCropBatch.getCropName());
            }
            if (updatedCropBatch.getCropType() != null) {
                cropBatch.setCropType(updatedCropBatch.getCropType());
            }
            if (updatedCropBatch.getQuantity() != null) {
                cropBatch.setQuantity(updatedCropBatch.getQuantity());
            }
            if (updatedCropBatch.getBasePrice() != null) {
                cropBatch.setBasePrice(updatedCropBatch.getBasePrice());
            }
            if (updatedCropBatch.getHarvestDate() != null) {
                cropBatch.setHarvestDate(updatedCropBatch.getHarvestDate());
            }
            if (updatedCropBatch.getExpiryDate() != null) {
                cropBatch.setExpiryDate(updatedCropBatch.getExpiryDate());
            }
            if (updatedCropBatch.getDescription() != null) {
                cropBatch.setDescription(updatedCropBatch.getDescription());
            }
            if (updatedCropBatch.getStatus() != null) {
                cropBatch.setStatus(updatedCropBatch.getStatus());
            }
            if (updatedCropBatch.getImageUrl() != null) {
                cropBatch.setImageUrl(updatedCropBatch.getImageUrl());
            }
            if (updatedCropBatch.getLocation() != null) {
                cropBatch.setLocation(updatedCropBatch.getLocation());
            }
            if (updatedCropBatch.getIsOrganic() != null) {
                cropBatch.setIsOrganic(updatedCropBatch.getIsOrganic());
            }
            if (updatedCropBatch.getUnit() != null) {
                cropBatch.setUnit(updatedCropBatch.getUnit());
            }

            cropBatch.setUpdatedAt(LocalDateTime.now());
            CropBatch savedCropBatch = cropBatchRepository.save(cropBatch);
            CropBatchDTO responseDTO = CropBatchMapper.toDTO(savedCropBatch);

            log.info("Crop batch updated with ID: {}", savedCropBatch.getId());
            return new ResponseStructure<>(HttpStatus.OK.value(), "Crop batch updated successfully", responseDTO);
        } catch (Exception e) {
            log.error("Error updating crop batch with ID {}: {}", id, e.getMessage(), e);
            return new ResponseStructure<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to update crop batch", null);
        }
    }
    @Override
    @Transactional
    public ResponseStructure<CropBatchDTO> updateCropStatus(Long cropId, CropStatus newStatus) {
        try {
            Optional<CropBatch> cropBatch = cropBatchRepository.findById(cropId);
            if (cropBatch.isEmpty()) {
                return new ResponseStructure<>(HttpStatus.NOT_FOUND.value(), "Crop batch not found", null);
            }

            CropBatch crop = cropBatch.get();

            if (crop.getStatus() == CropStatus.SOLD) {
                return new ResponseStructure<>(HttpStatus.BAD_REQUEST.value(), "Cannot delete a sold crop", null);
            }

            crop.setStatus(newStatus);
            crop.setUpdatedAt(LocalDateTime.now());

            CropBatch updatedCrop = cropBatchRepository.save(crop);
            CropBatchDTO responseDTO = CropBatchMapper.toDTO(updatedCrop);
            log.info("Crop batch status updated to {} for ID: {}", newStatus, cropId);

            return new ResponseStructure<>(HttpStatus.OK.value(), "Crop status updated successfully", responseDTO);
        } catch (Exception e) {
            log.error("Error updating crop status for ID {}: {}", cropId, e.getMessage(), e);
            return new ResponseStructure<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to update crop status", null);
        }
    }

    @Override
    @Transactional
    public ResponseStructure<Void> deleteCropBatch(Long id) {
        try {
            Optional<CropBatch> cropBatch = cropBatchRepository.findById(id);
            if (cropBatch.isEmpty()) {
                return new ResponseStructure<>(HttpStatus.NOT_FOUND.value(), "Crop batch not found", null);
            }

            if (cropBatch.get().getStatus() == CropStatus.SOLD) {
                return new ResponseStructure<>(HttpStatus.BAD_REQUEST.value(), "Cannot delete a sold crop", null);
            }

            cropBatchRepository.deleteById(id);
            log.info("Crop batch deleted with ID: {}", id);
            return new ResponseStructure<>(HttpStatus.NO_CONTENT.value(), "Crop batch deleted successfully", null);
        } catch (Exception e) {
            log.error("Error deleting crop batch with ID {}: {}", id, e.getMessage(), e);
            return new ResponseStructure<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to delete crop batch", null);
        }
    }
    @Override
    public ResponseStructure<List<CropBatchDTO>> getCropsByFarmerId(Long farmerId) {
        try {
            Optional<User> farmer = userRepository.findById(farmerId);
            if (farmer.isEmpty()) {
                return new ResponseStructure<>(HttpStatus.NOT_FOUND.value(), "Farmer not found", null);
            }

            List<CropBatch> cropBatches = cropBatchRepository.findByFarmerId(farmerId);
            List<CropBatchDTO> cropBatchDTOs = cropBatches.stream()
                    .map(CropBatchMapper::toDTO)
                    .toList();
            return new ResponseStructure<>(HttpStatus.OK.value(), "Farmer's crop batches retrieved successfully", cropBatchDTOs);
        } catch (Exception e) {
            log.error("Error retrieving crop batches for farmer ID {}: {}", farmerId, e.getMessage(), e);
            return new ResponseStructure<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve farmer's crop batches", null);
        }
    }
    @Override
    public ResponseStructure<PagedResponse<CropBatchDTO>> getCropsByFarmerIdPaged(Long farmerId, int page, int size) {
        try {
            Optional<User> farmer = userRepository.findById(farmerId);
            if (farmer.isEmpty()) {
                return new ResponseStructure<>(HttpStatus.NOT_FOUND.value(), "Farmer not found", null);
            }
            Page<CropBatch> cropPage = cropBatchRepository.findByFarmerId(farmerId,
                    PageRequest.of(page, size,
                            Sort.by("createdAt").descending()));
            List<CropBatchDTO> dtos = cropPage.getContent().stream()
                    .map(CropBatchMapper::toDTO).toList();
            PagedResponse<CropBatchDTO> paged = new PagedResponse<>(
                    dtos, cropPage.getNumber(), cropPage.getSize(),
                    cropPage.getTotalElements(), cropPage.getTotalPages(), cropPage.isLast());
            return new ResponseStructure<>(HttpStatus.OK.value(), "Farmer's crop batches retrieved successfully", paged);
        } catch (Exception e) {
            log.error("Error retrieving paged crops for farmer {}: {}", farmerId, e.getMessage(), e);
            return new ResponseStructure<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve farmer's crop batches", null);
        }
    }
    @Override
    public ResponseStructure<List<CropBatchDTO>> getCropsByStatus(CropStatus status) {
        try {
            List<CropBatch> cropBatches = cropBatchRepository.findByStatus(status);
            List<CropBatchDTO> cropBatchDTOs = cropBatches.stream()
                    .map(CropBatchMapper::toDTO)
                    .toList();
            return new ResponseStructure<>(HttpStatus.OK.value(), "Crop batches retrieved by status successfully", cropBatchDTOs);
        } catch (Exception e) {
            log.error("Error retrieving crop batches by status {}: {}", status, e.getMessage(), e);
            return new ResponseStructure<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve crop batches by status", null);
        }
    }
    @Override
    public ResponseStructure<List<CropBatch>> getAvailableCropBatches() {
        try {
            List<CropBatch> availableCrops = cropBatchRepository.findByStatus(CropStatus.AVAILABLE);
            return new ResponseStructure<>(HttpStatus.OK.value(), "Available crop batches retrieved successfully", availableCrops);
        } catch (Exception e) {
            log.error("Error retrieving available crop batches: {}", e.getMessage(), e);
            return new ResponseStructure<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve available crop batches", null);
        }
    }
    @Override
    public ResponseStructure<List<CropBatch>> searchCropsByName(String cropName) {
        try {

            String sanitizedCropName = cropName.replaceAll("[^a-zA-Z0-9\\s]", "").trim();

            if (sanitizedCropName.isEmpty()) {
                return new ResponseStructure<>(HttpStatus.BAD_REQUEST.value(), "Invalid search term", null);
            }

            List<CropBatch> cropBatches = cropBatchRepository.findByCropNameContainingIgnoreCase(sanitizedCropName);
            return new ResponseStructure<>(HttpStatus.OK.value(), "Search completed successfully", cropBatches);
        } catch (Exception e) {
            log.error("Error searching crops: {}", e.getMessage(), e);
            return new ResponseStructure<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Search failed", null);
        }
    }

}
