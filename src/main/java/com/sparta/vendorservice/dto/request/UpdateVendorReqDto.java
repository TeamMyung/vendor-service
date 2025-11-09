package com.sparta.vendorservice.dto.request;

import com.sparta.vendorservice.domain.VendorType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class UpdateVendorReqDto {
    private VendorType vendorType;
    private String vendorAddress;
    private Long userId;
}
