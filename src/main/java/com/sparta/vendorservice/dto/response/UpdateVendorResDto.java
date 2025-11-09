package com.sparta.vendorservice.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sparta.vendorservice.domain.VendorType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class UpdateVendorResDto {
    private UUID vendorId;
    private String vendorName;
    private VendorType vendorType;
    private String vendorAddress;
    private UUID hubId;
    private Long userId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    public UpdateVendorResDto(UUID vendorId, String vendorName, VendorType vendorType, String vendorAddress, UUID hubId, Long userId, LocalDateTime updatedAt) {
        this.vendorId = vendorId;
        this.vendorName = vendorName;
        this.vendorType = vendorType;
        this.vendorAddress = vendorAddress;
        this.hubId = hubId;
        this.userId = userId;
        this.updatedAt = LocalDateTime.now();
    }
}
