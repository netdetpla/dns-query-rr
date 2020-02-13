FROM openjdk:11.0.5-jre-stretch

ADD ["build/libs/dns-query-rr-1-all.jar", "settings.properties", "/"]

CMD java -jar dns-query-rr-1-all.jar