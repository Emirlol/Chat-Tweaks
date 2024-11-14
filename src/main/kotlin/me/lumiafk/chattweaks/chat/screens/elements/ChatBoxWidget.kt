package me.lumiafk.chattweaks.chat.screens.elements

import dev.isxander.yacl3.config.v3.value
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import me.lumiafk.chattweaks.chat.ChatBox
import me.lumiafk.chattweaks.chat.RenderTimestamp
import me.lumiafk.chattweaks.chat.screens.Initializable
import me.lumiafk.chattweaks.config.config
import me.lumiafk.chattweaks.util.ColorPalette
import me.lumiafk.chattweaks.util.ColorUtil.multiplyOpacity
import me.lumiafk.chattweaks.util.VisibleChatHudLine
import me.lumiafk.chattweaks.util.client
import me.lumiafk.chattweaks.util.isPointIn
import net.datafaker.Faker
import net.minecraft.client.gui.*
import net.minecraft.client.gui.hud.ChatHudLine
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.text.Text
import net.minecraft.util.math.MathHelper
import java.awt.Color
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.math.min
import kotlin.math.roundToInt

@Suppress("DuplicatedCode")
class ChatBoxWidget(chatBox: ChatBox, val configuring: Boolean = false) : ChatBox(chatBox.name, mutableListOf(), chatBox.data), ParentElement, Drawable, Selectable, Initializable {
	private var previousX = data.x
	private var previousY = data.y
	private var heldFromX = 0.0
	private var heldFromY = 0.0
	private val children = mutableListOf<Corner>()
	private val drawables = mutableListOf<Drawable>()
	private var focusedElement: Element? = null
	private var dragging = false
	var messages = mutableListOf<ChatHudLine>()
	override val scrollFromBottom get() = visibleMessages.size - min(visibleMessages.size, getVisibleLineCount(focused)) - scrolledLines

	@JvmField
	var focused = false

	internal var endX = data.x + data.width
	internal var endY = data.y + getHeight(focused)

	val faker = Faker()

	init {
		val max = client.window.scaledHeight / lineHeight
		while (messages.size < max) {
			messages += ChatHudLine(0, Text.of(faker.lorem().sentence()), null, null)
		}
	}

	override val visibleMessages = ObjectArrayList<VisibleChatHudLine>()

	override fun init() {
		refresh()
		if (configuring) {
			TopRight(this).also {
				children += it
				drawables += it
			}
			TopLeft(this).also {
				children += it
				drawables += it
			}
			BottomRight(this).also {
				children += it
				drawables += it
			}
			BottomLeft(this).also {
				children += it
				drawables += it
			}
		}
	}

	fun refresh(): Boolean {
		visibleMessages.clear()
		messages.forEach { addVisibleMessage(it, focused) }
		return true
	}

	override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
		scroll(-verticalAmount.toInt())
		return true
	}

	override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean {
		return super<ChatBox>.isMouseOver(mouseX, mouseY)
	}

	override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		render(context, delta, 0, mouseX, mouseY, focused)
	}

	override fun render(drawContext: DrawContext, tickDelta: Float, currentTick: Int, mouseX: Int, mouseY: Int, focused: Boolean) {
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
			val x1 = MathHelper.lerp(tickDelta, previousX, data.x)
			val x2 = x1 + scaledWidth
			val y2 = scaledWindowHeight.toInt() - MathHelper.lerp(tickDelta, previousY, data.y) - ((min(visibleMessages.size, getVisibleLineCount(focused)) - line) * lineHeight)
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

		drawContext.matrices.pop()

		previousX = data.x
		previousY = data.y

		drawables.forEach { it.render(drawContext, mouseX, mouseY, tickDelta) }

		//This should really be a drawable but I cba
		if (configuring) drawContext.drawBorder(MathHelper.lerp(tickDelta, previousX, data.x), client.window.scaledHeight - MathHelper.lerp(tickDelta, previousY, data.y) - getHeight(focused), data.width, getHeight(focused), ColorPalette.TEXT.multiplyOpacity(0.7))
	}

	override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
		if (super.mouseClicked(mouseX, mouseY, button)) return true
		else if (isMouseOver(mouseX, mouseY)) {
			when (button) {
				0 -> {
					heldFromX = mouseX - data.x
					heldFromY = mouseY - (client.window.scaledHeight - data.y)
				}
				1 -> {
					focused = !focused
					onFocusChange(focused)
				}
				else -> return false
			}

			return true
		}
		return false
	}

	override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
		if (!super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY) && button == 0) {
			val x = (mouseX - heldFromX).roundToInt()
			if (x + data.width > client.window.scaledWidth) data.x = client.window.scaledWidth - data.width
			else if (x < 0) data.x = 0
			else data.x = x

			val y = (mouseY - heldFromY).roundToInt()
			if (y - maxHeight < 0) data.y = client.window.scaledHeight - maxHeight
			else if (y > client.window.scaledHeight) data.y = 0
			else data.y = client.window.scaledHeight - y

			endX = data.x + data.width
			endY = data.y + getHeight(focused)
			return true
		}
		return false
	}

	override fun setFocused(focused: Element?) {
		this.focusedElement = focused
	}

	override fun getFocused(): Element? = focusedElement

	override fun isFocused(): Boolean = focused

	override fun setFocused(focused: Boolean) {
	}

	override fun children(): MutableList<out Element> = children

	override fun isDragging(): Boolean = dragging

	override fun setDragging(dragging: Boolean) {
		this.dragging = dragging
	}

	override fun appendNarrations(builder: NarrationMessageBuilder?) {}

	override fun getType() = Selectable.SelectionType.NONE

	fun setHeight(height: Int) {
		if (focused) data.focusedHeight = height
		else data.unfocusedHeight = height
	}

	override fun scroll(lines: Int) {
		scrolledLines += lines
		scrolledLines = scrolledLines.coerceAtLeast(0)
		scrolledLines = scrolledLines.coerceAtMost(
			if (visibleMessages.size > getVisibleLineCount(focused)) visibleMessages.size - getVisibleLineCount(focused)
			else 0
		)
	}

	abstract class Corner(val parent: ChatBoxWidget) : Element, Drawable {
		var heldFromX = 0.0
		var heldFromY = 0.0

		val windowHeight get() = client.window.scaledHeight
		val windowWidth get() = client.window.scaledWidth

		//Halved values
		val width = 4
		val height = 4
		val color = ColorPalette.MAUVE.multiplyOpacity(0.7)

		//Avoid jvm signature clash
		@JvmField
		var focused = false
		var isDragging = false
		val minSize = 20

		override fun setFocused(focused: Boolean) {
			this.focused = focused
		}

		override fun isFocused(): Boolean = focused

		override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
			if (isDragging && button == 0) {
				isDragging = false
				return true
			}
			return false
		}

		override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
			return button == 0 && (isDragging || isMouseOver(mouseX, mouseY)) && parent.refresh()
		}
	}

	class TopLeft(parent: ChatBoxWidget) : Corner(parent) {
		override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
			val x = MathHelper.lerp(delta, parent.previousX, parent.data.x)
			val y = windowHeight - MathHelper.lerp(delta, parent.previousY, parent.data.y) - parent.getHeight(parent.focused)
			context.fill(x - width, y - height, x + width, y + height, color)
		}

		override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
			if (!super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) return false
			val x = (mouseX - heldFromX).roundToInt()
			if (x < 0) {
				parent.data.x = 0
				parent.data.width = parent.endX - parent.data.x
			} else if (x + minSize > parent.endX) {
				parent.data.x = parent.endX - minSize
				parent.data.width = minSize
			} else {
				parent.data.x = x
				parent.data.width = parent.endX - x
			}

			val y = (windowHeight - (mouseY - heldFromY)).roundToInt()
			if (y > windowHeight) {
				parent.endY = windowHeight
				parent.setHeight(parent.endY - parent.data.y)
			} else if (y - minSize < parent.data.y) {
				parent.endY = parent.data.y + minSize
				parent.setHeight(minSize)
			} else {
				parent.endY = y
				parent.setHeight(parent.endY - parent.data.y)
			}

			return true
		}

		override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
			if (isMouseOver(mouseX, mouseY) && button == 0) {
				heldFromX = mouseX - parent.data.x
				heldFromY = mouseY - (windowHeight - parent.data.y - parent.getHeight(parent.focused))
				isDragging = true
				return true
			}
			return false
		}

		override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean = isPointIn(
			mouseX.roundToInt(),
			mouseY.roundToInt(),
			parent.data.x - width,
			client.window.scaledHeight - parent.data.y - parent.getHeight(parent.focused) - height,
			parent.data.x + width,
			client.window.scaledHeight - parent.data.y - parent.getHeight(parent.focused) + height
		)
	}

	class TopRight(parent: ChatBoxWidget) : Corner(parent) {
		override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
			val x = MathHelper.lerp(delta, parent.previousX, parent.data.x) + parent.data.width
			val y = client.window.scaledHeight - MathHelper.lerp(delta, parent.previousY, parent.data.y) - parent.getHeight(parent.focused)
			context.fill(x - width, y - height, x + width, y + height, color)
		}

		override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
			if (!super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) return false
			val x = (mouseX - heldFromX).roundToInt()
			if (x > windowWidth) {
				parent.endX = windowWidth
				parent.data.width = parent.endX - parent.data.x
			} else if (x - minSize < parent.data.x) {
				parent.data.width = minSize
				parent.endX = parent.data.x + parent.data.width
			} else {
				parent.endX = x
				parent.data.width = parent.endX - parent.data.x
			}

			val y = (windowHeight - (mouseY - heldFromY)).roundToInt()
			if (y > windowHeight) {
				parent.endY = windowHeight
				parent.setHeight(parent.endY - parent.data.y)
			} else if (y - minSize < parent.data.y) {
				parent.endY = parent.data.y + minSize
				parent.setHeight(minSize)
			} else {
				parent.endY = y
				parent.setHeight(parent.endY - parent.data.y)
			}

			return true
		}

		override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean = isPointIn(
			mouseX.roundToInt(),
			mouseY.roundToInt(),
			parent.data.x + parent.data.width - width,
			windowHeight - parent.data.y - parent.getHeight(parent.focused) - height,
			parent.data.x + parent.data.width + width,
			windowHeight - parent.data.y - parent.getHeight(parent.focused) + height
		)

		override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
			if (isMouseOver(mouseX, mouseY) && button == 0) {
				heldFromX = mouseX - (parent.data.x + parent.data.width)
				heldFromY = mouseY - (windowHeight - parent.data.y - parent.getHeight(parent.focused))
				isDragging = true
				return true
			}
			return false
		}
	}

	class BottomLeft(parent: ChatBoxWidget) : Corner(parent) {
		override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
			val x = MathHelper.lerp(delta, parent.previousX, parent.data.x)
			val y = windowHeight - MathHelper.lerp(delta, parent.previousY, parent.data.y)
			context.fill(x - width, y - height, x + width, y + height, color)
		}

		override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean = isPointIn(
			mouseX.roundToInt(),
			mouseY.roundToInt(),
			parent.data.x - width,
			windowHeight - parent.data.y - height,
			parent.data.x + width,
			windowHeight - parent.data.y + height
		)

		override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
			if (!super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) return false
			val x = (mouseX - heldFromX).roundToInt()
			if (x < 0) {
				parent.data.x = 0
				parent.data.width = parent.endX - parent.data.x
			} else if (x + minSize > parent.endX) {
				parent.data.x = parent.endX - minSize
				parent.data.width = minSize
			} else {
				parent.data.x = x
				parent.data.width = parent.endX - x
			}

			val y = (windowHeight - (mouseY - heldFromY)).roundToInt()
			if (y < 0) {
				parent.data.y = 0
				parent.setHeight(parent.endY - parent.data.y)
			} else if (y + minSize > parent.endY) {
				parent.data.y = parent.endY - minSize
				parent.setHeight(minSize)
			} else {
				parent.data.y = y
				parent.setHeight(parent.endY - parent.data.y)
			}

			return true
		}

		override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
			if (isMouseOver(mouseX, mouseY) && button == 0) {
				heldFromX = mouseX - parent.data.x
				heldFromY = mouseY - (windowHeight - parent.data.y)
				isDragging = true
				return true
			}
			return false
		}
	}

	class BottomRight(parent: ChatBoxWidget) : Corner(parent) {
		override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
			val x = MathHelper.lerp(delta, parent.previousX, parent.data.x) + parent.data.width
			val y = windowHeight - MathHelper.lerp(delta, parent.previousY, parent.data.y)
			context.fill(x - width, y - height, x + width, y + height, color)
		}

		override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean = isPointIn(
			mouseX.roundToInt(),
			mouseY.roundToInt(),
			parent.data.x + parent.data.width - width,
			windowHeight - parent.data.y - height,
			parent.data.x + parent.data.width + width,
			windowHeight - parent.data.y + height
		)

		override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
			if (!super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) return false
			val x = (mouseX - heldFromX).roundToInt()
			if (x > windowWidth) {
				parent.endX = windowWidth
				parent.data.width = parent.endX - parent.data.x
			} else if (x - minSize < parent.data.x) {
				parent.data.width = minSize
				parent.endX = parent.data.x + parent.data.width
			} else {
				parent.endX = x
				parent.data.width = parent.endX - parent.data.x
			}

			val y = (windowHeight - (mouseY - heldFromY)).roundToInt()
			if (y < 0) {
				parent.data.y = 0
				parent.setHeight(parent.endY - parent.data.y)
			} else if (y + minSize > parent.endY) {
				parent.data.y = parent.endY - minSize
				parent.setHeight(minSize)
			} else {
				parent.data.y = y
				parent.setHeight(parent.endY - parent.data.y)
			}

			return true
		}

		override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
			if (isMouseOver(mouseX, mouseY) && button == 0) {
				heldFromX = mouseX - (parent.data.x + parent.data.width)
				heldFromY = mouseY - (windowHeight - parent.data.y)
				isDragging = true
				return true
			}
			return false
		}
	}
}