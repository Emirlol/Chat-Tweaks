package me.lumiafk.chattweaks.chat.screens.elements

import me.lumiafk.chattweaks.util.ColorPalette
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ButtonTextures
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.screen.narration.NarrationPart
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.sound.SoundManager
import net.minecraft.text.MutableText
import net.minecraft.text.OrderedText
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Colors
import net.minecraft.util.Identifier
import net.minecraft.util.StringHelper
import net.minecraft.util.Util
import net.minecraft.util.math.MathHelper
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Environment(EnvType.CLIENT)
open class TextFieldWidget(private val textRenderer: TextRenderer?, x: Int, y: Int, width: Int, height: Int, copyFrom: TextFieldWidget?, text: Text?) : ClickablePosRenderedWidget(x, y, width, height, text) {
	private var text = ""
	var maxLength = 32
		set(maxLength) {
			field = maxLength
			if (text.length > maxLength) {
				this.text = text.substring(0, maxLength)
				this.onChanged(this.text)
			}
		}

	private var drawsBackground = true
	var focusUnlocked = true
	private var isEditable = true

	var error = ""

	/**
	 * The index of the leftmost character that is rendered on a screen.
	 */
	private var firstCharacterIndex = 0
	var cursor: Int = 0
		private set
	private var selectionEnd = 0
	private var editableColor = DEFAULT_EDITABLE_COLOR
	private var uneditableColor = 7368816
	var suggestion: String? = null
	var changedListener: ((String) -> Unit)? = null
	var textPredicate = { obj: String? -> obj != null }
	var renderTextProvider = { string: String, _: Int -> OrderedText.styledForwardsVisitedString(string, Style.EMPTY) }
	var placeholder: Text? = null
	private var lastSwitchFocusTime = Util.getMeasuringTimeMs()

	constructor(textRenderer: TextRenderer?, width: Int, height: Int, text: Text?) : this(textRenderer, 0, 0, width, height, text)

	constructor(textRenderer: TextRenderer?, x: Int, y: Int, width: Int, height: Int, text: Text?) : this(textRenderer, x, y, width, height, null, text)

	init {
		if (copyFrom != null) {
			this.setText(copyFrom.getText())
		}
	}

	override fun getNarrationMessage(): MutableText {
		val text = this.message
		return Text.translatable("gui.narrate.editBox", text, this.text)
	}

	fun setText(text: String, notifyListener: Boolean = true) {
		if (textPredicate(text)) {
			if (text.length > this.maxLength) {
				this.text = text.substring(0, this.maxLength)
			} else {
				this.text = text
			}

			this.setCursorToEnd(false)
			this.setSelectionEnd(this.cursor)
			if (notifyListener) this.onChanged(text)
		}
	}

	fun getText(): String {
		return this.text
	}

	val selectedText: String
		get() {
			val i = min(cursor.toDouble(), selectionEnd.toDouble()).toInt()
			val j = max(cursor.toDouble(), selectionEnd.toDouble()).toInt()
			return text.substring(i, j)
		}

	fun write(text: String?) {
		val i = min(cursor.toDouble(), selectionEnd.toDouble()).toInt()
		val j = max(cursor.toDouble(), selectionEnd.toDouble()).toInt()
		var k = this.maxLength - this.text.length - (i - j)
		if (k > 0) {
			var string = StringHelper.stripInvalidChars(text)
			var l = string.length
			if (k < l) {
				if (Character.isHighSurrogate(string[k - 1])) {
					k--
				}

				string = string.substring(0, k)
				l = k
			}

			val string2 = StringBuilder(this.text).replace(i, j, string).toString()
			if (textPredicate(string2)) {
				this.text = string2
				this.setSelectionStart(i + l)
				this.setSelectionEnd(this.cursor)
				this.onChanged(this.text)
			}
		}
	}

	private fun onChanged(newText: String) {
		changedListener?.invoke(newText)
	}

	private fun erase(offset: Int) {
		if (Screen.hasControlDown()) {
			this.eraseWords(offset)
		} else {
			this.eraseCharacters(offset)
		}
	}

	fun eraseWords(wordOffset: Int) {
		if (text.isNotEmpty()) {
			if (this.selectionEnd != this.cursor) this.write("")
			else this.eraseCharactersTo(this.getWordSkipPosition(wordOffset))
		}
	}

	fun eraseCharacters(characterOffset: Int) {
		this.eraseCharactersTo(this.getCursorPosWithOffset(characterOffset))
	}

	fun eraseCharactersTo(position: Int) {
		if (text.isNotEmpty()) {
			if (this.selectionEnd != this.cursor) this.write("")
			else {
				val i = min(position.toDouble(), cursor.toDouble()).toInt()
				val j = max(position.toDouble(), cursor.toDouble()).toInt()
				if (i != j) {
					val string = StringBuilder(this.text).delete(i, j).toString()
					if (textPredicate(string)) {
						this.text = string
						this.setCursor(i, false)
					}
				}
			}
		}
	}

	private fun getWordSkipPosition(wordOffset: Int, cursorPosition: Int = this.cursor, skipOverSpaces: Boolean = true): Int {
		var i = cursorPosition
		val bl = wordOffset < 0
		val j = abs(wordOffset.toDouble()).toInt()

		for (k in 0 until j) {
			if (!bl) {
				val length = text.length
				i = text.indexOf(' ', i)
				if (i == -1) i = length
				else while (skipOverSpaces && i < length && text[i] == ' ') i++
			} else {
				while (skipOverSpaces && i > 0 && text[i - 1] == ' ') i--
				while (i > 0 && text[i - 1] != ' ') i--
			}
		}

		return i
	}

	fun moveCursor(offset: Int, shiftKeyPressed: Boolean) {
		this.setCursor(this.getCursorPosWithOffset(offset), shiftKeyPressed)
	}

	private fun getCursorPosWithOffset(offset: Int): Int = Util.moveCursor(this.text, this.cursor, offset)

	fun setCursor(cursor: Int, shiftKeyPressed: Boolean) {
		this.setSelectionStart(cursor)
		if (!shiftKeyPressed) this.setSelectionEnd(this.cursor)
		this.onChanged(this.text)
	}

	fun setSelectionStart(cursor: Int) {
		this.cursor = MathHelper.clamp(cursor, 0, text.length)
		this.updateFirstCharacterIndex(this.cursor)
	}

	fun setCursorToStart(shiftKeyPressed: Boolean) = this.setCursor(0, shiftKeyPressed)

	fun setCursorToEnd(shiftKeyPressed: Boolean) = this.setCursor(text.length, shiftKeyPressed)

	override fun render(context: DrawContext, x: Int, y: Int, width: Int, height: Int, mouseX: Int, mouseY: Int, delta: Float) {
		this.x = x
		this.y = y
		this.width = width
		this.height = height
		this.render(context, mouseX, mouseY, delta)
	}

	override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
		return if (this.isNarratable && this.isFocused) {
			when (keyCode) {
				259 -> {
					if (this.isEditable) this.erase(-1)

					true
				}

				261 -> {
					if (this.isEditable) this.erase(1)

					true
				}

				262 -> {
					if (Screen.hasControlDown()) this.setCursor(this.getWordSkipPosition(1), Screen.hasShiftDown())
					else this.moveCursor(1, Screen.hasShiftDown())

					true
				}

				263 -> {
					if (Screen.hasControlDown()) this.setCursor(this.getWordSkipPosition(-1), Screen.hasShiftDown())
					else this.moveCursor(-1, Screen.hasShiftDown())

					true
				}

				268 -> {
					this.setCursorToStart(Screen.hasShiftDown())
					true
				}

				269 -> {
					this.setCursorToEnd(Screen.hasShiftDown())
					true
				}

				//The 260, 264, 265, 266, 267 branch of the when expression was removed since they also fall into this else block, and the code is the same afterwards
				else -> when {
					Screen.isSelectAll(keyCode) -> {
						this.setCursorToEnd(false)
						this.setSelectionEnd(0)
						true
					}

					Screen.isCopy(keyCode) -> {
						MinecraftClient.getInstance().keyboard.clipboard = selectedText
						true
					}

					Screen.isPaste(keyCode) -> {
						if (this.isEditable) this.write(MinecraftClient.getInstance().keyboard.clipboard)

						true
					}

					else -> {
						if (Screen.isCut(keyCode)) {
							MinecraftClient.getInstance().keyboard.clipboard = selectedText
							if (this.isEditable) this.write("")

							return true
						}

						false
					}
				}
			}
		} else false
	}

	val isActive: Boolean
		get() = this.isNarratable && this.isFocused && this.isEditable

	override fun charTyped(chr: Char, modifiers: Int): Boolean {
		return if (!this.isActive) false
		else if (StringHelper.isValidChar(chr)) {
			if (this.isEditable) this.write(chr.toString())
			true
		} else false
	}

	override fun onClick(mouseX: Double, mouseY: Double) {
		var i = MathHelper.floor(mouseX) - this.x
		if (this.drawsBackground) i -= 4

		val string = textRenderer!!.trimToWidth(text.substring(this.firstCharacterIndex), this.innerWidth)
		this.setCursor(textRenderer.trimToWidth(string, i).length + this.firstCharacterIndex, Screen.hasShiftDown())
	}

	override fun playDownSound(soundManager: SoundManager) {}

	public override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		if (this.visible) {
			if (this.drawsBackground) {
				val identifier = TEXTURES[this.isNarratable, this.isFocused]
				context.drawGuiTexture({ texture: Identifier? -> RenderLayer.getGuiTextured(texture) }, identifier, this.x, this.y, this.getWidth(), this.getHeight())
			}

			val i = if (this.isEditable) this.editableColor else this.uneditableColor
			val j = this.cursor - this.firstCharacterIndex
			val string = textRenderer!!.trimToWidth(text.substring(this.firstCharacterIndex), this.innerWidth)
			val cursorWithinString = j >= 0 && j <= string.length
			val drawCursor = this.isFocused && (Util.getMeasuringTimeMs() - this.lastSwitchFocusTime) / CURSOR_BLINK_DELAY % 2L == 0L && cursorWithinString
			val k = if (this.drawsBackground) this.x + 4 else this.x
			val l = if (this.drawsBackground) this.y + (this.height - 8) / 2 else this.y
			var m = k
			val n = MathHelper.clamp(this.selectionEnd - this.firstCharacterIndex, 0, string.length)
			if (string.isNotEmpty()) {
				val string2 = if (cursorWithinString) string.substring(0, j) else string
				m = context.drawTextWithShadow(this.textRenderer, renderTextProvider(string2, this.firstCharacterIndex) as OrderedText, k, l, i)
			}

			val bl3 = this.cursor < text.length || text.length >= this.maxLength
			var o = m
			if (!cursorWithinString) {
				o = if (j > 0) k + this.width else k
			} else if (bl3) {
				o = m - 1
				m--
			}

			if (string.isNotEmpty() && cursorWithinString && j < string.length) {
				context.drawTextWithShadow(this.textRenderer, renderTextProvider(string.substring(j), this.cursor) as OrderedText, m, l, i)
			}

			if (this.placeholder != null && string.isEmpty() && !this.isFocused) {
				context.drawTextWithShadow(this.textRenderer, this.placeholder, m, l, i)
			}

			if (!bl3 && this.suggestion != null) {
				context.drawTextWithShadow(this.textRenderer, this.suggestion, o - 1, l, Colors.GRAY)
			}

			if (drawCursor) {
				if (bl3) context.fill(RenderLayer.getGuiOverlay(), o, l - 1, o + 1, l + 1 + 9, VERTICAL_CURSOR_COLOR)
				else context.drawTextWithShadow(this.textRenderer, HORIZONTAL_CURSOR, o, l, i)
			}

			if (n != j) {
				val p = k + textRenderer.getWidth(string.substring(0, n))
				this.drawSelectionHighlight(context, o, l - 1, p - 1, l + 1 + 9)
			}

			if (error.isNotEmpty()) {
				context.matrices.apply {
					push()
					translate(0F, 0F, 500F)
					context.fill(x, y + height, x + width, y + height + 13, ColorPalette.SURFACE2.rgb)
					translate(0F, 0F, 1F)
					context.drawVerticalLine(x, y + height, y + height + 13, ColorPalette.RED.rgb)
					context.drawVerticalLine(x + width, y + height, y + height + 13, ColorPalette.RED.rgb)
					context.drawHorizontalLine(x, x + width, y + height + 13, ColorPalette.RED.rgb)
					translate(0F, 0F, 1F)
					context.drawTextWithShadow(textRenderer, error, x + 3, y + height + 2, ColorPalette.RED.rgb)
					pop()
				}
			}
		}
	}

	private fun drawSelectionHighlight(context: DrawContext, x1: Int, y1: Int, x2: Int, y2: Int) {
		var x1 = x1
		var y1 = y1
		var x2 = x2
		var y2 = y2
		if (x1 < x2) {
			val i = x1
			x1 = x2
			x2 = i
		}

		if (y1 < y2) {
			val i = y1
			y1 = y2
			y2 = i
		}

		if (x2 > this.x + this.width) {
			x2 = this.x + this.width
		}

		if (x1 > this.x + this.width) {
			x1 = this.x + this.width
		}

		context.fill(RenderLayer.getGuiTextHighlight(), x1, y1, x2, y2, Colors.BLUE)
	}

	override fun setFocused(focused: Boolean) {
		if (this.focusUnlocked || focused) {
			super.setFocused(focused)
			if (focused) {
				this.lastSwitchFocusTime = Util.getMeasuringTimeMs()
			}
		}
	}

	val innerWidth: Int
		get() = if (this.drawsBackground) this.width - 8 else this.width

	fun setSelectionEnd(index: Int) {
		this.selectionEnd = MathHelper.clamp(index, 0, text.length)
		this.updateFirstCharacterIndex(this.selectionEnd)
	}

	private fun updateFirstCharacterIndex(cursor: Int) {
		if (this.textRenderer != null) {
			this.firstCharacterIndex = min(firstCharacterIndex.toDouble(), text.length.toDouble()).toInt()
			val i = this.innerWidth
			val string = textRenderer.trimToWidth(text.substring(this.firstCharacterIndex), i)
			val j = string.length + this.firstCharacterIndex
			if (cursor == this.firstCharacterIndex) {
				this.firstCharacterIndex -= textRenderer.trimToWidth(this.text, i, true).length
			}

			if (cursor > j) {
				this.firstCharacterIndex += cursor - j
			} else if (cursor <= this.firstCharacterIndex) {
				this.firstCharacterIndex -= (this.firstCharacterIndex - cursor)
			}

			this.firstCharacterIndex = MathHelper.clamp(this.firstCharacterIndex, 0, text.length)
		}
	}

	fun getCharacterX(index: Int): Int = if (index > text.length) this.x else this.x + textRenderer!!.getWidth(text.substring(0, index))

	public override fun appendClickableNarrations(builder: NarrationMessageBuilder) {
		builder.put(NarrationPart.TITLE, this.narrationMessage)
	}

	companion object {
		private val TEXTURES = ButtonTextures(
			Identifier.ofVanilla("widget/text_field"), Identifier.ofVanilla("widget/text_field_highlighted")
		)
		const val field_32194: Int = -1
		const val field_32195: Int = 1
		private const val field_32197 = 1
		private const val VERTICAL_CURSOR_COLOR = -3092272
		private const val HORIZONTAL_CURSOR = "_"
		const val DEFAULT_EDITABLE_COLOR: Int = 14737632
		private const val CURSOR_BLINK_DELAY = 300
	}
}
