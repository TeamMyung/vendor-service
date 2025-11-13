package com.sparta.vendorservice.service;

import com.sparta.vendorservice.domain.Vendor;
import com.sparta.vendorservice.dto.common.SearchParam;
import com.sparta.vendorservice.dto.request.CreateVendorReqDto;
import com.sparta.vendorservice.dto.request.DeleteVendorReqDto;
import com.sparta.vendorservice.dto.request.UpdateVendorReqDto;
import com.sparta.vendorservice.dto.response.*;
import com.sparta.vendorservice.global.authz.Action;
import com.sparta.vendorservice.global.authz.Authorize;
import com.sparta.vendorservice.global.authz.Resource;
import com.sparta.vendorservice.global.client.HubClient;
import com.sparta.vendorservice.global.exception.ErrorCode;
import com.sparta.vendorservice.global.exception.VendorException;
import com.sparta.vendorservice.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class VendorService {
    private final VendorRepository vendorRepository;
    private final HubClient hubClient;

    // 업체 전체 조회
    @Transactional(readOnly = true)
    //@Authorize(resource = Resource.VENDOR, action = Action.READ)
    public Page<GetVendorPageResDto> getVendorPage(SearchParam searchParam, Pageable pageable, String role) {
        Page<GetVendorPageResDto> page = vendorRepository.findVendorPage(searchParam, pageable, role);

        // 허브 이름 매핑
        page.getContent().forEach(vendor -> {
            UUID hubId = vendorRepository.findHubIdByVendorId(vendor.getVendorId()).orElse(null);
            String hubName = (hubId != null) ? hubClient.getHubName(hubId) : null;
            vendor.setHubName(hubName);
        });

        return page;
    }

    // 업체 상세 조회
    @Transactional(readOnly = true)
    //@Authorize(resource = Resource.VENDOR, action = Action.READ, targetVendorId = "#vendorId")
    public GetVendorDetailResDto getVendorDetail(UUID vendorId, String role) {
        GetVendorDetailResDto vendorDetail = vendorRepository.findVendorDetail(vendorId, role)
                .orElseThrow(() -> new VendorException(ErrorCode.VENDOR_NOT_FOUND));

        // vendorId로 DB에서 hubId 조회 (없으면 null)
        UUID hubId = vendorRepository.findHubIdByVendorId(vendorId).orElse(null);

        // hubClient 호출 (hubId가 null이면 hubName도 null)
        String hubName = (hubId != null) ? hubClient.getHubName(hubId) : null;
        vendorDetail.setHubName(hubName);

        return vendorDetail;
    }

    // 업체 생성
    @Transactional
    //@Authorize(resource = Resource.VENDOR, action = Action.CREATE, targetHubId = "#request.hubId")
    public CreateVendorResDto createVendor(CreateVendorReqDto request, String role, Long userId, UUID hubId, UUID vendorId) {

        // 허브 존재 여부 확인
        if (!hubClient.existsHub(request.getHubId())) {
            throw new VendorException(ErrorCode.HUB_NOT_FOUND);
        }

        if (vendorRepository.existsByVendorName(request.getVendorName())) {
            throw new VendorException(ErrorCode.VENDOR_DUPLICATE_NAME);
        }

        Vendor vendor = Vendor.ofNewVendor(request.getVendorName(),
                request.getVendorType(),
                request.getVendorAddress(),
                request.getHubId());

        if (!hasPermission(vendor, role, hubId, vendorId, "CREATE")) {
            throw new VendorException(ErrorCode.VENDOR_NOT_AUTH);
        }

        vendorRepository.save(vendor);

        return new CreateVendorResDto(
                vendor.getVendorId(),
                vendor.getVendorName(),
                vendor.getVendorType(),
                vendor.getVendorAddress(),
                vendor.getHubId(),
                userId,
                vendor.getCreatedAt());
    }

    // 업체 수정
    @Transactional
    //@Authorize(resource = Resource.VENDOR, action = Action.UPDATE, targetVendorId = "#vendorId")
    public UpdateVendorResDto updateVendor(UUID vendorId, UpdateVendorReqDto request, String role, Long userId, UUID hubId, UUID vendorIdHeader) {

        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new VendorException(ErrorCode.VENDOR_NOT_FOUND));


        if (!hasPermission(vendor, role, hubId, vendorIdHeader, "UPDATE")) {
            throw new VendorException(ErrorCode.VENDOR_NOT_AUTH);
        }

        vendor.update(request);
        return new UpdateVendorResDto(
                vendor.getVendorId(),
                vendor.getVendorName(),
                vendor.getVendorType(),
                vendor.getVendorAddress(),
                vendor.getHubId(),
                userId,
                vendor.getUpdatedAt());
    }

    // 업체 삭제
    @Transactional
    //@Authorize(resource = Resource.VENDOR, action = Action.DELETE, targetVendorId = "#request.vendorId")
    public DeleteVendorResDto deleteVendor(DeleteVendorReqDto request, String role, Long userId, UUID hubId, UUID vendorIdHeader) {

        List<UUID> requestedIds = request.getVendorIds();
        List<UUID> deletedIds = new ArrayList<>();
        List<UUID> alreadyDeletedIds = new ArrayList<>();

        for (UUID VendorId : requestedIds) {
            vendorRepository.findById(VendorId)
                    .ifPresentOrElse(Vendor -> {
                        if (!hasPermission(Vendor, role, hubId, vendorIdHeader, "DELETE")) {
                            throw new VendorException(ErrorCode.VENDOR_NOT_AUTH);
                        }
                        if (Vendor.getDeletedAt() == null) {
                            Vendor.delete(userId); // 수정 예정
                            vendorRepository.save(Vendor);
                            deletedIds.add(Vendor.getVendorId());

                        } else {
                            alreadyDeletedIds.add(Vendor.getVendorId());
                        }
                    }, () -> {
                        alreadyDeletedIds.add(VendorId);
                    });
        }

        String message;
        if (deletedIds.isEmpty()) {
            message = "삭제할 업체를 찾을 수 없습니다.";
        } else {
            message = deletedIds.size() + "건의 업체가 삭제되었습니다.";
        }

        return DeleteVendorResDto.builder()
                .vendorIds(deletedIds)
                .message(message)
                .build();
    }

    // 권한 체크
    private boolean hasPermission(Vendor vendor, String role, UUID hubId, UUID vendorId, String action) {
        boolean isAdmin = "MASTER".equals(role);
        boolean isVendorManager = "VENDOR_MANAGER".equals(role)
                && Objects.equals(vendor.getVendorId(), vendorId);
        boolean isHubManager = "HUB_MANAGER".equals(role)
                && Objects.equals(vendor.getHubId(), hubId);
        switch (action) {
            case "CREATE":
            case "DELETE":
                // CREATE/DELETE는 MASTER와 자기 허브 HUB_MANAGER만 가능, VENDOR_MANAGER 불가
                return isAdmin || isHubManager;
            case "UPDATE":
                // UPDATE는 MASTER, 자기 허브 HUB_MANAGER, 자기 업체 VENDOR_MANAGER 가능
                return isAdmin || isHubManager || isVendorManager;
            default:
                return false;
        }
    }
}
