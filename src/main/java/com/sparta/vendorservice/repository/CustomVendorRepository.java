package com.sparta.vendorservice.repository;

import com.sparta.vendorservice.dto.common.SearchParam;
import com.sparta.vendorservice.dto.response.GetVendorDetailResDto;
import com.sparta.vendorservice.dto.response.GetVendorPageResDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface CustomVendorRepository {
    // 검색 조건, 페이지 정보 기반 업체 목록 동적 조회
    Page<GetVendorPageResDto> findVendorPage(SearchParam searchParam, Pageable pageable, String role);

    // 상세 정보 조회
    Optional<GetVendorDetailResDto> findVendorDetail(UUID vendorId, String role);

    // 업체 이름 중복 체크
    boolean existsByVendorName(String vendorName);
}
