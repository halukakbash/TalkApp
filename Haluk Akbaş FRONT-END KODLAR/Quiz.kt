package com.halukakbash.talk_app.data

data class Quiz(
    val id: String,
    val title: String,
    val category: String,
    val difficulty: String,
    val questions: List<Question>,
    val timeLimit: Int? = null, // in seconds, null means no time limit
    val passingScore: Int = 70 // percentage needed to pass
)

data class Question(
    val id: String,
    val text: String,
    val type: QuestionType,
    val options: List<String>,
    val correctAnswer: String,
    val explanation: String? = null,
    val audioUrl: String? = null // for listening questions
)

enum class QuestionType {
    MULTIPLE_CHOICE,
    TRUE_FALSE,
    LISTENING,
    SPEAKING,
    FILL_IN_BLANK
}

data class QuizResult(
    val quizId: String,
    val userId: String,
    val score: Int,
    val timeSpent: Int,
    val completedAt: Long = System.currentTimeMillis(),
    val incorrectQuestions: List<String> // List of question IDs
) 