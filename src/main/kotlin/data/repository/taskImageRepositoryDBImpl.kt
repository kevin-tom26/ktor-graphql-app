package com.gql_ktor_sec.data.repository

import com.gql_ktor_sec.data.models.TaskImgModel
import com.gql_ktor_sec.domain.repository.TaskImageRepository
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase

class TaskImageRepositoryDbImpl(private val db: CoroutineDatabase) : TaskImageRepository {

    private val collection : CoroutineCollection<TaskImgModel> = db.getCollection<TaskImgModel>()

    override suspend fun getAllImgTasks(): List<TaskImgModel> = collection.find().toList()

    override suspend fun getImgTaskByTitle(name: String): TaskImgModel? = collection.findOne(
        //TaskImgModel::task regex Regex("^$name$", RegexOption.IGNORE_CASE)
        "{ 'task.title': { \$regex: '^$name$', '\$options': 'i' } }"
    )

    override suspend fun addImgTask(imgTask: TaskImgModel): TaskImgModel {
        if(getImgTaskByTitle(imgTask.task.title) != null){
            throw IllegalStateException("cannot duplicate task name")
        }
        collection.insertOne(imgTask)
        return imgTask
    }

    override suspend fun deleteImgTask(id: String): Boolean {
        val deleteResult = collection.deleteOneById(id)
        return deleteResult.wasAcknowledged() && deleteResult.deletedCount > 0
    }
}