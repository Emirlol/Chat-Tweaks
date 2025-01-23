@file:Suppress("UnstableApiUsage")

package me.lumiafk.chattweaks.config.configs

import dev.isxander.yacl3.config.v3.ConfigEntry
import dev.isxander.yacl3.config.v3.register
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import me.lumiafk.chattweaks.chat.ChatBox
import me.lumiafk.chattweaks.config.ConfigProxy
import me.lumiafk.chattweaks.util.Codecs.mutableList

object OtherConfig : ConfigProxy {
	val chatWidth by register(1024)

	val chatAlwaysVisible by register(false)

	val chatBoxes: ConfigEntry<MutableList<ChatBox>> by register(ObjectArrayList<ChatBox>().apply {
		add(ChatBox.DEFAULT_CHAT_BOX)
	}, ChatBox.CODEC.mutableList())
}