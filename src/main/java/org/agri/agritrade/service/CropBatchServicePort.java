package org.agri.agritrade.service;

import org.agri.agritrade.dto.response.CropBatchDTO;
import org.agri.agritrade.dto.response.PagedResponse;
import org.agri.agritrade.dto.response.ResponseStructure;
import org.agri.agritrade.util.enums.CropStatus;

import java.util.List;

public interface CropBatchServicePort {
    ResponseStructure<CropBatchDTO> createCropBatch(CropBatchDTO dto);
    ResponseStructure<List<CropBatchDTO>> getAllCropBatches();
    ResponseStructure<PagedResponse<CropBatchDTO>> getAllCropBatchesPaged(int page, int size);
    ResponseStructure<CropBatchDTO> getCropBatchById(Long id);
    ResponseStructure<CropBatchDTO> updateCropBatch(Long id, CropBatchDTO updatedCropBatch);
    ResponseStructure<Void> deleteCropBatch(Long id);
    ResponseStructure<List<CropBatchDTO>> getCropsByFarmerId(Long farmerId);
    ResponseStructure<PagedResponse<CropBatchDTO>> getCropsByFarmerIdPaged(Long farmerId, int page, int size);
    ResponseStructure<List<CropBatchDTO>> getCropsByStatus(CropStatus status);
    ResponseStructure<java.util.List<org.agri.agritrade.entity.CropBatch>> getAvailableCropBatches();
    ResponseStructure<java.util.List<org.agri.agritrade.entity.CropBatch>> searchCropsByName(String cropName);
    ResponseStructure<CropBatchDTO> updateCropStatus(Long cropId, CropStatus newStatus);
}
