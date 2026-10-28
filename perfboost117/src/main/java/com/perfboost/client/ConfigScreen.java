package com.perfboost.client;

import com.perfboost.config.PerfConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class ConfigScreen extends Screen {
    private static int page;
    private final Screen parent;
    private ButtonWidget recButton;

    public ConfigScreen(Screen parent) {
        super(lit("PerfBoost — настройки"));
        this.parent = parent;
    }

    private static Text recText(PerfConfig c) {
        return lit("Рекомендуемое: " + (c.recommended
                ? "ВКЛ (авто, уровень " + (Math.max(0, c.recommendedLevel) + 1) + "/4)" : "ВЫКЛ"));
    }

    /** Любая ручная правка выключает авто-режим, значения остаются как выставил игрок. */
    private void touch() {
        PerfConfig c = PerfConfig.get();
        c.touchManual();
        recButton.setMessage(recText(c));
    }

    private static Text lit(String s) {
        return new LiteralText(s);
    }

    private ButtonWidget btn(int x, int y, int w, int h, Text t, ButtonWidget.PressAction a) {
        return new ButtonWidget(x, y, w, h, t, a);
    }

    private void slider(int x, int y, int w, String label, int value, IntConsumer set) {
        addDrawableChild(new DistanceSlider(x, y, w, 20, label, value, v -> {
            set.accept(v);
            touch();
        }));
    }

    @Override
    protected void init() {
        PerfConfig c = PerfConfig.get();
        int w = 150, step = 22, lx = this.width / 2 - w - 5, rx = this.width / 2 + 5, top = 28;

        recButton = btn(lx, top, 2 * w + 10, 20, recText(c), b -> {
            PerfConfig cfg = PerfConfig.get();
            if (cfg.recommended) cfg.disableRecommended();
            else cfg.enableRecommended(HardwareProfile.baselineLevel());
            this.client.setScreen(new ConfigScreen(parent));
        });
        addDrawableChild(recButton);

        int y0 = top + step + 6;
        if (page == 0) {
            int y = y0;
            slider(lx, y, w, "Броня", c.armorDistance, v -> c.armorDistance = v); y += step;
            slider(lx, y, w, "Оружие в руках", c.weaponDistance, v -> c.weaponDistance = v); y += step;
            slider(lx, y, w, "Плащи/элитры", c.capeDistance, v -> c.capeDistance = v); y += step;
            slider(lx, y, w, "Слои скина", c.skinLayerDistance, v -> c.skinLayerDistance = v); y += step;
            slider(lx, y, w, "Glint зачарований", c.glintDistance, v -> c.glintDistance = v); y += step;
            slider(lx, y, w, "Тени сущностей", c.shadowDistance, v -> c.shadowDistance = v);
            y = y0;
            slider(rx, y, w, "Дальность частиц", c.particleMaxDistance, v -> c.particleMaxDistance = v); y += step;
            slider(rx, y, w, "Коллизии частиц", c.particleCollisionDistance, v -> c.particleCollisionDistance = v); y += step;
            slider(rx, y, w, "Заморозка сущностей", c.entityFreezeDistance, v -> c.entityFreezeDistance = v); y += step;
            slider(rx, y, w, "Дальность предметов", c.itemRenderDistance, v -> c.itemRenderDistance = v); y += step;
            slider(rx, y, w, "Сундуки, таблички...", c.blockEntityDistance, v -> c.blockEntityDistance = v); y += step;
            slider(rx, y, w, "Рамки, стойки, картины", c.frameDistance, v -> c.frameDistance = v);
        } else {
            int y = y0;
            toggle(lx, y, w, "Скрытие в паузе", () -> c.hideEntitiesOnPause, v -> c.hideEntitiesOnPause = v); y += step;
            toggle(lx, y, w, "Лимит тиков предметов", () -> c.itemTickCap, v -> c.itemTickCap = v); y += step;
            toggle(lx, y, w, "Сон гравитац. блоков", () -> c.gravitySleep, v -> c.gravitySleep = v); y += step;
            addDrawableChild(btn(lx, y, w, 20, text("Авто-настр. игры", c.autoGameSettings), b -> {
                c.autoGameSettings = !c.autoGameSettings;
                b.setMessage(text("Авто-настр. игры", c.autoGameSettings));
                PerfConfig.save();
                GameSettingsTuner.forceNext();
            }));
            y = y0;
            toggle(rx, y, w, "Отсечение вне обзора", () -> c.offscreenCulling, v -> c.offscreenCulling = v); y += step;
            toggle(rx, y, w, "LOD анимации мобов", () -> c.lodAnimation, v -> c.lodAnimation = v); y += step;
            addDrawableChild(btn(rx, y, w, 20, text("Окно статистики", c.showStats), b -> {
                c.showStats = !c.showStats;
                b.setMessage(text("Окно статистики", c.showStats));
                PerfConfig.save();
            }));
        }

        int by = this.height - 26, bw = 100, bx = this.width / 2 - 155;
        addDrawableChild(btn(bx, by, bw, 20, lit("Тест FPS"), b -> {
            if (this.client.world == null) {
                b.setMessage(lit("Зайдите в мир"));
                return;
            }
            Benchmark.start(this.client);
            this.client.setScreen(null);
        }));
        addDrawableChild(btn(bx + bw + 5, by, bw, 20, lit("Страница " + (page + 1) + "/2"), b -> {
            page = 1 - page;
            this.client.setScreen(new ConfigScreen(parent));
        }));
        addDrawableChild(btn(bx + 2 * (bw + 5), by, bw, 20, lit("Готово"), b -> onClose()));
    }

    private void toggle(int x, int y, int w, String label, BooleanSupplier get, Consumer<Boolean> set) {
        addDrawableChild(btn(x, y, w, 20, text(label, get.getAsBoolean()), b -> {
            boolean nv = !get.getAsBoolean();
            set.accept(nv);
            b.setMessage(text(label, nv));
            touch();
        }));
    }

    private static Text text(String label, boolean on) {
        return lit(label + ": " + (on ? "ВКЛ" : "ВЫКЛ"));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 12, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void removed() {
        PerfConfig.save();
    }

    @Override
    public void onClose() {
        this.client.setScreen(parent);
    }
}
