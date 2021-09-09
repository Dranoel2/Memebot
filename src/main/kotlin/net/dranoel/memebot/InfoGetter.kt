package net.dranoel.memebot

import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

object InfoGetter {
    private val client = HttpClient(Apache) {
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
    }
    suspend fun getIds(): MutableList<String> {
        val ids: MutableList<String> = mutableListOf()
        val data: List<MemeTemplateData> = getTemplates()
        data.forEach {
            ids.add(it.id)
        }
        return ids
    }
    private suspend fun getTemplates(): List<MemeTemplateData> {
        val response: List<MemeTemplateData> = client.get("https://api.memegen.link/templates/") {
            accept(ContentType.Application.Json)
        }
        //println(response.toString())
        return response
    }
    suspend fun getTemplateFromID(id: String): MemeTemplateData {
        val templates = getTemplates()
        val filteredTemplates = templates.filter { it.id == id }
        return filteredTemplates[0]
    }

    suspend fun verifyUrl(url: String): Boolean {
        val response: HttpResponse = client.request("https://ktor.io/") {
            // Configure request parameters exposed by HttpRequestBuilder
        }
        return response.status.value == 200
    }
}