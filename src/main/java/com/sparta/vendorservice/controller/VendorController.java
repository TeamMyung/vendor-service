package com.sparta.vendorservice.controller;

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
    public ResponseEntity<ApiResponse<CreateVendorResDto>> createVendor(@RequestBody CreateVendorReqDto request) {
        CreateVendorResDto response = vendorService.createVendor(request);
        return ResponseEntity.ok(new ApiResponse<>(response));
    }

    // 업체 수정
    @PutMapping("/{vendorId}")
    public ResponseEntity<ApiResponse<UpdateVendorResDto>> updateVendor(
            @PathVariable UUID vendorId,
            @RequestBody UpdateVendorReqDto request
    ) {
        UpdateVendorResDto response = vendorService.updateVendor(vendorId, request);
        return ResponseEntity.ok(new ApiResponse<>(response));
    }

    // 업체 삭제
    @DeleteMapping
    public ResponseEntity<ApiResponse<DeleteVendorResDto>> deleteVendor(
            @RequestBody DeleteVendorReqDto request
    ) {
        DeleteVendorResDto response = vendorService.deleteVendor(request);
        return ResponseEntity.ok(new ApiResponse<>(response));
    }

    // 업체 리스트 조회
    @GetMapping
    public ResponseEntity<ApiResponse<Page<GetVendorPageResDto>>> getVendorPage(
            @RequestParam(required = false) SearchParam searchParam,
            @PageableDefault(size = 10, sort = { "createdAt", "updatedAt" }, direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam String role
    ) {
        Page<GetVendorPageResDto> response = vendorService.getVendorPage(searchParam, pageable, role);
        return ResponseEntity.ok(new ApiResponse<>(response));
    }

    // 업체 상세 조회
    @GetMapping("/{vendorId}")
    public ResponseEntity<ApiResponse<GetVendorDetailResDto>> getVendorDetail(@PathVariable UUID vendorId) {
        GetVendorDetailResDto response = vendorService.getVendorDetail(vendorId);
        return ResponseEntity.ok(new ApiResponse<>(response));
    }

}
