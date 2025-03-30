package com.gql_ktor_sec.data.models

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class TaskModel(
    @BsonId
    val id : String = ObjectId().toString(),
    val title : String,
    val body : String,
    val isEnabled: Boolean,
    val priority: Priority
)

enum class Priority{
    HIGH,
    MEDIUM,
    LOW
}



@Serializable
data class TaskInput(
    val title : String,
    val body : String,
    val isEnabled: Boolean,
    val priority: Priority
){
    fun toTaskModel() : TaskModel {
        return TaskModel(
            title = title,
            body = body,
            isEnabled = isEnabled,
            priority = priority
        )
    }
}