package me.lumiafk.chattweaks.mixin.accessors;

import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(YACLScreen.class)
public interface YACLScreenAccessor {
	@Accessor
	Screen getParent();
}
