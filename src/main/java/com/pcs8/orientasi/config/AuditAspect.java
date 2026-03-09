package com.pcs8.orientasi.config;

import com.pcs8.orientasi.config.annotation.Auditable;
import com.pcs8.orientasi.domain.enums.AuditAction;
import com.pcs8.orientasi.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * AOP Aspect untuk automatic audit logging.
 * 
 * <p>Aspect ini akan intercept method yang ditandai dengan @Auditable
 * dan secara otomatis mencatat audit log setelah method berhasil dieksekusi.</p>
 * 
 * <h3>Cara Kerja:</h3>
 * <ol>
 *   <li>Method dengan @Auditable akan di-intercept</li>
 *   <li>Setelah method return (success), audit log akan dicatat</li>
 *   <li>Entity ID diambil dari return value atau method parameter</li>
 * </ol>
 * 
 * <h3>Catatan Penting:</h3>
 * <ul>
 *   <li>Untuk UPDATE/DELETE yang perlu old value, gunakan {@link AuditableService} helper</li>
 *   <li>Aspect ini cocok untuk CREATE karena tidak perlu old value</li>
 *   <li>Logging dilakukan secara async untuk tidak memblok main transaction</li>
 * </ul>
 * 
 * @see Auditable
 * @see AuditableService
 */
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    private final AuditService auditService;
    private final UserContext userContext;

    /**
     * Pointcut untuk method dengan @Auditable annotation.
     * Dieksekusi setelah method return successfully.
     */
    @AfterReturning(
            pointcut = "@annotation(auditable)",
            returning = "result"
    )
    public void auditAfterReturning(JoinPoint joinPoint, Auditable auditable, Object result) {
        try {
            // Get user info di main thread
            UUID userId = userContext.getCurrentUserId();
            String username = userContext.getCurrentUsername();
            
            String entityName = auditable.entity();
            AuditAction action = auditable.action();

            UUID entityId = extractEntityId(joinPoint, auditable, result);
            
            if (entityId == null) {
                log.warn("Could not extract entity ID for audit. Entity: {}, Method: {}",
                        entityName, joinPoint.getSignature().getName());
                return;
            }

            switch (action) {
                case CREATE:
                    auditService.logCreate(entityName, entityId, result, userId, username);
                    break;
                case UPDATE:
                    // Untuk UPDATE, kita hanya log new value dari aspect
                    // Old value harus di-handle manual di service atau menggunakan AuditableService
                    auditService.logUpdate(entityName, entityId, null, result, userId, username);
                    break;
                case DELETE:
                    // Untuk DELETE, old value perlu di-capture sebelum delete
                    // Harus di-handle manual di service
                    Object[] args = joinPoint.getArgs();
                    Object oldValue = args.length > 1 ? args[1] : null;
                    auditService.logDelete(entityName, entityId, oldValue, userId, username);
                    break;
            }

            log.debug("Audit logged via aspect: {} {} - {}", entityName, entityId, action);

        } catch (Exception e) {
            log.error("Error in audit aspect: {}", e.getMessage(), e);
            // Don't throw - audit should not break main flow
        }
    }

    /**
     * Extract entity ID dari result atau method parameter.
     */
    private UUID extractEntityId(JoinPoint joinPoint, Auditable auditable, Object result) {
        // Try to get ID from result first (for CREATE/UPDATE)
        if (result != null) {
            UUID idFromResult = extractIdFromObject(result);
            if (idFromResult != null) {
                return idFromResult;
            }
        }

        // Get from method parameter
        Object[] args = joinPoint.getArgs();
        int idParamIndex = auditable.idParamIndex();
        
        if (args.length > idParamIndex) {
            Object idParam = args[idParamIndex];
            if (idParam instanceof UUID) {
                return (UUID) idParam;
            } else if (idParam instanceof String) {
                try {
                    return UUID.fromString((String) idParam);
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }
        }

        return null;
    }

    /**
     * Extract ID field dari object menggunakan reflection.
     */
    private UUID extractIdFromObject(Object obj) {
        try {
            // Try getId() method
            Method getIdMethod = obj.getClass().getMethod("getId");
            Object id = getIdMethod.invoke(obj);
            if (id instanceof UUID) {
                return (UUID) id;
            }
        } catch (Exception e) {
            // No getId method or error - that's okay
        }
        return null;
    }
}
