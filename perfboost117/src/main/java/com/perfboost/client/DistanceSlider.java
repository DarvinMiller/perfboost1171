package com.perfboost.client;

import com.perfboost.config.PerfConfig;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.util.function.IntConsumer;

public class DistanceSlider extends SliderWidget {
    private final String label;
    private final IntConsumer setter;

    public DistanceSlider(int x, int y, int w, int h, String label, int initial, IntConsumer setter) {
        super(x, y, w, h, LiteralText.EMPTY, (double) initial / PerfConfig.UNLIMITED);
        this.label = label;
        this.setter = setter;
        updateMessage();
    }

    private int current() {
        return (int) Math.round(this.value * PerfConfig.UNLIMITED);
    }

    @Override
    protected void updateMessage() {
        int d = current();
        setMessage(new LiteralText(label + ": " + (d >= PerfConfig.UNLIMITED ? "без лимита" : d + " бл.")));
    }

    @Override
    protected void applyValue() {
        setter.accept(current());
    }
}
