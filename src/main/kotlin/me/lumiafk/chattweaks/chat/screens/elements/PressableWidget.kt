package me.lumiafk.chattweaks.chat.screens.elements

import me.lumiafk.chattweaks.util.identifier
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ButtonTextures
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.input.KeyCodes
import net.minecraft.client.render.RenderLayer
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.ColorHelper
import net.minecraft.util.math.MathHelper

@Environment(EnvType.CLIENT)
abstract class PressableWidget(x: Int, y: Int, width: Int, height: Int, text: Text?) : ClickablePosRenderedWidget(x, y, width, height, text) {
	abstract fun onPress()

	override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		val minecraftClient = MinecraftClient.getInstance()
		context.drawGuiTexture(
			{ texture: Identifier -> RenderLayer.getGuiTextured(texture) },
			TEXTURES[active, this.isSelected],
			this.x,
			this.y,
			this.getWidth(),
			this.getHeight(),
			ColorHelper.getWhite(this.alpha)
		)
		val color = if (this.active) 16777215 else 10526880
		this.drawMessage(context, minecraftClient.textRenderer, color or (MathHelper.ceil(this.alpha * 255.0f) shl 24))
	}

	override fun render(context: DrawContext, x: Int, y: Int, width: Int, height: Int, mouseX: Int, mouseY: Int, delta: Float) {
		this.x = x
		this.y = y
		this.width = width
		this.height = height
		this.render(context, mouseX, mouseY, delta)
	}

	open fun drawMessage(context: DrawContext, textRenderer: TextRenderer, color: Int) {
		this.drawScrollableText(context, textRenderer, X_MARGIN, color)
	}

	override fun onClick(mouseX: Double, mouseY: Double) {
		this.onPress()
	}

	override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {
		appendDefaultNarrations(builder)
	}

	override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
		return if (!this.active || !this.visible) false
		else if (KeyCodes.isToggle(keyCode)) {
			this.playDownSound(MinecraftClient.getInstance().soundManager)
			this.onPress()
			true
		} else false
	}

	companion object {
		protected const val X_MARGIN: Int = 2
		private val TEXTURES = ButtonTextures(
			"widget/button".identifier, "widget/button_disabled".identifier, "widget/button_highlighted".identifier
		)
	}
}
