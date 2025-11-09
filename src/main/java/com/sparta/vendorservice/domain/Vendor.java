package com.sparta.vendorservice.domain;

import com.sparta.vendorservice.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "p_vendors")
public class Vendor extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID vendorId;

    @Column(nullable = false, length = 100)
    private String vendorName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VendorType vendorType;

    @Column(nullable = false)
    private String hubAddress;

    @Column(nullable = false)
    private UUID hubId;

    private Vendor(String vendorName, VendorType vendorType, String hubAddress, UUID hubId) {
        this.vendorName = vendorName;
        this.vendorType = vendorType;
        this.hubAddress = hubAddress;
        this.hubId = hubId;
    }

    private static Vendor ofNewVendor(String vendorName, VendorType vendorType, String hubAddress, UUID hubId) {
        return new Vendor(vendorName, vendorType, hubAddress, hubId);
    }

}
