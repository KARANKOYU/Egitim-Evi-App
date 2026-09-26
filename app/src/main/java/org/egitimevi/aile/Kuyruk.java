package org.egitimevi.aile;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Gönderilmeyi bekleyen konumlar. İnternet yokken alınan konumlar burada
 * birikir, bağlanılan ilk anda sırayla gönderilir. En fazla 5000 konum
 * tutulur (en eskiler atılır); 7 günden eski konum zaten gönderilmez.
 */
public final class Kuyruk {
    private static final String DOSYA = "kuyruk.json";
    private static final int EN_FAZLA = 5000;
    private static final long YEDI_GUN = 7L * 24 * 60 * 60 * 1000;

    private Kuyruk() { }

    private static File dosya(Context c) { return new File(c.getFilesDir(), DOSYA); }

    public static synchronized JSONArray oku(Context c) {
        File f = dosya(c);
        if (!f.exists()) return new JSONArray();
        try (FileInputStream in = new FileInputStream(f)) {
            byte[] b = new byte[(int) f.length()];
            int okunan = 0;
            while (okunan < b.length) {
                int n = in.read(b, okunan, b.length - okunan);
                if (n < 0) break;
                okunan += n;
            }
            return new JSONArray(new String(b, 0, okunan, StandardCharsets.UTF_8));
        } catch (IOException | JSONException e) {
            return new JSONArray();
        }
    }

    private static void yaz(Context c, JSONArray a) {
        File gecici = new File(c.getFilesDir(), DOSYA + ".yeni");
        try (FileOutputStream o = new FileOutputStream(gecici)) {
            o.write(a.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            return;
        }
        //noinspection ResultOfMethodCallIgnored
        gecici.renameTo(dosya(c));
    }

    public static synchronized void ekle(Context c, JSONObject konum) {
        JSONArray a = oku(c);
        a.put(konum);
        long sinir = System.currentTimeMillis() - YEDI_GUN;
        JSONArray kalan = new JSONArray();
        int bas = Math.max(0, a.length() - EN_FAZLA);
        for (int i = bas; i < a.length(); i++) {
            JSONObject k = a.optJSONObject(i);
            if (k != null && k.optLong("zaman", 0) >= sinir) kalan.put(k);
        }
        yaz(c, kalan);
    }

    public static synchronized int boyut(Context c) { return oku(c).length(); }

    /** İlk n konumu verir (silmeden). */
    public static synchronized JSONArray bastan(Context c, int n) {
        JSONArray a = oku(c), s = new JSONArray();
        for (int i = 0; i < Math.min(n, a.length()); i++) s.put(a.opt(i));
        return s;
    }

    /** Gönderilen ilk n konumu siler. */
    public static synchronized void sil(Context c, int n) {
        JSONArray a = oku(c), kalan = new JSONArray();
        for (int i = n; i < a.length(); i++) kalan.put(a.opt(i));
        yaz(c, kalan);
    }

    public static synchronized void temizle(Context c) {
        //noinspection ResultOfMethodCallIgnored
        dosya(c).delete();
    }
}
