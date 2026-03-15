package com.u4e50.rikkahub.ui.pages.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.rerere.ai.core.MessageRole
import me.rerere.ai.ui.UIMessage
import me.rerere.ai.ui.UIMessagePart
import com.u4e50.rikkahub.data.datastore.DEFAULT_ASSISTANT_ID
import com.u4e50.rikkahub.data.datastore.Settings
import com.u4e50.rikkahub.data.datastore.SettingsStore
import com.u4e50.rikkahub.data.model.Conversation
import com.u4e50.rikkahub.data.model.MessageNode
import com.u4e50.rikkahub.data.repository.ConversationRepository
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.random.Random
import kotlin.uuid.Uuid

class DebugVM(
    private val settingsStore: SettingsStore,
    private val conversationRepository: ConversationRepository,
) : ViewModel() {
    val settings: StateFlow<Settings> = settingsStore.settingsFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, Settings.dummy())

    fun updateSettings(settings: Settings) {
        viewModelScope.launch {
            settingsStore.update(settings)
        }
    }

    /**
     * 鍒涘缓涓€涓秴澶х殑瀵硅瘽鐢ㄤ簬娴嬭瘯 CursorWindow 闄愬埗
     * @param sizeMB 鐩爣澶у皬锛圡B锛?
     */
    fun createOversizedConversation(sizeMB: Int = 3) {
        viewModelScope.launch {
            val targetSize = sizeMB * 1024 * 1024
            val messageNodes = mutableListOf<MessageNode>()
            var currentSize = 0

            // 鐢熸垚澶ч噺娑堟伅鐩村埌杈惧埌鐩爣澶у皬
            var index = 0
            while (currentSize < targetSize) {
                // 鐢熸垚涓€涓寘鍚ぇ閲忔枃鏈殑娑堟伅锛堢害 100KB 姣忔潯锛?
                val largeText = buildString {
                    repeat(100) {
                        append("This is a long debug text used to test CursorWindow size limits. ")
                        append("The 'Row too big to fit into CursorWindow' error usually appears when a single row grows too large. ")
                        append("Lorem ipsum dolor sit amet, consectetur adipiscing elit. ")
                        append("Index: $index, Block: $it. ")
                    }
                }

                val userMessage = UIMessage(
                    id = Uuid.random(),
                    role = MessageRole.USER,
                    parts = listOf(UIMessagePart.Text(largeText)),
                    createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                )
                val assistantMessage = UIMessage(
                    id = Uuid.random(),
                    role = MessageRole.ASSISTANT,
                    parts = listOf(UIMessagePart.Text("鍥炲: $largeText")),
                    createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                )

                messageNodes.add(MessageNode.of(userMessage))
                messageNodes.add(MessageNode.of(assistantMessage))

                currentSize += largeText.length * 2 * 2 // 澶х害浼扮畻
                index++
            }

            val conversation = Conversation(
                id = Uuid.random(),
                assistantId = DEFAULT_ASSISTANT_ID,
                title = "Oversized conversation test (${sizeMB}MB)",
                messageNodes = messageNodes,
            )

            conversationRepository.insertConversation(conversation)
        }
    }

    fun createConversationWithMessages(messageCount: Int = 1024) {
        viewModelScope.launch {
            val messageNodes = ArrayList<MessageNode>(messageCount)
            val timeZone = TimeZone.currentSystemDefault()
            repeat(messageCount) { index ->
                val role = if (index % 2 == 0) MessageRole.USER else MessageRole.ASSISTANT
                val message = UIMessage(
                    id = Uuid.random(),
                    role = role,
                    parts = listOf(UIMessagePart.Text(randomMessageText(index, role))),
                    createdAt = Clock.System.now().toLocalDateTime(timeZone),
                )
                messageNodes.add(MessageNode.of(message))
            }

            val conversation = Conversation(
                id = Uuid.random(),
                assistantId = DEFAULT_ASSISTANT_ID,
                title = "${messageCount} message test",
                messageNodes = messageNodes,
            )

            conversationRepository.insertConversation(conversation)
        }
    }

    private fun randomMessageText(index: Int, role: MessageRole): String {
        val fragments = listOf(
            "fast", "random", "message", "sample", "used", "for", "testing", "list", "render", "scroll",
            "chat", "conversation", "content", "structure", "verify", "paging", "order", "stable", "system",
        )
        val wordCount = Random.nextInt(6, 14)
        val prefix = if (role == MessageRole.USER) "User" else "Assistant"
        val body = List(wordCount) { fragments.random() }.joinToString(" ")
        return "$prefix#${index + 1}: $body"
    }
}

