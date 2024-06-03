# Use the official OpenJDK base image
FROM adoptopenjdk/openjdk17:alpine-jre

# Set the working directory inside the container
WORKDIR /app

# Copy the compiled Java application JAR file into the container
COPY target/cronjob-notification.jar /app/cronjob-notification.jar

# Command to run the Java application
CMD ["java", "-jar", "cronjob-notification.jar"]
