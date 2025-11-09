package com.sparta.vendorservice.repository;

import com.sparta.vendorservice.domain.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VendorRepository extends JpaRepository<Vendor, UUID>, CustomVendorRepository {
}
