package com.pcs8.orientasi.config.annotation;

import com.pcs8.orientasi.domain.enums.AuditAction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation untuk menandai method yang perlu di-audit.
 * 
 * <p>Gunakan annotation ini pada method service untuk secara otomatis
 * mencatat audit log ketika method tersebut dipanggil.</p>
 * 
 * <h3>Contoh Penggunaan:</h3>
 * <pre>{@code
 * @Auditable(entity = "Aplikasi", action = AuditAction.CREATE)
 * public AplikasiResponse create(AplikasiRequest request) {
 *     // implementation
 * }
 * 
 * @Auditable(entity = "Aplikasi", action = AuditAction.UPDATE, captureOldValue = true)
 * public AplikasiResponse update(UUID id, AplikasiRequest request) {
 *     // implementation
 * }
 * 
 * @Auditable(entity = "Aplikasi", action = AuditAction.DELETE, captureOldValue = true)
 * public void delete(UUID id) {
 *     // implementation
 * }
 * }</pre>
 * 
 * <h3>Cara Kerja:</h3>
 * <ol>
 *   <li>AOP Aspect akan intercept method yang ditandai dengan annotation ini</li>
 *   <li>Jika captureOldValue = true, data sebelum operasi akan di-capture</li>
 *   <li>Method akan dieksekusi</li>
 *   <li>Setelah method selesai, audit log akan dicatat secara async</li>
 * </ol>
 * 
 * @see com.pcs8.orientasi.config.AuditAspect
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    /**
     * Nama entity yang di-audit.
     * Contoh: "Aplikasi", "Bidang", "User", "Role"
     */
    String entity();

    /**
     * Jenis aksi yang dilakukan.
     */
    AuditAction action();

    /**
     * Apakah perlu capture old value sebelum operasi.
     * Set true untuk UPDATE dan DELETE.
     * Default: false
     */
    boolean captureOldValue() default false;

    /**
     * Index parameter yang berisi entity ID (0-based).
     * Digunakan untuk mengambil ID dari method parameter.
     * Default: 0 (parameter pertama)
     */
    int idParamIndex() default 0;

    /**
     * Nama method repository untuk fetch old value.
     * Digunakan jika captureOldValue = true.
     * Default: "findById"
     */
    String findMethod() default "findById";
}
