package me.lumiafk.chattweaks.config.configs

import dev.isxander.yacl3.config.v2.api.SerialEntry
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import me.lumiafk.chattweaks.chat.ChatBox

class OtherConfig {
	@SerialEntry
	@JvmField
	var chatWidth = 1024

	@SerialEntry
	@JvmField
	var chatAlwaysVisible = false

	@SerialEntry
	@JvmField
	var chatBoxes = ObjectArrayList<ChatBox>().apply { add(ChatBox.DEFAULT_CHAT_BOX) }
}