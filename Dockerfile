# See https://docs.spring.io/spring-boot/reference/packaging/container-images/dockerfiles.html#packaging.container-images.dockerfiles.cds
FROM bellsoft/liberica-openjdk-debian:25-cds AS builder
LABEL authors="julius.krah"
LABEL org.opencontainers.image.description="Simple Commerce is a ecommerce platform for the sale of digital products."

WORKDIR /builder
ARG JAR_DIR=app/build/libs
COPY gradlew build.gradle.kts gradle.properties settings.gradle.kts       ./
COPY gradle/                                                              ./gradle/
COPY app/src/                                                             ./app/src/
COPY app/build.gradle.kts                                                 ./app/
COPY aspects/src/                                                         ./aspects/src/
COPY aspects/build.gradle.kts                                             ./aspects/
COPY minio-docker-compose/src/                                            ./minio-docker-compose/src/
COPY minio-docker-compose/build.gradle.kts                                ./minio-docker-compose/
COPY buildSrc/src/                                                        ./buildSrc/src/
COPY buildSrc/build.gradle.kts                                            ./buildSrc/
COPY integration-test/build.gradle.kts                                    ./integration-test/
RUN chmod +x ./gradlew && ./gradlew --no-daemon --stacktrace --info --console=plain build -x test
RUN find ${JAR_DIR} -name '*.jar' -a ! -name '*plain.jar' -exec mv '{}' application.jar \;
RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted

FROM bellsoft/liberica-openjre-debian:25-cds
ARG SPRING_INSTRUMENT=spring-instrument.jar
ARG SPRING_INSTRUMENT_VERSION=6.2.1
ARG SPRING_FLYWAY_ENABLED=false
ARG SPRING_DATASOURCE_HIKARI_ALLOW_POOL_SUSPENSION=true
ARG SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/simple_commerce
ARG SPRING_JPA_DATABASE_PLATFORM=org.hibernate.dialect.PostgreSQLDialect
ARG SPRING_JPA_PROPERTIES_HIBERNATE_BOOT_ALLOW_JDBC_METADATA_ACCESS=false
ARG SPRING_JPA_HIBERNATE_DDL_AUTO=none
ARG SPRING_SQL_INIT_MODE=never
ARG OBJECTSTORE_OPTIONS_DEFAULT_BUCKET=false
ENV SPRING_DOCKER_COMPOSE_ENABLED=false
ENV JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS} -XX:+UnlockDiagnosticVMOptions -XX:+AllowArchivingWithJavaAgent --enable-preview -javaagent:${SPRING_INSTRUMENT}"
WORKDIR /application
ADD https://repo1.maven.org/maven2/org/springframework/spring-instrument/${SPRING_INSTRUMENT_VERSION}/spring-instrument-${SPRING_INSTRUMENT_VERSION}.jar ${SPRING_INSTRUMENT}
COPY --from=builder /builder/extracted/dependencies/ ./
COPY --from=builder /builder/extracted/spring-boot-loader/ ./
COPY --from=builder /builder/extracted/snapshot-dependencies/ ./
COPY --from=builder /builder/extracted/application/ ./
RUN java -XX:ArchiveClassesAtExit=application.jsa -Dspring.context.exit=onRefresh -jar application.jar

EXPOSE 8080
ENTRYPOINT ["java", "-XX:SharedArchiveFile=application.jsa", "-jar", "application.jar"]
