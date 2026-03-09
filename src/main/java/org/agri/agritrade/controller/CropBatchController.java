package org.agri.agritrade.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.agri.agritrade.dto.CropBatchDTO;
import org.agri.agritrade.dto.PagedResponse;
import org.agri.agritrade.dto.ResponseStructure;
import org.agri.agritrade.entity.enums.CropStatus;
import org.agri.agritrade.service.CropBatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/crop-batches")
@RequiredArgsConstructor
public class CropBatchController {

    private final CropBatchService cropBatchService;

    @PostMapping
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<ResponseStructure<CropBatchDTO>> createCropBatch(@Valid @RequestBody CropBatchDTO cropBatchDTO) {
        ResponseStructure<CropBatchDTO> response = cropBatchService.createCropBatch(cropBatchDTO);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping
    public ResponseEntity<ResponseStructure<List<CropBatchDTO>>> getAllCropBatches() {
        ResponseStructure<List<CropBatchDTO>> response = cropBatchService.getAllCropBatches();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/paged")
    public ResponseEntity<ResponseStructure<PagedResponse<CropBatchDTO>>> getAllCropBatchesPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        ResponseStructure<PagedResponse<CropBatchDTO>> response = cropBatchService.getAllCropBatchesPaged(page, size);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseStructure<CropBatchDTO>> getCropBatchById(@PathVariable Long id) {
        ResponseStructure<CropBatchDTO> response = cropBatchService.getCropBatchById(id);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<ResponseStructure<CropBatchDTO>> updateCropBatch(
            @PathVariable Long id,
            @Valid @RequestBody CropBatchDTO updatedCropBatch) {
        ResponseStructure<CropBatchDTO> response = cropBatchService.updateCropBatch(id, updatedCropBatch);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<ResponseStructure<Void>> deleteCropBatch(@PathVariable Long id) {
        ResponseStructure<Void> response = cropBatchService.deleteCropBatch(id);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/farmer/{farmerId}")
    public ResponseEntity<ResponseStructure<List<CropBatchDTO>>> getCropsByFarmerId(@PathVariable Long farmerId) {
        ResponseStructure<List<CropBatchDTO>> response = cropBatchService.getCropsByFarmerId(farmerId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/farmer/{farmerId}/paged")
    public ResponseEntity<ResponseStructure<PagedResponse<CropBatchDTO>>> getCropsByFarmerIdPaged(
            @PathVariable Long farmerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        ResponseStructure<PagedResponse<CropBatchDTO>> response = cropBatchService.getCropsByFarmerIdPaged(farmerId, page, size);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ResponseStructure<List<CropBatchDTO>>> getCropsByStatus(@PathVariable CropStatus status) {
        ResponseStructure<List<CropBatchDTO>> response = cropBatchService.getCropsByStatus(status);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<ResponseStructure<CropBatchDTO>> updateCropStatus(
            @PathVariable Long id,
            @RequestParam CropStatus status) {
        ResponseStructure<CropBatchDTO> response = cropBatchService.updateCropStatus(id, status);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
