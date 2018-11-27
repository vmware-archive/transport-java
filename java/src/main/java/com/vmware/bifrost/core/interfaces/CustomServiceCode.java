package com.vmware.bifrost.core.interfaces;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomServiceCode {
    String serviceName() default "";
    String methodName() default "";
}
