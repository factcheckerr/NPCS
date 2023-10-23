FROM maven:3.8-openjdk-11
WORKDIR /sparqlprov/rewriting-java/
COPY pom.xml .
#RUN mvn dependency:go-offline -B --fail-never
#RUN mvn clean install
COPY src/ src/
#RUN mvn install -U dependency:copy-dependencies
#RUN mvn org.apache.maven.plugins:maven-compiler-plugin:compile
#RUN mvn package
RUN mvn assembly:assembly -DdescriptorId=jar-with-dependencies

CMD ["java", "-cp", "target/sparqlprovstar-rewriting-1.0-SNAPSHOT-jar-with-dependencies.jar", "npcs.App"]
