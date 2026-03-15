package com.u4e50.rikkahub.data.ai.transformers

import me.rerere.ai.core.MessageRole
import me.rerere.ai.ui.UIMessage
import me.rerere.ai.ui.UIMessagePart
import com.u4e50.rikkahub.data.model.Assistant
import com.u4e50.rikkahub.data.model.InjectionPosition
import com.u4e50.rikkahub.data.model.PromptInjection
import com.u4e50.rikkahub.data.model.Lorebook
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.uuid.Uuid

class PromptInjectionTransformerTest {

    // region Helper functions
    private fun createAssistant(
        modeInjectionIds: Set<Uuid> = emptySet(),
        lorebookIds: Set<Uuid> = emptySet()
    ) = Assistant(
        modeInjectionIds = modeInjectionIds,
        lorebookIds = lorebookIds
    )

    private fun createModeInjection(
        id: Uuid = Uuid.random(),
        name: String = "Test Injection",
        enabled: Boolean = true,
        priority: Int = 0,
        position: InjectionPosition = InjectionPosition.AFTER_SYSTEM_PROMPT,
        content: String = "Injected content",
        injectDepth: Int = 4,
        role: MessageRole = MessageRole.USER
    ) = PromptInjection.ModeInjection(
        id = id,
        name = name,
        enabled = enabled,
        priority = priority,
        position = position,
        content = content,
        injectDepth = injectDepth,
        role = role
    )

    private fun createRegexInjection(
        id: Uuid = Uuid.random(),
        name: String = "Test Regex",
        enabled: Boolean = true,
        priority: Int = 0,
        position: InjectionPosition = InjectionPosition.AFTER_SYSTEM_PROMPT,
        content: String = "Regex injected content",
        injectDepth: Int = 4,
        role: MessageRole = MessageRole.USER,
        keywords: List<String> = listOf("trigger"),
        useRegex: Boolean = false,
        caseSensitive: Boolean = false,
        scanDepth: Int = 5,
        constantActive: Boolean = false
    ) = PromptInjection.RegexInjection(
        id = id,
        name = name,
        enabled = enabled,
        priority = priority,
        position = position,
        content = content,
        injectDepth = injectDepth,
        role = role,
        keywords = keywords,
        useRegex = useRegex,
        caseSensitive = caseSensitive,
        scanDepth = scanDepth,
        constantActive = constantActive
    )

    private fun createLorebook(
        id: Uuid = Uuid.random(),
        name: String = "Test Lorebook",
        enabled: Boolean = true,
        entries: List<PromptInjection.RegexInjection> = emptyList()
    ) = Lorebook(
        id = id,
        name = name,
        enabled = enabled,
        entries = entries
    )

    private fun getMessageText(message: UIMessage): String {
        return message.parts
            .filterIsInstance<UIMessagePart.Text>()
            .joinToString("") { it.text }
    }

    private fun createAssistantWithUnexecutedTool(toolCallId: String, toolName: String): UIMessage {
        return UIMessage(
            role = MessageRole.ASSISTANT,
            parts = listOf(
                UIMessagePart.Tool(
                    toolCallId = toolCallId,
                    toolName = toolName,
                    input = "{}",
                    output = emptyList()
                )
            )
        )
    }

    private fun createAssistantWithExecutedTool(toolCallId: String, toolName: String): UIMessage {
        return UIMessage(
            role = MessageRole.ASSISTANT,
            parts = listOf(
                UIMessagePart.Tool(
                    toolCallId = toolCallId,
                    toolName = toolName,
                    input = "{}",
                    output = listOf(UIMessagePart.Text("result"))
                )
            )
        )
    }
    // endregion

    // region No injection tests
    @Test
    fun `no injections should return original messages`() {
        val messages = listOf(
            UIMessage.system("System prompt"),
            UIMessage.user("Hello"),
            UIMessage.assistant("Hi there!")
        )

        val result = transformMessages(
            messages = messages,
            assistant = createAssistant(),
            modeInjections = emptyList(),
            lorebooks = emptyList()
        )

        assertEquals(messages, result)
    }

    @Test
    fun `disabled mode injection should not be applied`() {
        val injectionId = Uuid.random()
        val injection = createModeInjection(
            id = injectionId,
            enabled = false,
            content = "Should not appear"
        )

        val messages = listOf(
            UIMessage.system("System prompt"),
            UIMessage.user("Hello")
        )

        val result = transformMessages(
            messages = messages,
            assistant = createAssistant(modeInjectionIds = setOf(injectionId)),
            modeInjections = listOf(injection),
            lorebooks = emptyList()
        )

        assertEquals(messages, result)
    }

    @Test
    fun `unlinked mode injection should not be applied`() {
        val injection = createModeInjection(content = "Should not appear")

        val messages = listOf(
            UIMessage.system("System prompt"),
            UIMessage.user("Hello")
        )

        val result = transformMessages(
            messages = messages,
            assistant = createAssistant(), // No linked injections
            modeInjections = listOf(injection),
            lorebooks = emptyList()
        )

        assertEquals(messages, result)
    }
    // endregion

    // region AFTER_SYSTEM_PROMPT tests
    @Test
    fun `mode injection with AFTER_SYSTEM_PROMPT should append to system message`() {
        val injectionId = Uuid.random()
        val injection = createModeInjection(
            id = injectionId,
            position = InjectionPosition.AFTER_SYSTEM_PROMPT,
            content = "Appended content"
        )

        val messages = listOf(
            UIMessage.system("Original system prompt"),
            UIMessage.user("Hello")
        )

        val result = transformMessages(
            messages = messages,
            assistant = createAssistant(modeInjectionIds = setOf(injectionId)),
            modeInjections = listOf(injection),
            lorebooks = emptyList()
        )

        assertEquals(2, result.size)
        val systemText = getMessageText(result[0])
        assertTrue(systemText.startsWith("Original system prompt"))
        assertTrue(systemText.endsWith("Appended content"))
    }
    // endregion

    // region BEFORE_SYSTEM_PROMPT tests
    @Test
    fun `mode injection with BEFORE_SYSTEM_PROMPT should prepend to system message`() {
        val injectionId = Uuid.random()
        val injection = createModeInjection(
            id = injectionId,
            position = InjectionPosition.BEFORE_SYSTEM_PROMPT,
            content = "Prepended content"
        )

        val messages = listOf(
            UIMessage.system("Original system prompt"),
            UIMessage.user("Hello")
        )

        val result = transformMessages(
            messages = messages,
            assistant = createAssistant(modeInjectionIds = setOf(injectionId)),
            modeInjections = listOf(injection),
            lorebooks = emptyList()
        )

        assertEquals(2, result.size)
        val systemText = getMessageText(result[0])
        assertTrue(systemText.startsWith("Prepended content"))
        assertTrue(systemText.contains("Original system prompt"))
    }

    @Test
    fun `injection without existing system message should create new system message`() {
        val injectionId = Uuid.random()
        val injection = createModeInjection(
            id = injectionId,
            position = InjectionPosition.AFTER_SYSTEM_PROMPT,
            content = "New system content"
        )

        val messages = listOf(
            UIMessage.user("Hello"),
            UIMessage.assistant("Hi!")
        )

        val result = transformMessages(
            messages = messages,
            assistant = createAssistant(modeInjectionIds = setOf(injectionId)),
            modeInjections = listOf(injection),
            lorebooks = emptyList()
        )

        assertEquals(3, result.size)
        assertEquals(MessageRole.SYSTEM, result[0].role)
        assertEquals("New system content", getMessageText(result[0]))
    }
    // endregion

    // region TOP_OF_CHAT tests
    @Test
    fun `mode injection with TOP_OF_CHAT should insert before first user message`() {
        val injectionId = Uuid.random()
        val injection = createModeInjection(
            id = injectionId,
            position = InjectionPosition.TOP_OF_CHAT,
            content = "Top of chat content"
        )

        val messages = listOf(
            UIMessage.system("System prompt"),
            UIMessage.user("Hello"),
            UIMessage.assistant("Hi!")
        )

        val result = transformMessages(
            messages = messages,
            assistant = createAssistant(modeInjectionIds = setOf(injectionId)),
            modeInjections = listOf(injection),
            lorebooks = emptyList()
        )

        assertEquals(4, result.size)
        assertEquals(MessageRole.SYSTEM, result[0].role)
        assertEquals("System prompt", getMessageText(result[0]))
        assertEquals(MessageRole.USER, result[1].role)
        assertEquals("Top of chat content", getMessageText(result[1]))
        assertEquals(MessageRole.USER, result[2].role)
    }
    // endregion

    // region BOTTOM_OF_CHAT tests
    @Test
    fun `mode injection with BOTTOM_OF_CHAT should insert before last message`() {
        val injectionId = Uuid.random()
        val injection = createModeInjection(
            id = injectionId,
            position = InjectionPosition.BOTTOM_OF_CHAT,
            content = "Bottom of chat content"
        )

        val messages = listOf(
            UIMessage.system("System prompt"),
            UIMessage.user("Hello"),
            UIMessage.assistant("Hi!"),
            UIMessage.user("How are you?")
        )

        val result = transformMessages(
            messages = messages,
            assistant = createAssistant(modeInjectionIds = setOf(injectionId)),
            modeInjections = listOf(injection),
            lorebooks = emptyList()
        )

        assertEquals(5, result.size)
        assertEquals(MessageRole.USER, result[3].role)
        assertEquals("Bottom of chat content", getMessageText(result[3]))
        assertEquals(MessageRole.USER, result[4].role)
        assertEquals("How are you?", getMessageText(result[4]))
    }
    // endregion

    // region AT_DEPTH tests
    @Test
    fun `mode injection with AT_DEPTH should insert at specified depth from end`() {
        val injectionId = Uuid.random()
        val injection = createModeInjection(
            id = injectionId,
            position = InjectionPosition.AT_DEPTH,
            injectDepth = 2,
            content = "At depth 2 content"
        )

        val messages = listOf(
            UIMessage.system("System prompt"),
            UIMessage.user("Message 1"),
            UIMessage.assistant("Response 1"),
            UIMessage.user("Message 2"),
            UIMessage.assistant("Response 2")
        )

        val result = transformMessages(
            messages = messages,
            assistant = createAssistant(modeInjectionIds = setOf(injectionId)),
            modeInjections = listOf(injection),
            lorebooks = emptyList()
        )

        // depth=2 means insert before the 2nd message from the end
        // Original: [System, User1, Asst1, User2, Asst2] (5 messages)
        // Insert at index 5-2=3, so: [System, User1, Asst1, Injected, User2, Asst2]
        assertEquals(6, result.size)
        assertEquals(MessageRole.USER, result[3].role)
        assertEquals("At depth 2 content", getMessageText(result[3]))
        assertEquals(MessageRole.USER, result[4].role)
        assertEquals("Message 2", getMessageText(result[4]))
    }

    @Test
    fun `AT_DEPTH with depth 1 should insert before last message`() {
        val injectionId = Uuid.random()
        val injection = createModeInjection(
            id = injectionId,
            position = InjectionPosition.AT_DEPTH,
            injectDepth = 1,
            content = "Before last"
        )

        val messages = listOf(
            UIMessage.system("System prompt"),
            UIMessage.user("Hello"),
            UIMessage.assistant("Hi!")
        )

        val result = transformMessages(
            messages = messages,
            assistant = createAssistant(modeInjectionIds = setOf(injectionId)),
            modeInjections = listOf(injection),
            lorebooks = emptyList()
        )

        assertEquals(4, result.size)
        assertEquals("Before last", getMessageText(result[2]))
        assertEquals("Hi!", getMessageText(result[3]))
    }

    @Test
    fun `AT_DEPTH with depth larger than message count should insert at beginning`() {
        val injectionId = Uuid.random()
        val injection = createModeInjection(
            id = injectionId,
            position = InjectionPosition.AT_DEPTH,
            injectDepth = 100,
            content = "Large depth content"
        )

        val messages = listOf(
            UIMessage.system("System prompt"),
            UIMessage.user("Hello")
        )

        val result = transformMessages(
            messages = messages,
            assistant = createAssistant(modeInjectionIds = setOf(injectionId)),
            modeInjections = listOf(injection),
            lorebooks = emptyList()
        )

        assertEquals(3, result.size)
        assertEquals("Large depth content", getMessageText(result[0]))
    }

    @Test
    fun `multiple AT_DEPTH injections with different depths should all apply`() {
        val id1 = Uuid.random()
        val id2 = Uuid.random()

        val injections = listOf(
            createModeInjection(
                id = id1,
                position = InjectionPosition.AT_DEPTH,
                injectDepth = 1,
                content = "Depth 1"
            ),
            createModeInjection(
                id = id2,
                position = InjectionPosition.AT_DEPTH,
                injectDepth = 3,
                content = "Depth 3"
            )
        )

        val messages = listOf(
            UIMessage.system("System prompt"),
            UIMessage.user("Message 1"),
            UIMessage.assistant("Response 1"),
            UIMessage.user("Message 2"),
            UIMessage.assistant("Response 2")
        )

        val result = transformMessages(
            messages = messages,
            assistant = createAssistant(modeInjectionIds = setOf(id1, id2)),
            modeInjections = injections,
            lorebooks = emptyList()
        )

        // Both should be inserted
        assertEquals(7, result.size)
        assertTrue(result.any { getMessageText(it).contains("Depth 1") })
        assertTrue(result.any { getMessageText(it).contains("Depth 3") })
    }

    @Test
    fun `multiple AT_DEPTH injections with same depth should be merged`() {
        val id1 = Uuid.random()
        val id2 = Uuid.random()

        val injections = listOf(
            createModeInjection(
                id = id1,
                position = InjectionPosition.AT_DEPTH,
                injectDepth = 2,
                priority = 10,
                content = "Higher priority"
            ),
            createModeInjection(
                id = id2,
                position = InjectionPosition.AT_DEPTH,
                injectDepth = 2,
                priority = 5,
                content = "Lower priority"
            )
        )

        val messages = listOf(
            UIMessage.system("System prompt"),
            UIMessage.user("Hello"),
            UIMessage.assistant("Hi!")
        )

        val result = transformMessages(
            messages = messages,
            assistant = createAssistant(modeInjectionIds = setOf(id1, id2)),
            modeInjections = injections,
            lorebooks = emptyList()
        )

        // Same depth injections should be merged into one message
        assertEquals(4, result.size)
        val injectedText = getMessageText(result[1])
        assertTrue(injectedText.contains("Higher priority"))
        assertTrue(injectedText.contains("Lower priority"))
        // Higher priority should come first
        assertTrue(injectedText.indexOf("Higher priority") < injectedText.indexOf("Lower priority"))
    }
    // endregion

    // region Priority tests
    @Test
    fun `injections should be ordered by priority descending`() {
        val id1 = Uuid.random()
        val id2 = Uuid.random()
        val id3 = Uuid.random()

        val injections = listOf(
            createModeInjection(id = id1, priority = 1, content = "Priority 1"),
            createModeInjection(id = id2, priority = 3, content = "Priority 3"),
            createModeInjection(id = id3, priority = 2, content = "Priority 2")
        )

        val messages = listOf(
            UIMessage.system("System"),
            UIMessage.user("Hello")
        )

        val result = transformMessages(
            messages = messages,
            assistant = createAssistant(modeInjectionIds = setOf(id1, id2, id3)),
            modeInjections = injections,
            lorebooks = emptyList()
        )

        val systemText = getMessageText(result[0])
        // Higher priority should come first when joining
        assertTrue(systemText.contains("Priority 3"))
        assertTrue(systemText.indexOf("Priority 3") < systemText.indexOf("Priority 2"))
        assertTrue(systemText.indexOf("Priority 2") < systemText.indexOf("Priority 1"))
    }
    // endregion

    // region Lorebook tests
    @Test
    fun `lorebook with keyword match should trigger injection`() {
        val lorebookId = Uuid.random()
        val regexInjection = createRegexInjection(
            keywords = listOf("magic"),
            content = "Magic system explanation"
        )
        val lorebook = createLorebook(
            id = lorebookId,
            entries = listOf(regexInjection)
        )

        val messages = listOf(
            UIMessage.system("System prompt"),
            UIMessage.user("Tell me about magic")
        )

        val result = transformMessages(
            messages = messages,
            assistant = createAssistant(lorebookIds = setOf(lorebookId)),
            modeInjections = emptyList(),
            lorebooks = listOf(lorebook)
        )

        val systemText = getMessageText(result[0])
        assertTrue(systemText.contains("Magic system explanation"))
    }

    @Test
    fun `lorebook without keyword match should not trigger injection`() {
        val lorebookId = Uuid.random()
        val regexInjection = createRegexInjection(
            keywords = listOf("magic"),
            content = "Should not appear"
        )
        val lorebook = createLorebook(
            id = lorebookId,
            entries = listOf(regexInjection)
        )

        val messages = listOf(
            UIMessage.system("System prompt"),
            UIMessage.user("Tell me about science")
        )

        val result = transformMessages(
            messages = messages,
            assistant = createAssistant(lorebookIds = setOf(lorebookId)),
            modeInjections = emptyList(),
            lorebooks = listOf(lorebook)
        )

        assertEquals(2, result.size)
        val systemText = getMessageText(result[0])
        assertEquals("System prompt", systemText)
    }

    @Test
    fun `lorebook with constantActive should always trigger`() {
        val lorebookId = Uuid.random()
        val regexInjection = createRegexInjection(
            keywords = emptyList(),
            constantActive = true,
            content = "Always active content"
        )
        val lorebook = createLorebook(
            id = lorebookId,
            entries = listOf(regexInjection)
        )

        val messages = listOf(
            UIMessage.system("System prompt"),
            UIMessage.user("Any message")
        )

        val result = transformMessages(
            messages = messages,
            assistant = createAssistant(lorebookIds = setOf(lorebookId)),
            modeInjections = emptyList(),
            lorebooks = listOf(lorebook)
        )

        val systemText = getMessageText(result[0])
        assertTrue(systemText.contains("Always active content"))
    }

    @Test
    fun `lorebook with case insensitive match should trigger`() {
        val lorebookId = Uuid.random()
        val regexInjection = createRegexInjection(
            keywords = listOf("MAGIC"),
            caseSensitive = false,
            content = "Case insensitive match"
        )
        val lorebook = createLorebook(
            id = lorebookId,
            entries = listOf(regexInjection)
        )

        val messages = listOf(
            UIMessage.system("System prompt"),
            UIMessage.user("tell me about magic")
        )

        val result = transformMessages(
            messages = messages,
            assistant = createAssistant(lorebookIds = setOf(lorebookId)),
            modeInjections = emptyList(),
            lorebooks = listOf(lorebook)
        )

        val systemText = getMessageText(result[0])
        assertTrue(systemText.contains("Case insensitive match"))
    }

    @Test
    fun `lorebook with case sensitive match should not trigger on different case`() {
        val lorebookId = Uuid.random()
        val regexInjection = createRegexInjection(
            keywords = listOf("MAGIC"),
            caseSensitive = true,
            content = "Should not appear"
        )
        val lorebook = createLorebook(
            id = lorebookId,
            entries = listOf(regexInjection)
        )

        val messages = listOf(
            UIMessage.system("System prompt"),
            UIMessage.user("tell me about magic")
        )

        val result = transformMessages(
            messages = messages,
            assistant = createAssistant(lorebookIds = setOf(lorebookId)),
            modeInjections = emptyList(),
            lorebooks = listOf(lorebook)
        )

        assertEquals(2, result.size)
        val systemText = getMessageText(result[0])
        assertEquals("System prompt", systemText)
    }

    @Test
    fun `lorebook with regex pattern should match`() {
        val lorebookId = Uuid.random()
        val regexInjection = createRegexInjection(
            keywords = listOf("mag.*spell"),
            useRegex = true,
            content = "Regex match content"
        )
        val lorebook = createLorebook(
            id = lorebookId,
            entries = listOf(regexInjection)
        )

        val messages = listOf(
            UIMessage.system("System prompt"),
            UIMessage.user("Can you explain magic and spell casting?")
        )

        val result = transformMessages(
            messages = messages,
            assistant = createAssistant(lorebookIds = setOf(lorebookId)),
            modeInjections = emptyList(),
            lorebooks = listOf(lorebook)
        )

        val systemText = getMessageText(result[0])
        assertTrue(systemText.contains("Regex match content"))
    }

    @Test
    fun `scanDepth should limit message scanning range`() {
        val lorebookId = Uuid.random()
        val regexInjection = createRegexInjection(
            keywords = listOf("old keyword"),
            scanDepth = 2, // 鍙壂鎻忔渶杩?鏉℃秷鎭?
            content = "Should not appear"
        )
        val lorebook = createLorebook(
            id = lorebookId,
            entries = listOf(regexInjection)
        )

        val messages = listOf(
            UIMessage.system("System prompt"),
            UIMessage.user("Message with old keyword"), // 绗?鏉＄敤鎴锋秷鎭紙瓒呭嚭鎵弿鑼冨洿锛?
            UIMessage.assistant("Response 1"),
            UIMessage.user("Message 2"),
            UIMessage.assistant("Response 2"),
            UIMessage.user("Latest message") // 鏈€杩戠殑娑堟伅锛屼笉鍖呭惈鍏抽敭璇?
        )

        val result = transformMessages(
            messages = messages,
            assistant = createAssistant(lorebookIds = setOf(lorebookId)),
            modeInjections = emptyList(),
            lorebooks = listOf(lorebook)
        )

        // 鍏抽敭璇嶅湪绗?鏉＄敤鎴锋秷鎭腑锛屼絾 scanDepth=2 鍙壂鎻忔渶鍚?鏉?
        // 鎵€浠ヤ笉搴旇瑙﹀彂娉ㄥ叆
        assertEquals(6, result.size)
        val systemText = getMessageText(result[0])
        assertEquals("System prompt", systemText)
    }

    @Test
    fun `scanDepth should trigger when keyword is within range`() {
        val lorebookId = Uuid.random()
        val regexInjection = createRegexInjection(
            keywords = listOf("latest"),
            scanDepth = 2,
            content = "Triggered content"
        )
        val lorebook = createLorebook(
            id = lorebookId,
            entries = listOf(regexInjection)
        )

        val messages = listOf(
            UIMessage.system("System prompt"),
            UIMessage.user("Old message"),
            UIMessage.assistant("Response"),
            UIMessage.user("This is the latest message") // 鍦ㄦ壂鎻忚寖鍥村唴
        )

        val result = transformMessages(
            messages = messages,
            assistant = createAssistant(lorebookIds = setOf(lorebookId)),
            modeInjections = emptyList(),
            lorebooks = listOf(lorebook)
        )

        val systemText = getMessageText(result[0])
        assertTrue(systemText.contains("Triggered content"))
    }

    @Test
    fun `different entries should use their own scanDepth`() {
        val lorebookId = Uuid.random()
        val shallowEntry = createRegexInjection(
            keywords = listOf("old keyword"),
            scanDepth = 1, // 鍙壂鎻忔渶鍚?鏉?
            content = "Shallow scan content"
        )
        val deepEntry = createRegexInjection(
            keywords = listOf("old keyword"),
            scanDepth = 10, // 鎵弿鏈€鍚?0鏉?
            content = "Deep scan content"
        )
        val lorebook = createLorebook(
            id = lorebookId,
            entries = listOf(shallowEntry, deepEntry)
        )

        val messages = listOf(
            UIMessage.system("System prompt"),
            UIMessage.user("Message with old keyword"), // 杈冩棭鐨勬秷鎭?
            UIMessage.assistant("Response 1"),
            UIMessage.user("Response 2"),
            UIMessage.assistant("Response 3"),
            UIMessage.user("Latest message without keyword")
        )

        val result = transformMessages(
            messages = messages,
            assistant = createAssistant(lorebookIds = setOf(lorebookId)),
            modeInjections = emptyList(),
            lorebooks = listOf(lorebook)
        )

        val systemText = getMessageText(result[0])
        // shallowEntry (scanDepth=1) 涓嶅簲瑙﹀彂锛屽洜涓烘渶鍚?鏉℃秷鎭笉鍚叧閿瘝
        assertTrue(!systemText.contains("Shallow scan content"))
        // deepEntry (scanDepth=10) 搴旇瑙﹀彂锛屽洜涓烘棭鏈熸秷鎭寘鍚叧閿瘝
        assertTrue(systemText.contains("Deep scan content"))
    }

    @Test
    fun `disabled world book should not trigger`() {
        val lorebookId = Uuid.random()
        val regexInjection = createRegexInjection(
            keywords = listOf("magic"),
            content = "Should not appear"
        )
        val lorebook = createLorebook(
            id = lorebookId,
            enabled = false,
            entries = listOf(regexInjection)
        )

        val messages = listOf(
            UIMessage.system("System prompt"),
            UIMessage.user("Tell me about magic")
        )

        val result = transformMessages(
            messages = messages,
            assistant = createAssistant(lorebookIds = setOf(lorebookId)),
            modeInjections = emptyList(),
            lorebooks = listOf(lorebook)
        )

        assertEquals(2, result.size)
        val systemText = getMessageText(result[0])
        assertEquals("System prompt", systemText)
    }
    // endregion

    // region Multiple injections tests
    @Test
    fun `multiple injections at different positions should all apply`() {
        val id1 = Uuid.random()
        val id2 = Uuid.random()
        val id3 = Uuid.random()

        val injections = listOf(
            createModeInjection(
                id = id1,
                position = InjectionPosition.BEFORE_SYSTEM_PROMPT,
                content = "Before"
            ),
            createModeInjection(
                id = id2,
                position = InjectionPosition.AFTER_SYSTEM_PROMPT,
                content = "After"
            ),
            createModeInjection(
                id = id3,
                position = InjectionPosition.TOP_OF_CHAT,
                content = "Top"
            )
        )

        val messages = listOf(
            UIMessage.system("System"),
            UIMessage.user("Hello")
        )

        val result = transformMessages(
            messages = messages,
            assistant = createAssistant(modeInjectionIds = setOf(id1, id2, id3)),
            modeInjections = injections,
            lorebooks = emptyList()
        )

        assertEquals(3, result.size)
        val systemText = getMessageText(result[0])
        assertTrue(systemText.startsWith("Before"))
        assertTrue(systemText.contains("System"))
        assertTrue(systemText.endsWith("After"))
        assertEquals("Top", getMessageText(result[1]))
    }

    @Test
    fun `combined mode injection and world book should both apply`() {
        val modeId = Uuid.random()
        val lorebookId = Uuid.random()

        val modeInjection = createModeInjection(
            id = modeId,
            content = "Mode content"
        )

        val regexInjection = createRegexInjection(
            keywords = listOf("hello"),
            content = "WorldBook content"
        )
        val lorebook = createLorebook(
            id = lorebookId,
            entries = listOf(regexInjection)
        )

        val messages = listOf(
            UIMessage.system("System prompt"),
            UIMessage.user("hello world")
        )

        val result = transformMessages(
            messages = messages,
            assistant = createAssistant(
                modeInjectionIds = setOf(modeId),
                lorebookIds = setOf(lorebookId)
            ),
            modeInjections = listOf(modeInjection),
            lorebooks = listOf(lorebook)
        )

        val systemText = getMessageText(result[0])
        assertTrue(systemText.contains("Mode content"))
        assertTrue(systemText.contains("WorldBook content"))
    }
    // endregion

    // region collectInjections tests
    @Test
    fun `collectInjections should return empty for no matching conditions`() {
        val result = collectInjections(
            messages = listOf(UIMessage.user("Hello")),
            assistant = createAssistant(),
            modeInjections = listOf(createModeInjection()),
            lorebooks = emptyList()
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `collectInjections should collect linked and enabled mode injections`() {
        val id1 = Uuid.random()
        val id2 = Uuid.random()

        val injections = listOf(
            createModeInjection(id = id1, enabled = true),
            createModeInjection(id = id2, enabled = false)
        )

        val result = collectInjections(
            messages = listOf(UIMessage.user("Hello")),
            assistant = createAssistant(modeInjectionIds = setOf(id1, id2)),
            modeInjections = injections,
            lorebooks = emptyList()
        )

        assertEquals(1, result.size)
        assertEquals(id1, result[0].id)
    }
    // endregion

    // region applyInjections tests
    @Test
    fun `applyInjections with empty map should return original messages`() {
        val messages = listOf(
            UIMessage.system("System"),
            UIMessage.user("Hello")
        )

        val result = applyInjections(messages, emptyMap())

        assertEquals(messages, result)
    }

    @Test
    fun `applyInjections should handle messages without system message`() {
        val injection = createModeInjection(
            position = InjectionPosition.BEFORE_SYSTEM_PROMPT,
            content = "Before content"
        )

        val messages = listOf(
            UIMessage.user("Hello"),
            UIMessage.assistant("Hi!")
        )

        val result = applyInjections(
            messages,
            mapOf(InjectionPosition.BEFORE_SYSTEM_PROMPT to listOf(injection))
        )

        assertEquals(3, result.size)
        assertEquals(MessageRole.SYSTEM, result[0].role)
        assertEquals("Before content", getMessageText(result[0]))
    }
    // endregion

    // region Tool call chain preservation tests
    @Test
    fun `BOTTOM_OF_CHAT should not break tool call chain`() {
        val injectionId = Uuid.random()
        val injection = createModeInjection(
            id = injectionId,
            position = InjectionPosition.BOTTOM_OF_CHAT,
            content = "Bottom injection"
        )

        // 娑堟伅搴忓垪: SYSTEM -> USER -> ASSISTANT(unexecuted tool) -> ASSISTANT(executed tool)
        val messages = listOf(
            UIMessage.system("System prompt"),
            UIMessage.user("Call a tool"),
            createAssistantWithUnexecutedTool("call_123", "test_tool"),
            createAssistantWithExecutedTool("call_123", "test_tool")
        )

        val result = transformMessages(
            messages = messages,
            assistant = createAssistant(modeInjectionIds = setOf(injectionId)),
            modeInjections = listOf(injection),
            lorebooks = emptyList()
        )

        // 娉ㄥ叆搴旇鍦ㄥ伐鍏疯皟鐢ㄩ摼涔嬪墠锛岃€屼笉鏄湪涓棿
        assertEquals(5, result.size)

        // 楠岃瘉宸ュ叿璋冪敤閾炬病鏈夎鐮村潖
        val unexecutedIndex = result.indexOfFirst { it.getTools().any { t -> !t.isExecuted } }
        val executedIndex = result.indexOfFirst { it.getTools().any { t -> t.isExecuted } }

        // executed tool 搴旇绱ц窡鍦?unexecuted tool 鍚庨潰
        assertEquals(unexecutedIndex + 1, executedIndex)

        // 娉ㄥ叆鐨勬秷鎭簲璇ュ湪宸ュ叿璋冪敤閾句箣鍓?
        val injectedIndex = result.indexOfFirst { getMessageText(it).contains("Bottom injection") }
        assertTrue(injectedIndex < unexecutedIndex)
    }

    @Test
    fun `AT_DEPTH should not break tool call chain`() {
        val injectionId = Uuid.random()
        val injection = createModeInjection(
            id = injectionId,
            position = InjectionPosition.AT_DEPTH,
            injectDepth = 1, // 灏濊瘯鍦ㄦ渶鍚庝竴鏉℃秷鎭箣鍓嶆彃鍏?
            content = "Depth injection"
        )

        // 娑堟伅搴忓垪: SYSTEM -> USER -> ASSISTANT(unexecuted tool) -> ASSISTANT(executed tool)
        val messages = listOf(
            UIMessage.system("System prompt"),
            UIMessage.user("Call a tool"),
            createAssistantWithUnexecutedTool("call_456", "test_tool"),
            createAssistantWithExecutedTool("call_456", "test_tool")
        )

        val result = transformMessages(
            messages = messages,
            assistant = createAssistant(modeInjectionIds = setOf(injectionId)),
            modeInjections = listOf(injection),
            lorebooks = emptyList()
        )

        assertEquals(5, result.size)

        // 楠岃瘉宸ュ叿璋冪敤閾炬病鏈夎鐮村潖
        val unexecutedIndex = result.indexOfFirst { it.getTools().any { t -> !t.isExecuted } }
        val executedIndex = result.indexOfFirst { it.getTools().any { t -> t.isExecuted } }
        assertEquals(unexecutedIndex + 1, executedIndex)
    }

    @Test
    fun `multiple tool calls should all be preserved`() {
        val injectionId = Uuid.random()
        val injection = createModeInjection(
            id = injectionId,
            position = InjectionPosition.AT_DEPTH,
            injectDepth = 2,
            content = "Depth injection"
        )

        // 娑堟伅搴忓垪: SYSTEM -> USER -> ASSISTANT(unexecuted1) -> ASSISTANT(executed1) -> ASSISTANT(unexecuted2) -> ASSISTANT(executed2)
        val messages = listOf(
            UIMessage.system("System prompt"),
            UIMessage.user("Call tools"),
            createAssistantWithUnexecutedTool("call_1", "tool_1"),
            createAssistantWithExecutedTool("call_1", "tool_1"),
            createAssistantWithUnexecutedTool("call_2", "tool_2"),
            createAssistantWithExecutedTool("call_2", "tool_2")
        )

        val result = transformMessages(
            messages = messages,
            assistant = createAssistant(modeInjectionIds = setOf(injectionId)),
            modeInjections = listOf(injection),
            lorebooks = emptyList()
        )

        // 楠岃瘉涓や釜宸ュ叿璋冪敤閾鹃兘娌℃湁琚牬鍧?
        val unexecuted = result.mapIndexedNotNull { index, msg ->
            if (msg.getTools().any { !it.isExecuted }) index else null
        }
        val executed = result.mapIndexedNotNull { index, msg ->
            if (msg.getTools().any { it.isExecuted }) index else null
        }

        assertEquals(2, unexecuted.size)
        assertEquals(2, executed.size)

        // 姣忎釜 unexecuted tool 鍚庨潰绱ц窡鐫€瀵瑰簲鐨?executed tool
        unexecuted.forEachIndexed { i, callIndex ->
            assertEquals(callIndex + 1, executed[i])
        }
    }

    @Test
    fun `findSafeInsertIndex should return safe position before tool call chain`() {
        val messages = listOf(
            UIMessage.system("System prompt"),
            UIMessage.user("Hello"),
            createAssistantWithUnexecutedTool("call_1", "tool"),
            createAssistantWithExecutedTool("call_1", "tool")
        )

        // 灏濊瘯鍦ㄧ储寮?3 (executed tool 浣嶇疆) 鎻掑叆
        val safeIndex = findSafeInsertIndex(messages, 3)
        // 搴旇杩斿洖 2 (unexecuted tool 涔嬪墠)
        assertEquals(2, safeIndex)
    }

    @Test
    fun `findSafeInsertIndex should handle consecutive tool call chains`() {
        val messages = listOf(
            UIMessage.system("System prompt"),
            UIMessage.user("Hello"),
            createAssistantWithUnexecutedTool("call_1", "tool1"),
            createAssistantWithExecutedTool("call_1", "tool1"),
            createAssistantWithUnexecutedTool("call_2", "tool2"),
            createAssistantWithExecutedTool("call_2", "tool2")
        )

        // 灏濊瘯鍦ㄧ储寮?5 (鏈€鍚庝竴涓?executed tool) 鎻掑叆
        val safeIndex = findSafeInsertIndex(messages, 5)
        // 搴旇杩斿洖 4 (绗簩涓?unexecuted tool 涔嬪墠)
        assertEquals(4, safeIndex)

        // 灏濊瘯鍦ㄧ储寮?3 (绗竴涓?executed tool) 鎻掑叆
        val safeIndex2 = findSafeInsertIndex(messages, 3)
        // 搴旇杩斿洖 2 (绗竴涓?unexecuted tool 涔嬪墠)
        assertEquals(2, safeIndex2)
    }

    @Test
    fun `findSafeInsertIndex should return original index when not in tool chain`() {
        val messages = listOf(
            UIMessage.system("System prompt"),
            UIMessage.user("Hello"),
            UIMessage.assistant("Hi!"),
            UIMessage.user("How are you?")
        )

        // 娌℃湁宸ュ叿璋冪敤锛屽簲璇ヨ繑鍥炲師绱㈠紩
        assertEquals(3, findSafeInsertIndex(messages, 3))
        assertEquals(2, findSafeInsertIndex(messages, 2))
        assertEquals(0, findSafeInsertIndex(messages, 0))
    }

    @Test
    fun `injection after completed tool chain should work normally`() {
        val injectionId = Uuid.random()
        val injection = createModeInjection(
            id = injectionId,
            position = InjectionPosition.BOTTOM_OF_CHAT,
            content = "Bottom injection"
        )

        // 娑堟伅搴忓垪: SYSTEM -> USER -> ASSISTANT(executed tool) -> ASSISTANT(final response) -> USER
        val messages = listOf(
            UIMessage.system("System prompt"),
            UIMessage.user("Call a tool"),
            createAssistantWithExecutedTool("call_1", "tool"),
            UIMessage.assistant("Here's the result"),
            UIMessage.user("Thanks!")
        )

        val result = transformMessages(
            messages = messages,
            assistant = createAssistant(modeInjectionIds = setOf(injectionId)),
            modeInjections = listOf(injection),
            lorebooks = emptyList()
        )

        assertEquals(6, result.size)

        // 娉ㄥ叆搴旇鍦ㄦ渶鍚庝竴鏉＄敤鎴锋秷鎭箣鍓?
        val injectedIndex = result.indexOfFirst { getMessageText(it).contains("Bottom injection") }
        val lastUserIndex = result.indexOfLast { it.role == MessageRole.USER && getMessageText(it) == "Thanks!" }
        assertEquals(lastUserIndex - 1, injectedIndex)
    }

    @Test
    fun `findSafeInsertIndex should handle assistant with multiple tools in one message`() {
        // 涓€涓?assistant 娑堟伅鍖呭惈澶氫釜 tool锛坅gentic loop 鍦烘櫙锛?
        val multiToolAssistant = UIMessage(
            role = MessageRole.ASSISTANT,
            parts = listOf(
                UIMessagePart.Tool(
                    toolCallId = "call_1",
                    toolName = "tool_1",
                    input = "{}",
                    output = emptyList()
                ),
                UIMessagePart.Tool(
                    toolCallId = "call_2",
                    toolName = "tool_2",
                    input = "{}",
                    output = emptyList()
                )
            )
        )
        val executedToolAssistant = UIMessage(
            role = MessageRole.ASSISTANT,
            parts = listOf(
                UIMessagePart.Tool(
                    toolCallId = "call_1",
                    toolName = "tool_1",
                    input = "{}",
                    output = listOf(UIMessagePart.Text("result1"))
                ),
                UIMessagePart.Tool(
                    toolCallId = "call_2",
                    toolName = "tool_2",
                    input = "{}",
                    output = listOf(UIMessagePart.Text("result2"))
                )
            )
        )

        val messages = listOf(
            UIMessage.system("System prompt"),
            UIMessage.user("Hello"),
            multiToolAssistant,
            executedToolAssistant
        )

        // 灏濊瘯鍦ㄧ储寮?3 鎻掑叆锛屽簲璇ヨ繑鍥?2锛堝 tool 娑堟伅涔嬪墠锛?
        val safeIndex = findSafeInsertIndex(messages, 3)
        assertEquals(2, safeIndex)
    }
    // endregion
}

