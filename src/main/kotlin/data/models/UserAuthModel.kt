package com.gql_ktor_sec.data.models

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class UserAuthModel (
    @BsonId
    val id : String = ObjectId().toString(),
    val userName : String? = null,
    val email : String? = null,
    val hashPassword : String? = null,
    val refreshToken : String
){
    fun toAuthResponseModel() : UserAuthResponseModel {
        return UserAuthResponseModel(
            id = id,
            userName = userName,
            email = email,
            accessToken = "",
            refreshToken = refreshToken
        )
    }
}

@Serializable
data class UserAuthResponseModel(
    val id : String,
    val userName : String? = null,
    val email : String? = null,
    val accessToken : String,
    val refreshToken : String
)