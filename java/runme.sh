./gradlew clean buildBootJar && java -Done-jar.silent=true -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -jar build/libs/vmw-bifrost-boot.jar
