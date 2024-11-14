@file:Suppress("UnstableApiUsage")
package me.lumiafk.chattweaks.chat.screens

import dev.isxander.yacl3.config.v3.value
import dev.isxander.yacl3.gui.YACLScreen
import me.lumiafk.chattweaks.chat.ChatBox
import me.lumiafk.chattweaks.chat.screens.elements.AbstractWidget.Companion.OUTER_PADDING
import me.lumiafk.chattweaks.chat.screens.elements.Background
import me.lumiafk.chattweaks.chat.screens.elements.ButtonEntryList
import me.lumiafk.chattweaks.chat.screens.elements.DescriptionPane
import me.lumiafk.chattweaks.config.ConfigHandler
import me.lumiafk.chattweaks.config.config
import me.lumiafk.chattweaks.mixin.accessors.YACLScreenAccessor
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import kotlin.math.ceil
import kotlin.math.floor

class ChatBoxesConfigScreen(private val parent: YACLScreen) : Screen(Text.of("Chat boxes config")) {
	private val chatBoxesCloned: MutableList<ChatBox> = config.otherConfig.chatBoxes.value.map(ChatBox::clone) as MutableList<ChatBox>

	lateinit var list: ButtonEntryList

	init {
		parent.finishOrSave()
		client?.setScreen(this)
	}

	override fun init() {
		addDrawable(Background(0, 0, width, height))

		val descriptionPane = DescriptionPane(floor(width * 4.0 / 5.0).toInt(), 0, ceil(width * 1.0 / 5.0).toInt(), height, this)
		addDrawableChild(descriptionPane)
		initList()
		addDrawableChild(list)

		children().filterIsInstance<Initializable>().forEach(Initializable::init)
	}

	fun save() {
		config.otherConfig.chatBoxes.value.let {
			it.clear()
			it.addAll(chatBoxesCloned)
		}
		@Suppress("UnstableApiUsage")
		ConfigHandler.saveToFile()
		client?.inGameHud?.chatHud?.reset()
	}

	fun undo() {
		chatBoxesCloned.clear()
		chatBoxesCloned.addAll(config.otherConfig.chatBoxes.value.map(ChatBox::clone))
		initList()
	}

	fun initList() {
		list = ButtonEntryList(OUTER_PADDING, floor(width * 4.0 / 5.0).toInt() - (OUTER_PADDING * 2), height - (OUTER_PADDING * 2), chatBoxesCloned, this).apply {
			x = OUTER_PADDING
		}
	}

	override fun close() {
		client?.setScreen(ConfigHandler.generateScreen((parent as YACLScreenAccessor).parent))
	}
}