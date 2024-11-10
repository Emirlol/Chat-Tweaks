package me.lumiafk.chattweaks.mixin;

import net.minecraft.util.StringHelper;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(StringHelper.class)
public class StringHelperMixin {

	//While this does work, the server only accepts up to 256 characters so this isn't very useful.
	//It's left here since it's an actual solution to the problem, it's just that it's limited by the server - which includes singleplayer since it's runs a server too.
//	@ModifyArg(method = "truncateChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/StringHelper;truncate(Ljava/lang/String;IZ)Ljava/lang/String;"), index = 1)
//	private static int truncateChat(int i) {
//		return ConfigHandler.INSTANCE.getConfig().otherConfig.chatWidth;
//	}
}
