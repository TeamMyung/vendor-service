package com.sparta.vendorservice.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAQuery;
import com.sparta.vendorservice.domain.QVendor;
import com.sparta.vendorservice.domain.Vendor;
import com.sparta.vendorservice.dto.common.SearchParam;
import com.sparta.vendorservice.dto.response.GetVendorDetailResDto;
import com.sparta.vendorservice.dto.response.GetVendorPageResDto;
import com.sparta.vendorservice.dto.response.QGetVendorDetailResDto;
import com.sparta.vendorservice.dto.response.QGetVendorPageResDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.*;

@RequiredArgsConstructor
public class CustomVendorRepositoryImpl implements CustomVendorRepository {
    // todo : 타 서비스와 연결 처리

    private final JPAQueryFactory query;

    private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
            "createdAt", "updatedAt", "vendorName", "vendorAddress", "vendorType", "hubId");

    QVendor qVendor = QVendor.vendor;

    @Override
    public Page<GetVendorPageResDto> findVendorPage(SearchParam searchParam, Pageable pageable, String role) {
        int pageSize = pageable.getPageSize();
        List<Integer> allowedPageSizes = Arrays.asList(10, 30, 50);
        if (!allowedPageSizes.contains(pageSize)) {
            pageSize = 10;
        }

        Pageable adjustedPageable = PageRequest.of(pageable.getPageNumber(), pageSize, pageable.getSort());

        JPAQuery<GetVendorPageResDto> jpaQuery = query.select(getVendorProjection())
                .from(qVendor)
                .where(whereExpression(searchParam, role))
                .offset(adjustedPageable.getOffset())
                .limit(adjustedPageable.getPageSize());

        if (adjustedPageable.getSort().isSorted()) {
            for (Sort.Order order : adjustedPageable.getSort()) {
                if (!ALLOWED_SORT_PROPERTIES.contains(order.getProperty())) {
                    continue;
                }
                PathBuilder<Vendor> entityPath = new PathBuilder<>(Vendor.class, "vendor");
                jpaQuery.orderBy(new OrderSpecifier(
                        order.isAscending() ? Order.ASC : Order.DESC,
                        entityPath.get(order.getProperty())
                ));
            }
        } else {
            jpaQuery.orderBy(qVendor.createdAt.asc(), qVendor.updatedAt.asc());
        }

        JPAQuery<Long> cnt = query
                .select(qVendor.count())
                .from(qVendor)
                .where(whereExpression(searchParam, role));

        List<GetVendorPageResDto> results = jpaQuery.fetch();

        return PageableExecutionUtils.getPage(results, adjustedPageable, cnt::fetchOne);
    }

    @Override
    public Optional<GetVendorDetailResDto> findVendorDetail(UUID vendorId, String role) {
        GetVendorDetailResDto response = query
                .select(getVendorDetailProjection())
                .from(qVendor)
                .where(
                        "MASTER".equals(role)
                                ? qVendor.vendorId.eq(vendorId)
                                : qVendor.vendorId.eq(vendorId).and(qVendor.deletedAt.isNull())
                )
                .fetchOne();

        return Optional.ofNullable(response);
    }

    @Override
    public boolean existsByVendorName(String vendorName) {
        Long exist = query.select(qVendor.count())
                .from(qVendor)
                .where(qVendor.vendorName.eq(vendorName), qVendor.deletedAt.isNull())
                .fetchOne();
        return exist != null && exist > 0;
    }

    private QGetVendorPageResDto getVendorProjection() {
        return new QGetVendorPageResDto(
                qVendor.vendorId,
                qVendor.vendorName,
                qVendor.vendorType,
                qVendor.vendorAddress,
                Expressions.nullExpression() // 허브명 서비스에서 받아오기
        );
    }

    private QGetVendorDetailResDto getVendorDetailProjection() {
        return new QGetVendorDetailResDto(
                qVendor.vendorId,
                qVendor.vendorName,
                qVendor.vendorType,
                qVendor.vendorAddress,
                Expressions.nullExpression(), // 허브명 서비스에서 받아오기
                Expressions.constant(1L), // 수정 예정
                qVendor.createdAt,
                qVendor.updatedAt
        );
    }

    private BooleanBuilder whereExpression(SearchParam searchParam, String role) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if (!"MASTER".equals(role)) {
            // 마스터는 삭제된 것도 조회 가능
            booleanBuilder.and(qVendor.deletedAt.isNull());
        }

        // 검색 조건
        if (searchParam.getTerm() != null) {
            booleanBuilder.and(
                    qVendor.vendorName.containsIgnoreCase(searchParam.getTerm())
                            .or(qVendor.vendorAddress.containsIgnoreCase(searchParam.getTerm()))
            );
        }

        if (searchParam.getVendorType() != null) {
            booleanBuilder.and(qVendor.vendorType.eq(searchParam.getVendorType()));
        }

        if (searchParam.getHubId() != null) {
            booleanBuilder.and(qVendor.hubId.eq(searchParam.getHubId()));
        }

        return booleanBuilder;

    }
}
