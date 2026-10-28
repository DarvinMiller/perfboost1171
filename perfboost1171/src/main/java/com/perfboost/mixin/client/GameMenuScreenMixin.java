package com.perfboost.mixin.client;

import com.perfboost.client.ConfigScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Кнопка «PerfBoost» в меню паузы. */
@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {
    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void perfboost$button(CallbackInfo ci) {
        addDrawableChild(new ButtonWidget(8, 8, 90, 20, new LiteralText("PerfBoost"),
                b -> this.client.setScreen(new ConfigScreen(this))));
    }
}
