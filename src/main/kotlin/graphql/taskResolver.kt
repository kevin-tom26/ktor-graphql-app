package com.gql_ktor_sec.graphql

import com.expediagroup.graphql.generator.extensions.get
import com.expediagroup.graphql.server.operations.Mutation
import com.expediagroup.graphql.server.operations.Query
import com.gql_ktor_sec.data.models.Priority
import com.gql_ktor_sec.data.models.TaskInput
import com.gql_ktor_sec.data.models.TaskModel
import com.gql_ktor_sec.domain.repository.TaskRepository
import graphql.schema.DataFetchingEnvironment
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import javax.naming.AuthenticationException


class TaskQuery(private val taskRepository: TaskRepository) : Query {

    suspend fun getAllTasks(env: DataFetchingEnvironment): List<TaskModel> {
//        val call = env.graphQlContext.get<ApplicationCall>("call")
//        val principal = call.principal<JWTPrincipal>()
//        if (principal == null) {
//            throw AuthenticationException("Unauthorized access!")
//        }
        return taskRepository.getAllTasks()
    }

    suspend fun getTaskByPriority(priority: Priority): List<TaskModel> = taskRepository.getTaskByPriority(priority)

    suspend fun getTaskByTitle(name: String): TaskModel? = taskRepository.getTaskByTitle(name)
}

class TaskMutation(private val taskRepository: TaskRepository) : Mutation {

    suspend fun addTask(task: TaskInput): TaskModel {
        return taskRepository.addTask(task.toTaskModel())
    }

    suspend fun deleteTask(id: String): Boolean = taskRepository.deleteTask(id)
}