package me.lumiafk.chattweaks.chat.screens.elements

import me.lumiafk.chattweaks.util.ColorPalette
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Drawable

class Background(
	private val x: Int,
	private val y: Int,
	private val width: Int,
	private val height: Int,
	private val color: Int = ColorPalette.BASE.rgb
) : Drawable {
	override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		context.fill(x, y, x + width, y + height, color)
	}
}