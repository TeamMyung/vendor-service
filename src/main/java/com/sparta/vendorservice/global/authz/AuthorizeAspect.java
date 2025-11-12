package com.sparta.vendorservice.global.authz;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import static com.sparta.vendorservice.global.authz.Action.*;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@Aspect
@Component
@RequiredArgsConstructor
public class AuthorizeAspect {

    private final HttpServletRequest request;

    private static final String ROLE_MASTER = "MASTER";
    private static final String ROLE_HUB_MANAGER = "HUB_MANAGER";
    private static final String ROLE_VENDOR_MANAGER = "VENDOR_MANAGER";
    private static final String ROLE_DELIVERY_MANAGER = "DELIVERY_MANAGER";

    @Autowired(required = false)
    private final ExpressionParser parser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(ann)")
    public Object around(ProceedingJoinPoint pjp, Authorize ann) throws Throwable {
        // 헤더로 들어온 게이트웨이 토큰에서 데이터(문자열) 추출
        String role = getHeader("role");
        String userId = getHeader("user_id");
        String hubId = getHeader("hub_id");
        String vendId = getHeader("vendor_id");

        // SpEL로 식별자 추출
        var context = new StandardEvaluationContext();
        var method = ((MethodSignature) pjp.getSignature()).getMethod();
        var specific = org.springframework.aop.support.AopUtils.getMostSpecificMethod(method, pjp.getTarget().getClass());
        var names = discoverer.getParameterNames(specific);

        Object[] args = pjp.getArgs();
        if (names != null) for (int i = 0; i < names.length; i++) context.setVariable(names[i], args[i]);

        String targetHubId = eval(ann.targetHubId(), context);
        String targetVendorId = eval(ann.targetVendorId(), context);
        String targetUserId = eval(ann.targetUserId(), context);

        if (ROLE_MASTER.equals(role)) return pjp.proceed();

        // 도메인 별 권한
        boolean allow = switch (ann.resource()) {
            case HUB -> hubPolicy(ann.action(), role);
            case HUB_PATH -> hubPathPolicy(ann.action(), role);
            case VENDOR -> vendorPolicy(ann.action(), role, hubId, vendId, targetHubId, targetVendorId);
            case PRODUCT -> productPolicy(ann.action(), role, hubId, vendId, targetHubId, targetVendorId);
            case ORDER -> orderPolicy(ann.action(), role, hubId, userId, targetHubId, targetUserId);
            case DELIVERY -> deliveryPolicy(ann.action(), role, hubId, userId, targetHubId, targetUserId);
            case DELIVERY_MANAGER ->
                    deliveryManagerPolicy(ann.action(), role, hubId, userId, targetHubId, targetUserId);
            case SLACK -> slackPolicy(ann.action(), role);
            case USER -> userPolicy(ann.action(), role, userId, targetUserId);
        };

        if (!allow) throw new ResponseStatusException(FORBIDDEN, "권한 없음");
        return pjp.proceed();
    }

    // ============================ 유틸 메서드 (도메인 별 권한) ============================

    /**
     * 허브 관리
     * MASTER => CREATE, READ, UPDATE, DELETE
     * HUB_MANAGER, VENDOR_MANAGER, DELIVERY_MANAGER => READ
     */
    private boolean hubPolicy(Action action, String role) {
        if (ROLE_HUB_MANAGER.equals(role) || ROLE_VENDOR_MANAGER.equals(role) || ROLE_DELIVERY_MANAGER.equals(role))
            return action == READ;
        return false;
    }

    /**
     * 허브 간 이동 정보 관리
     * MASTER => CREATE, READ, UPDATE, DELETE
     * HUB_MANAGER, VENDOR_MANAGER, DELIVERY_MANAGER => READ
     */
    private boolean hubPathPolicy(Action action, String role) {
        if (ROLE_HUB_MANAGER.equals(role) || ROLE_VENDOR_MANAGER.equals(role) || ROLE_DELIVERY_MANAGER.equals(role))
            return action == READ;
        return false;
    }

    /**
     * 업체 관리
     * MASTER => CREATE, READ, UPDATE, DELETE
     * HUB_MANAGER => CREATE, READ, UPDATE, DELETE (하위 업체)
     * VENDOR_MANAGER => READ / UPDATE (소속 업체)
     * DELIVERY_MANAGER => READ
     */
    private boolean vendorPolicy(
            Action action, String role, String hubId, String vendorId,
            String targetHubId, String targetVendorId
    ) {
        if (ROLE_HUB_MANAGER.equals(role)) {
            return (action == CREATE || action == READ || action == UPDATE || action == DELETE)
                    && equals(hubId, targetHubId);
        }
        if (ROLE_VENDOR_MANAGER.equals(role)) {
            if (action == UPDATE) return equals(vendorId, targetVendorId);
            return action == READ;
        }
        if (ROLE_DELIVERY_MANAGER.equals(role)) return action == READ;
        return false;
    }

    /**
     * 상품 관리
     * MASTER => CREATE, READ, UPDATE, DELETE
     * HUB_MANAGER -> CREATE, READ, UPDATE, DELETE (소속 허브)
     * VENDOR_MANAGER -> READ / CREATE, UPDATE (소속 업체)
     * DELIVERY_MANAGER -> READ
     */
    private boolean productPolicy(
            Action action, String role, String hubId, String vendorId,
            String targetHubId, String targetVendId
    ) {
        if (ROLE_HUB_MANAGER.equals(role)) {
            return (action == CREATE || action == READ || action == UPDATE || action == DELETE)
                    && equals(hubId, targetHubId);
        }
        if (ROLE_VENDOR_MANAGER.equals(role)) {
            if (action == CREATE || action == UPDATE) return equals(vendorId, targetVendId);
            return action == READ;
        }
        if (ROLE_DELIVERY_MANAGER.equals(role)) return action == READ;
        return false;
    }

    /**
     * 주문 관리
     * MASTER => CREATE, READ, UPDATE, DELETE
     * HUB_MANAGER -> CREATE / READ, UPDATE, DELETE (소속 허브)
     * VENDOR_MANAGER -> CREATE, READ (본인 주문)
     * DELIVERY_MANAGER -> CREATE, READ (본인 주문)
     */
    private boolean orderPolicy(
            Action action, String role, String hubId, String userId,
            String targetHubId, String targetUserId
    ) {
        if (ROLE_HUB_MANAGER.equals(role)) {
            if (action == CREATE) return true;
            if (action == READ || action == UPDATE || action == DELETE) return equals(hubId, targetHubId);
            return false;
        }
        if (ROLE_VENDOR_MANAGER.equals(role) || ROLE_DELIVERY_MANAGER.equals(role)) {
            if (action == CREATE) return true;
            if (action == READ) return equals(userId, targetUserId);
            return false;
        }
        return false;
    }

    /**
     * 배송 관리
     * MASTER -> 전체 권한
     * HUB_MANAGER -> READ, UPDATE, DELETE (소속 허브)
     * VENDOR_MANAGER -> READ
     * DELIVERY_MANAGER -> READ, UPDATE (본인 배송)
     */
    private boolean deliveryPolicy(
            Action action, String role, String hubId, String userId,
            String targetHubId, String targetUserId
    ) {
        if (ROLE_HUB_MANAGER.equals(role)) {
            return (action == READ || action == UPDATE || action == DELETE) && equals(hubId, targetHubId);
        }
        if (ROLE_VENDOR_MANAGER.equals(role)) return action == READ;
        if (ROLE_DELIVERY_MANAGER.equals(role)) {
            if (action == UPDATE) return equals(userId, targetUserId);
            return action == READ;
        }
        return false;
    }

    /**
     * 배송 담당자 관리
     * MASTER => CREATE, READ, UPDATE, DELETE
     * HUB_MANAGER => CREATE, READ, UPDATE, DELETE (소속 허브)
     * - 단, HUB_TO_HUB의 경우 물류 센터 소속이기 때문에 관리 불가능
     * DELIVERY_MANAGER => READ (본인 정보)
     */
    private boolean deliveryManagerPolicy(
            Action action, String role, String hubId, String userId,
            String targetHubId, String targetUserId
    ) {
        if (ROLE_HUB_MANAGER.equals(role)) {
            return (action == CREATE || action == READ || action == UPDATE || action == DELETE)
                    && equals(hubId, targetHubId);
        }
        if (ROLE_DELIVERY_MANAGER.equals(role)) {
            return action == READ && equals(userId, targetUserId);
        }
        return false;
    }

    /**
     * 슬랙 메시지
     * MASTER -> 전체 권한
     * HUB_MANAGER, VENDOR_MANAGER, DELIVERY_MANAGER -> CREATE
     */
    private boolean slackPolicy(Action action, String role) {
        if (ROLE_HUB_MANAGER.equals(role) || ROLE_VENDOR_MANAGER.equals(role) || ROLE_DELIVERY_MANAGER.equals(role))
            return action == CREATE;
        return false;
    }

    /**
     * 회원 관리
     * MASTER => CREATE, READ, UPDATE, DELETE
     * HUB_MANAGER, VENDOR_MANAGER, DELIVERY_MANAGER => READ (본인 정보)
     */
    private boolean userPolicy(Action action, String role, String userId, String targetUserId) {
        if (ROLE_HUB_MANAGER.equals(role) || ROLE_VENDOR_MANAGER.equals(role) || ROLE_DELIVERY_MANAGER.equals(role))
            return action == READ && equals(userId, targetUserId);
        return false;
    }

    // ============================ 유틸 메서드 (헬퍼) ============================
    private String getHeader(String name) {
        String header = request.getHeader(name);
        return header == null ? null : header.trim();
    }

    private static boolean equals(String a, String b) {
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    private String eval(String spel, StandardEvaluationContext context) {
        if (!StringUtils.hasText(spel)) return null;
        Object v = parser.parseExpression(spel).getValue(context);
        return v == null ? null : String.valueOf(v);
    }
}
