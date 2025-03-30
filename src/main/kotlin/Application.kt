package com.gql_ktor_sec

import com.expediagroup.graphql.server.ktor.GraphQL
import com.expediagroup.graphql.server.ktor.graphQLPostRoute
import com.expediagroup.graphql.server.ktor.graphQLSDLRoute
import com.expediagroup.graphql.server.ktor.graphiQLRoute
import com.gql_ktor_sec.domain.repository.TaskImageRepository
import com.gql_ktor_sec.domain.repository.TaskRepository
import com.gql_ktor_sec.graphql.TaskMutation
import com.gql_ktor_sec.graphql.TaskQuery
import com.gql_ktor_sec.plugins.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureFrameworks()
    configureJwtAuthentication()
    configureMonitoring()
    configureGraphQL()
    //configureSerialization()
    configureRouting()
}
