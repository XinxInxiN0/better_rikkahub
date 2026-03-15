package com.u4e50.rikkahub.data.datastore.migration

import com.u4e50.rikkahub.data.datastore.Settings
import com.u4e50.rikkahub.data.model.Avatar
import com.u4e50.rikkahub.utils.JsonInstant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsJsonMigratorTest {
    @Test
    fun `migrate should restore legacy dummy avatar type`() {
        val legacySettingsJson = """
            {
              "displaySetting": {
                "userAvatar": {
                  "type": "me.rerere.rikkahub.data.model.Avatar.Dummy"
                }
              }
            }
        """.trimIndent()

        val migratedJson = SettingsJsonMigrator.migrate(legacySettingsJson)
        val settings = JsonInstant.decodeFromString<Settings>(migratedJson)

        assertEquals(Avatar.Dummy, settings.displaySetting.userAvatar)
    }

    @Test
    fun `migrate should preserve legacy avatar payload fields`() {
        val legacySettingsJson = """
            {
              "displaySetting": {
                "userAvatar": {
                  "type": "me.rerere.rikkahub.data.model.Avatar.Emoji",
                  "content": "🙂"
                }
              }
            }
        """.trimIndent()

        val migratedJson = SettingsJsonMigrator.migrate(legacySettingsJson)
        val settings = JsonInstant.decodeFromString<Settings>(migratedJson)

        assertTrue(settings.displaySetting.userAvatar is Avatar.Emoji)
        assertEquals("🙂", (settings.displaySetting.userAvatar as Avatar.Emoji).content)
    }
}
