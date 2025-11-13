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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/vendors")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;

    // 업체 추가
    @PostMapping
    public ResponseEntity<ApiResponse<CreateVendorResDto>> createVendor(@RequestBody CreateVendorReqDto request,
                                                                        @RequestHeader(value = "x-role") String role,
                                                                        @RequestHeader(value = "x-userid") String userIdHeader) {
        Long userId = Long.parseLong(userIdHeader);
        UUID hubId = getHubIdFromAuth();
        UUID vendorId = getVendorIdFromAuth();
        System.out.println(hubId);
        System.out.println(vendorId);
        CreateVendorResDto response = vendorService.createVendor(request, role, userId, hubId, vendorId);
        return ResponseEntity.ok(new ApiResponse<>(response));
    }

    // 업체 수정
    @PutMapping("/{vendorId}")
    public ResponseEntity<ApiResponse<UpdateVendorResDto>> updateVendor(
            @PathVariable UUID vendorId,
            @RequestBody UpdateVendorReqDto request,
            @RequestHeader(value = "x-role") String role,
            @RequestHeader(value = "x-userid") String userIdHeader) {
        Long userId = Long.parseLong(userIdHeader);
        UUID hubId = getHubIdFromAuth();
        UUID vendorIdHd = getVendorIdFromAuth();
        UpdateVendorResDto response = vendorService.updateVendor(vendorId, request, role, userId, hubId, vendorIdHd);
        return ResponseEntity.ok(new ApiResponse<>(response));
    }

    // 업체 삭제
    @DeleteMapping
    public ResponseEntity<ApiResponse<DeleteVendorResDto>> deleteVendor(
            @RequestBody DeleteVendorReqDto request,
            @RequestHeader(value = "x-role") String role,
            @RequestHeader(value = "x-userid") String userIdHeader) {
        Long userId = Long.parseLong(userIdHeader);
        UUID hubId = getHubIdFromAuth();
        UUID vendorIdHd = getVendorIdFromAuth();
        DeleteVendorResDto response = vendorService.deleteVendor(request, role, userId, hubId, vendorIdHd);
        return ResponseEntity.ok(new ApiResponse<>(response));
    }

    // 업체 리스트 조회
    @GetMapping
    public ResponseEntity<ApiResponse<Page<GetVendorPageResDto>>> getVendorPage(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID hubId,
            @RequestParam(required = false) VendorType vendorType,
            @PageableDefault(size = 10, sort = { "createdAt", "updatedAt" }, direction = Sort.Direction.ASC) Pageable pageable,
            @RequestHeader(value = "x-role") String role)
     {
         SearchParam searchParam = new SearchParam(search, hubId, vendorType);
        Page<GetVendorPageResDto> response = vendorService.getVendorPage(searchParam, pageable, role); // 임시
        return ResponseEntity.ok(new ApiResponse<>(response));
    }

    // 업체 상세 조회
    @GetMapping("/{vendorId}")
    public ResponseEntity<ApiResponse<GetVendorDetailResDto>> getVendorDetail(@PathVariable UUID vendorId,
                                                                              @RequestHeader(value = "x-role") String role) {
        GetVendorDetailResDto response = vendorService.getVendorDetail(vendorId, role); // 임시
        return ResponseEntity.ok(new ApiResponse<>(response));
    }

    private UUID getHubIdFromAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;

        Object principal = auth.getPrincipal();
        if (principal instanceof Map<?, ?> map) {
            return (UUID) map.get("hubId");
        }
        return null;
    }

    private UUID getVendorIdFromAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;

        Object principal = auth.getPrincipal();
        if (principal instanceof Map<?, ?> map) {
            return (UUID) map.get("vendorId");
        }
        return null;
    }




}
