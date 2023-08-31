#
# Build stage
#
FROM maven:3.9.4-eclipse-temurin-20 AS build
COPY . /home/app/
RUN mvn -f /home/app/pom.xml clean package

#
# Package stage
#
FROM eclipse-temurin:20
COPY --from=build /home/app/target/RepoGrabber-1.0-SNAPSHOT-jar-with-dependencies.jar /home/app/RepoGrabber.jar
ENTRYPOINT ["java","-jar","/home/app/RepoGrabber.jar"]