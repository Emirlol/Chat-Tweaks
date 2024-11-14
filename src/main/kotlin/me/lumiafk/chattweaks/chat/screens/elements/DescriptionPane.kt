package me.lumiafk.chattweaks.chat.screens.elements

import me.lumiafk.chattweaks.chat.screens.ChatBoxesConfigScreen
import me.lumiafk.chattweaks.util.ColorPalette.MANTLE
import me.lumiafk.chattweaks.util.ElementUtil.buttonWidget
import me.lumiafk.chattweaks.util.client
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Selectable
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.text.Text

class DescriptionPane(
	private val x: Int,
	private val y: Int,
	private val width: Int,
	private val height: Int,
	private val parent: Screen
) : AbstractWidget() {
	override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		drawables.forEach { it.render(context, mouseX, mouseY, delta) }
	}

	override fun appendNarrations(builder: NarrationMessageBuilder) {}

	override fun getType() = Selectable.SelectionType.NONE

	override fun init() {
		addDrawableChild(Background(x, y, width, height, MANTLE.rgb))
		val doneButtonX = x + 1 + OUTER_PADDING // 1 is the width of the vertical line
		val doneButtonWidth = width - (OUTER_PADDING * 2) - 1
		val doneButtonY = height - BUTTON_HEIGHT - OUTER_PADDING
		addClickableWidget(buttonWidget(Text.of("Done"), doneButtonX, doneButtonY, doneButtonWidth) {
			(client.currentScreen as? ChatBoxesConfigScreen)?.save()
			parent.close()
		})
		addClickableWidget(buttonWidget(Text.of("Undo"), doneButtonX, doneButtonY - BUTTON_HEIGHT - INNER_PADDING, doneButtonWidth) {
			parent.close()
		})
		addDrawableChild(VerticalSeparator(x, y, height))
	}
}