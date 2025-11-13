# ===== Build Stage =====
FROM eclipse-temurin:8-jdk-alpine AS build

ARG WORK_DIR
WORKDIR /app

# Copy Maven project files
COPY ${WORK_DIR}/pom.xml ./pom.xml
COPY build/maven/start.sh ./start.sh
COPY ${WORK_DIR}/src ./src

# Build JAR
RUN apk add --no-cache maven && mvn -B -f /app/pom.xml clean package

# ===== Runtime Stage =====
FROM eclipse-temurin:8-jre-alpine

WORKDIR /opt/egov

COPY --from=build /app/target/*.jar /app/start.sh /opt/egov/
RUN chmod +x /opt/egov/start.sh

CMD ["/opt/egov/start.sh"]
