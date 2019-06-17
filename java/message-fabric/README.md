# MessageFabricBrokerConnector for Bifrost EventBus

The MessageFabric sample app connects to a MessageFabric broker service. 
Before starting the sample app you should make sure that [Broker Service](https://gitlab.eng.vmware.com/atlas/message-fabric/message-broker) works properly, for local installation follow deployment guide in [README.md](https://gitlab.eng.vmware.com/atlas/message-fabric/message-broker/blob/master/README.md)

To build and start the sample app run the **runme.sh** script. After the app boots you can:
- post requests using **http://localhost:8090/rest/post-request?request=test-request**
- get all request log entries using **http://localhost:8090/rest/get-log**

NOTE: Atlas message client uses springboot 2.1.4 while the core Bifrost project uses springboot 1.5.18. If you want to use the MessageFabric Broker connector, in your project you need to specify the following spring framework dependencies:
- compile "org.springframework.boot:spring-boot-starter-web:2.1.4.RELEASE"
- compile "org.springframework.boot:spring-boot-starter-websocket:2.1.4.RELEASE"
- compile "org.springframework.security:spring-security-web:5.1.4.RELEASE"
- compile "org.springframework.security:spring-security-config:5.1.4.RELEASE"