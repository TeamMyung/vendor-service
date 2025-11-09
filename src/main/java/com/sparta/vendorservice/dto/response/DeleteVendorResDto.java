package com.sparta.vendorservice.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class DeleteVendorResDto {
    private List<UUID> vendorIds;
    private String message;
}
