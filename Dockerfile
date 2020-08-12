FROM adoptopenjdk:11.0.8_10-jdk-hotspot
COPY target/universal/cddadb/ /cddadb
WORKDIR /cddadb
EXPOSE 9000
ENTRYPOINT ["./bin/cddadb", "-Dconfig.file=/cddadb-conf/prod.conf"]