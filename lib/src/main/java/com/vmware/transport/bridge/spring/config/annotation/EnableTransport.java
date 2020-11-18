/*
 * Copyright 2018-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.bridge.spring.config.annotation;

import com.vmware.transport.bridge.spring.config.TransportSpringConfig;
import org.springframework.context.annotation.Import;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(TransportSpringConfig.class)
public @interface EnableTransport {

}