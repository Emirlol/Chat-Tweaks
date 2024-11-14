package me.lumiafk.chattweaks.chat.screens.elements

import me.lumiafk.chattweaks.util.client
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Drawable
import net.minecraft.text.Text

class TextWidget(
	val text: Text,
	val x: Int = client.window.scaledWidth / 2,
	val y: Int = client.window.scaledHeight / 2,
) : Drawable {
	override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		context.matrices.apply {
			push()
			translate(0.0, 0.0, 50.0)
			context.drawCenteredTextWithShadow(client.textRenderer, text, x, y, 0xFFFFFF)
			pop()
		}
	}
}