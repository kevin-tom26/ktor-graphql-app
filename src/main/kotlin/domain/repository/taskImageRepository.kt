package com.gql_ktor_sec.domain.repository
import com.gql_ktor_sec.data.models.TaskImgModel

interface TaskImageRepository{

    suspend fun getAllImgTasks() : List<TaskImgModel>

    suspend fun getImgTaskByTitle(name: String) : TaskImgModel?

    suspend fun addImgTask(imgTask: TaskImgModel) : TaskImgModel

    suspend fun deleteImgTask(id: String) : Boolean

}