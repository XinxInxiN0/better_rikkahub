package com.u4e50.rikkahub.data.datastore.migration

import android.util.Log
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import com.u4e50.rikkahub.utils.JsonInstant
import com.u4e50.rikkahub.utils.jsonPrimitiveOrNull

private const val TAG = "SettingsJsonMigrator"
private const val LEGACY_APP_PACKAGE_PREFIX = "me.rerere.rikkahub."
private const val CURRENT_APP_PACKAGE_PREFIX = "com.u4e50.rikkahub."

/**
 * 瀵瑰浠芥枃浠朵腑鐨?settings.json 搴旂敤涓?DataStore migration 鐩稿悓鐨勮縼绉婚€昏緫銆?
 *
 * DataStore migration 浣滅敤浜庡垎鏁ｇ殑 key-value 瀛樺偍锛岃€屽浠芥枃浠朵腑鐨?settings.json
 * 鏄暣涓?[com.u4e50.rikkahub.data.datastore.Settings] 瀵硅薄鐨勫簭鍒楀寲缁撴灉銆?
 * 姝ゅ伐鍏风被璐熻矗鍦ㄥ弽搴忓垪鍖栧墠瀵规棫鏍煎紡鐨?JSON 鎵ц绛変环鐨勮縼绉绘搷浣溿€?
 */
object SettingsJsonMigrator {

    /**
     * 瀵?settings JSON 瀛楃涓蹭緷娆″簲鐢ㄦ墍鏈夌増鏈殑杩佺Щ銆?
     * 鑻ュ彂鐢熷紓甯稿垯杩斿洖鍘熷 JSON锛屼笉涓柇鎭㈠娴佺▼銆?
     */
    fun migrate(settingsJson: String): String {
        return runCatching {
            val root = JsonInstant.parseToJsonElement(settingsJson).jsonObject.toMutableMap()

            // V1: 淇 mcpServers 涓叏闄愬畾绫诲悕鐨?type 瀛楁
            root["mcpServers"]?.let { element ->
                val migrated = migrateMcpServersJson(JsonInstant.encodeToString(element))
                root["mcpServers"] = JsonInstant.parseToJsonElement(migrated)
            }

            // V2: 淇 assistants 涓?UIMessagePart 鐨?type 瀛楁
            root["assistants"]?.let { element ->
                val migrated = migrateAssistantsJson(JsonInstant.encodeToString(element))
                root["assistants"] = JsonInstant.parseToJsonElement(migrated)
            }

            // V3: 将 assistants 中内嵌的 quickMessages 提取为全局 quickMessages
            root["assistants"]?.let { element ->
                val (migratedAssistants, extractedQuickMessages) =
                    migrateAssistantsQuickMessages(JsonInstant.encodeToString(element))
                root["assistants"] = JsonInstant.parseToJsonElement(migratedAssistants)

                if (extractedQuickMessages.isNotEmpty()) {
                    val existing = root["quickMessages"]
                    val existingArray = existing?.let {
                        runCatching { JsonInstant.parseToJsonElement(JsonInstant.encodeToString(it)) as? JsonArray }.getOrNull()
                    } ?: JsonArray(emptyList())
                    val existingIds = existingArray.mapNotNull {
                        (it as? JsonObject)?.get("id")?.toString()?.trim('"')
                    }.toSet()
                    val merged = JsonArray(
                        existingArray + extractedQuickMessages.filter { e ->
                            val id = (e as? JsonObject)?.get("id")?.toString()?.trim('"')
                            id != null && id !in existingIds
                        }
                    )
                    root["quickMessages"] = merged
                }
            }

            val migratedRoot = migrateLegacyTypeNames(JsonObject(root)).jsonObject
            JsonInstant.encodeToString(migratedRoot)
        }.onFailure {
            Log.e(TAG, "migrate: Failed to migrate settings JSON, using original", it)
        }.getOrDefault(settingsJson)
    }

    private fun migrateLegacyTypeNames(element: JsonElement): JsonElement {
        return when (element) {
            is JsonObject -> JsonObject(
                element.mapValues { (key, value) ->
                    if (key == "type") {
                        migrateLegacyTypeValue(value)
                    } else {
                        migrateLegacyTypeNames(value)
                    }
                }
            )

            is JsonArray -> JsonArray(element.map(::migrateLegacyTypeNames))
            else -> element
        }
    }

    private fun migrateLegacyTypeValue(element: JsonElement): JsonElement {
        val typeName = element.jsonPrimitiveOrNull?.contentOrNull ?: return element
        if (!typeName.startsWith(LEGACY_APP_PACKAGE_PREFIX)) return element

        return JsonPrimitive(typeName.replace(LEGACY_APP_PACKAGE_PREFIX, CURRENT_APP_PACKAGE_PREFIX))
    }
}

