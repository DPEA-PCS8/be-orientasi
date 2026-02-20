package com.pcs8.orientasi.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify required roles for accessing an endpoint.
 * Can be applied to methods or classes.
 * If applied to a class, all methods in that class will require the specified roles.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresRole {

    /**
     * The role names required to access this endpoint.
     * User must have at least one of these roles.
     */
    String[] value();

    /**
     * If true, user must have ALL specified roles.
     * If false (default), user needs only ONE of the specified roles.
     */
    boolean requireAll() default false;
}
