## 1. JDK 베이스 이미지
#FROM gradle:8.5-jdk17 AS build
#WORKDIR /app
#
## 2. 프로젝트 복사 및 빌드
#COPY . .
#RUN gradle shadowJar --no-daemon
#
## 3. 실제 실행 이미지
#FROM openjdk:17-jdk-slim
#WORKDIR /app
#
#COPY --from=build /app/build/libs/*-all.jar app.jar
#
## 4. Render가 열어줄 포트 환경변수 사용
#ENV PORT=8080
#EXPOSE 8080
#
#CMD ["java", "-jar", "app.jar"]

# 1. Gradle로 빌드
FROM gradle:8.5-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle shadowJar --no-daemon

# 2. 실행 환경
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/build/libs/*-all.jar app.jar

# 3. Railway가 지정하는 포트 사용
ENV PORT=8080
EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
