FROM openjdk:19
EXPOSE 8080
EXPOSE 81
EXPOSE 82
EXPOSE 83
ADD target/object-storage-manager.jar object-storage-manager.jar
ENTRYPOINT ["java", "-jar", "/object-storage-manager.jar"]