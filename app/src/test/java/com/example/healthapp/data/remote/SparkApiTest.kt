package com.example.healthapp.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class SparkApiTest {
    @Test
    fun `chat returns assistant content`() = runBlocking {
        val engine = MockEngine { _ ->
            val body = """{"choices":[{"message":{"role":"assistant","content":"你好"}}]}"""
            respond(body, headers = headersOf("Content-Type", ContentType.Application.Json.toString()))
        }
        val client = HttpClient(engine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val api = SparkApi(client) { "test_password" }
        val result = api.chat(listOf(SparkApi.SparkMessage("user", "hi")))
        assertEquals("你好", result)
    }
}

