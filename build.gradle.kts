plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "3.1.0"
  kotlin("plugin.spring") version "1.4.30"
  kotlin("plugin.jpa") version "1.4.30"
  kotlin("kapt") version "1.4.30"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("io.springfox:springfox-boot-starter:3.0.0")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.4")

  implementation("org.flywaydb:flyway-core:7.5.2")
  implementation("com.zaxxer:HikariCP:4.0.1")
  implementation("com.h2database:h2")
  implementation("com.oracle.database.jdbc:ojdbc10:19.9.0.0")

  implementation("org.mapstruct:mapstruct:1.4.2.Final")
  kapt("org.mapstruct:mapstruct-processor:1.4.2.Final")

  developmentOnly("org.springframework.boot:spring-boot-devtools")

  testImplementation("io.jsonwebtoken:jjwt:0.9.1")
  testImplementation("com.github.tomakehurst:wiremock-standalone:2.27.2")
  testImplementation("com.github.javafaker:javafaker:1.0.2")
}
