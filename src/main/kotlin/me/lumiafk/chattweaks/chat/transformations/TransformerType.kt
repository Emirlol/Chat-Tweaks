package me.lumiafk.chattweaks.chat.transformations

import com.mojang.serialization.Codec
import me.lumiafk.chattweaks.chat.transformations.filters.*
import me.lumiafk.chattweaks.chat.transformations.transformers.RegexReplacer
import me.lumiafk.chattweaks.chat.transformations.transformers.StringReplacer
import net.minecraft.util.StringIdentifiable

enum class TransformerType(val chatTransformer: ChatTransformer, val formatted: String, val description: String) : StringIdentifiable {
	REGEX_CONTAINS(RegexContainsFilter, "Regex Contains Filter", """
		Filters out any messages that contain a match for the provided regex pattern.
		For example, "\\d+" will filter out any messages that contain a number.
	""".trimIndent()),
	REGEX_MATCHES(RegexMatchFilter, "Regex Matches Filter", """
		Filters out any messages that fully match the provided regex pattern.
		For example, ".*\\d+.*" will filter out any messages that contain a number.
	""".trimIndent()),
	REGEX_REPLACE(RegexReplacer, "Regex Replace All Matches", """
		Replaces all matches of the provided regex pattern with the provided replacement.
		For example, "\\d+" with "#" will replace all numbers with "#".
	""".trimIndent()),
	STRING_REPLACE(StringReplacer, "String Replace All Matches", """
		Replaces all occurrences of the provided string with the provided replacement.
		For example, "apple" with "orange" will replace all occurrences of "apple" with "orange".
	""".trimIndent()),
	STRING_EQUALITY(StringEqualityFilter, "String Equality Filter", """
		Filters out any messages that fully match the provided string.
	""".trimIndent()),
	STRING_CONTAINS(StringContainsFilter, "String Contains Filter", """
		Filters out any messages that contain the provided string.
	""".trimIndent()),
	STRING_STARTS_WITH(StringStartsWithFilter, "String Starts With Filter", """
		Filters out any messages that start with the provided string.
	""".trimIndent()),
	STRING_ENDS_WITH(StringEndsWithFilter, "String Ends With Filter", """
		Filters out any messages that end with the provided string.
	""".trimIndent());

	override fun asString(): String = name.lowercase()

	companion object {
		val CODEC: Codec<TransformerType> = StringIdentifiable.createBasicCodec(TransformerType::values)
	}
}