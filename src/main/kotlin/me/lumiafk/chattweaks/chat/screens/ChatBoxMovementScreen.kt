package me.lumiafk.chattweaks.chat.screens

import me.lumiafk.chattweaks.chat.ChatBox
import me.lumiafk.chattweaks.chat.screens.elements.Background
import me.lumiafk.chattweaks.chat.screens.elements.ChatBoxWidget
import me.lumiafk.chattweaks.chat.screens.elements.TextWidget
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.lwjgl.glfw.GLFW

class ChatBoxMovementScreen(val parent: Screen?, chatBox: ChatBox, otherChatBoxes: List<ChatBox>) : Screen(Text.of("Chat box movement")) {
	val chatBoxOpacity = chatBox.data.opacity
	val indexBasedChatBoxOpacityList = otherChatBoxes.map { it.data.opacity }
	val chatBoxWidget = ChatBoxWidget(chatBox.apply { data.opacity = 1f })
	val otherChatBoxesWidgets = otherChatBoxes.onEach { it.data.opacity = 0.2f }.map { ChatBoxWidget(it) }

	override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
		if (button == GLFW.GLFW_MOUSE_BUTTON_1 && chatBoxWidget.isMouseOver(mouseX, mouseY) && chatBoxWidget.mouseClicked(mouseX, mouseY, button)) {
			return true
		} else if (button == GLFW.GLFW_MOUSE_BUTTON_2) {
			chatBoxWidget.focused = !chatBoxWidget.focused
			return true
		}
		return false
	}

	override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
		return chatBoxWidget.isMouseOver(mouseX, mouseY) && chatBoxWidget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
	}

	override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
		return chatBoxWidget.isMouseOver(mouseX, mouseY) && chatBoxWidget.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
	}

	override fun getFocused() = chatBoxWidget

	init {
		if (client?.player == null) addDrawable(Background(0, 0, width, height))
		otherChatBoxesWidgets.forEach(::addDrawable)
		addDrawableChild(chatBoxWidget)
		addDrawable(TextWidget(Text.literal("Right click to change between focused & unfocused chat height").formatted(Formatting.DARK_GRAY)))
	}

	override fun close() {
		chatBoxWidget.data.opacity = chatBoxOpacity
		otherChatBoxesWidgets.forEachIndexed { index, chatBoxWidget ->
			chatBoxWidget.data.opacity = indexBasedChatBoxOpacityList[index]
		}
		client?.setScreen(parent)
	}
}