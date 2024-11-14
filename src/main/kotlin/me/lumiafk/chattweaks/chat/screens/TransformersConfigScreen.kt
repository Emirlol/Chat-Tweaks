package me.lumiafk.chattweaks.chat.screens

import me.lumiafk.chattweaks.chat.ChatBox
import me.lumiafk.chattweaks.chat.screens.elements.AbstractWidget.Companion.OUTER_PADDING
import me.lumiafk.chattweaks.chat.screens.elements.Background
import me.lumiafk.chattweaks.chat.screens.elements.DescriptionPane
import me.lumiafk.chattweaks.chat.screens.elements.TransformerEntryList
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import kotlin.math.ceil
import kotlin.math.floor

class TransformersConfigScreen(private val parent: ChatBoxesConfigScreen, val chatBox: ChatBox) : Screen(Text.of("Chat boxes config")) {
	lateinit var list: TransformerEntryList

	override fun init() {
		addDrawable(Background(0, 0, width, height))

		val descriptionPane = DescriptionPane(floor(width * 4.0 / 5.0).toInt(), 0, ceil(width * 1.0 / 5.0).toInt(), height, this)
		addDrawableChild(descriptionPane)
		initList()
		addDrawableChild(list)

		children().filterIsInstance<Initializable>().forEach(Initializable::init)
	}

	fun undo() {
		initList()
	}

	fun initList() {
		list = TransformerEntryList(OUTER_PADDING, floor(width * 4.0 / 5.0).toInt() - (OUTER_PADDING * 2), height - (OUTER_PADDING * 2), chatBox, this).apply {
			x = OUTER_PADDING
		}
	}

	fun save() = parent.save()

	override fun close() {
		client?.setScreen(parent)
	}
}