package me.lumiafk.chattweaks.chat.screens.elements

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.DrawContext

@Environment(EnvType.CLIENT)
interface PositionedDrawable {
	fun render(context: DrawContext, x: Int, y: Int, width: Int, height: Int, mouseX: Int, mouseY: Int, delta: Float)
}