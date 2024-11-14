package me.lumiafk.chattweaks.config

import com.google.common.collect.ImmutableList
import dev.isxander.yacl3.api.OptionGroup
import dev.isxander.yacl3.api.PlaceholderCategory
import dev.isxander.yacl3.gui.YACLScreen
import me.lumiafk.chattweaks.chat.screens.ChatBoxesConfigScreen
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import java.util.function.BiFunction

object ChatBoxesPlaceholder : PlaceholderCategory {
	override fun name(): Text = Text.translatable("chattweaks.config.category.chatboxes")

	override fun groups(): ImmutableList<OptionGroup> = ImmutableList.of()

	override fun tooltip(): Text = Text.empty()

	override fun screen(): BiFunction<MinecraftClient, YACLScreen, Screen> = BiFunction { _, yaclScreen -> ChatBoxesConfigScreen(yaclScreen) }
}