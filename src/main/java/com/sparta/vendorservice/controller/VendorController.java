package com.sparta.vendorservice.controller;

import com.sparta.vendorservice.domain.VendorType;
import com.sparta.vendorservice.dto.common.SearchParam;
import com.sparta.vendorservice.dto.request.CreateVendorReqDto;
import com.sparta.vendorservice.dto.request.DeleteVendorReqDto;
import com.sparta.vendorservice.dto.request.UpdateVendorReqDto;
import com.sparta.vendorservice.dto.response.*;
import com.sparta.vendorservice.global.response.ApiResponse;
import com.sparta.vendorservice.service.VendorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/vendors")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;

    // 업체 추가
    @PostMapping
    public ResponseEntity<ApiResponse<CreateVendorResDto>> createVendor(@RequestBody CreateVendorReqDto request,
                                                                        @RequestHeader(value = "role") String role,
                                                                        @RequestHeader(value = "user_id") String userIdHeader,
                                                                        @RequestHeader(value = "hub_id", required = false)String hubIdHeader,
                                                                        @RequestHeader(value = "vendor_id", required = false) String vendorIdHeader) {
        Long userId = Long.parseLong(userIdHeader);
        // 허브/벤더 헤더가 존재하면 UUID로 변환, 없으면 null
        UUID hubId = (hubIdHeader != null && !hubIdHeader.isBlank()) ? UUID.fromString(hubIdHeader) : null;
        UUID vendorId = (vendorIdHeader != null && !vendorIdHeader.isBlank()) ? UUID.fromString(vendorIdHeader) : null;
        CreateVendorResDto response = vendorService.createVendor(request, role, userId, hubId, vendorId);
        return ResponseEntity.ok(new ApiResponse<>(response));
    }

    // 업체 수정
    @PutMapping("/{vendorId}")
    public ResponseEntity<ApiResponse<UpdateVendorResDto>> updateVendor(
            @PathVariable UUID vendorId,
            @RequestBody UpdateVendorReqDto request,
            @RequestHeader(value = "role") String role,
            @RequestHeader(value = "user_id") String userIdHeader,
            @RequestHeader(value = "hub_id", required = false)String hubIdHeader,
            @RequestHeader(value = "vendor_id", required = false) String vendorIdHeader
    ) {
        Long userId = Long.parseLong(userIdHeader);
        // 허브/벤더 헤더가 존재하면 UUID로 변환, 없으면 null
        UUID hubId = (hubIdHeader != null && !hubIdHeader.isBlank()) ? UUID.fromString(hubIdHeader) : null;
        UUID vendorIdHd = (vendorIdHeader != null && !vendorIdHeader.isBlank()) ? UUID.fromString(vendorIdHeader) : null;
        UpdateVendorResDto response = vendorService.updateVendor(vendorId, request, role, userId, hubId, vendorIdHd);
        return ResponseEntity.ok(new ApiResponse<>(response));
    }

    // 업체 삭제
    @DeleteMapping
    public ResponseEntity<ApiResponse<DeleteVendorResDto>> deleteVendor(
            @RequestBody DeleteVendorReqDto request,
            @RequestHeader(value = "role") String role,
            @RequestHeader(value = "user_id") String userIdHeader,
            @RequestHeader(value = "hub_id", required = false)String hubIdHeader,
            @RequestHeader(value = "vendor_id", required = false) String vendorIdHeader
    ) {
        Long userId = Long.parseLong(userIdHeader);
        // 허브/벤더 헤더가 존재하면 UUID로 변환, 없으면 null
        UUID hubId = (hubIdHeader != null && !hubIdHeader.isBlank()) ? UUID.fromString(hubIdHeader) : null;
        UUID vendorId = (vendorIdHeader != null && !vendorIdHeader.isBlank()) ? UUID.fromString(vendorIdHeader) : null;
        DeleteVendorResDto response = vendorService.deleteVendor(request, role, userId, hubId, vendorId);
        return ResponseEntity.ok(new ApiResponse<>(response));
    }

    // 업체 리스트 조회
    @GetMapping
    public ResponseEntity<ApiResponse<Page<GetVendorPageResDto>>> getVendorPage(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID hubId,
            @RequestParam(required = false) VendorType vendorType,
            @PageableDefault(size = 10, sort = { "createdAt", "updatedAt" }, direction = Sort.Direction.ASC) Pageable pageable,
            @RequestHeader(value = "role") String role)
     {
         SearchParam searchParam = new SearchParam(search, hubId, vendorType);
        Page<GetVendorPageResDto> response = vendorService.getVendorPage(searchParam, pageable, role); // 임시
        return ResponseEntity.ok(new ApiResponse<>(response));
    }

    // 업체 상세 조회
    @GetMapping("/{vendorId}")
    public ResponseEntity<ApiResponse<GetVendorDetailResDto>> getVendorDetail(@PathVariable UUID vendorId,
                                                                              @RequestHeader(value = "role") String role) {
        GetVendorDetailResDto response = vendorService.getVendorDetail(vendorId, role); // 임시
        return ResponseEntity.ok(new ApiResponse<>(response));
    }

}
