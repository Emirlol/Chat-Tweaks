package me.lumiafk.chattweaks.chat

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.isxander.yacl3.config.v3.value
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import me.lumiafk.chattweaks.chat.transformations.Transformer
import me.lumiafk.chattweaks.config.config
import me.lumiafk.chattweaks.util.ChatHudUtil.isChatFocused
import me.lumiafk.chattweaks.util.ChatHudUtil.isChatHidden
import me.lumiafk.chattweaks.util.ColorPalette
import me.lumiafk.chattweaks.util.ColorUtil.multiplyOpacity
import me.lumiafk.chattweaks.util.VisibleChatHudLine
import me.lumiafk.chattweaks.util.client
import me.lumiafk.chattweaks.util.isPointIn
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.hud.ChatHudLine
import net.minecraft.client.util.ChatMessages
import net.minecraft.text.OrderedText
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextCodecs
import net.minecraft.util.math.MathHelper
import java.awt.Color
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

@Suppress("UnstableApiUsage")
open class ChatBox(
	var name: Text,
	val transformers: MutableList<Transformer>,
	val data: ChatBoxData
) : Cloneable {
	open val visibleMessages = ObjectArrayList<VisibleChatHudLine>()

	var hasUnreadNewMessages = false

	var scrolledLines = 0
	open val scrollFromBottom get() = visibleMessages.size - min(visibleMessages.size, getVisibleLineCount()) - scrolledLines

	val scaledWidth get() = MathHelper.floor(data.width / data.scale).toDouble()

	val maxHeight get() = max(data.focusedHeight, data.unfocusedHeight)

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
		val p = (-8.0 * (chatLineSpacing + 1.0) + (4.0 * chatLineSpacing)).roundToInt() // Idk what this is to be honest. Probably centers the text within the given line spacing
		var q = 0 // Idk this either

		var highlight = false

		var lastDrawnTimestamp: Instant? = null
		for (line in 1..(min(visibleMessages.size, getVisibleLineCount(focused)))) {
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
					if (!visible.startOfEntry || (lastDrawnTimestamp != null && lastDrawnTimestamp.plusMillis(config.timeStampConfig.groupingMillis.value).isAfter(visible.originAddedTime))) {
						drawContext.fill(x1 + length + TIMESTAMP_MARGIN, y1, x1 + length + TIMESTAMP_MARGIN + 1, y2, config.timeStampConfig.textColor.value.multiplyOpacity(opacity))
					} else {
						drawContext.fill(x1, y1, x1 + length + TIMESTAMP_MARGIN, y2, config.timeStampConfig.backgroundColor.value.multiplyOpacity(opacity))
						drawContext.drawTextWithShadow(client.textRenderer, Text.literal(timestamp).withColor(config.timeStampConfig.textColor.value.multiplyOpacity(textOpacity)), x1 + 1, y2 + p, Color.WHITE.multiplyOpacity(textOpacity))
						lastDrawnTimestamp = visible.originAddedTime
					}

					drawContext.matrices.translate(length.toFloat() + (TIMESTAMP_MARGIN * 2), 0f, 0f)
				}

				RenderTimestamp.Right -> {
					drawContext.matrices.push()
					drawContext.matrices.translate(scaledWidth.toFloat() + 8, 0f, 0f)
					if (!visible.startOfEntry || (lastDrawnTimestamp != null && lastDrawnTimestamp.plusMillis(config.timeStampConfig.groupingMillis.value).isAfter(visible.originAddedTime))) {
						drawContext.fill(x1 + TIMESTAMP_MARGIN, y1, x1 + TIMESTAMP_MARGIN + 1, y2, config.timeStampConfig.textColor.value.multiplyOpacity(opacity))
					} else {
						drawContext.fill(x1 + TIMESTAMP_MARGIN, y1, x1 + TIMESTAMP_MARGIN + length, y2, config.timeStampConfig.backgroundColor.value.multiplyOpacity(opacity))
						drawContext.drawTextWithShadow(client.textRenderer, Text.literal(timestamp).withColor(config.timeStampConfig.textColor.value.multiplyOpacity(textOpacity)), x1 + TIMESTAMP_MARGIN + 1, y2 + p, Color.WHITE.multiplyOpacity(textOpacity))
						lastDrawnTimestamp = visible.originAddedTime
					}
					drawContext.matrices.pop()
				}

				RenderTimestamp.None -> {}
			}

			if (visible.startOfEntry) highlight = !highlight

			drawContext.fill(x1, y1, x2, y2, data.backgroundColor.multiplyOpacity(opacity))
			if (config.hudConfig.drawAlternatingRow.value && highlight) {
				drawContext.fill(x1, y1, x2, y2, config.hudConfig.alternatingRowColor.value.multiplyOpacity(opacity))
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

	open fun scroll(lines: Int) {
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

	fun transform(text: Text): Text? {
		var transformed: Text? = text
		for (transformer in transformers) {
			if (transformed == null) break
			transformed = with(transformer.context) {
				transformer.type.chatTransformer.transform(transformed)
			}
		}
		return transformed
	}

	fun addVisibleMessage(message: ChatHudLine, isChatFocused: Boolean) {
		val text = transform(message.content) ?: return
		val list: List<OrderedText> = ChatMessages.breakRenderedChatMessageLines(text, MathHelper.floor(scaledWidth), client.textRenderer)

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

	public override fun clone(): ChatBox = ChatBox(name.copy(), transformers.toMutableList(), data.clone())

	open fun isMouseOver(mouseX: Double, mouseY: Double): Boolean {
		return isPointIn(mouseX.toInt(), mouseY.toInt(), data.x, client.window.scaledHeight - data.y - maxHeight, data.x + data.width, client.window.scaledHeight - data.y)
	}

	fun getTextStyleAt(x: Double, y: Double): Style? {
		val d: Double = this.toChatLineX(x)
		val e: Double = this.toChatLineY(y)
		val i: Int = this.getMessageLineIndex(d, e)
		val visible = visibleMessages.getOrNull(visibleMessages.lastIndex - i) ?: return null
		return client.textRenderer.textHandler.getStyleAt(visible.orderedText, MathHelper.floor(d))
	}

	fun toChatLineX(x: Double) = (x - data.x) / data.scale

	fun toChatLineY(y: Double) = (client.window.scaledHeight - y - data.y) / (data.scale * lineHeight)

	fun getMessageLineIndex(chatLineX: Double, chatLineY: Double): Int {
		if (!isChatFocused || isChatHidden) return -1
		if (chatLineX < 0 || chatLineX > MathHelper.floor(data.width / data.scale).toDouble()) return -2

		//chatLineY is the line number from the bottom of the chat box
		val i = min(getVisibleLineCount(), visibleMessages.size)
		if (chatLineY < 0 || chatLineY >= i) return -3

		val j = MathHelper.floor(chatLineY + scrollFromBottom)
		if (j !in visibleMessages.indices) return -4

		return j
	}

	fun onFocusChange(focused: Boolean) {
		scroll(100_000_000) // Just a big-ass number to scroll to the bottom that is not Int.MAX_VALUE since that causes overflow, that I spent 30 minutes to debug...
	}

	companion object {
		val lineHeight get() = (9.0 * (client.options.chatLineSpacing.value + 1.0)).toInt()

		val DEFAULT_CHAT_BOX = ChatBox(Text.of("Default"), mutableListOf(), ChatBoxData.DEFAULT)

		val CODEC: Codec<ChatBox> = RecordCodecBuilder.create { instance ->
			instance.group(
				TextCodecs.CODEC.fieldOf("name").forGetter { it.name },
				Transformer.CODEC.listOf().fieldOf("transformers").forGetter { it.transformers },
				ChatBoxData.CODEC.fieldOf("data").forGetter { it.data }
			).apply(instance, ::ChatBox)
		}

		//Used as both the margin between chat and timestamps and for the margins from the left and right sides of the timestamp background (half instead of full though)
		const val TIMESTAMP_MARGIN = 4

		const val MESSAGE_DISAPPEAR_TICKS = 200

		fun getMessageOpacityMultiplier(age: Int) = ((1.0 - (age.toDouble() / MESSAGE_DISAPPEAR_TICKS)) * 10.0).coerceIn(0.0, 1.0).pow(2)
	}
}