package com.example.healthapp.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.healthapp.model.AIChatMessage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

val Context.aiChatDataStore by preferencesDataStore(name = "ai_chat_store")

object AIChatStore {
    private val HISTORY = stringPreferencesKey("history")

    suspend fun load(context: Context): List<AIChatMessage> {
        return context.aiChatDataStore.data
            .map { prefs ->
                val text = prefs[HISTORY]
                if (text.isNullOrBlank()) emptyList()
                else runCatching { parseMessages(text) }.getOrDefault(emptyList())
            }
            .first()
    }

    suspend fun save(context: Context, messages: List<AIChatMessage>) {
        val json = runCatching { toJson(messages) }.getOrNull()
        if (json != null) {
            context.aiChatDataStore.edit { prefs ->
                prefs[HISTORY] = json
            }
        }
    }

    suspend fun clear(context: Context) {
        context.aiChatDataStore.edit { prefs ->
            prefs.remove(HISTORY)
        }
    }

    private fun parseMessages(text: String): List<AIChatMessage> {
        val arr = JSONArray(text)
        val list = mutableListOf<AIChatMessage>()
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            val id = o.optInt("id")
            val message = o.optString("message")
            val isUser = o.optBoolean("isUser")
            val timestamp = o.optString("timestamp")
            list += AIChatMessage(id = id, message = message, isUser = isUser, timestamp = timestamp)
        }
        return list
    }

    private fun toJson(messages: List<AIChatMessage>): String {
        val arr = JSONArray()
        messages.forEach { m ->
            val o = JSONObject()
            o.put("id", m.id)
            o.put("message", m.message)
            o.put("isUser", m.isUser)
            o.put("timestamp", m.timestamp)
            arr.put(o)
        }
        return arr.toString()
    }
}
