# homePIX photo-sharing application [![Build Status](https://github.com/spring-projects/spring-petclinic/actions/workflows/maven-build.yml/badge.svg)](https://github.com/spring-projects/spring-petclinic/actions/workflows/maven-build.yml)

[![Open in Gitpod](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io/#https://github.com/spring-projects/spring-petclinic)

## Acknowledgements

homePIX is forked from the PetClinic application.

## Running homePIX locally
homePIX is a [Spring Boot](https://spring.io/guides/gs/spring-boot) application built using [Maven](https://spring.io/guides/gs/maven/) or [Gradle](https://spring.io/guides/gs/gradle/). You can build a jar file and run it from the command line (it should work just as well with Java 11 or newer):


```
git clone https://github.com/ElliottBignell/homePIX-spring.git
cd homePIX-spring
./mvnw package
java -jar target/*.jar
```

You can then access homePIX here: https://localhost:8443/
