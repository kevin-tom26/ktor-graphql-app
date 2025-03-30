package com.gql_ktor_sec.data.repository

import com.gql_ktor_sec.data.models.Priority
import com.gql_ktor_sec.data.models.TaskModel
import com.gql_ktor_sec.domain.repository.TaskRepository
import org.litote.kmongo.and
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.regex

class TaskRepositoryDbImpl(private val db: CoroutineDatabase) : TaskRepository {

    private val collection : CoroutineCollection<TaskModel> = db.getCollection<TaskModel>()

    override suspend fun getAllTasks(): List<TaskModel> = collection.find().toList()

    override suspend fun getTaskByPriority(priority: Priority): List<TaskModel> = collection.find(TaskModel::priority eq priority).toList()

    override suspend fun getTaskByTitle(name: String): TaskModel? = collection.findOne(TaskModel::title regex Regex("^$name$", RegexOption.IGNORE_CASE))


    override suspend fun addTask(task: TaskModel): TaskModel {
        if(getTaskByTitle(task.title) != null){
            throw IllegalStateException("cannot duplicate task name")
        }
        collection.insertOne(task)
        return task
    }

    override suspend fun deleteTask(id: String): Boolean {
        val deleteResult = collection.deleteOneById(id)
        return deleteResult.wasAcknowledged() && deleteResult.deletedCount > 0
    }

    override suspend fun getEnabledTaskWithPriority(enabled: Boolean, priority: Priority): List<TaskModel> = collection.find(
        and(
            TaskModel::isEnabled eq enabled,
            TaskModel::priority eq priority
        )
    ).toList()
}