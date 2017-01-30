FROM openjdk:8

ADD target/dependency/*.jar target/*.jar /europa/lib/
ADD run /europa/run
ADD public /europa/public

EXPOSE 80
EXPOSE 443
WORKDIR /europa

# Add tini:
ENV TINI_VERSION v0.13.2
ADD https://github.com/krallin/tini/releases/download/${TINI_VERSION}/tini /tini
ADD https://github.com/krallin/tini/releases/download/${TINI_VERSION}/tini.asc /tini.asc
RUN gpg --keyserver ha.pool.sks-keyservers.net --recv-keys 595E85A6B1B4779EA4DAAEC70B588DFF0527A9B7 \
 && gpg --verify /tini.asc && chmod +x /tini && rm /tini.asc
ENTRYPOINT ["/tini", "--"]

CMD ["./run"]
