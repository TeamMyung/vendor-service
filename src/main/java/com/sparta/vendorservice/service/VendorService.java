package com.sparta.vendorservice.service;

import com.sparta.vendorservice.domain.Vendor;
import com.sparta.vendorservice.dto.request.CreateVendorReqDto;
import com.sparta.vendorservice.dto.request.DeleteVendorReqDto;
import com.sparta.vendorservice.dto.request.UpdateVendorReqDto;
import com.sparta.vendorservice.dto.response.*;
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
    // todo : 타 서비스와 연결 처리(유저, 허브)

    // 업체 전체 조회
    @Transactional(readOnly = true)
    public Page<GetVendorPageResDto> getVendorPage(String searchParam, Pageable pageable) {
        return vendorRepository.findVendorPage(searchParam, pageable);
    }

    // 업체 상세 조회
    @Transactional(readOnly = true)
    public GetVendorDetailResDto getVendorDetail(UUID VendorId) {
        return vendorRepository.findVendorDetail(VendorId)
                .orElseThrow(() -> new VendorException(ErrorCode.VENDOR_NOT_FOUND));
    }

    // 업체 생성
    @Transactional
    public CreateVendorResDto createVendor(CreateVendorReqDto request) {

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
