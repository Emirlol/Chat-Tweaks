package me.lumiafk.chattweaks.chat.screens.elements

import me.lumiafk.chattweaks.chat.screens.Initializable
import net.minecraft.client.gui.AbstractParentElement
import net.minecraft.client.gui.Drawable
import net.minecraft.client.gui.Selectable
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.ClickableWidget

abstract class AbstractWidget : AbstractParentElement(), Drawable, Initializable, Selectable {
	protected var children = mutableListOf<ClickableWidget>()
	protected var drawables = mutableListOf<Drawable>()
	override fun children() = children

	fun addDrawableChild(drawable: Drawable) {
		drawables += drawable
	}

	fun addClickableWidget(widget: ClickableWidget) {
		children += widget
		drawables += widget
	}

	companion object {
		const val INNER_PADDING = 5
		const val OUTER_PADDING = 10
		const val BUTTON_HEIGHT = ButtonWidget.DEFAULT_HEIGHT
	}
}