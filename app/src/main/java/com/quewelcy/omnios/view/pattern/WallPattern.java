package com.quewelcy.omnios.view.pattern;

import android.graphics.Bitmap;

import java.util.List;

import static java.util.Arrays.asList;

abstract class WallPattern {

    private static final List<List<Integer>> COLORS = asList(
            asList(0xFF247BA0, 0xFF70C1B3, 0xFFB2DBBF, 0xFFF3FFBD, 0xFFFF1654),
            asList(0xFFFFCDB2, 0xFFFFB4A2, 0xFFE5989B, 0xFFB5838D, 0xFF6D6875),
            asList(0xFF50514F, 0xFFF25F5C, 0xFFFFE066, 0xFF247BA0, 0xFF70C1B3),
            asList(0xFFFE938C, 0xFFE6B89C, 0xFFEAD2AC, 0xFF9CAFB7, 0xFF4281A4),
            asList(0xFFFFFFFF, 0xFF84DCC6, 0xFFA5FFD6, 0xFFFFA69E, 0xFFFF686B),
            asList(0xFFF0B67F, 0xFFFE5F55, 0xFFD6D1B1, 0xFFC7EFCF, 0xFFEEF5DB),
            asList(0xFF3D315B, 0xFF444B6E, 0xFF708B75, 0xFF9AB87A, 0xFFF8F991),
            asList(0xFFBCE784, 0xFF5DD39E, 0xFF348AA7, 0xFF525174, 0xFF513B56),
            asList(0xFFD8E2DC, 0xFFFFE5D9, 0xFFFFCAD4, 0xFFF4ACB7, 0xFF9D8189),
            asList(0xFFD8DBE2, 0xFFA9BCD0, 0xFF58A4B0, 0xFF373F51, 0xFF1B1B1E),
            asList(0xFF264653, 0xFF2A9D8F, 0xFFE9C46A, 0xFFF4A261, 0xFFE76F51),
            asList(0xFF011627, 0xFFFDFFFC, 0xFF2EC4B6, 0xFFE71D36, 0xFFFF9F1C),
            asList(0xFF000000, 0xFF14213D, 0xFFFCA311, 0xFFE5E5E5, 0xFFFFFFFF),
            asList(0xFF9C89B8, 0xFFF0A6CA, 0xFFEFC3E6, 0xFFF0E6EF, 0xFFB8BEDD),
            asList(0xFF13293D, 0xFF006494, 0xFF247BA0, 0xFF1B98E0, 0xFFE8F1F2),
            asList(0xFFFAF3DD, 0xFFC8D5B9, 0xFF8FC0A9, 0xFF68B0AB, 0xFF4A7C59),
            asList(0xFFFFBF00, 0xFFE83F6F, 0xFF2274A5, 0xFF32936F, 0xFFFFFFFF),
            asList(0xFF06AED5, 0xFF086788, 0xFFF0C808, 0xFFFFF1D0, 0xFFDD1C1A),
            asList(0xFF0B132B, 0xFF1C2541, 0xFF3A506B, 0xFF5BC0BE, 0xFF6FFFE9),
            asList(0xFFF6511D, 0xFFFFB400, 0xFF00A6ED, 0xFF7FB800, 0xFF0D2C54),
            asList(0xFF083D77, 0xFFEBEBD3, 0xFFF4D35E, 0xFFEE964B, 0xFFF95738),
            asList(0xFF2B2D42, 0xFF8D99AE, 0xFFEDF2F4, 0xFFEF233C, 0xFFD90429),
            asList(0xFFE63946, 0xFFF1FAEE, 0xFFA8DADC, 0xFF457B9D, 0xFF1D3557),
            asList(0xFF05668D, 0xFF028090, 0xFF00A896, 0xFF02C39A, 0xFFF0F3BD),
            asList(0xFFED6A5A, 0xFFF4F1BB, 0xFF9BC1BC, 0xFF5CA4A9, 0xFFE6EBE0),
            asList(0xFF114B5F, 0xFF028090, 0xFFE4FDE1, 0xFF456990, 0xFFF45B69),
            asList(0xFFFFFFFF, 0xFF00171F, 0xFF003459, 0xFF007EA7, 0xFF00A8E8),
            asList(0xFFED6A5A, 0xFFF4F1BB, 0xFF9BC1BC, 0xFF5CA4A9, 0xFFE6EBE0));
    final int width;
    final int height;
    private int position;
    private int scheme;

    WallPattern(int size) {
        this.width = size;
        this.height = size;
    }

    WallPattern(int width, int height) {
        this.width = width;
        this.height = height;
    }

    int chooseSchemeAndGetColor() {
        do {
            scheme = (int) (Math.random() * COLORS.size());
        } while (scheme < 0 || scheme >= COLORS.size());
        position = 0;
        return getNextColor();
    }

    int getNextColor() {
        if (position >= COLORS.get(scheme).size()) {
            position = 0;
        }
        return COLORS.get(scheme).get(position++);
    }

    public abstract Bitmap getBitmap();
}
