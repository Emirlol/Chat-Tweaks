package me.lumiafk.chattweaks.config

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder
import dev.isxander.yacl3.dsl.*
import me.lumiafk.chattweaks.ChatTweaks
import me.lumiafk.chattweaks.text
import me.lumiafk.chattweaks.translatable
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.gui.screen.Screen
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.format.DateTimeFormatter

object ConfigHandler {
	private val HANDLER: ConfigClassHandler<Config> = ConfigClassHandler.createBuilder(Config::class.java).serializer {
		GsonConfigSerializerBuilder.create(it)
			.setPath(FabricLoader.getInstance().configDir.resolve("${ChatTweaks.NAMESPACE}/config.json"))
			.build()
	}.build()

	private val logger: Logger = LoggerFactory.getLogger("ChatTweaks Config")

	fun save() = HANDLER.save()

	fun load() = HANDLER.load()

	val config: Config get() = HANDLER.instance()

	val default: Config get() = HANDLER.defaults()

	fun generateScreen(parent: Screen?): Screen = YetAnotherConfigLib(ChatTweaks.NAMESPACE) {
		save(::save)
		title("ChatTweaks".text)
		val timestamps by categories.registering {
			val enabled by rootOptions.registering {
				name("chattweaks.config.timestamps.enabled".translatable)
				binding(config.timeStampConfig::enabled, default.timeStampConfig.enabled)
				controller = tickBox()
			}
			val alwaysShow by rootOptions.registering {
				name("chattweaks.config.timestamps.alwaysShow".translatable)
				binding(config.timeStampConfig::alwaysShow, default.timeStampConfig.alwaysShow)
				controller = tickBox()
			}
			val textColor by rootOptions.registering {
				name("chattweaks.config.timestamps.textColor".translatable)
				binding(config.timeStampConfig::textColor, default.timeStampConfig.textColor)
				controller = colorPicker(true)
			}
			val backgroundColor by rootOptions.registering {
				name("chattweaks.config.timestamps.backgroundColor".translatable)
				binding(config.timeStampConfig::backgroundColor, default.timeStampConfig.backgroundColor)
				controller = colorPicker(true)
			}
			val dateTimeFormat by rootOptions.registering {
				name("chattweaks.config.timestamps.format".translatable)
				tooltip("chattweaks.config.timestamps.format.tooltip".translatable)
				binding(default.timeStampConfig.dateTimeFormat,
					{ config.timeStampConfig.dateTimeFormat },
					{
						config.timeStampConfig.dateTimeFormat = it
						try {
							config.timeStampConfig.dateTimeFormatter = DateTimeFormatter.ofPattern(it)
						} catch (e: Exception) {
							logger.warn("Failed to parse date time format ($it): ${e.message}")
						}

					}
				)
				controller = stringField()
			}
		}

		val hud by categories.registering {
			val drawAlternatingRow by rootOptions.registering {
				name("chattweaks.config.hud.drawAlternatingRow".translatable)
				tooltip("chattweaks.config.hud.drawAlternatingRow.tooltip".translatable)
				binding(config.hudConfig::drawAlternatingRow, default.hudConfig.drawAlternatingRow)
				controller = tickBox()
			}
			val alternatingRowColor by rootOptions.registering {
				name("chattweaks.config.hud.alternatingRowColor".translatable)
				tooltip("chattweaks.config.hud.alternatingRowColor.tooltip".translatable)
				binding(config.hudConfig::alternatingRowColor, default.hudConfig.alternatingRowColor)
				controller = colorPicker(true)
			}
			val backgroundColor by rootOptions.registering {
				name("chattweaks.config.hud.backgroundColor".translatable)
				tooltip("chattweaks.config.hud.backgroundColor.tooltip".translatable)
				binding(config.hudConfig::backgroundColor, default.hudConfig.backgroundColor)
				controller = colorPicker(true)
			}
			val hideMessageIndicator by rootOptions.registering {
				name("chattweaks.config.hud.hideMessageIndicator".translatable)
				tooltip("chattweaks.config.hud.hideMessageIndicator.tooltip".translatable)
				binding(config.hudConfig::hideMessageIndicator, default.hudConfig.hideMessageIndicator)
				controller = tickBox()
			}
		}
	}.generateScreen(parent)
}