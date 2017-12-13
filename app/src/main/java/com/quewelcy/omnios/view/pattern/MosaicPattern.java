package com.quewelcy.omnios.view.pattern;

import java.util.List;

import static java.util.Arrays.asList;

abstract class MosaicPattern extends WallPattern {

    private static final List<List<Integer>> MOSAIC_COLORS = asList(
            asList(0xFF6F256F, 0xFF8A418A),
            asList(0xFF6F256F, 0xFF531053),
            asList(0xFF482E74, 0xFF654C91),
            asList(0xFF482E74, 0xFF2F1757),
            asList(0xFF2F4172, 0xFF4C5E8F),
            asList(0xFF2F4172, 0xFF182956),
            asList(0xFF226666, 0xFF3C7F7F),
            asList(0xFF226666, 0xFF0F4D4D),
            asList(0xFF582A72, 0xFF3F0F5A),
            asList(0xFF582A72, 0xFF744E89),
            asList(0xFF983351, 0xFF780F2F),
            asList(0xFF983351, 0xFFB8647D),
            asList(0xFFAA5639, 0xFF862F11),
            asList(0xFFAA5639, 0xFFCE8870),
            asList(0xFF236A62, 0xFF0A534C),
            asList(0xFF236A62, 0xFF45807A));

    MosaicPattern(int width, int height) {
        super(width, height);
    }

    @Override
    protected List<List<Integer>> getColorSchemes() {
        return MOSAIC_COLORS;
    }
}
