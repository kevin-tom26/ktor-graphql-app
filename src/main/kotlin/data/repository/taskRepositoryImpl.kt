package com.gql_ktor_sec.data.repository

import com.gql_ktor_sec.data.models.Priority
import com.gql_ktor_sec.data.models.TaskModel
import com.gql_ktor_sec.domain.repository.TaskRepository

class TaskRepositoryImpl : TaskRepository {

    private val tasks = mutableListOf<TaskModel>()

    override suspend fun getAllTasks(): List<TaskModel> = tasks

    override suspend fun getTaskByPriority(priority: Priority): List<TaskModel> = tasks.filter { it.priority == priority }

    override suspend fun getTaskByTitle(name: String): TaskModel? = tasks.find { it.title.equals(name, ignoreCase = true) }


    override suspend fun addTask(task: TaskModel): TaskModel {
        if(getTaskByTitle(task.title) != null){
            throw IllegalStateException("cannot duplicate task name")
        }
        tasks.add(task)
        return task
    }

    override suspend fun deleteTask(id: String): Boolean {
        return tasks.removeIf { it.id == id}
    }

    override suspend fun getEnabledTaskWithPriority(enabled: Boolean, priority: Priority): List<TaskModel> = tasks.filter { it.isEnabled == enabled && it.priority == priority }
}