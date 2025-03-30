package com.gql_ktor_sec.plugins

import com.gql_ktor_sec.di.appModule
import io.ktor.server.application.*
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureFrameworks() {
    install(Koin) {
        //slf4jLogger()
        modules(appModule)
    }
}
