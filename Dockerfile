
FROM gradle:7.1-jdk16 AS build
COPY --chown=gradle:gradle . /judgebot
WORKDIR /judgebot
RUN gradle shadowJar --no-daemon

FROM openjdk:11.0.8-jre-slim
RUN mkdir /config/
COPY --from=build /judgebot/build/libs/Judgebot.jar /

ENTRYPOINT ["java", "-jar", "/Judgebot.jar"]