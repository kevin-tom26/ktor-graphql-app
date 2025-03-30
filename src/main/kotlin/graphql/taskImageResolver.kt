package com.gql_ktor_sec.graphql

import com.expediagroup.graphql.server.operations.Mutation
import com.expediagroup.graphql.server.operations.Query
import com.gql_ktor_sec.data.models.*
import com.gql_ktor_sec.domain.repository.TaskImageRepository
import com.gql_ktor_sec.services.ImageUploadToCloud
import io.ktor.http.content.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.io.readByteArray


class TaskImageQuery(private val taskImageRepository: TaskImageRepository) : Query {

    suspend fun getAllImgTasks(): List<TaskImgModel> = taskImageRepository.getAllImgTasks()

    suspend fun getImgTaskByTitle(name: String): TaskImgModel? = taskImageRepository.getImgTaskByTitle(name)
}

class TaskImageMutation(private val taskImageRepository: TaskImageRepository) : Mutation {

    suspend fun addImgTask(image: Upload?, task: TaskInput): TaskImgModel {
        var imageUrl: String? = null
        if(image != null){
            //val imageFile = image.file as? PartData.FileItem
            val imageBytes = image.bytes
            val fileName = image.fileName ?: "uploaded_image.jpg"

            imageUrl = ImageUploadToCloud(imageBytes,fileName)
        }
        val taskImgModel = TaskImgModel(task = task.toTaskModel(), image = imageUrl)
        return taskImageRepository.addImgTask(taskImgModel)
    }

    suspend fun deleteImgTask(id: String): Boolean = taskImageRepository.deleteImgTask(id)
}