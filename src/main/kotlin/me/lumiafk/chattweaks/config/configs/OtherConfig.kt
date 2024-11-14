@file:Suppress("UnstableApiUsage")

package me.lumiafk.chattweaks.config.configs

import dev.isxander.yacl3.config.v3.ConfigEntry
import dev.isxander.yacl3.config.v3.register
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import me.lumiafk.chattweaks.chat.ChatBox
import me.lumiafk.chattweaks.chat.ChatBoxData
import me.lumiafk.chattweaks.chat.transformations.Transformer
import me.lumiafk.chattweaks.chat.transformations.TransformerContext
import me.lumiafk.chattweaks.chat.transformations.TransformerType
import me.lumiafk.chattweaks.config.ConfigProxy
import me.lumiafk.chattweaks.util.Codecs.mutableList
import me.lumiafk.chattweaks.util.text

object OtherConfig : ConfigProxy {
	val chatWidth by register(1024)

	val chatAlwaysVisible by register(false)

	val chatBoxes: ConfigEntry<MutableList<ChatBox>> by register(ObjectArrayList<ChatBox>().apply {
		add(ChatBox.DEFAULT_CHAT_BOX)
		add(ChatBox("Hi".text, mutableListOf(Transformer(TransformerType.STRING_REPLACE, TransformerContext(stringFilter = "potato", replacement = "carrot"))), ChatBoxData.DEFAULT.clone().apply { y += 200 }))
		add(ChatBox("Hi".text, mutableListOf(Transformer(TransformerType.STRING_REPLACE, TransformerContext(stringFilter = "potato", replacement = "hi there"))), ChatBoxData.DEFAULT.clone().apply { y += 400 }))
	}, ChatBox.CODEC.mutableList())
}