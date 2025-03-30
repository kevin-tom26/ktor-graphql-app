package com.gql_ktor_sec.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class GraphqlRequest(
    val query: String,
    val variables: Map<String, JsonElement>? = null
)