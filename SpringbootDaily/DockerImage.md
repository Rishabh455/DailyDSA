DOCKERIZING SPRING BOOT APPLICATION — INTERVIEW REVISION

1. Why Docker?

Docker is used to package the Spring Boot application along with all dependencies into a container so it runs consistently in every environment.

---

2. Basic Steps

Step 1:
Generate JAR file

mvn clean package

Creates:
target/app.jar

---

Step 2:
Create Dockerfile

Example:

FROM openjdk:17
COPY target/app.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]

---

Step 3:
Build Docker Image

docker build -t springboot-app .

---

Step 4:
Run Docker Container

docker run -p 8080:8080 springboot-app

---

3. Simple Flow Diagram

Spring Boot App
↓
Build JAR (mvn package)
↓
Create Dockerfile
↓
Build Docker Image
↓
Run Docker Container

---

4. MOST IMPORTANT INTERVIEW ANSWER

To create a Docker image for a Spring Boot application, first we generate the executable JAR file using Maven or Gradle.

Then we create a Dockerfile that contains the base JDK image, copies the JAR file, and defines the startup command.

Using docker build we create the Docker image, and using docker run we start the containerized Spring Boot application.
