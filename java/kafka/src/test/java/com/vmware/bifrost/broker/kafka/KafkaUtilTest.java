/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.broker.kafka;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.HashMap;
import java.util.Map;

public class KafkaUtilTest {

   @Test
   public void testWrapMessage() {
      KafkaMessageWrapper wrapper = KafkaUtil.wrapMessage("key", 123);
      Assert.assertEquals(wrapper.getKey(), "key");
      Assert.assertEquals(wrapper.getMessage(), 123);
   }

   @Test
   public void testCreateDefaultKafkaTemplate() {
      Map<String, Object> config = new HashMap<>();
      KafkaTemplate template = KafkaUtil.createDefaultKafkaTemplate(config);
      Assert.assertNotNull(template);
   }
}
