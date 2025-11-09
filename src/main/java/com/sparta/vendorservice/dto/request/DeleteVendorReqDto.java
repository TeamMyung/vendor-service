package com.sparta.vendorservice.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class DeleteVendorReqDto {
    private List<UUID> vendorIds;
}
