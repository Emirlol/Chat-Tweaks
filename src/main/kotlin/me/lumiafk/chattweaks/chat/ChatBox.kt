package me.lumiafk.chattweaks.chat

import it.unimi.dsi.fastutil.objects.ObjectArrayList
import me.lumiafk.chattweaks.config.ConfigHandler.config
import me.lumiafk.chattweaks.util.ColorPalette
import me.lumiafk.chattweaks.util.ColorUtil.multiplyOpacity
import me.lumiafk.chattweaks.util.VisibleChatHudLine
import me.lumiafk.chattweaks.util.client
import me.lumiafk.chattweaks.util.isPointIn
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.hud.ChatHudLine
import net.minecraft.client.util.ChatMessages
import net.minecraft.text.Text
import net.minecraft.util.math.MathHelper
import org.intellij.lang.annotations.Language
import java.awt.Color
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.regex.Pattern
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

open class ChatBox(var name: Text, var pattern: Pattern?, val data: ChatBoxData) : Cloneable {
	constructor(name: Text, @Language("RegExp") pattern: String?, data: ChatBoxData) : this(name, if (pattern.isNullOrEmpty()) null else Pattern.compile(pattern), data)

	open val visibleMessages = ObjectArrayList<VisibleChatHudLine>()

	var hasUnreadNewMessages = false

	var scrolledLines = 0

	val scaledWidth get() = MathHelper.floor(data.width / data.scale).toDouble()

	val maxHeight get() = max(data.focusedHeight, data.unfocusedHeight)

	// tickDelta currently unused, will have fun with it later :)
	@Suppress("DuplicatedCode")
	open fun render23(drawContext: DrawContext, tickDelta: Float, currentTick: Int, mouseX: Int, mouseY: Int, focused: Boolean) {
		val scale = data.scale //f
		val scaledWidth = MathHelper.ceil(data.width / scale) //k
		drawContext.matrices.push()
		drawContext.matrices.scale(scale, scale, 1f)
		val scaledWindowHeight = drawContext.scaledWindowHeight / scale //m
		//This is used for message indicator, so it's not necessary for now
//		val messageIndexUnderMouse = getMessageIndex(toChatLineX(mouseX.toDouble()), toChatLineY(mouseY.toDouble())) //n
		//d = data.opacity
		//g = chatLineSpacing
		val chatLineSpacing: Double = client.options.chatLineSpacing.value //Not configurable per-chatbox since I doubt anyone would want different chat line spacings on different chat boxes. That's just too disorienting.
		//o = lineHeight
		val p = Math.round(-8.0 * (chatLineSpacing + 1.0) + (4.0 * chatLineSpacing)).toInt() // Idk what this is to be honest. Probably centers the text within the given line spacing
		var q = 0 //Idk this either

		var highlight = false

		var lastDrawnTimestamp: Instant? = null
		for (line in 0..(min(visibleMessages.size - scrolledLines, getVisibleLineCount()))) {
			val lineIndex = line + scrolledLines
			val visible = visibleMessages.getOrNull(lineIndex) ?: continue
			val messageAge = currentTick - visible.creationTick //t
			if (messageAge >= MESSAGE_DISAPPEAR_TICKS && !focused) continue
			val opacity = if (focused) 1.0 else getMessageOpacityMultiplier(messageAge) //h
			val textOpacity = opacity * data.opacity //u
			q++
			if (textOpacity < 0.01176470588) continue // Stop rendering when opacity is below a threshold. This is equal to an alpha of 3 out of 255.
			// Backgrounds
			val x1 = data.x
			val x2 = x1 + scaledWidth + 8
			val y2 = scaledWindowHeight.toInt() - data.y - (line * lineHeight)
			val y1 = y2 - lineHeight

			val timestamp = config.timeStampConfig.dateTimeFormatter.format(LocalDateTime.ofInstant(visible.originAddedTime, ZoneId.systemDefault()))
			val length = client.textRenderer.getWidth(timestamp)
			when (data.renderTimestamp) {
				RenderTimestamp.Left -> {
					if (!visible.startOfEntry || (lastDrawnTimestamp != null && lastDrawnTimestamp.plusMillis(config.timeStampConfig.groupingMillis).isAfter(visible.originAddedTime))) {
						drawContext.fill(x1 + length + TIMESTAMP_MARGIN, y1, x1 + length + TIMESTAMP_MARGIN + 1, y2, config.timeStampConfig.textColor.rgb)
					} else {
						drawContext.fill(x1, y1, x1 + length + TIMESTAMP_MARGIN, y2, config.timeStampConfig.backgroundColor.multiplyOpacity(opacity))
						drawContext.drawTextWithShadow(client.textRenderer, Text.literal(timestamp).withColor(config.timeStampConfig.textColor.rgb), x1 + 1, y2 + p, Color.WHITE.multiplyOpacity(textOpacity))
						lastDrawnTimestamp = visible.originAddedTime
					}

					drawContext.matrices.translate(length.toFloat() + (TIMESTAMP_MARGIN * 2), 0f, 0f)
				}

				RenderTimestamp.Right -> {
					drawContext.matrices.push()
					drawContext.matrices.translate(scaledWidth.toFloat() + 8, 0f, 0f)
					if (!visible.startOfEntry || (lastDrawnTimestamp != null && lastDrawnTimestamp.plusMillis(config.timeStampConfig.groupingMillis).isAfter(visible.originAddedTime))) {
						drawContext.fill(x1 + TIMESTAMP_MARGIN, y1, x1 + TIMESTAMP_MARGIN + 1, y2, config.timeStampConfig.textColor.rgb)
					} else {
						drawContext.fill(x1 + TIMESTAMP_MARGIN, y1, x1 + TIMESTAMP_MARGIN + length, y2, config.timeStampConfig.backgroundColor.multiplyOpacity(opacity))
						drawContext.drawTextWithShadow(client.textRenderer, Text.literal(timestamp).withColor(config.timeStampConfig.textColor.rgb), x1 + TIMESTAMP_MARGIN + 1, y2 + p, Color.WHITE.multiplyOpacity(textOpacity))
						lastDrawnTimestamp = visible.originAddedTime
					}
					drawContext.matrices.pop()
				}

				RenderTimestamp.None -> {}
			}

			if (visible.startOfEntry) highlight = !highlight

			drawContext.fill(x1, y1, x2, y2, data.backgroundColor.multiplyOpacity(opacity))
			if (config.hudConfig.drawAlternatingRow && highlight) {
				drawContext.fill(x1, y1, x2, y2, config.hudConfig.alternatingRowColor.multiplyOpacity(opacity))
			}

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
	}

	// Assumes visiblemessages aren't reversed when adding
	@Suppress("DuplicatedCode")
	open fun render(drawContext: DrawContext, tickDelta: Float, currentTick: Int, mouseX: Int, mouseY: Int, focused: Boolean) {
		val scale = data.scale //f
		val scaledWidth = MathHelper.ceil(data.width / scale) //k
		drawContext.matrices.push()
		drawContext.matrices.scale(scale, scale, 1f)
		val scaledWindowHeight = drawContext.scaledWindowHeight / scale //m
		//This is used for message indicator, so it's not necessary
//		val messageIndexUnderMouse = getMessageIndex(toChatLineX(mouseX.toDouble()), toChatLineY(mouseY.toDouble())) //n
		//d = data.opacity
		//g = chatLineSpacing
		val chatLineSpacing: Double = client.options.chatLineSpacing.value //Not configurable per-chatbox since I doubt anyone would want different chat line spacings on different chat boxes. That's just too disorienting.
		//o = lineHeight
		val p = Math.round(-8.0 * (chatLineSpacing + 1.0) + (4.0 * chatLineSpacing)).toInt() // Idk what this is to be honest. Probably centers the text within the given line spacing
		var q = 0 // Idk this either

		var highlight = false

		var lastDrawnTimestamp: Instant? = null
		for (line in 1.. (min(visibleMessages.size, getVisibleLineCount(focused)))) {
			val lineIndex = line + scrolledLines - 1
			val visible = visibleMessages.getOrNull(lineIndex) ?: continue
			val messageAge = currentTick - visible.creationTick //t
			if (messageAge >= MESSAGE_DISAPPEAR_TICKS && !focused) continue
			val opacity = if (focused) 1.0 else getMessageOpacityMultiplier(messageAge) //h
			val textOpacity = opacity * data.opacity //u
			q++
			if (textOpacity < 0.01176470588) continue // Stop rendering when opacity is below a threshold. This is equal to an alpha of 3 out of 255.
			// Backgrounds
			val x1 = data.x
			val x2 = x1 + scaledWidth + 8
			val y2 = scaledWindowHeight.toInt() - data.y - ((min(visibleMessages.size, getVisibleLineCount(focused)) - line) * lineHeight)
			val y1 = y2 - lineHeight

			val timestamp = config.timeStampConfig.dateTimeFormatter.format(LocalDateTime.ofInstant(visible.originAddedTime, ZoneId.systemDefault()))
			val length = client.textRenderer.getWidth(timestamp)
			when (data.renderTimestamp) {
				RenderTimestamp.Left -> {
					if (!visible.startOfEntry || (lastDrawnTimestamp != null && lastDrawnTimestamp.plusMillis(config.timeStampConfig.groupingMillis).isAfter(visible.originAddedTime))) {
						drawContext.fill(x1 + length + TIMESTAMP_MARGIN, y1, x1 + length + TIMESTAMP_MARGIN + 1, y2, config.timeStampConfig.textColor.multiplyOpacity(opacity))
					} else {
						drawContext.fill(x1, y1, x1 + length + TIMESTAMP_MARGIN, y2, config.timeStampConfig.backgroundColor.multiplyOpacity(opacity))
						drawContext.drawTextWithShadow(client.textRenderer, Text.literal(timestamp).withColor(config.timeStampConfig.textColor.multiplyOpacity(textOpacity)), x1 + 1, y2 + p, Color.WHITE.multiplyOpacity(textOpacity))
						lastDrawnTimestamp = visible.originAddedTime
					}

					drawContext.matrices.translate(length.toFloat() + (TIMESTAMP_MARGIN * 2), 0f, 0f)
				}

				RenderTimestamp.Right -> {
					drawContext.matrices.push()
					drawContext.matrices.translate(scaledWidth.toFloat() + 8, 0f, 0f)
					if (!visible.startOfEntry || (lastDrawnTimestamp != null && lastDrawnTimestamp.plusMillis(config.timeStampConfig.groupingMillis).isAfter(visible.originAddedTime))) {
						drawContext.fill(x1 + TIMESTAMP_MARGIN, y1, x1 + TIMESTAMP_MARGIN + 1, y2, config.timeStampConfig.textColor.multiplyOpacity(opacity))
					} else {
						drawContext.fill(x1 + TIMESTAMP_MARGIN, y1, x1 + TIMESTAMP_MARGIN + length, y2, config.timeStampConfig.backgroundColor.multiplyOpacity(opacity))
						drawContext.drawTextWithShadow(client.textRenderer, Text.literal(timestamp).withColor(config.timeStampConfig.textColor.multiplyOpacity(textOpacity)), x1 + TIMESTAMP_MARGIN + 1, y2 + p, Color.WHITE.multiplyOpacity(textOpacity))
						lastDrawnTimestamp = visible.originAddedTime
					}
					drawContext.matrices.pop()
				}

				RenderTimestamp.None -> {}
			}

			if (visible.startOfEntry) highlight = !highlight

			drawContext.fill(x1, y1, x2, y2, data.backgroundColor.multiplyOpacity(opacity))
			if (config.hudConfig.drawAlternatingRow && highlight) {
				drawContext.fill(x1, y1, x2, y2, config.hudConfig.alternatingRowColor.multiplyOpacity(opacity))
			}


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

		//Will redo this
//		//Honestly idk what this does, just copied it from the original code
//		//Scrollbar perhaps?
//		if (focused) {
//			val supposedHeight = visibleMessages.size * lineHeight
//			val renderedHeight = q * lineHeight
//			if (supposedHeight != renderedHeight) {
//				val af = (scrolledLines * lineHeight / visibleMessages.size - scaledWindowHeight).toInt()
//				val u = renderedHeight * renderedHeight / supposedHeight
//				val v = if (af > 0) 170 else 96
//				val w = if (hasUnreadNewMessages) 13382451 else 3355562
//				val x = scaledWidth + 4
//				drawContext.fill(x, -af, x + 2, -af - u, 100, w + (v shl 24))
//				drawContext.fill(x + 2, -af, x + 1, -af - u, 100, 13421772 + (v shl 24))
//			}
//		}
		drawContext.matrices.pop()
	}

	fun matches(text: Text) = pattern?.matcher(text.string)?.matches() ?: true

	fun scroll(lines: Int) {
		scrolledLines += lines
		scrolledLines = scrolledLines.coerceAtLeast(0)
		scrolledLines = scrolledLines.coerceAtMost(
			if (visibleMessages.size > getVisibleLineCount()) visibleMessages.size - getVisibleLineCount()
			else 0
		)
	}

	fun getVisibleLineCount(focused: Boolean = client.inGameHud.chatHud.isChatFocused) = getHeight(focused) / lineHeight

	fun getMaxVisibleLineCount() = maxHeight / lineHeight

	fun getHeight(focused: Boolean): Int {
		return if (focused) data.focusedHeight else data.unfocusedHeight
	}

	fun addVisibleMessage(message: ChatHudLine, isChatFocused: Boolean) {
		if (!matches(message.content())) return

		val list = ChatMessages.breakRenderedChatMessageLines(message.content(), MathHelper.floor(scaledWidth), client.textRenderer)

		for (orderedText in list) {
			val startOfEntry = orderedText === list.first()
			visibleMessages.add(VisibleChatHudLine(message, orderedText, startOfEntry))
			if (isChatFocused && visibleMessages.size >= getVisibleLineCount(true) && scrolledLines < visibleMessages.size - getVisibleLineCount() - 1) {
				hasUnreadNewMessages = true
			} else {
				scroll(1)
			}

		}
	}
	public override fun clone(): ChatBox = ChatBox(name.copy(), pattern?.toString(), data.clone())

	open fun isMouseOver(mouseX: Double, mouseY: Double): Boolean {
		return isPointIn(mouseX.toInt(), mouseY.toInt(), data.x, client.window.scaledHeight - data.y - maxHeight, data.x + data.width, client.window.scaledHeight - data.y)
	}

	fun onFocusChange(focused: Boolean) {
		scroll(100000) // Just a big-ass number that is not Int.MAX_VALUE since that causes overflow, that I spent 30 minutes to debug...
	}

	companion object {
		val lineHeight get() = (9.0 * (client.options.chatLineSpacing.value + 1.0)).toInt()
		val DEFAULT_CHAT_BOX = ChatBox(Text.of("Default"), null as Pattern?, ChatBoxData.DEFAULT)

		//Used as both the margin between chat and timestamps and for the margins from the left and right sides of the timestamp background (half instead of full though)
		const val TIMESTAMP_MARGIN = 4

		const val MESSAGE_DISAPPEAR_TICKS = 200

		fun getMessageOpacityMultiplier(age: Int) = ((1.0 - (age.toDouble() / MESSAGE_DISAPPEAR_TICKS)) * 10.0).coerceIn(0.0, 1.0).pow(2)
	}
}