package com.gql_ktor_sec.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.gql_ktor_sec.data.models.StatusResponse
import com.gql_ktor_sec.data.models.UserAuthModel
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.util.*
import java.util.*

fun Application.configureJwtAuthentication(){
    val hashedUserTable = createHashedUserTable()
    install(Authentication){
        jwt("jwt-auth"){
            realm = "ktor.io"
            verifier(JwtConfig.verifier)
            validate { jwtCredential ->
                val userId = jwtCredential.payload.getClaim("userId").asString()
                if(!userId.isNullOrBlank()){
                    JWTPrincipal(jwtCredential.payload)
                }else{
                    null
                }
            }

            challenge { _, _ ->
                call.respond(status = HttpStatusCode.Unauthorized, StatusResponse(401, "Token is nor valid or has expired"))
            }
        }

        basic("basic-auth"){
            validate { credentials ->
                hashedUserTable.authenticate(credentials)
            }
        }
    }
}

fun createHashedUserTable() : UserHashedTableAuth{

    val digestFunction = getDigestFunction("SHA-256"){"${System.getenv("HASH_SALT") ?: "fallback-salt"}${it.length}"}
    val adminUsername = System.getenv("ADMIN_USERNAME")
    val adminPassword = System.getenv("ADMIN_PASSWORD")
    return UserHashedTableAuth(
        digester = digestFunction,
        table = mapOf(adminUsername to digestFunction(adminPassword))
    )
}

object JwtConfig{
    private val secret = System.getenv("JWT_SECRET")
    private val issuer = System.getenv("JWT_ISSUER")
    private val accessTokenExpiration = System.getenv("JWT_ACCESS_EXPIRATION").toLong()
    private val refreshTokenExpiration = System.getenv("JWT_REFRESH_EXPIRATION").toLong()
    private val algorithm = Algorithm.HMAC256(secret)

    val verifier : JWTVerifier = JWT.require(algorithm).withIssuer(issuer).build()

    fun generateAccessToken(user: UserAuthModel) : String {
        return JWT.create()
            .withIssuer(issuer)
            .withClaim("userId", user.id)
            .withExpiresAt(Date(System.currentTimeMillis() + accessTokenExpiration))
            .sign(algorithm)
    }

    fun generateRefreshToken(user: UserAuthModel) : String{
        return JWT.create()
            .withIssuer(issuer)
            .withClaim("userId", user.id)
            .withExpiresAt(Date(System.currentTimeMillis() + refreshTokenExpiration))
            .sign(algorithm)
    }

    fun verifyRefreshToken(refreshToken: String) : String?{
        return try {
            val decodedJWT = JWT.require(algorithm).withIssuer(issuer).build().verify(refreshToken)
            decodedJWT.getClaim("userId").asString()
        }catch (e: Exception){
            null
        }
    }
}