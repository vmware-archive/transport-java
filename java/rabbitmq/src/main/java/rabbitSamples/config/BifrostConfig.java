/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package rabbitSamples.config;

import com.vmware.bifrost.bridge.spring.config.BifrostBridgeConfigurer;
import com.vmware.bifrost.broker.rabbitmq.RabbitMessageBrokerConnector;
import com.vmware.bifrost.bus.EventBus;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BifrostConfig implements BifrostBridgeConfigurer {

   @Autowired
   EventBus eventBus;

   private ConnectionFactory rbMqConnectionFactory;
   private ConnectionFactory logRbMqConnectionFactory;

   @Override
   public void configureGalacticChannels() {

      rbMqConnectionFactory = createConnectionFactory("/");
      logRbMqConnectionFactory = createConnectionFactory("log");

      RabbitMessageBrokerConnector rbBrokerConnector =
            new RabbitMessageBrokerConnector(rbMqConnectionFactory);

      eventBus.registerMessageBroker(rbBrokerConnector);
      eventBus.markChannelAsGalactic(GalacticChannels.REQUEST_CHANNEL,
            rbBrokerConnector.newGalacticChannelConfig("request-q", "request-q"));
      eventBus.markChannelAsGalactic(GalacticChannels.RESPONSE_CHANNEL,
            rbBrokerConnector.newGalacticChannelConfig("response-q", "response-q"));

      RabbitMessageBrokerConnector logRbBrokerConnector =
            new RabbitMessageBrokerConnector(logRbMqConnectionFactory, new Jackson2JsonMessageConverter());
      eventBus.registerMessageBroker(logRbBrokerConnector);
      eventBus.markChannelAsGalactic(GalacticChannels.REQUEST_LOG_CHANNEL,
            logRbBrokerConnector.newGalacticChannelConfig("request-log-q", "request-log-q"));
   }

   private ConnectionFactory createConnectionFactory(String virtualHost) {
      CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
      // A local RabbitMQ broker can be started by running "docker-compose up" command.
      connectionFactory.setHost("localhost");
      connectionFactory.setUsername("admin");
      connectionFactory.setPassword("vmware");
      connectionFactory.setVirtualHost(virtualHost);
      connectionFactory.setPublisherConfirms(true);
      return connectionFactory;
   }
}
