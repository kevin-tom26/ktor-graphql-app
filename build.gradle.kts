
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
}

group = "com.gql_ktor_sec"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.koin.core)
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.serialization.jackson)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)

    //mongo
    implementation(libs.kmongo)
    implementation(libs.kmongo.coroutine)
    implementation(libs.mongo.driver)

    //GraphQL
    implementation(libs.graphql.kotlin.server)
    implementation(libs.graphql.kotlin.schema.generator)

    //ktor client
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.engine)
    implementation(libs.ktor.client.logging)

    //JBCrypt
    implementation(libs.jbcrypt)

}
