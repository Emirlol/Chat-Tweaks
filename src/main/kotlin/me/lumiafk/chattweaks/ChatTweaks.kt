package me.lumiafk.chattweaks

import com.mojang.brigadier.Command
import me.lumiafk.chattweaks.config.ConfigHandler
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback

object ChatTweaks : ClientModInitializer {
	override fun onInitializeClient() {
		check(ConfigHandler.load()) { "Failed to load config." }
		ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
			dispatcher.register(
				literal(NAMESPACE)
					.then(literal("config")
						.executes { context ->
							context.source.client.let {
								it.send {
									it.setScreen(ConfigHandler.generateScreen(it.currentScreen))
								}
							}
							Command.SINGLE_SUCCESS
						})
			)
		}
	}

	const val NAMESPACE = "chattweaks"
}