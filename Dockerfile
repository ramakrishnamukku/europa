FROM openjdk:8

ADD target/dependency/*.jar target/*.jar /europa/lib/
ADD run /europa/run
ADD public /europa/public

EXPOSE 80
WORKDIR /europa

CMD ["./run"]
