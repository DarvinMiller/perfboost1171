package com.perfboost;

import com.perfboost.config.PerfConfig;
import net.fabricmc.api.ModInitializer;

public class PerfBoost implements ModInitializer {
    public static final String MOD_ID = "perfboost";

    @Override
    public void onInitialize() {
        PerfConfig.load();
    }
}
