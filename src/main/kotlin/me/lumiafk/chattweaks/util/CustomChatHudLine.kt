package me.lumiafk.chattweaks.util

import net.minecraft.client.gui.hud.ChatHudLine
import net.minecraft.text.OrderedText

data class VisibleChatHudLine(
	val chatHudLine: ChatHudLine,
	var orderedText: OrderedText,
	var endOfEntry: Boolean
) {
	// Syntax highlighting borks here due to lacking kotlin support by the mcdev plugin
	// It's not an actual error, this is valid code
	var originAddedTime
		get() = chatHudLine.getAddedTime()
		set(value) = chatHudLine.setAddedTime(value)

	val creationTick get() = chatHudLine.creationTick
}