FROM openjdk:17
VOLUME /tmp
COPY target/click-0.0.3.jar click-0.0.3.jar
ENTRYPOINT ["java","-jar","/click-0.0.3-.jar","--spring.profiles.active=prod"]