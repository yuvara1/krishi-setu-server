package org.agri.agritrade.repository;

import org.agri.agritrade.entity.CropBatch;
import org.agri.agritrade.entity.User;
import org.agri.agritrade.util.enums.CropStatus;
import org.agri.agritrade.util.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameOrEmail(String username, String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id); // Add this method
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.role = :role")
    Optional<User> findByUsernameAndRole(@Param("username") String username, @Param("role") Role role);

    @Query("SELECT c FROM CropBatch c WHERE c.cropName LIKE %:cropName% AND c.status = :status")
    List<CropBatch> searchByNameAndStatus(@Param("cropName") String cropName, @Param("status") CropStatus status);
}
