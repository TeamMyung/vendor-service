package com.sparta.vendorservice.domain;

import com.sparta.vendorservice.dto.request.UpdateVendorReqDto;
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
    private String vendorAddress;

    @Column(nullable = false)
    private UUID hubId;

    private Vendor(String vendorName, VendorType vendorType, String vendorAddress, UUID hubId) {
        this.vendorName = vendorName;
        this.vendorType = vendorType;
        this.vendorAddress = vendorAddress;
        this.hubId = hubId;
    }

    public static Vendor ofNewVendor(String vendorName, VendorType vendorType, String vendorAddress, UUID hubId) {
        return new Vendor(vendorName, vendorType, vendorAddress, hubId);
    }

    public void update(UpdateVendorReqDto request) {
        if (request.getVendorAddress() != null) this.vendorAddress = request.getVendorAddress();
        if (request.getVendorType() != null) this.vendorType = request.getVendorType();
    }

}
