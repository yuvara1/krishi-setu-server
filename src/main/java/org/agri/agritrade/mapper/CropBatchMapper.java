package org.agri.agritrade.mapper;

import org.agri.agritrade.dto.response.CropBatchDTO;
import org.agri.agritrade.entity.CropBatch;

public class CropBatchMapper {

    public static CropBatchDTO toDTO(CropBatch entity) {
        if (entity == null) return null;

        CropBatchDTO dto = new CropBatchDTO();
        dto.setId(entity.getId());
        dto.setFarmerId(entity.getFarmer().getId());
        dto.setFarmerName(entity.getFarmer().getFullName());
        dto.setFarmerEmail(entity.getFarmer().getEmail());
        dto.setCropName(entity.getCropName());
        dto.setCropType(entity.getCropType());
        dto.setQuantity(entity.getQuantity());
        dto.setBasePrice(entity.getBasePrice());
        dto.setHarvestDate(entity.getHarvestDate());
        dto.setExpiryDate(entity.getExpiryDate());
        dto.setDescription(entity.getDescription());
        dto.setStatus(entity.getStatus());
        dto.setImageUrl(entity.getImageUrl());
        dto.setLocation(entity.getLocation());
        dto.setIsOrganic(entity.getIsOrganic());
        dto.setUnit(entity.getUnit());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        // Populate bids count and highest bid
        if (entity.getBids() != null) {
            dto.setTotalBids(entity.getBids().size());
            dto.setHighestBidAmount(entity.getBids().stream()
                    .map(bid -> bid.getBidAmount())
                    .max(java.math.BigDecimal::compareTo)
                    .orElse(java.math.BigDecimal.ZERO));
        }

        return dto;
    }

    public static CropBatch toEntity(CropBatchDTO dto) {
        if (dto == null) return null;

        CropBatch entity = new CropBatch();
        entity.setId(dto.getId());
        entity.setCropName(dto.getCropName());
        entity.setCropType(dto.getCropType());
        entity.setQuantity(dto.getQuantity());
        entity.setBasePrice(dto.getBasePrice());
        entity.setHarvestDate(dto.getHarvestDate());
        entity.setExpiryDate(dto.getExpiryDate());
        entity.setDescription(dto.getDescription());
        entity.setStatus(dto.getStatus());
        entity.setImageUrl(dto.getImageUrl());
        entity.setLocation(dto.getLocation());
        entity.setIsOrganic(dto.getIsOrganic());
        entity.setUnit(dto.getUnit());
        return entity;
    }
}
