package com.reap.domain.repository

interface ChatRepository {
    suspend fun postQuestion(question : String) : String?
}