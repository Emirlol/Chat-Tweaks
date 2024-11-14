package me.lumiafk.chattweaks.chat.screens.elements

import me.lumiafk.chattweaks.util.ColorPalette.MAUVE
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Drawable

class VerticalSeparator(private val x: Int, private val y: Int, private val screenHeight: Int, private val color: Int = MAUVE.rgb) : Drawable {
	override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		context.fill(x - 1, y + VERTICAL_LINE_VERTICAL_PADDING, x + 1, screenHeight - VERTICAL_LINE_VERTICAL_PADDING, color)
	}

	companion object {
		private const val VERTICAL_LINE_VERTICAL_PADDING = 20
	}
}