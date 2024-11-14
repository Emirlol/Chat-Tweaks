@file:Suppress("UnstableApiUsage")

package me.lumiafk.chattweaks.config

import dev.isxander.yacl3.api.OptionEventListener
import dev.isxander.yacl3.api.StateManager
import dev.isxander.yacl3.config.v3.JsonFileCodecConfig
import dev.isxander.yacl3.config.v3.register
import dev.isxander.yacl3.dsl.*
import me.lumiafk.chattweaks.ChatTweaks
import me.lumiafk.chattweaks.util.client
import me.lumiafk.chattweaks.util.literal
import me.lumiafk.chattweaks.util.text
import me.lumiafk.chattweaks.util.translatable
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.ClickEvent
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.io.path.createParentDirectories
import kotlin.io.path.notExists

private const val CONFIG_FILE_NAME = "config.json"
private val configPath = FabricLoader.getInstance().configDir.resolve("${ChatTweaks.NAMESPACE}/${CONFIG_FILE_NAME}")

object ConfigHandler : JsonFileCodecConfig<ConfigHandler>(configPath) {
	private const val FORMAT_DOCS = "https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html"

	private val logger: Logger = LoggerFactory.getLogger("ChatTweaks Config")

	fun generateScreen(parent: Screen?): Screen = YetAnotherConfigLib(ChatTweaks.NAMESPACE) {
		save(::saveToFile)
		title("ChatTweaks".text)
		val timestamps by categories.registering {
			name("chattweaks.config.category.timestamps".translatable)
			val enabled = rootOptions.register(config.timeStampConfig.enabled) {
				name("chattweaks.config.timestamps.enabled".translatable)
				controller = tickBox()
			}
			val alwaysShow = rootOptions.register(config.timeStampConfig.alwaysShow) {
				name("chattweaks.config.timestamps.alwaysShow".translatable)
				descriptionBuilder { text("chattweaks.config.timestamps.alwaysShow.tooltip".translatable) }
				controller = tickBox()
			}
			val groupingMillis by rootOptions.registering {
				name("chattweaks.config.timestamps.groupingMillis".translatable)
				descriptionBuilder { text("chattweaks.config.timestamps.groupingMillis.tooltip".translatable) }
				stateManager(StateManager.createInstant(config.timeStampConfig.groupingMillis.asBinding()))
				controller = slider(1L..15000L)
				addListener { _, event ->
					if (event == OptionEventListener.Event.STATE_CHANGE) client.inGameHud.chatHud.reset()
				}
			}
			val textColor = rootOptions.register(config.timeStampConfig.textColor) {
				name("chattweaks.config.timestamps.textColor".translatable)
				controller = colorPicker(true)
			}
			val backgroundColor = rootOptions.register(config.timeStampConfig.backgroundColor) {
				name("chattweaks.config.timestamps.backgroundColor".translatable)
				controller = colorPicker(true)
			}
			val dateTimeFormat = rootOptions.register(config.timeStampConfig.dateTimeFormat) {
				name("chattweaks.config.timestamps.format".translatable)
				descriptionBuilder {
					text(
						Text.translatable("chattweaks.config.timestamps.format.tooltip",
							FORMAT_DOCS.literal.styled {
								it.withClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, FORMAT_DOCS))
									.withColor(Formatting.BLUE)
									.withUnderline(true)
							}
						)
					)
				}
				controller = stringField()
			}
		}

		val hud by categories.registering {
			name("chattweaks.config.category.hud".translatable)
			val drawAlternatingRow = rootOptions.register(config.hudConfig.drawAlternatingRow) {
				name("chattweaks.config.hud.drawAlternatingRow".translatable)
				descriptionBuilder { text("chattweaks.config.hud.drawAlternatingRow.tooltip".translatable) }
				controller = tickBox()
			}
			val alternatingRowColor = rootOptions.register(config.hudConfig.alternatingRowColor) {
				name("chattweaks.config.hud.alternatingRowColor".translatable)
				descriptionBuilder { text("chattweaks.config.hud.alternatingRowColor.tooltip".translatable) }
				controller = colorPicker(true)
			}
		}

		val other by categories.registering {
			name("chattweaks.config.category.other".translatable)
			val chatWidth = rootOptions.register(config.otherConfig.chatWidth) {
				name("chattweaks.config.other.chatWidth".translatable)
				descriptionBuilder {
					text(
						"chattweaks.config.other.chatWidth.tooltip[0]".translatable,
						"".text,
						"chattweaks.config.other.chatWidth.tooltip[1]".translatable,
						"".text,
						"chattweaks.config.other.chatWidth.tooltip[2]".translatable
					)
				}
				controller = numberField(0, Int.MAX_VALUE)
			}
			val chatAlwaysVisible = rootOptions.register(config.otherConfig.chatAlwaysVisible) {
				name("chattweaks.config.other.chatAlwaysVisible".translatable)
				descriptionBuilder { text("chattweaks.config.other.chatAlwaysVisible.tooltip".translatable) }
				controller = tickBox()
			}
		}
		val chatBoxes = categories.register("chatBoxes", ChatBoxesPlaceholder)
	}.generateScreen(parent)

	fun load() {
		if (configPath.notExists()) {
			logger.info("Config file not found, creating one.")
			configPath.createParentDirectories()
			saveToFile()
		}
		loadFromFile()
	}
}