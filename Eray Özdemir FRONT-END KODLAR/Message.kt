data class Message(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val content: String = "",
    val timestamp: Long = 0,
    val isRead: Boolean = false
) {
    // Add no-arg constructor for Firestore
    constructor() : this("", "", "", "", 0, false)
} 