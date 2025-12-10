package com.example.healthapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthapp.data.repository.AIChatRepository
import com.example.healthapp.model.AIChatMessage
import android.content.Context
import com.example.healthapp.data.AIChatStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AIChatViewModel(private val repo: AIChatRepository = AIChatRepository()) : ViewModel() {
    private val _messages = MutableStateFlow<List<AIChatMessage>>(emptyList())
    val messages: StateFlow<List<AIChatMessage>> = _messages

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _source = MutableStateFlow<String?>(null)
    val source: StateFlow<String?> = _source

    private val _forceModel = MutableStateFlow(false)
    val forceModel: StateFlow<Boolean> = _forceModel

    private val _preferStreaming = MutableStateFlow(false)
    val preferStreaming: StateFlow<Boolean> = _preferStreaming

    fun setForceModel(v: Boolean) { _forceModel.value = v }
    fun setPreferStreaming(v: Boolean) { _preferStreaming.value = v }

    init {
        val initMsg = AIChatMessage(1, "你好，我是你的AI健康助手。可以咨询运动、饮食、睡眠等问题。", false, "现在")
        _messages.value = listOf(initMsg)
        _forceModel.value = true
    }

    fun send(message: String, context: Context) {
        if (message.isBlank()) return
        val userMsg = AIChatMessage(_messages.value.size + 1, message, true, "现在")
        _messages.value = _messages.value + userMsg
        val typingMsg = AIChatMessage(_messages.value.size + 1, "正在输入…", false, "现在")
        _messages.value = _messages.value + typingMsg
        _loading.value = true
        _error.value = null
        viewModelScope.launch {
            var streamText = ""
            val reply = runCatching {
                repo.sendMessage(
                    _messages.value,
                    message,
                    onToken = { token ->
                        viewModelScope.launch {
                            streamText += token
                            val cur = _messages.value.toMutableList()
                            if (cur.isNotEmpty()) {
                                val last = cur.last()
                                cur[cur.lastIndex] = AIChatMessage(last.id, streamText, false, "现在")
                                _messages.value = cur
                            }
                        }
                    },
                    force = _forceModel.value,
                    preferStreaming = _preferStreaming.value,
                    context = context
                )
            }
                .onFailure { _error.value = it.message }
                .getOrNull()
            val current = _messages.value
            if (reply != null) {
                if (reply.source == "spark_ws") {
                    val cur = current.toMutableList()
                    if (cur.isNotEmpty()) {
                        val last = cur.last()
                        cur[cur.lastIndex] = AIChatMessage(last.id, reply.text, false, "现在")
                        _messages.value = cur
                    } else {
                        _messages.value = listOf(AIChatMessage(1, reply.text, false, "现在"))
                    }
                } else {
                    val withoutTyping = if (current.isNotEmpty()) current.dropLast(1) else current
                    _messages.value = withoutTyping + AIChatMessage(withoutTyping.size + 1, reply.text, false, "现在")
                }
            } else {
                val withoutTyping = if (current.isNotEmpty()) current.dropLast(1) else current
                _messages.value = withoutTyping
            }
            _source.value = reply?.source
            runCatching { AIChatStore.clear(context) }
            _loading.value = false
        }
    }

    fun restore(context: Context) {
        viewModelScope.launch {
            val saved = runCatching { AIChatStore.load(context) }.getOrNull()
            if (!saved.isNullOrEmpty()) {
                _messages.value = saved
            }
        }
    }

    fun clear(context: Context) {
        viewModelScope.launch {
            _loading.value = false
            _error.value = null
            _source.value = null
            _messages.value = listOf(AIChatMessage(1, "你好，我是你的AI健康助手。可以咨询运动、饮食、睡眠等问题。", false, "现在"))
            runCatching { AIChatStore.clear(context) }
        }
    }
}
