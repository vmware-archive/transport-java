FROM openjdk:8-jdk-alpine
VOLUME /tmp
COPY build/libs/vmw-bifrost-boot.jar fabric-service.jar
EXPOSE 8090
ENTRYPOINT ["java","-Done-jar.silent=true","-Djava.security.egd=file:/dev/./urandom","-jar","/fabric-service.jar"]
