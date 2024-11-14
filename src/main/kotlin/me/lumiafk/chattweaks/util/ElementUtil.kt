package me.lumiafk.chattweaks.util

import me.lumiafk.chattweaks.chat.screens.elements.ButtonWidget
import me.lumiafk.chattweaks.chat.screens.elements.PressAction
import me.lumiafk.chattweaks.chat.screens.elements.TextFieldWidget
import net.minecraft.client.font.TextRenderer
import net.minecraft.text.Text

object ElementUtil {
	fun buttonWidget(text: Text, x: Int, y: Int, width: Int, action: PressAction): ButtonWidget = buttonWidget(text, x, y, width, ButtonWidget.DEFAULT_HEIGHT, action)

	fun buttonWidget(text: Text, x: Int, y: Int, width: Int, height: Int, action: PressAction): ButtonWidget = ButtonWidget.builder(text, action).dimensions(x, y, width, height).build()

	/**
	 * This overload is for ButtonEntryList, where the x and y are passed in when rendering instead.
	 */
	fun buttonWidget(text: Text, width: Int, action: PressAction): ButtonWidget = buttonWidget(text, 0, 0, width, ButtonWidget.DEFAULT_HEIGHT, action)

	fun textFieldWidget(textRenderer: TextRenderer?, width: Int, name: Text?, text: String) = TextFieldWidget(textRenderer, width, ButtonWidget.DEFAULT_HEIGHT, name).apply { setText(text, false) }
}