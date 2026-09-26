package org.egitimevi.aile;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Giriş yapan kişinin oturumu: sunucunun verdiği oturum anahtarı ve son bilinen hesap
 * bilgisi (user, children, portallar, kapalı özellikler). Telefonun uygulamaya özel
 * deposunda durur (yedeklenmez, başka uygulama okuyamaz). Çıkışta silinir.
 * Anahtar sunucuda 7 gün geçerlidir; süresi dolunca uygulama giriş ekranını açar.
 */
public final class Oturum {
    private static final String DOSYA = "oturum";

    private Oturum() { }

    private static SharedPreferences sp(Context c) {
        return c.getApplicationContext().getSharedPreferences(DOSYA, Context.MODE_PRIVATE);
    }

    public static String anahtar(Context c) { return sp(c).getString("anahtar", ""); }
    public static boolean acik(Context c) { return !anahtar(c).isEmpty(); }

    /** Sunucunun oturum cevabı ({token, user, children, portallar, kapaliOzellikler, kvkkGuncel...}). */
    public static void yaz(Context c, JSONObject cevap) {
        SharedPreferences.Editor e = sp(c).edit();
        String anahtar = cevap.optString("token", "");
        if (!anahtar.isEmpty()) e.putString("anahtar", anahtar);
        bilgiYaz(e, cevap);
        e.apply();
    }

    /** /api/me gibi anahtar içermeyen cevaplarla hesap bilgisini tazeler. */
    public static void tazele(Context c, JSONObject cevap) {
        SharedPreferences.Editor e = sp(c).edit();
        bilgiYaz(e, cevap);
        e.apply();
    }

    private static void bilgiYaz(SharedPreferences.Editor e, JSONObject j) {
        if (j.has("user")) e.putString("user", String.valueOf(j.optJSONObject("user")));
        if (j.has("children")) e.putString("children", String.valueOf(j.optJSONArray("children")));
        if (j.has("portallar")) e.putString("portallar", String.valueOf(j.optJSONArray("portallar")));
        if (j.has("kapaliOzellikler")) e.putString("kapali", String.valueOf(j.optJSONArray("kapaliOzellikler")));
        if (j.has("kvkkGuncel")) e.putBoolean("kvkkGuncel", j.optBoolean("kvkkGuncel", true));
        if (j.has("kvkkSurum")) e.putString("kvkkSurum", j.optString("kvkkSurum"));
        e.putBoolean("kisilikSec", j.optBoolean("kisilikSec", false));
        e.putString("cocuk", j.optString("cocuk", ""));
    }

    public static JSONObject kisi(Context c) { return nesne(sp(c).getString("user", "{}")); }
    public static JSONArray cocuklar(Context c) { return dizi(sp(c).getString("children", "[]")); }
    public static JSONArray portallar(Context c) { return dizi(sp(c).getString("portallar", "[]")); }
    public static JSONArray kapaliOzellikler(Context c) { return dizi(sp(c).getString("kapali", "[]")); }
    public static boolean kvkkGuncel(Context c) { return sp(c).getBoolean("kvkkGuncel", true); }
    public static boolean kisilikSec(Context c) { return sp(c).getBoolean("kisilikSec", false); }
    public static String rol(Context c) { return kisi(c).optString("role", ""); }

    /** Velinin bakmakta olduğu çocuk (birden çok çocukta seçilir). */
    public static String seciliCocuk(Context c) {
        String s = sp(c).getString("seciliCocuk", "");
        if (s.isEmpty()) s = sp(c).getString("cocuk", "");
        return s;
    }

    public static void cocukSec(Context c, String id) { sp(c).edit().putString("seciliCocuk", id).apply(); }

    public static boolean ozellikAcik(Context c, String ozellik) {
        JSONArray k = kapaliOzellikler(c);
        for (int i = 0; i < k.length(); i++) if (ozellik.equals(k.optString(i))) return false;
        return true;
    }

    public static void kapat(Context c) { sp(c).edit().clear().apply(); }

    private static JSONObject nesne(String s) {
        try { return new JSONObject(s == null || s.equals("null") ? "{}" : s); } catch (JSONException e) { return new JSONObject(); }
    }

    private static JSONArray dizi(String s) {
        try { return new JSONArray(s == null || s.equals("null") ? "[]" : s); } catch (JSONException e) { return new JSONArray(); }
    }
}
