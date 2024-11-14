package me.lumiafk.chattweaks.util

import dev.isxander.yacl3.config.v3.value
import me.lumiafk.chattweaks.ChatTweaks
import me.lumiafk.chattweaks.config.config
import net.minecraft.network.message.ChatVisibility

object ChatHudUtil {
	val isChatFocused get() = client.inGameHud.chatHud.isChatFocused

	val isChatHidden get() = client.options.chatVisibility.value == ChatVisibility.HIDDEN

	//Intended to be called by ChatHud.isChatFocused
	fun isChatFocused(backingValue: Boolean) = config.otherConfig.chatAlwaysVisible.value || ChatTweaks.peekChatKeybinding.isPressed || backingValue
}