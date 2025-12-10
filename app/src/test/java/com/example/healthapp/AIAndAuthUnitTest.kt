package com.example.healthapp

import com.example.healthapp.data.remote.AuthApi
import com.example.healthapp.data.repository.AIChatRepository
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class AIAndAuthUnitTest {
    @Test
    fun testAuthErrorMapping() {
        assertEquals("账号或密码不正确", AuthApi.mapErrorForTest("Invalid login"))
        assertEquals("邮箱已存在", AuthApi.mapErrorForTest("email already exists"))
        assertEquals("请求过于频繁，请稍后再试", AuthApi.mapErrorForTest("Rate limit exceeded"))
    }

    @Test
    fun testAIChatFallbackExercise() {
        val repo = AIChatRepository()
        val context = mapOf("avg_steps_7d" to "3000")
        val reply = repo.testFallback("如何锻炼提升心肺？", context)
        assertTrue(reply.contains("建议进行"))
    }
}

