package me.lumiafk.chattweaks.chat.screens.elements

import me.lumiafk.chattweaks.chat.ChatBox
import me.lumiafk.chattweaks.chat.screens.Initializable
import me.lumiafk.chattweaks.chat.screens.elements.AbstractWidget.Companion.BUTTON_HEIGHT
import me.lumiafk.chattweaks.chat.screens.elements.AbstractWidget.Companion.INNER_PADDING
import me.lumiafk.chattweaks.chat.screens.elements.TransformerEntryList.AbstractEntry
import me.lumiafk.chattweaks.chat.transformations.Transformer
import me.lumiafk.chattweaks.chat.transformations.TransformerContext
import me.lumiafk.chattweaks.chat.transformations.TransformerType
import me.lumiafk.chattweaks.chat.transformations.filters.FilterMode
import me.lumiafk.chattweaks.chat.transformations.transformers.ReplacementMode
import me.lumiafk.chattweaks.util.*
import me.lumiafk.chattweaks.util.ElementUtil.buttonWidget
import me.lumiafk.chattweaks.util.ElementUtil.textFieldWidget
import me.lumiafk.chattweaks.util.TextUtil.text
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.Selectable
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ElementListWidget
import net.minecraft.util.Formatting
import java.util.function.Consumer
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException
import kotlin.math.abs

class TransformerEntryList(y: Int, width: Int, height: Int, val chatBox: ChatBox, val parent: Screen) : ElementListWidget<AbstractEntry>(MinecraftClient.getInstance(), width, height, y, BUTTON_HEIGHT + INNER_PADDING), Initializable {
	override fun init() {
		for (i in chatBox.transformers.indices) {
			addEntry(ElementEntry(chatBox.transformers[i], this))
			if (i != chatBox.transformers.lastIndex) addEntry(ArrowEntry)
		}

		addEntry(NewEntry(this))
		initChildren()
	}

	override fun getRowWidth(): Int = width
	override fun getRowLeft(): Int = this.x
	override fun getRowTop(index: Int): Int = this.y - scrollY.toInt() + index * this.itemHeight + this.headerHeight

	override fun renderHeader(context: DrawContext, x: Int, y: Int) {
		context.drawCenteredTextWithShadow(client.textRenderer, "Transformers".literal(Formatting.BOLD), x + width / 2, y, 0xFFFFFF)
	}

	fun refreshButtonActiveness() {
		var list = children().filterIsInstance<ElementEntry>()
		for (i in list.indices) {
			val it = list[i]
			it.upButton.active = i != 0
			it.downButton.active = i != chatBox.transformers.lastIndex
		}
	}

	abstract class AbstractEntry() : Entry<AbstractEntry>()

	object ArrowEntry : AbstractEntry() {
		override fun children(): List<Element> = emptyList()

		override fun selectableChildren(): List<Selectable> = emptyList()
		override fun render(context: DrawContext, index: Int, y: Int, x: Int, entryWidth: Int, entryHeight: Int, mouseX: Int, mouseY: Int, hovered: Boolean, tickDelta: Float) {
			context.drawCenteredTextWithShadow(client.textRenderer, "↓", x + entryWidth / 2, y + entryHeight / 2, ColorPalette.OVERLAY2.rgb)
		}
	}

	class NewEntry(var parent: TransformerEntryList) : AbstractEntry() {
		val button = buttonWidget("Add new transformer".literal(ColorPalette.GREEN.rgb), 1) {
			val transformer = Transformer(TransformerType.STRING_REPLACE, TransformerContext())
			parent.chatBox.transformers += transformer
			val element = ElementEntry(transformer, parent)
			// Ensures the new entry button is always at the bottom
			parent.removeEntry(this)
			if (parent.children().isNotEmpty()) parent.addEntry(ArrowEntry)
			parent.addEntry(element)
			parent.addEntry(this)
			element.init()
		}

		var children = listOf(button)
		override fun children(): List<Element> = children
		override fun selectableChildren(): List<Selectable> = children

		override fun render(context: DrawContext, index: Int, y: Int, x: Int, entryWidth: Int, entryHeight: Int, mouseX: Int, mouseY: Int, hovered: Boolean, tickDelta: Float) {
			button.render(context, x, y, entryWidth, entryHeight, mouseX, mouseY, tickDelta)
		}
	}

	class ElementEntry(var transformer: Transformer, val parent: TransformerEntryList) : AbstractEntry(), Initializable {
		val upButton = buttonWidget("↑".literal(ColorPalette.RED.rgb), -10) {
			parent.chatBox.transformers.let { list ->
				val index = list.indexOf(transformer)
				list[index] = list[index - 1].also { list[index - 1] = list[index] }
			}
			parent.children().let { list ->
				val index = list.indexOf(this)
				list[index - 2] = list[index].also { list[index] = list[index - 2] }
			}
			parent.refreshButtonActiveness()
		}
		// +2 and -2 because the arrow entry is also a child
		val downButton = buttonWidget("↓".literal(ColorPalette.RED.rgb), -10) {
			parent.chatBox.transformers.let { list ->
				val index = list.indexOf(transformer)
				list[index] = list[index + 1].also { list[index + 1] = list[index] }
			}
			parent.children().let { list ->
				val index = list.indexOf(this)
				list[index + 2] = list[index].also { list[index] = list[index + 2] }
			}
			parent.refreshButtonActiveness()
		}
		private var children = mutableListOf<ClickablePosRenderedWidget>()

		override fun children() = children
		override fun selectableChildren() = children

		override fun init() {
			children += buttonWidget(
				text {
					+"Type: ".literal.withColor(ColorPalette.SUBTEXT1.rgb)
					+transformer.type.formatted.literal.withColor(ColorPalette.TEXT.rgb)
				}, -5
			) {
				transformer.type = TransformerType.entries[(transformer.type.ordinal + 1) % TransformerType.entries.size]
				children.clear()
				init()
			}
			when (transformer.type) {
				TransformerType.REGEX_CONTAINS, TransformerType.REGEX_MATCHES -> {
					children += createRegexTextField(transformer)
					children += createFilterModeButton(transformer)
				}

				TransformerType.REGEX_REPLACE -> {
					children += createRegexTextField(transformer)
					children += createReplacementTextField(transformer)
					children += createReplacementModeButton(transformer)
				}

				TransformerType.STRING_REPLACE -> {
					children += createStringTextField(transformer)
					children += createReplacementTextField(transformer)
					children += createReplacementModeButton(transformer)
				}

				TransformerType.STRING_EQUALITY, TransformerType.STRING_CONTAINS, TransformerType.STRING_STARTS_WITH, TransformerType.STRING_ENDS_WITH -> {
					children += createStringTextField(transformer)
					children += createFilterModeButton(transformer)
					children += createCaseIgnoreButton(transformer)
				}
			}
			children += upButton
			children += downButton
			parent.refreshButtonActiveness()
			children += buttonWidget("\uD83D\uDDD9".literal(ColorPalette.RED.rgb), -10) {
				parent.chatBox.transformers.remove(transformer)
				val index = parent.children()!!.indexOf(this)
				if (index != 0) {
					val previous = parent.children()!![index - 1]
					if (previous is ArrowEntry) parent.children()!!.removeAt(index - 1)
					// No need to delete the next arrow if there is one since there has to be one between the previous and the next transformer
					// Essentially, every entry is only responsible for the arrow behind them unless it's the first one
				} else {
					val next = parent.children()!!.getOrNull(1)
					if (next is ArrowEntry) parent.children()!!.removeAt(1)
				}
				parent.removeEntry(this)
			}
		}

		override fun render(context: DrawContext, index: Int, y: Int, x: Int, entryWidth: Int, entryHeight: Int, mouseX: Int, mouseY: Int, hovered: Boolean, tickDelta: Float) {
			val totalWidth = entryWidth - ((children.size - 1) * INNER_PADDING) - children.filter { it.columnWidth < 0 }.sumOf { entryWidth / abs(it.columnWidth) }
			// If any widget's size is smaller than 0, it'll have a fixed size according to `available width/abs value of their size` and the remaining space will be distributed among the widgets according to their width
			val totalWidthOfDrawables = children.filter { it.columnWidth > 0 }.sumOf(ClickablePosRenderedWidget::columnWidth) // They are supposed to have arbitrary widths and the available space will be distributed among them according to their width
			val unitWidth = totalWidth / totalWidthOfDrawables
			var excessWidth = totalWidth - (unitWidth * totalWidthOfDrawables) // The remainder of the division will be distributed among the widgets

			var drawableWidth: Int
			var drawableX = x
			for (drawable in children) {
				drawableWidth = if (drawable.columnWidth < 0) entryWidth / (abs(drawable.columnWidth)) else drawable.columnWidth * unitWidth

				if (drawable.columnWidth > 0 && excessWidth > 0) {
					drawableWidth++
					excessWidth--
				}

				drawable.render(context, drawableX, y, drawableWidth, BUTTON_HEIGHT, mouseX, mouseY, tickDelta)
				drawableX += drawableWidth + INNER_PADDING
			}
			return
		}

		fun createRegexTextField(transformer: Transformer) = textFieldWidget(client.textRenderer, 2, "Regex: ${transformer.context.pattern.pattern()}".text, transformer.context.pattern.pattern()).apply {
			maxLength = 512
			changedListener = Consumer {
				try {
					transformer.context.pattern = if (it.isEmpty()) Pattern.compile(".*") else it.toPattern()
					error = ""
				} catch (e: PatternSyntaxException) {
					error = e.description
				}
			}
		}

		fun createStringTextField(transformer: Transformer) = textFieldWidget(client.textRenderer, 2, "String Filter: ${transformer.context.stringFilter}".text, transformer.context.stringFilter).apply {
			maxLength = 256
			changedListener = Consumer {
				transformer.context.stringFilter = it
			}
		}

		fun createReplacementTextField(transformer: Transformer) = textFieldWidget(client.textRenderer, 2, "Replacement: ${transformer.context.replacement}".text, transformer.context.replacement).apply {
			maxLength = 256
			changedListener = Consumer {
				transformer.context.replacement = it
			}
		}

		fun createFilterModeButton(transformer: Transformer) = buttonWidget(
			text {
				+"Filter Mode: ".literal(ColorPalette.SUBTEXT1.rgb)
				+transformer.context.filterMode.formatted.literal(ColorPalette.TEXT.rgb)
			}, 2
		) {
			transformer.context.filterMode = FilterMode.entries[(transformer.context.filterMode.ordinal + 1) % FilterMode.entries.size]
			children.clear()
			init()
		}

		fun createReplacementModeButton(transformer: Transformer) = buttonWidget(
			text {
				+"Replacement Mode: ".literal(ColorPalette.SUBTEXT1.rgb)
				+transformer.context.replacementMode.formatted.literal(ColorPalette.TEXT.rgb)
			}, 2
		) {
			transformer.context.replacementMode = ReplacementMode.entries[(transformer.context.replacementMode.ordinal + 1) % ReplacementMode.entries.size]
			children.clear()
			init()
		}

		fun createCaseIgnoreButton(transformer: Transformer) = buttonWidget(
			text {
				+"Ignore Case: ".literal(ColorPalette.SUBTEXT1.rgb)
				+transformer.context.ignoreCase.toString().replaceFirstChar(Char::uppercaseChar).literal(if (transformer.context.ignoreCase) ColorPalette.GREEN.rgb else ColorPalette.RED.rgb)
			}, 2
		) {
			transformer.context.ignoreCase = !transformer.context.ignoreCase
			children.clear()
			init()
		}
	}

	/*no-op*/
	override fun drawMenuListBackground(context: DrawContext) {}

	/*no-op*/
	override fun drawHeaderAndFooterSeparators(context: DrawContext) {}
}
