package org.egitimevi.aile;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;

/**
 * Uygulamanın görünüş ölçüleri: sitenin (00-temel.css) renkleri, köşe yarıçapları ve
 * yazı düzeni. Renkler res/values(-night)/renkler.xml'den gelir; koyu tema telefonun
 * ayarını izler.
 */
public final class Tema {
    private Tema() { }

    /* Köşe yarıçapları (dp): sitedeki --r-buyuk, --r, --r-kucuk, --r-mini. */
    public static final int R_BUYUK = 28, R_ORTA = 22, R_KUCUK = 14, R_MINI = 9;

    /* Yazı tipleri: gövde sans, başlık serif (sitede Plex Sans ve Newsreader). */
    public static final Typeface GOVDE = Typeface.create("sans-serif", Typeface.NORMAL);
    public static final Typeface ORTA = Typeface.create("sans-serif-medium", Typeface.NORMAL);
    public static final Typeface KALIN = Typeface.create("sans-serif", Typeface.BOLD);
    public static final Typeface BASLIK = Typeface.create(Typeface.SERIF, Typeface.BOLD);
    public static final Typeface KOD = Typeface.MONOSPACE;

    public static int renk(Context c, int id) { return c.getColor(id); }

    public static int dp(Context c, float d) { return Math.round(d * c.getResources().getDisplayMetrics().density); }

    /** Düz ya da çizgili yuvarlak köşeli zemin. cizgi 0 ise çizgisiz. */
    public static GradientDrawable zemin(Context c, int renk, int yaricapDp, int cizgiRenk, int cizgiDp) {
        GradientDrawable z = new GradientDrawable();
        z.setColor(renk);
        z.setCornerRadius(dp(c, yaricapDp));
        if (cizgiDp > 0) z.setStroke(dp(c, cizgiDp), cizgiRenk);
        return z;
    }

    /** Dokununca dalgalanan zemin (düğme, liste satırı). */
    public static Drawable dalgali(Context c, Drawable icerik, int yaricapDp) {
        GradientDrawable maske = new GradientDrawable();
        maske.setColor(0xFFFFFFFF);
        maske.setCornerRadius(dp(c, yaricapDp));
        return new RippleDrawable(ColorStateList.valueOf(renk(c, R.color.ana_cizgi)), icerik, maske);
    }

    /** Adın baş harflerinden avatar rengi: aynı kişi her yerde aynı renkte (site: avatar()). */
    public static int avatarRengi(String anahtar) {
        int[] renkler = { 0xFFD62839, 0xFF0FA3B1, 0xFF7C3AED, 0xFFC2410C, 0xFF15803D, 0xFF1D4ED8, 0xFFB45309, 0xFF0D9488 };
        int h = 0;
        String s = anahtar == null ? "" : anahtar;
        for (int i = 0; i < s.length(); i++) h = (h * 31 + s.charAt(i)) & 0x7fffffff;
        return renkler[h % renkler.length];
    }

    /** "Zeynep Şahin" -> "ZŞ" (site: avatar()). */
    public static String basHarfler(String ad) {
        if (ad == null) return "?";
        String[] p = ad.trim().split("\\s+");
        if (p.length == 0 || p[0].isEmpty()) return "?";
        String s = p[0].substring(0, 1);
        if (p.length > 1) s += p[p.length - 1].substring(0, 1);
        return s.toUpperCase(java.util.Locale.forLanguageTag("tr-TR"));
    }
}
