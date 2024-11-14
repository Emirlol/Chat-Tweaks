package me.lumiafk.chattweaks.chat.transformations

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.lumiafk.chattweaks.chat.transformations.filters.FilterMode
import me.lumiafk.chattweaks.chat.transformations.transformers.ReplacementMode
import me.lumiafk.chattweaks.util.Codecs
import java.util.regex.Pattern

data class TransformerContext(
	var pattern: Pattern = Pattern.compile(".*"),
	var stringFilter : String = "",
	var ignoreCase: Boolean = false,
	var filterMode: FilterMode = FilterMode.WHITELIST,
	var stripFormatting: Boolean = true,
	var replacement: String = "",
	var replacementMode: ReplacementMode = ReplacementMode.PLAINTEXT_REPLACEMENT
) {
	companion object {
		val CODEC: Codec<TransformerContext> = RecordCodecBuilder.create { instance ->
			instance.group(
				Codecs.PATTERN.optionalFieldOf("pattern", Pattern.compile(".*")).forGetter { it.pattern },
				Codec.STRING.optionalFieldOf("stringFilter", "").forGetter { it.stringFilter },
				Codec.BOOL.optionalFieldOf("ignoreCase", false).forGetter { it.ignoreCase },
				FilterMode.CODEC.fieldOf("filterMode").forGetter { it.filterMode },
				Codec.BOOL.optionalFieldOf("stripFormatting", true).forGetter { it.stripFormatting },
				Codec.STRING.optionalFieldOf("replacement", "").forGetter { it.replacement },
				ReplacementMode.CODEC.optionalFieldOf("replacementMode", ReplacementMode.PLAINTEXT_REPLACEMENT).forGetter { it.replacementMode }
			).apply(instance, ::TransformerContext)
		}
	}
}