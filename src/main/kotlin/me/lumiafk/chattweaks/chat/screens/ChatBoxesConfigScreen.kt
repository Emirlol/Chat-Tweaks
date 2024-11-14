package me.lumiafk.chattweaks.chat.screens

import me.lumiafk.chattweaks.chat.ChatBox
import me.lumiafk.chattweaks.chat.screens.elements.AbstractWidget.Companion.OUTER_PADDING
import me.lumiafk.chattweaks.chat.screens.elements.Background
import me.lumiafk.chattweaks.chat.screens.elements.ButtonEntryList
import me.lumiafk.chattweaks.chat.screens.elements.DescriptionPane
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import kotlin.math.ceil
import kotlin.math.floor

class ChatBoxesConfigScreen(private val parent: Screen?, private var chatBoxes: MutableList<ChatBox>) : Screen(Text.of("Chat boxes config")) {
	private val chatBoxesCloned: MutableList<ChatBox>

	init {
		chatBoxes.map(ChatBox::clone).also { chatBoxesCloned = it as MutableList<ChatBox> }
	}

	//This one is called on each window move/resize tick, so this can't be used to actually initialize the data, or we'd be reading from disk every frame
	override fun init() {
		addDrawable(Background(0, 0, width, height))

		val descriptionPane = DescriptionPane(floor(width * 4.0 / 5.0).toInt(), 0, ceil(width * 1.0 / 5.0).toInt(), height, this)
		addDrawableChild(descriptionPane)

		val list = ButtonEntryList(OUTER_PADDING, floor(width * 4.0 / 5.0).toInt() - (OUTER_PADDING * 2), height - (OUTER_PADDING * 2), chatBoxesCloned).apply {
			x = OUTER_PADDING
		}
		addDrawableChild(list)

		children().filterIsInstance<Initializable>().forEach(Initializable::init)
	}

	fun save() {
		chatBoxes.clear()
		chatBoxes.addAll(chatBoxesCloned)
	}

	override fun close() {
		client?.setScreen(parent)
	}
}