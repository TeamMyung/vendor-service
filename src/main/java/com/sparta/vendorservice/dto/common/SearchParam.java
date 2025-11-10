package com.sparta.vendorservice.dto.common;

import com.sparta.vendorservice.domain.VendorType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SearchParam {
    private String term;
    private UUID hubId;
    private VendorType vendorType;
}
