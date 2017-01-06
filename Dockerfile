FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/uberjar/not-so-secure-webapp.jar /not-so-secure-webapp/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/not-so-secure-webapp/app.jar"]
