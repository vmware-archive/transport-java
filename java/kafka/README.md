# KafkaMessageBrokerConnector for Bifrost EventBus

The Kafka sample app connects to a Kafka message broker. To start a local broker in a docker container use the following commands:
1. export SERVER_HOST={your ip}
2. docker-compose up

To build and start the sample app run the **runme.sh** script. After the app boots you can:
- post requests using **http://localhost:8090/rest/post-request?request=test-request&requestKey=test-key**
- get cached log entries using **http://localhost:8090/rest/get-log** - this will show log entries since the app was started.
