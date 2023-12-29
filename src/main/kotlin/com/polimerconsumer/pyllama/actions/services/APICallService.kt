package com.polimerconsumer.pyllama.actions.services

import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.intellij.openapi.components.Service
import kotlinx.coroutines.runBlocking

@Service
class APICallService {
    fun callChatGPTAPI(methodCode: String): String {
        val apiKey: String = System.getenv("API_KEY") ?: System.getProperty("API_KEY") ?: ""
        val compressedMethodCode = methodCode.replace(Regex("\\s\\s+"), " ").trim()
        val prompt =
            "You will be provided a method coded in python, and your task is to explain this code in a concise way:\n$compressedMethodCode"
        val openAI = OpenAI(token = apiKey)
        val chatCompletionRequest = ChatCompletionRequest(
            model = ModelId("gpt-3.5-turbo"),
            messages = listOf(
                ChatMessage(
                    role = ChatRole.System,
                    content = "You are a python code explainer"
                ),
                ChatMessage(
                    role = ChatRole.User,
                    content = prompt
                )
            )
        )
        val completion: ChatCompletion
        runBlocking { completion = openAI.chatCompletion(chatCompletionRequest) }
        return completion.choices.first().message.messageContent.toString().substring(20)
    }
}