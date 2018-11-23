/*
 * Copyright 2018 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.bridge.spring.config.annotation;

import com.vmware.bifrost.bridge.spring.config.BifrostSpringConfig;
import org.springframework.context.annotation.Import;

/**
 * Adding this annotation to an {@code @Configuration} class imports the Bifrost message bus
 * components, e.g.:
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableBifrost
 * public class MyAppConfiguration {
 *
 * }
 * </pre>
 */
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(BifrostSpringConfig.class)
public @interface EnableBifrost {

}