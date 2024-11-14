package me.lumiafk.chattweaks.chat.screens.elements

import it.unimi.dsi.fastutil.objects.ObjectArrayList
import me.lumiafk.chattweaks.chat.ChatBox
import me.lumiafk.chattweaks.config.ConfigHandler
import me.lumiafk.chattweaks.util.ColorPalette
import me.lumiafk.chattweaks.util.ColorUtil.multiplyOpacity
import me.lumiafk.chattweaks.util.VisibleChatHudLine
import me.lumiafk.chattweaks.util.client
import net.datafaker.Faker
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Drawable
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.Selectable
import net.minecraft.client.gui.hud.ChatHudLine
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.text.Text
import net.minecraft.util.math.MathHelper
import java.awt.Color
import java.util.regex.Pattern
import kotlin.math.min
import kotlin.math.roundToInt

class ChatBoxWidget(chatBox: ChatBox) : ChatBox(chatBox.name, null as Pattern?, chatBox.data), Element, Drawable, Selectable {
	private var previousX = data.x
	private var previousY = client.window.scaledHeight - data.y
	private var heldFromX = 0.0
	private var heldFromY = 0.0

	@JvmField
	var focused = false

	val faker = Faker()
	override val visibleMessages = ObjectArrayList<VisibleChatHudLine>()

	init {
		val max = getMaxVisibleLineCount()
		while (visibleMessages.size < max) {
			addDummyMessage(faker.lorem().sentence())
		}
	}

	override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
		scroll(verticalAmount.toInt())
		return true
	}

	override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean {
		return super<ChatBox>.isMouseOver(mouseX, mouseY)
	}

	fun addDummyMessage(string: String) = addVisibleMessage(ChatHudLine(0, Text.of(string), null, null), focused)

	override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		render(context, delta, 0, mouseX, mouseY, focused)
	}

	override fun render(drawContext: DrawContext, tickDelta: Float, currentTick: Int, mouseX: Int, mouseY: Int, focused: Boolean) {
		val scale = data.scale //f
		val scaledWidth = MathHelper.ceil(data.width / scale) //k
		drawContext.matrices.push()
		drawContext.matrices.scale(scale, scale, 1f)
		drawContext.matrices.translate(1 / scale, 1 / scale, 1f)
		val scaledWindowHeight = drawContext.scaledWindowHeight / scale //m
		//This is used for message indicator, so it's not necessary for now
//		val messageIndexUnderMouse = getMessageIndex(toChatLineX(mouseX.toDouble()), toChatLineY(mouseY.toDouble())) //n
		//d = data.opacity
		//g = chatLineSpacing
		val chatLineSpacing: Double = client.options.chatLineSpacing.value //Not configurable per-chatbox since I doubt anyone would want different chat line spacings on different chat boxes. That's just too disorienting.
		//o = lineHeight
		val p = Math.round(-8.0 * (chatLineSpacing + 1.0) + (4.0 * chatLineSpacing)).toInt() // Idk what this is to be honest. Probably centers the text within the given line spacing
		var q = 0 //Idk this either

		for (line in 0..(min(visibleMessages.size - scrolledLines, getVisibleLineCount(focused)))) {
			val lineIndex = line + scrolledLines
			val visible = visibleMessages.getOrNull(lineIndex) ?: continue
			val messageAge = currentTick - visible.creationTick //t
			if (messageAge >= MESSAGE_DISAPPEAR_TICKS && !focused) continue
			val opacity = if (focused) 1.0 else getMessageOpacityMultiplier(messageAge) //h
			val textOpacity = opacity * data.opacity //u
			q++
			if (textOpacity < 0.01176470588) continue // Stop rendering when opacity is below a threshold. This is equal to an alpha of 3 out of 255.
			// Backgrounds
			val x1 = MathHelper.lerp(tickDelta, previousX, data.x)
			val x2 = x1 + scaledWidth
			val y2 = scaledWindowHeight.toInt() - MathHelper.lerp(tickDelta, previousY, data.y) - (line * lineHeight)
			val y1 = y2 - lineHeight
			drawContext.fill(x1, y1, x2, y2, data.backgroundColor.multiplyOpacity(opacity))
			drawContext.fill(x1, y1, x2, y2, ConfigHandler.config.hudConfig.alternatingRowColor.multiplyOpacity(opacity))
			//Not drawing message indicator for now

			drawContext.matrices.apply {
				push()
				translate(0F, 0F, 50F)
				drawContext.drawTextWithShadow(client.textRenderer, visible.orderedText, x1, y2 + p, Color.WHITE.multiplyOpacity(textOpacity))
				pop()
			}
		}

		val unprocessedMessages = client.messageHandler.unprocessedMessageCount
		if (unprocessedMessages > 0) {
			drawContext.matrices.apply {
				push()
				translate(0F, scaledWindowHeight, 0F)
				drawContext.fill(2, 0, scaledWidth + 4, 9, ColorPalette.BASE.multiplyOpacity(data.opacity))
				translate(0F, 0F, 50F)
				drawContext.drawTextWithShadow(client.textRenderer, Text.translatable("chat.queue", unprocessedMessages), 0, 1, Color.WHITE.multiplyOpacity(0.5 * data.opacity))
			}
		}

		//Honestly idk what this does, just copied it from the original code
		//Scrollbar perhaps?
		if (focused) {
			val supposedHeight = visibleMessages.size * lineHeight
			val renderedHeight = q * lineHeight
			if (supposedHeight != renderedHeight) {
				val af = (scrolledLines * lineHeight / visibleMessages.size - scaledWindowHeight).toInt()
				val u = renderedHeight * renderedHeight / supposedHeight
				val v = if (af > 0) 170 else 96
				val w = if (hasUnreadNewMessages) 13382451 else 3355562
				val x = scaledWidth + 4
				drawContext.fill(x, -af, x + 2, -af - u, 100, w + (v shl 24))
				drawContext.fill(x + 2, -af, x + 1, -af - u, 100, 13421772 + (v shl 24))
			}
		}
		drawContext.matrices.pop()

		previousX = data.x
		previousY = data.y
	}

	override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
		if (button == 0) {
			heldFromX = mouseX - data.x
			heldFromY = mouseY - (client.window.scaledHeight - data.y)
			return true
		}
		return false
	}

	override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
		if (button == 0) {
			val x = (mouseX - heldFromX).roundToInt()
			if (x + data.width > client.window.scaledWidth) data.x = client.window.scaledWidth - data.width
			else if (x < 0) data.x = 0
			else data.x = x

			val y = (mouseY - heldFromY).roundToInt()
			if (y - maxHeight < 0) data.y = client.window.scaledHeight - maxHeight
			else if (y > client.window.scaledHeight) data.y = 0
			else data.y = client.window.scaledHeight - y
			return true
		}
		return false
	}

	override fun isFocused(): Boolean {
		return focused
	}

	override fun setFocused(focused: Boolean) {}

	override fun appendNarrations(builder: NarrationMessageBuilder?) {}

	override fun getType(): Selectable.SelectionType = if (focused) Selectable.SelectionType.FOCUSED else Selectable.SelectionType.NONE
}