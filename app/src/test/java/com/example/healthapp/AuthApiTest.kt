package com.example.healthapp

import com.example.healthapp.data.remote.AuthApi
import org.junit.Test
import org.junit.Assert.assertEquals

class AuthApiTest {
    @Test
    fun testInvalidGrantMapping() {
        val msg = AuthApi.mapErrorForTest("invalid grant")
        assertEquals("账号或密码不正确", msg)
    }

    @Test
    fun testEmailExistsMapping() {
        val msg = AuthApi.mapErrorForTest("Email already exists")
        assertEquals("邮箱已存在", msg)
    }

    @Test
    fun testRateLimitMapping() {
        val msg = AuthApi.mapErrorForTest("Only request this once")
        assertEquals("请求过于频繁，请稍后再试", msg)
    }
}

