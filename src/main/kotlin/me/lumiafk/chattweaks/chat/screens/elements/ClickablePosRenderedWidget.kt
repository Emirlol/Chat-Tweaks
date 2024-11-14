package me.lumiafk.chattweaks.chat.screens.elements

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.text.Text

@Environment(EnvType.CLIENT)
abstract class ClickablePosRenderedWidget(x: Int, y: Int, width: Int, height: Int, message: Text?) : ClickableWidget(x, y, width, height, message), PositionedDrawable {
	//For lerping when rendering
	var previousX = x
	var previousY = y
}