package com.gql_ktor_sec.di

import com.gql_ktor_sec.data.repository.TaskImageRepositoryDbImpl
import com.gql_ktor_sec.data.repository.TaskRepositoryDbImpl
import com.gql_ktor_sec.data.repository.UserAuthRepositoryImpl
import com.gql_ktor_sec.domain.repository.TaskImageRepository
import com.gql_ktor_sec.domain.repository.TaskRepository
import com.gql_ktor_sec.domain.repository.UserAuthRepository
import org.koin.dsl.module
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo


private val connectionString = System.getenv("MONGO_DB_URI")
    //"mongodb://localhost:27017"

val appModule = module{
    single {
        KMongo.createClient(connectionString)
            .coroutine
            .getDatabase("Task_db")

//        val settings = MongoClientSettings.builder()
//            .applyConnectionString(ConnectionString(connectionString))
//            .applyToSslSettings{ it.enabled(true) }
//            .build()
//          KMongo.createClient(settings)
//            .coroutine
//            .getDatabase("Task_db")
    }
    single<TaskRepository> {
        TaskRepositoryDbImpl(get())
    }
//
    single<TaskImageRepository> {
        TaskImageRepositoryDbImpl(get())
    }
//
    single<UserAuthRepository> {
        UserAuthRepositoryImpl(get())
    }
}