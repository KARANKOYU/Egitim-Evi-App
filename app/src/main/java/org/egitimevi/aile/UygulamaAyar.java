package org.egitimevi.aile;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Uygulamanın (sitenin açıldığı ana ekranın) telefonda kalan bilgileri: bildirim
 * yoklamak ve servisçinin sefer konumunu göndermek için kullanılan uygulama anahtarı,
 * son görülen bildirimin imleci, okulun servis saatleri ve açık sefer.
 *
 * Uygulama anahtarı hesaba giriş vermez: yalnız bu iki işe yarar. Oturum (giriş)
 * sitenin kendi deposunda (WebView) kalır. Çocuğun telefonunun (Aile) anahtarı ayrıdır
 * (Ayarlar).
 */
public final class UygulamaAyar {
    private static final String DOSYA = "uygulama";

    private UygulamaAyar() { }

    private static SharedPreferences sp(Context c) {
        return c.getApplicationContext().getSharedPreferences(DOSYA, Context.MODE_PRIVATE);
    }

    public static String anahtar(Context c) { return sp(c).getString("anahtar", ""); }
    public static boolean anahtarVar(Context c) { return !anahtar(c).isEmpty(); }
    public static String imlec(Context c) { return sp(c).getString("imlec", ""); }
    public static String servisSaatleri(Context c) { return sp(c).getString("servisSaatleri", ""); }
    public static String sefer(Context c) { return sp(c).getString("sefer", ""); }
    public static boolean sunucuSecildi(Context c) { return sp(c).getBoolean("sunucuSecildi", false); }

    public static void anahtarYaz(Context c, String anahtar) {
        sp(c).edit().putString("anahtar", anahtar).remove("imlec").apply();
    }

    public static void imlecYaz(Context c, String imlec, String saatler) {
        sp(c).edit().putString("imlec", imlec == null ? "" : imlec)
            .putString("servisSaatleri", saatler == null ? "" : saatler).apply();
    }

    public static void seferYaz(Context c, String seferId) { sp(c).edit().putString("sefer", seferId == null ? "" : seferId).apply(); }

    public static void sunucuSecildi(Context c, boolean secildi) { sp(c).edit().putBoolean("sunucuSecildi", secildi).apply(); }

    /** Çıkış: anahtar, imleç ve sefer unutulur (sunucu adresi kalır). */
    public static void cik(Context c) {
        boolean secildi = sunucuSecildi(c);
        sp(c).edit().clear().putBoolean("sunucuSecildi", secildi).apply();
    }
}
