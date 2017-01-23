FROM distelli/java-base

ADD target/dependency/*.jar target/*.jar /europa/lib/
ADD run.sh /europa/run.sh
ADD public/ /europa/public

EXPOSE 8080

CMD ["/europa/run.sh", "--log-to-console"]
