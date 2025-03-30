package com.gql_ktor_sec.data.models

import io.ktor.http.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class TaskImgModel(
    @BsonId
    val id : String = ObjectId().toString(),
    val task : TaskModel,
    val image : String?
)

@Serializable
data class Upload(
    val bytes: ByteArray,
    val fileName: String?,
    @Contextual val contentType: ContentType?)
