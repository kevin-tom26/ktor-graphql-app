package com.gql_ktor_sec.services

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

suspend fun ImageUploadToCloud(imageBytes: ByteArray, fileName: String) : String{
    val apiKey: String = System.getenv("IMAGE_HOST_API")

    val client = HttpClient(CIO){
        install(Logging){
            level = LogLevel.ALL
        }
    }

    val response: HttpResponse = client.submitFormWithBinaryData(
        url = "https://api.imgbb.com/1/upload?key=$apiKey",
        formData = formData {
            append("image", imageBytes, Headers.build {
                append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
            })
        }
    )

    val responseBody = response.bodyAsText() // Convert response to JSON string
    println("imgbb Response: $responseBody")
    val json = Json.decodeFromString<JsonObject>(responseBody) // Parse JSON response

    return json["data"]?.jsonObject?.get("url")?.jsonPrimitive?.content
        ?: throw Exception("Failed to upload image")
}