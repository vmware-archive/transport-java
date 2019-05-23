# RabbitMessageBrokerConnector for Bifrost EventBus

The RabbitMQ sample app connects to a RabbitMQ message broker. To start a local broker in a docker container run **docker-compose up --build**.
To verify that the message broker is running and monitor its queues you can log to **localhost:15672** with **admin/vmware**

To build and start the sample app run the **runme.sh** script. After the app boots you can:
- post requests using **http://localhost:8090/rest/post-request?request=test-request**
- read next request log entry from the queue using **http://localhost:8090/rest/get-log-entry**
