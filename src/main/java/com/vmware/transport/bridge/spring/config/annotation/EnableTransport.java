/*
 * Copyright 2018 VMware, Inc. All rights reserved.
 */
package com.vmware.transport.bridge.spring.config.annotation;

import com.vmware.transport.bridge.spring.config.TransportSpringConfig;
import org.springframework.context.annotation.Import;

/**
 * Adding this annotation to an {@code @Configuration} class imports the Transport message bus
 * components, e.g.:
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableTransport
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
@Import(TransportSpringConfig.class)
public @interface EnableTransport {

}