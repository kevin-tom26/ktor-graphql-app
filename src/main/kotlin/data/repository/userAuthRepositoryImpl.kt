package com.gql_ktor_sec.data.repository

import com.gql_ktor_sec.data.models.RestResponse
import com.gql_ktor_sec.data.models.StatusResponse
import com.gql_ktor_sec.data.models.UserAuthModel
import com.gql_ktor_sec.data.models.UserAuthResponseModel
import com.gql_ktor_sec.domain.repository.UserAuthRepository
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.setValue

class UserAuthRepositoryImpl(private val db: CoroutineDatabase) : UserAuthRepository {

    private val collection : CoroutineCollection<UserAuthModel> = db.getCollection<UserAuthModel>()

    override suspend fun getUserById(id: String): RestResponse<UserAuthResponseModel?> {
        val user = collection.findOneById(id)
        return if(user != null && user.id.isNotBlank()){
            RestResponse(
                status = StatusResponse(200, "Success"),
                response = user.toAuthResponseModel()
            )
        }else{
            RestResponse(
                status = StatusResponse(404, "Not Found"),
                response = null
            )
        }
    }

    override suspend fun getUserByIdForUpdate(id: String): UserAuthModel? {
        return collection.findOneById(id)
    }

    override suspend fun getUserByUserName(userName: String): UserAuthModel? {
        return collection.findOne(UserAuthModel::userName eq userName)
    }

    override suspend fun getUserByEmail(email: String): UserAuthModel? {
        return collection.findOne(UserAuthModel::email eq email)
    }

    override suspend fun addUser(user: UserAuthModel): RestResponse<UserAuthResponseModel?> {

        return if(collection.insertOne(user).wasAcknowledged()){
            RestResponse(
                status = StatusResponse(200, "Success"),
                response = user.toAuthResponseModel()
            )
        } else{
                RestResponse(
                    status = StatusResponse(404, "Not Found"),
                    response = null
                )
            }
        }

    override suspend fun updateRefreshToken(id: String, refreshToken: String) {
        collection.updateOneById(id, setValue(UserAuthModel::refreshToken, refreshToken))
    }
}