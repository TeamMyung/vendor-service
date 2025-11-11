package com.sparta.vendorservice.service;

import com.sparta.vendorservice.domain.Vendor;
import com.sparta.vendorservice.dto.common.SearchParam;
import com.sparta.vendorservice.dto.request.CreateVendorReqDto;
import com.sparta.vendorservice.dto.request.DeleteVendorReqDto;
import com.sparta.vendorservice.dto.request.UpdateVendorReqDto;
import com.sparta.vendorservice.dto.response.*;
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
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class VendorService {
    private final VendorRepository vendorRepository;
    private final HubClient hubClient;

    // todo : 권한별 처리

    // 업체 전체 조회
    @Transactional(readOnly = true)
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
    public CreateVendorResDto createVendor(CreateVendorReqDto request) {

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

        vendorRepository.save(vendor);

        return new CreateVendorResDto(
                vendor.getVendorId(),
                vendor.getVendorName(),
                vendor.getVendorType(),
                vendor.getVendorAddress(),
                vendor.getHubId(),
                request.getUserId(),
                vendor.getCreatedAt());
    }

    // 업체 수정
    @Transactional
    public UpdateVendorResDto updateVendor(UUID vendorId, UpdateVendorReqDto request) {

        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new VendorException(ErrorCode.VENDOR_NOT_FOUND));

        vendor.update(request);
        return new UpdateVendorResDto(
                vendor.getVendorId(),
                vendor.getVendorName(),
                vendor.getVendorType(),
                vendor.getVendorAddress(),
                vendor.getHubId(),
                request.getUserId(),
                vendor.getUpdatedAt());
    }

    // 업체 삭제
    @Transactional
    public DeleteVendorResDto deleteVendor(DeleteVendorReqDto request) {

        List<UUID> requestedIds = request.getVendorIds();
        List<UUID> deletedIds = new ArrayList<>();
        List<UUID> alreadyDeletedIds = new ArrayList<>();

        for (UUID VendorId : requestedIds) {
            vendorRepository.findById(VendorId)
                    .ifPresentOrElse(Vendor -> {
                        if (Vendor.getDeletedAt() == null) {
                            Vendor.delete(1L); // 수정 예정
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





}
