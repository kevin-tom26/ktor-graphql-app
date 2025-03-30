package com.gql_ktor_sec.plugins

import com.expediagroup.graphql.generator.extensions.get
import com.expediagroup.graphql.generator.extensions.plus
import com.expediagroup.graphql.server.types.GraphQLResponse
import com.gql_ktor_sec.data.models.RestResponse
import com.gql_ktor_sec.data.models.StatusResponse
import com.gql_ktor_sec.domain.repository.TaskImageRepository
import com.gql_ktor_sec.domain.repository.TaskRepository
import com.gql_ktor_sec.graphql.TaskImageMutation
import com.gql_ktor_sec.graphql.TaskImageQuery
import com.gql_ktor_sec.graphql.TaskMutation
import com.gql_ktor_sec.graphql.TaskQuery
import graphql.schema.Coercing
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import graphql.schema.GraphQLScalarType
import io.ktor.http.content.*
import com.expediagroup.graphql.generator.hooks.SchemaGeneratorHooks
import com.expediagroup.graphql.server.ktor.*
import com.gql_ktor_sec.data.models.Upload
import com.gql_ktor_sec.domain.repository.UserAuthRepository
import com.gql_ktor_sec.routes.authRoutes
import com.gql_ktor_sec.routes.uploadRoutes
import graphql.GraphQLContext
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLType
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.runBlocking
import kotlinx.io.readByteArray
import javax.naming.AuthenticationException
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation


fun Application.configureGraphQL(){
    val taskRepository by inject<TaskRepository>()
    val taskImgRepository by inject<TaskImageRepository>()
    val authRepository by inject<UserAuthRepository>()

    install(GraphQL){
        schema {
            packages = listOf("com.gql_ktor_sec")
            queries = listOf(TaskQuery(taskRepository), TaskImageQuery(taskImgRepository))
            mutations = listOf(TaskMutation(taskRepository), TaskImageMutation(taskImgRepository))
            hooks = customHooks
        }

        server {
            contextFactory = CustomGraphQLContextFactory()
        }

    }

    routing {
        authRoutes(authRepository)
       // install(CustomResponsePlugin)
        authenticate("jwt-auth"){
            graphQLPostRoute()
        }
        graphiQLRoute()
        graphQLSDLRoute()

       uploadRoutes()


//        post("/graphql") {
//            val respons = call.receive<JsonObject>()
//            val query = Json.encodeToString(respons["query"])
//            val parameter = Json.encodeToString(respons["variables"])
//
//            println("query is - $query")
//            println("paramter is - $parameter")
//            val response: GraphQLServerResponse? = graphQLConfig.server.execute(call.request) // Pass correct request type
//
//            val wrappedResponse = RestResponse(
//                status = StatusResponse(200, "Success"),
//                response = response
//            )
//
//            call.respond(HttpStatusCode.OK, wrappedResponse) // Send the wrapped response
//        }
//        post("/graphql") {
//            val response = call.receive<GraphQLServerResponse>()
//
//            val isGraphiQL = call.request.headers["User-Agent"]?.contains("GraphiQL") == true
//            if (isGraphiQL) {
//                call.respond(response)  // Return raw GraphQL response for GraphiQL
//            } else {
//                call.respondWrappedGraphQL(response)  // Wrap response for API clients
//            }
//        }

    }
}

val UploadScalar = GraphQLScalarType.newScalar()
    .name("Upload")
    .description("Custom scalar type for handling file uploads")
    .coercing(object : Coercing<Upload, String> {
        @Deprecated("Deprecated in Java")
        override fun serialize(dataFetcherResult: Any): String {
            throw IllegalArgumentException("File upload serialization is not supported")
        }

        @Deprecated("Deprecated in Java")
        override fun parseValue(input: Any): Upload {
            return when (input) {
                is Upload -> input  // Already an Upload object, return as is
                is PartData.FileItem -> Upload(
                    bytes = runBlocking { input.provider().readRemaining().readByteArray() },
                    fileName = input.originalFileName,
                    contentType = input.contentType
                )
                else -> throw IllegalArgumentException("Expected a FileItem or Upload instance")
            }
        }

        @Deprecated("Deprecated in Java")
        override fun parseLiteral(input: Any): Upload {
            throw IllegalArgumentException("File upload literals are not supported")
        }
    })
    .build()

val customHooks = object : SchemaGeneratorHooks {
    override fun willGenerateGraphQLType(type: KType): GraphQLType? {
        return if (type.classifier == Upload::class) {
            UploadScalar
        } else {
            null
        }
    }
//    override fun didGenerateQueryField(
//        kClass: KClass<*>,
//        function: KFunction<*>,
//        fieldDefinition: GraphQLFieldDefinition
//    ): GraphQLFieldDefinition {
//        // Check if the function has @Authenticated annotation
//        if (function.findAnnotation<Authenticated>() != null) {
//            return fieldDefinition.transform { builder ->
//                builder.dataFetcher { env ->
//                    val call = env.graphQlContext.get<ApplicationCall>()
//                    val principal = call?.principal<JWTPrincipal>()
//
//                    if (principal == null) {
//                        throw AuthenticationException("Unauthorized access!")
//                    }
//                    function.call(env.getSource())
//                }
//            }
//        }
//        return fieldDefinition
//    }
}

//suspend fun ApplicationCall.respondWrappedGraphQL(response: GraphQLServerResponse) {
//    val jsonResponse = Json.encodeToJsonElement(response).jsonObject
//    val dataObject = jsonResponse["data"]?.jsonObject
//
//    if (!dataObject.isNullOrEmpty()) {
//        respond(HttpStatusCode.OK, RestResponse(StatusResponse(200, "Success"), dataObject))
//    } else {
//        respond(
//            HttpStatusCode.BadRequest,
//            RestResponse(
//                StatusResponse(400, "Invalid GraphQL Query"),
//                null
//            )
//        )
//    }
//}
val CustomResponsePlugin = createApplicationPlugin("CustomResponsePlugin") {
    onCallRespond { _, content ->
        println("Raw Content: $content")

        val responseData = when (content) {
            is GraphQLResponse<*> -> {
                println("Extracting 'data' from GraphQLResponse")
                content.data // Directly access the `data` field
            }
            else -> {
                println("Unhandled content type: ${content::class}")
                content // Return original response if not GraphQLResponse
            }
        }

        val wrappedResponse = RestResponse(
            status = StatusResponse(200, "Success"),
            response = responseData
        )

        transformBody { wrappedResponse }
    }
}

class CustomGraphQLContextFactory : DefaultKtorGraphQLContextFactory() {
    override suspend fun generateContext(request: ApplicationRequest): GraphQLContext {
        val call = request.call
        return super.generateContext(request).plus(mapOf("call" to call))
    }
}
