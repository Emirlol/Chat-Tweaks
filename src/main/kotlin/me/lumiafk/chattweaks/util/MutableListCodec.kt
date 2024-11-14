package me.lumiafk.chattweaks.util

import com.mojang.datafixers.util.Pair
import com.mojang.datafixers.util.Unit
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.Lifecycle
import java.util.stream.Stream

data class MutableListCodec<E>(val elementCodec: Codec<E>, val minSize: Int, val maxSize: Int) : Codec<MutableList<E>> {
	private fun <R> createTooShortError(size: Int): DataResult<R> {
		return DataResult.error { "List is too short: $size, expected range [$minSize-$maxSize]" }
	}

	private fun <R> createTooLongError(size: Int): DataResult<R> {
		return DataResult.error { "List is too long: $size, expected range [$minSize-$maxSize]" }
	}

	override fun <T> encode(input: MutableList<E>, ops: DynamicOps<T>, prefix: T): DataResult<T> {
		if (input.size < minSize) {
			return createTooShortError(input.size)
		}
		if (input.size > maxSize) {
			return createTooLongError(input.size)
		}
		val builder = ops.listBuilder()
		for (element in input) {
			builder.add(elementCodec.encodeStart(ops, element))
		}
		return builder.build(prefix)
	}

	override fun <T> decode(ops: DynamicOps<T>, input: T): DataResult<Pair<MutableList<E>, T>> {
		return ops.getList(input).setLifecycle(Lifecycle.stable()).flatMap {
			val decoder = DecoderState<T>(ops)
			it.accept { value -> decoder.accept(value) }
			decoder.build()
		}
	}

	override fun toString(): String {
		return "ListCodec[$elementCodec]"
	}

	private inner class DecoderState<T>(private val ops: DynamicOps<T>) {
		private val elements = ArrayList<E>()
		private val failed = Stream.builder<T>()
		private var result = INITIAL_RESULT
		private var totalCount = 0

		fun accept(value: T) {
			totalCount++
			if (elements.size >= maxSize) {
				failed.add(value)
				return
			}
			val elementResult = elementCodec.decode(ops, value)
			elementResult.error().ifPresent { failed.add(value) }
			elementResult.resultOrPartial().ifPresent { elements.add(it.getFirst()) }
			result = result.apply2stable({ result, _ -> result }, elementResult)
		}

		fun build(): DataResult<Pair<MutableList<E>, T>> {
			if (elements.size < minSize) {
				return createTooShortError(elements.size)
			}
			val errors: T = ops.createList(failed.build())
			val pair: Pair<MutableList<E>, T> = Pair.of(ArrayList<E>(elements), errors)
			if (totalCount > maxSize) {
				result = createTooLongError(totalCount)
			}
			return result.map { pair }.setPartial(pair)
		}
	}

	companion object {
		private val INITIAL_RESULT = DataResult.success(Unit.INSTANCE, Lifecycle.stable())
	}
}
