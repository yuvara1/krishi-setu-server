package org.agri.agritrade.repository;

import org.agri.agritrade.entity.CropBatch;
import org.agri.agritrade.entity.enums.CropStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CropBatchRepository extends JpaRepository<CropBatch, Long> {
    List<CropBatch> findByFarmerId(Long farmerId);
    List<CropBatch> findByStatus(CropStatus status);
    List<CropBatch> findByFarmerIdAndStatus(Long farmerId, CropStatus status);
    List<CropBatch> findByCropNameContainingIgnoreCase(String cropName);

    Page<CropBatch> findByFarmerId(Long farmerId, Pageable pageable);
    Page<CropBatch> findByStatus(CropStatus status, Pageable pageable);
}
