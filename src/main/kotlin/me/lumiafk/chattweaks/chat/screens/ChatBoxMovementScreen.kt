package me.lumiafk.chattweaks.chat.screens

import me.lumiafk.chattweaks.chat.ChatBox
import me.lumiafk.chattweaks.chat.screens.elements.Background
import me.lumiafk.chattweaks.chat.screens.elements.ChatBoxWidget
import me.lumiafk.chattweaks.chat.screens.elements.TextWidget
import me.lumiafk.chattweaks.util.literal
import me.lumiafk.chattweaks.util.text
import net.minecraft.client.gui.screen.Screen
import net.minecraft.util.Formatting

class ChatBoxMovementScreen(val parent: Screen?, chatBox: ChatBox, otherChatBoxes: List<ChatBox>) : Screen("Chat box movement".text) {
	val chatBoxOpacity = chatBox.data.opacity
	val indexBasedChatBoxOpacityList = otherChatBoxes.map { it.data.opacity }
	val chatBoxWidget = ChatBoxWidget(chatBox.apply { data.opacity = 1f }, true)
	val otherChatBoxesWidgets = otherChatBoxes.onEach { it.data.opacity = 0.2f }.map { ChatBoxWidget(it) }

	override fun getFocused() = chatBoxWidget

	init {
		if (client?.player == null) addDrawable(Background(0, 0, width, height))
		otherChatBoxesWidgets.forEach(::addDrawable)
		addDrawableChild(chatBoxWidget)
		addDrawable(TextWidget("Right click to change between focused & unfocused chat height".literal(Formatting.DARK_GRAY)))
		children().filterIsInstance<Initializable>().forEach(Initializable::init)
	}

	override fun close() {
		chatBoxWidget.data.opacity = chatBoxOpacity
		otherChatBoxesWidgets.forEachIndexed { index, chatBoxWidget ->
			chatBoxWidget.data.opacity = indexBasedChatBoxOpacityList[index]
		}
		client?.setScreen(parent)
	}
}