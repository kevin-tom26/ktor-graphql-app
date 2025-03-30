package com.gql_ktor_sec.routes

import com.expediagroup.graphql.server.ktor.GraphQL
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.gql_ktor_sec.data.models.Upload
import graphql.ExecutionInput
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import kotlinx.io.readByteArray
import kotlinx.serialization.json.*

fun Route.uploadRoutes() {
    val uploadPlugin = createRouteScopedPlugin("UploadPlugin") {
        install(ContentNegotiation) {
            jackson()
        }
    }

    post("/graphql/upload") {
        val multipart = call.receiveMultipart()
        var operations: String? = null
        var map: String? = null
        val fileParts = mutableMapOf<String, Upload>()
        val graphQL = application.plugin(GraphQL)

        val acceptHeader = call.request.headers["Accept"]
        println("Client Accept Header: $acceptHeader")

        // Read the multipart data
        multipart.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    when (part.name) {
                        "operations" -> operations = part.value
                        "map" -> map = part.value
                    }
                }
                is PartData.FileItem -> {
                    val fileBytes = part.provider().readRemaining().readByteArray()
                    fileParts[part.name ?: "0"] = Upload(
                        bytes = fileBytes,
                        fileName = part.originalFileName,
                        contentType = part.contentType
                    ) // Store file as Upload object
                }
                else -> Unit
            }
            part.dispose()
        }

        if (operations == null || map == null) {
            call.respond(HttpStatusCode.BadRequest, "Missing operations or map field")
            return@post
        }

        // Parse JSON
        val operationsJson = Json.decodeFromString<JsonObject>(operations!!)
        val mapJson = Json.decodeFromString<JsonObject>(map!!)

        // Extract query and variables
        val query = operationsJson["query"]?.jsonPrimitive?.content
        val variables = operationsJson["variables"]?.jsonObject
            ?.mapValues { it.value.toPrimitive() }
            ?.toMutableMap() ?: mutableMapOf()

//        val taskJson = variables["task"] as? JsonObject
//        if (taskJson != null) {
//            variables["task"] = Json.decodeFromJsonElement<TaskInput>(taskJson)
//        }

        // Replace file references in variables with Upload objects
        mapJson.forEach { (key, value) ->
            if (fileParts.containsKey(key)) {
                val variablePath = value.jsonArray[0].jsonPrimitive.content // e.g., "variables.image"
                val keys = variablePath.removePrefix("variables.").split(".") // Remove "variables." prefix

                var current: MutableMap<String, Any?> = variables as MutableMap<String, Any?>
                for (i in keys.indices) {
                    val field = keys[i]
                    if (i == keys.lastIndex) {
                        // Set the file object at the final field location
                        current[field] = fileParts[key]
                    } else {
                        // Ensure intermediate maps exist
                        val nextMap = current[field]
                        if (nextMap !is MutableMap<*, *>) {
                            current[field] = mutableMapOf<String, Any?>()
                        }
                        @Suppress("UNCHECKED_CAST")
                        current = current[field] as MutableMap<String, Any?>
                    }
                }
            }

        }

        // Execute GraphQL mutation with Upload object
        val executionInput = ExecutionInput.newExecutionInput()
            .query(query)
            .variables(variables as Map<String, Any>?)
            .build()


        val result = graphQL.engine.execute(executionInput)

        val jsonResponse = jacksonObjectMapper().writeValueAsString(result.toSpecification())
        call.respondText(jsonResponse, ContentType.Application.Json)

      //  call.respond(result.toSpecification())
    }
}

fun JsonElement.toPrimitive(): Any {
    return when (this) {
        is JsonPrimitive -> {
            when {
                this.isString -> this.content // Preserve as String
                this.booleanOrNull != null -> this.boolean
                this.intOrNull != null -> this.int
                this.longOrNull != null -> this.long
                this.floatOrNull != null -> this.float
                this.doubleOrNull != null -> this.double
                else -> this.content // Fallback (treat unknown cases as String)
            }
        }
        is JsonObject -> this.mapValues { it.value.toPrimitive() } // Convert JsonObject to Map
        is JsonArray -> this.map { it.toPrimitive() } // Convert JsonArray to List
        else -> this // Fallback
    }
}
