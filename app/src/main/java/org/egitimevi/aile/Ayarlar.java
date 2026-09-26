package org.egitimevi.aile;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Telefonda kalan ayarlar: sunucu adresi, cihaz anahtarı, öğrencinin adı ve
 * velinin seçtiği gönderme aralıkları. Cihaz anahtarı yalnızca konum ve
 * kullanım göndermeye yarar; öğrencinin hesabına giriş vermez.
 */
public final class Ayarlar {
    private static final String DOSYA = "aile";
    public static final String VARSAYILAN_SUNUCU = "https://egitimevi.org";

    private Ayarlar() { }

    private static SharedPreferences sp(Context c) {
        return c.getApplicationContext().getSharedPreferences(DOSYA, Context.MODE_PRIVATE);
    }

    public static String sunucu(Context c) { return sp(c).getString("sunucu", VARSAYILAN_SUNUCU); }
    public static String cihazAnahtari(Context c) { return sp(c).getString("cihaz", ""); }
    public static String ogrenciAdi(Context c) { return sp(c).getString("ogrenci", ""); }
    public static boolean bagli(Context c) { return !cihazAnahtari(c).isEmpty(); }

    /** Velinin seçtiği aralıklar (dakika). Wi-Fi'de sık, mobil veride seyrek. */
    public static int wifiAraligi(Context c) { return sp(c).getInt("wifiDk", 5); }
    public static int mobilAraligi(Context c) { return sp(c).getInt("mobilDk", 15); }
    public static boolean konumAcik(Context c) { return sp(c).getBoolean("konumAcik", true); }
    public static boolean kullanimAcik(Context c) { return sp(c).getBoolean("kullanimAcik", true); }

    public static long sonGonderim(Context c) { return sp(c).getLong("sonGonderim", 0); }
    public static long sonKonum(Context c) { return sp(c).getLong("sonKonum", 0); }
    public static String sonHata(Context c) { return sp(c).getString("sonHata", ""); }

    /** Yalnızca deneme paketinde sorulan sunucu adresi (yayın paketi egitimevi.org). */
    public static void sunucuYaz(Context c, String sunucu) { sp(c).edit().putString("sunucu", sunucu).apply(); }

    public static void baglan(Context c, String sunucu, String anahtar, String ogrenci) {
        sp(c).edit().putString("sunucu", sunucu).putString("cihaz", anahtar).putString("ogrenci", ogrenci).apply();
    }

    public static void ayariYaz(Context c, int wifiDk, int mobilDk, boolean konum, boolean kullanim) {
        sp(c).edit().putInt("wifiDk", Math.max(1, wifiDk)).putInt("mobilDk", Math.max(1, mobilDk))
            .putBoolean("konumAcik", konum).putBoolean("kullanimAcik", kullanim).apply();
    }

    public static void gonderildi(Context c, long zaman) {
        sp(c).edit().putLong("sonGonderim", zaman).putString("sonHata", "").apply();
    }

    public static void konumAlindi(Context c, long zaman) { sp(c).edit().putLong("sonKonum", zaman).apply(); }

    public static void hata(Context c, String mesaj) { sp(c).edit().putString("sonHata", mesaj == null ? "" : mesaj).apply(); }

    public static void cik(Context c) {
        String sunucu = sunucu(c);
        sp(c).edit().clear().putString("sunucu", sunucu).apply();
    }
}
