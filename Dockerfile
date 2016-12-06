FROM distelli/java-base

ADD target/dependency/*.jar target/*.jar /europa/lib/
ADD run /europa/run

EXPOSE 9001

CMD ["/europa/run", "--log-to-console"]
