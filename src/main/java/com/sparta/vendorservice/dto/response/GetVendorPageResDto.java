package com.sparta.vendorservice.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import com.sparta.vendorservice.domain.VendorType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
public class GetVendorPageResDto {
    private UUID vendorId;
    private String vendorName;
    private VendorType vendorType;
    private String vendorAddress;
    private UUID hubId;

    @QueryProjection
    public GetVendorPageResDto(UUID vendorId, String vendorName, VendorType vendorType, String vendorAddress, UUID hubId) {
        this.vendorId = vendorId;
        this.vendorName = vendorName;
        this.vendorType = vendorType;
        this.vendorAddress = vendorAddress;
        this.hubId = hubId;
    }
}
