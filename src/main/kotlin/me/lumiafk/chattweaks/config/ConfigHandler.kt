package me.lumiafk.chattweaks.config

import dev.isxander.yacl3.api.Option
import dev.isxander.yacl3.api.OptionEventListener
import dev.isxander.yacl3.api.StateManager
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder
import dev.isxander.yacl3.dsl.*
import me.lumiafk.chattweaks.*
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.ClickEvent
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.format.DateTimeFormatter

object ConfigHandler {
	private const val FORMAT_DOCS = "https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html"
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
			name("chattweaks.config.category.timestamps".translatable)
			val enabled by rootOptions.registering {
				name("chattweaks.config.timestamps.enabled".translatable)
				binding(config.timeStampConfig::enabled, default.timeStampConfig.enabled)
				controller = tickBox()
			}
			val alwaysShow by rootOptions.registering {
				name("chattweaks.config.timestamps.alwaysShow".translatable)
				descriptionBuilder { text("chattweaks.config.timestamps.alwaysShow.tooltip".translatable) }
				binding(config.timeStampConfig::alwaysShow, default.timeStampConfig.alwaysShow)
				controller = tickBox()
			}
			val groupingMillis by rootOptions.registering {
				name("chattweaks.config.timestamps.groupingMillis".translatable)
				descriptionBuilder { text("chattweaks.config.timestamps.groupingMillis.tooltip".translatable) }
				stateManager(StateManager.createInstant(default.timeStampConfig.groupingMillis, { config.timeStampConfig.groupingMillis }, { config.timeStampConfig.groupingMillis = it }))
				controller = slider(1L..15000L)
				addListener { _, event ->
					if (event == OptionEventListener.Event.STATE_CHANGE) client.inGameHud.chatHud.reset()
				}
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
			name("chattweaks.config.category.hud".translatable)
			val drawAlternatingRow by rootOptions.registering {
				name("chattweaks.config.hud.drawAlternatingRow".translatable)
				descriptionBuilder { text("chattweaks.config.hud.drawAlternatingRow.tooltip".translatable) }
				binding(config.hudConfig::drawAlternatingRow, default.hudConfig.drawAlternatingRow)
				controller = tickBox()
			}
			val alternatingRowColor by rootOptions.registering {
				name("chattweaks.config.hud.alternatingRowColor".translatable)
				descriptionBuilder { text("chattweaks.config.hud.alternatingRowColor.tooltip".translatable) }
				binding(config.hudConfig::alternatingRowColor, default.hudConfig.alternatingRowColor)
				controller = colorPicker(true)
			}
			val backgroundColor by rootOptions.registering {
				name("chattweaks.config.hud.backgroundColor".translatable)
				descriptionBuilder { text("chattweaks.config.hud.backgroundColor.tooltip".translatable) }
				binding(config.hudConfig::backgroundColor, default.hudConfig.backgroundColor)
				controller = colorPicker(true)
			}
			val hideMessageIndicator by rootOptions.registering {
				name("chattweaks.config.hud.hideMessageIndicator".translatable)
				descriptionBuilder { text("chattweaks.config.hud.hideMessageIndicator.tooltip".translatable) }
				binding(config.hudConfig::hideMessageIndicator, default.hudConfig.hideMessageIndicator)
				controller = tickBox()
			}
		}

		val other by categories.registering {
			name("chattweaks.config.category.other".translatable)
			val chatWidth by rootOptions.registering {
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
				binding(config.otherConfig::chatWidth, default.otherConfig.chatWidth)
				controller = numberField(0, Int.MAX_VALUE)
			}
		}

		val scale by categories.registering {
			name("chattweaks.config.category.scale".translatable)
			val overrideHeight by rootOptions.registering {
				name("chattweaks.config.scale.overrideHeight".translatable)
				stateManager(StateManager.createInstant(default.scaleConfig.overrideHeight, { config.scaleConfig.overrideHeight }, { config.scaleConfig.overrideHeight = it }))
				controller = tickBox()
				addListener { option, event ->
					if (event == OptionEventListener.Event.STATE_CHANGE || event == OptionEventListener.Event.INITIAL) {
						rootOptions.futureRef<Int>("focusedHeight").thenAccept {
							it.setAvailable(option.pendingValue())
						}
						rootOptions.futureRef<Int>("unfocusedHeight").thenAccept {
							it.setAvailable(option.pendingValue())
						}
					}
				}
			}
			val focusedHeight: Option<Int> by rootOptions.registering {
				name("chattweaks.config.scale.focusedHeight".translatable)
				stateManager(StateManager.createInstant(default.scaleConfig.focusedHeight, { config.scaleConfig.focusedHeight }, { config.scaleConfig.focusedHeight = it }))
				controller = slider(20..2000)
			}
			val unfocusedHeight by rootOptions.registering {
				name("chattweaks.config.scale.unfocusedHeight".translatable)
				stateManager(StateManager.createInstant(default.scaleConfig.unfocusedHeight, { config.scaleConfig.unfocusedHeight }, { config.scaleConfig.unfocusedHeight = it }))
				controller = slider(20..2000)
			}
			val overrideWidth by rootOptions.registering {
				name("chattweaks.config.scale.overrideWidth".translatable)
				controller = tickBox()
				stateManager(StateManager.createInstant(default.scaleConfig.overrideWidth, { config.scaleConfig.overrideWidth }, { config.scaleConfig.overrideWidth = it }))
				addListener { option, event ->
					when (event) {
						OptionEventListener.Event.INITIAL -> {
							rootOptions.futureRef<Int>("width").thenAccept {
								it.setAvailable(option.pendingValue())
							}
						}
						OptionEventListener.Event.STATE_CHANGE -> {
							rootOptions.futureRef<Int>("width").thenAccept {
								it.setAvailable(option.pendingValue())
							}
							client.inGameHud.chatHud.reset()
						}
						else -> {}
					}
				}
			}
			val width by rootOptions.registering {
				name("chattweaks.config.scale.width".translatable)
				stateManager(StateManager.createInstant(default.scaleConfig.width, { config.scaleConfig.width }, { config.scaleConfig.width = it }))
				controller = slider(20.. 2000)
				addListener { _, event ->
					if (event == OptionEventListener.Event.STATE_CHANGE) {
						client.inGameHud.chatHud.reset()
					}
				}
			}
		}
	}.generateScreen(parent)
}