package com.sparta.vendorservice.repository;

import com.sparta.vendorservice.domain.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface VendorRepository extends JpaRepository<Vendor, UUID>, CustomVendorRepository {
    @Query("SELECT v.hubId FROM Vendor v WHERE v.vendorId = :vendorId AND v.deletedAt IS NULL")
    Optional<UUID> findHubIdByVendorId(UUID vendorId);
}
