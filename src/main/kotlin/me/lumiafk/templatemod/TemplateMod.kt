package me.lumiafk.templatemod

import com.mojang.brigadier.Command
import me.lumiafk.templatemod.config.ConfigHandler
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil

object TemplateMod : ClientModInitializer {
	override fun onInitializeClient() {
		@Suppress("UnstableApiUsage")
		check(ConfigHandler.loadFromFile()) { "Failed to load config." }
		ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
			dispatcher.register(
				literal(NAMESPACE)
					.then(literal("config")
						.executes { context ->
							context.source.client.let {
								it.send {
									it.setScreen(ConfigHandler.createGui(it.currentScreen))
								}
							}
							Command.SINGLE_SUCCESS
						})
			)
		}
		createKeybinds()
	}

	private fun createKeybinds() {
		listOf(
			KEY_EXAMPLE
		).forEach { KeyBindingHelper.registerKeyBinding(it) }
	}

	const val NAME = "Template Mod"
	const val NAMESPACE = "templatemod"
	val KEY_EXAMPLE = KeyBinding("key.$NAMESPACE.example", InputUtil.GLFW_KEY_G, "category.$NAMESPACE.example")
}