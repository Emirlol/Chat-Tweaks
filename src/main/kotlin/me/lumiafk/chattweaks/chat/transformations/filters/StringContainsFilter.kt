package me.lumiafk.chattweaks.chat.transformations.filters

import me.lumiafk.chattweaks.chat.transformations.ChatTransformer
import me.lumiafk.chattweaks.chat.transformations.TransformerContext
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object StringContainsFilter: ChatTransformer {
	context(TransformerContext)
	override fun transform(input: Text): Text? = if (input.string.let { if (stripFormatting) Formatting.strip(it)!! else it }.contains(stringFilter, ignoreCase)) {
		if (filterMode == FilterMode.WHITELIST) input else null
	} else {
		if (filterMode == FilterMode.BLACKLIST) input else null
	}
}