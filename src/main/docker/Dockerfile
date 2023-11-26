FROM openjdk:17-jdk-slim as builder
WORKDIR application
# ADD maven/spring-homePIX-2.4.0.BUILD-SNAPSHOT.jar ./
# RUN java -Djarmode=layertools -jar spring-homePIX-2.4.0.BUILD-SNAPSHOT.jar extract
ADD spring-homepix-2.4.0.BUILD-SNAPSHOT.jar ./
RUN java -Djarmode=layertools -jar spring-homepix-2.4.0.BUILD-SNAPSHOT.jar extract


FROM openjdk:17-jdk-slim
WORKDIR application

COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/application/ ./

EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "org.springframework.boot.loader.JarLauncher"]
