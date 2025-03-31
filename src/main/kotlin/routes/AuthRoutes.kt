package com.gql_ktor_sec.routes

import com.gql_ktor_sec.data.models.RestResponse
import com.gql_ktor_sec.data.models.StatusResponse
import com.gql_ktor_sec.data.models.UserAuthModel
import com.gql_ktor_sec.domain.repository.UserAuthRepository
import com.gql_ktor_sec.plugins.JwtConfig
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.mindrot.jbcrypt.BCrypt

fun Route.authRoutes(authRepository: UserAuthRepository) {

//    val contentNegotiationPlugin = createRouteScopedPlugin("AuthContentNegotiation") {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }
//    }

    authenticate("basic-auth") {
        post("/signup") {
            val request = call.receiveParameters()
            val userName = request["userName"]
            val password = request["password"]
            val email = request["email"]

            if ((userName != null && authRepository.getUserByUserName(userName) != null) || (email != null && authRepository.getUserByEmail(
                    email
                ) != null)
            ) {
                call.respond(
                    message = RestResponse<String?>(
                        status = StatusResponse(statusCode = 404, message = "User already exists!"),
                        response = "User already exists!"
                    )
                )
                return@post
            }
            val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())
            val newUser =
                UserAuthModel(userName = userName, email = email, hashPassword = hashedPassword, refreshToken = "")
            call.respond(message = authRepository.addUser(newUser))
        }

        post("/login") {
            val request = call.receiveParameters()
            val userName = request["userName"]
            val password = request["password"]
            val email = request["email"]

            val user = when {
                userName != null -> authRepository.getUserByUserName(userName)
                email != null -> authRepository.getUserByEmail(email)
                else -> null
            }

            if (user == null || !BCrypt.checkpw(password, user.hashPassword)) {
                call.respond(
                    message = RestResponse<String?>(
                        status = StatusResponse(statusCode = 404, message = "Invalid credentials!"),
                        response = "Invalid credentials!"
                    )
                )
                return@post
            }
            val accessToken = JwtConfig.generateAccessToken(user)
            val refreshToken = JwtConfig.generateRefreshToken(user)
            authRepository.updateRefreshToken(user.id, refreshToken)

            val userResponse = authRepository.getUserById(user.id)

            if (userResponse.response != null) {
                val updatedUserResponse = userResponse.copy(
                    response = userResponse.response.copy(accessToken = accessToken)
                )
                call.respond(message = updatedUserResponse)
            } else {
                call.respond(message = userResponse)
            }
        }

        post("/refresh"){
            val request = call.receive<Map<String, String>>()
            val refreshToken = request["refreshToken"] ?: return@post call.respond(HttpStatusCode.NotFound,"No refresh token provided")
            val userId = JwtConfig.verifyRefreshToken(refreshToken) ?: return@post call.respond(HttpStatusCode.NotFound,"Invalid refresh token")

            val user = authRepository.getUserByIdForUpdate(userId) ?: return@post call.respond(HttpStatusCode.NotFound,"User not found")

            val newAccessToken = JwtConfig.generateAccessToken(user)
            val newRefreshToken = JwtConfig.generateRefreshToken(user)
            authRepository.updateRefreshToken(user.id, newRefreshToken)

            val userResponse = authRepository.getUserById(user.id)

            if(userResponse.response != null){
                val updatedUserResponse = userResponse.copy(
                    response = userResponse.response.copy(accessToken = newAccessToken)
                )
                call.respond(message = updatedUserResponse)
            }else{
                call.respond(message = userResponse)
            }
        }
    }
}