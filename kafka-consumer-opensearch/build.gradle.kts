import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.7.12"
    id("io.spring.dependency-management") version "1.0.15.RELEASE"
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // https://mvnrepository.com/artifact/org.apache.kafka/kafka-clients
    implementation("org.apache.kafka:kafka-clients:3.4.0")

    // https://mvnrepository.com/artifact/org.slf4j/slf4j-api
    implementation("org.slf4j:slf4j-api:2.0.5")

    // https://mvnrepository.com/artifact/org.slf4j/slf4j-simple
    implementation("org.slf4j:slf4j-simple:2.0.5")

    // https://mvnrepository.com/artifact/org.opensearch.client/opensearch-rest-high-level-client
    implementation("org.opensearch.client:opensearch-rest-high-level-client:2.6.0")

    // https://mvnrepository.com/artifact/com.google.code.gson/gson
    implementation("com.google.code.gson:gson:2.10.1")

    ////////
// versions used in the course
//    implementation("org.jetbrains.kotlin:kotlin-reflect")
//    // https://mvnrepository.com/artifact/org.apache.kafka/kafka-clients
//    implementation("org.apache.kafka:kafka-clients:3.1.0")
//
//    // https://mvnrepository.com/artifact/org.slf4j/slf4j-api
//    implementation("org.slf4j:slf4j-api:1.7.36")
//
//    // https://mvnrepository.com/artifact/org.slf4j/slf4j-simple
//    implementation("org.slf4j:slf4j-simple:1.7.36")
//
//    // https://mvnrepository.com/artifact/org.opensearch.client/opensearch-rest-high-level-client
//    implementation("org.opensearch.client:opensearch-rest-high-level-client:1.2.4")
//
//    // https://mvnrepository.com/artifact/com.google.code.gson/gson
//    implementation("com.google.code.gson:gson:2.9.0")


}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}