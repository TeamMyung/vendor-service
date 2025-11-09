package com.sparta.vendorservice.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sparta.vendorservice.domain.VendorType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class CreateVendorResDto {
    private UUID vendorId;
    private String vendorName;
    private VendorType vendorType;
    private String vendorAddress;
    private UUID hubId;
    private Long userId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public CreateVendorResDto(UUID vendorId, String vendorName, VendorType vendorType, String vendorAddress, UUID hubId, Long userId, LocalDateTime createdAt) {
        this.vendorId = vendorId;
        this.vendorName = vendorName;
        this.vendorType = vendorType;
        this.vendorAddress = vendorAddress;
        this.hubId = hubId;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
    }

}
