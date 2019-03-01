package com.vmware.bifrost.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vmware.bifrost.bridge.spring.BifrostEnabled;
import com.vmware.bifrost.bridge.spring.BifrostService;
import com.vmware.bifrost.bus.EventBus;
import com.vmware.bifrost.core.util.Loggable;
import com.vmware.bifrost.core.util.ServiceMethodLookupUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ResourceLoader;

/*
 * Copyright(c) VMware Inc. 2019
 */
@BifrostService
public abstract class AbstractBase extends Loggable implements BifrostEnabled {

    @Autowired
    protected EventBus bus;

    @Autowired
    protected ApplicationContext context;

    @Autowired
    protected ResourceLoader resourceLoader;

    @Autowired
    protected ServiceMethodLookupUtil methodLookupUtil;

    protected ObjectMapper mapper = new ObjectMapper();
}
