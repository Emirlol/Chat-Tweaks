package me.lumiafk.templatemod.config

import com.mojang.serialization.Codec
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder
import dev.isxander.yacl3.config.v3.JsonFileCodecConfig
import dev.isxander.yacl3.config.v3.register
import dev.isxander.yacl3.dsl.YetAnotherConfigLib
import dev.isxander.yacl3.dsl.binding
import dev.isxander.yacl3.dsl.controller
import me.lumiafk.templatemod.TemplateMod
import me.lumiafk.templatemod.Util.text
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.sound.SoundEvents

@Suppress("UnstableApiUsage")
object ConfigHandler : JsonFileCodecConfig<ConfigHandler>(FabricLoader.getInstance().configDir.resolve("TemplateMod/config.json5")) {
	val exampleBoolean by register(false, Codec.BOOL)
	val exampleInt by register(0, Codec.INT)

	fun createGui(parent: Screen?): Screen = YetAnotherConfigLib(TemplateMod.NAMESPACE) {
		save(::saveToFile)
		title("${TemplateMod.NAME} Config".text)
		val general by categories.registering {
			name("General".text)
			val exampleOption by rootOptions.registeringButton {
				name("Example".text)
				text("Click Here".text)
				action { _, _ ->
					MinecraftClient.getInstance().soundManager.play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_PLING, 1.0f))
				}
			}
			val exampleGroup by groups.registering {
				name("Example Group".text)
				val exampleSlider by options.registering {
					controller(IntegerSliderControllerBuilder::create) {
						step(1)
						range(0, 69)
					}
					binding = exampleInt.asBinding()
				}
				val exampleCheckbox by options.registering {
					controller(TickBoxControllerBuilder::create)
					binding = exampleBoolean.asBinding()
				}

			}
		}

	}.generateScreen(parent)
}