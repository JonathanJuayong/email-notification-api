import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.5.1"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.5.10"
    kotlin("plugin.spring") version "1.5.10"
    kotlin("plugin.allopen") version "1.5.10"
    kotlin("plugin.noarg") version "1.5.10"
    id("war")
    id("org.jmailen.kotlinter") version "3.4.4"
    id("io.gitlab.arturbosch.detekt") version "1.17.1"
    `maven-publish`
    jacoco
    id("de.ffuf.pass.gradle") version "1.0.21"
    id("org.liquibase.gradle") version "2.0.1"
}

val SPEC_VERSION = "4.0.0"

description = "SBShelly"
group = "ffufm.shelly.api"
version = "$SPEC_VERSION." + (if (project.hasProperty("patchVersion")) project.property("patchVersion") else "0")
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
    val azureArtifactsGradleAccessToken: String? by project
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
    maven {
        url = uri("https://pkgs.dev.azure.com/ffuf/_packaging/Pass/maven/v1")
        credentials {
            username = "AZURE_ARTIFACTS"
            password = System.getenv("AZURE_ARTIFACTS_ENV_ACCESS_TOKEN_2") ?: azureArtifactsGradleAccessToken
        }
    }
}
val antJUnit by configurations.getting
dependencies {

    api(group = "de.ffuf.pass", name = "passcommon", version = "2.1.22")
//    api(group = "ffufm.shelly.api.spec", name = "sbshellyspec", version = SPEC_VERSION)
    implementation("com.microsoft.azure:applicationinsights-spring-boot-starter:2.5.1")
    implementation("com.microsoft.azure:applicationinsights-logging-logback:2.5.1")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("com.mailjet:mailjet-client:5.2.0")

    runtimeOnly("com.h2database:h2") // only for local testing

    // UNIT TESTS
    testApi(group = "de.ffuf.pass", name = "PassTestCommon", version = "2.0.67")

    antJUnit("org.apache.ant", "ant-junit", "1.10.5")

    liquibaseRuntime("org.liquibase:liquibase-core:3.8.0")
    liquibaseRuntime("org.liquibase:liquibase-groovy-dsl:2.0.1")
    liquibaseRuntime("ch.qos.logback:logback-core:1.2.3")
    liquibaseRuntime("ch.qos.logback:logback-classic:1.2.3")
    liquibaseRuntime(group = "com.microsoft.sqlserver", name = "mssql-jdbc")
}

liquibase {
    activities {
        create("diffMain") {
            // USE DOCKER: docker run -e 'ACCEPT_EULA=Y' -e 'SA_PASSWORD=yourStrong(!)Password' -p 1433:1433 -d mcr.microsoft.com/mssql/server:2017-CU8-ubuntu ###
            (this.arguments as MutableMap<String, String>).apply {
                this["changeLogFile"] = "src/main/resources/db/changelog/db.changelog-diff.xml"
                this["url"] = "jdbc:sqlserver://localhost;databaseName=master"
                this["username"] = "sa"
                this["password"] = "yourStrong(!)Password"
// set e.g. the Dev Database to perform diffs
                this["referenceUrl"] = ""
                this["referenceUsername"] = ""
                this["referencePassword"] = ""
            }
        }
    }
}

allOpen {
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.MappedSuperclass")
    annotation("javax.persistence.Embeddable")
}

tasks.withType<KotlinCompile> {

    sourceCompatibility = JavaVersion.VERSION_1_8.toString()
    targetCompatibility = JavaVersion.VERSION_1_8.toString()

    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=compatibility")
        jvmTarget = "1.8"
    }

}

configurations.all {
    exclude(module = "jakarta.validation-api")
    exclude(module = "hibernate-validator")
}
