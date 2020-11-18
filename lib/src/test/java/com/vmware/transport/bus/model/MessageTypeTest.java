/*
 * Copyright 2017-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.bus.model;

import org.junit.Assert;
import org.junit.Test;

public class MessageTypeTest {

    @Test
    public void testModel() {
        Assert.assertEquals(3, MessageType.values().length);
        Assert.assertEquals(MessageType.valueOf("MessageTypeError"), MessageType.MessageTypeError);
        Assert.assertEquals(MessageType.valueOf("MessageTypeResponse"), MessageType.MessageTypeResponse);
        Assert.assertEquals(MessageType.valueOf("MessageTypeRequest"), MessageType.MessageTypeRequest);

    }

}