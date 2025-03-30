package com.gql_ktor_sec.domain.repository
import com.gql_ktor_sec.data.models.RestResponse
import com.gql_ktor_sec.data.models.UserAuthModel
import com.gql_ktor_sec.data.models.UserAuthResponseModel

interface UserAuthRepository{
    suspend fun getUserById(id : String) : RestResponse<UserAuthResponseModel?>

    suspend fun getUserByIdForUpdate(id : String) : UserAuthModel?

    suspend fun getUserByUserName(userName : String) : UserAuthModel?

    suspend fun getUserByEmail(email : String) : UserAuthModel?

    suspend fun addUser(user : UserAuthModel) : RestResponse<UserAuthResponseModel?>

    suspend fun updateRefreshToken(id: String, refreshToken: String)
}