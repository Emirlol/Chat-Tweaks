package me.lumiafk.chattweaks.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.lumiafk.chattweaks.OriginedOrderedText;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.util.Language;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ChatMessages.class)
public class ChatMessagesMixin {
	@Unique
	private static final ThreadLocal<StringVisitable> origin = ThreadLocal.withInitial(() -> null);

	@ModifyExpressionValue(method = "breakRenderedChatMessageLines", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/TextCollector;getCombined()Lnet/minecraft/text/StringVisitable;"))
	private static StringVisitable chatTweaks$originedOrderedText(StringVisitable original) {
		origin.set(original);
		return original;
	}

	@WrapOperation(method = "method_30886", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Language;reorder(Lnet/minecraft/text/StringVisitable;)Lnet/minecraft/text/OrderedText;"))
	private static OrderedText chatTweaks$originedOrderedText(Language instance, StringVisitable visitable, Operation<OrderedText> original) {
		if (origin.get() == null) return original.call(instance, visitable);
		return new OriginedOrderedText(original.call(instance, visitable), origin.get().hashCode());
	}

	@Inject(method = "breakRenderedChatMessageLines", at = @At("RETURN"))
	private static void chatTweaks$clearOrigin(StringVisitable stringVisitable, int i, TextRenderer textRenderer, CallbackInfoReturnable<List<OrderedText>> cir) {
		origin.remove();
	}

	@WrapOperation(method = "method_30886", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/OrderedText;concat(Lnet/minecraft/text/OrderedText;Lnet/minecraft/text/OrderedText;)Lnet/minecraft/text/OrderedText;"))
	private static OrderedText chatTweaks$originedOrderedText(OrderedText orderedText, OrderedText orderedText2, Operation<OrderedText> original) {
		OrderedText result = original.call(orderedText, orderedText2);
		if (orderedText2 instanceof OriginedOrderedText originedOrderedText && originedOrderedText.getOriginHashCode() != 0)
			return new OriginedOrderedText(result, originedOrderedText.getOriginHashCode()); //Propagate the origin hash code
		return result;
	}
}
