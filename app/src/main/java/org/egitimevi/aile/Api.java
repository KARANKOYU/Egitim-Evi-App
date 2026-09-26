package org.egitimevi.aile;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Eğitim Evi sunucusuyla konuşma (JSON). İnternetteki adreslere yalnızca
 * https ile gidilir; http yalnızca evdeki deneme sunucusu (yerel ağ) içindir.
 */
public final class Api {
    /** Çocuğun telefonunun (Aile) anahtarı: yalnız konum ve kullanım gönderir. */
    public static final String CIHAZ_BASLIGI = "X-Aile-Cihaz";
    /** Uygulamanın anahtarı: yalnız bildirim yoklar ve servisçinin sefer konumunu gönderir. */
    public static final String UYGULAMA_BASLIGI = "X-Cihaz";

    private Api() { }

    /** Sunucunun verdiği hata ya da bağlantı sorunu. */
    public static final class Hata extends IOException {
        public final int durum;
        /** Sunucunun hata cevabının tamamı (alan, kvkkGerek, sifreDegismeli, rolsuz...). */
        public final JSONObject govde;
        public Hata(int durum, String mesaj) { this(durum, mesaj, new JSONObject()); }
        public Hata(int durum, String mesaj, JSONObject govde) { super(mesaj); this.durum = durum; this.govde = govde; }
    }

    /** Adres geçerli mi: https, ya da yerel ağdaki bir adreste http. */
    public static String adresSorunu(String adres) {
        try {
            URL u = new URL(adres);
            if ("https".equals(u.getProtocol())) return null;
            if (!"http".equals(u.getProtocol())) return "Adres https:// ile başlamalı.";
            String h = u.getHost();
            if (h.equals("localhost") || h.startsWith("10.") || h.startsWith("192.168.") || h.matches("^172\\.(1[6-9]|2\\d|3[01])\\..*")) {
                return null;
            }
            return "İnternetteki sunucuya yalnızca https:// ile bağlanılır.";
        } catch (IOException e) {
            return "Adres geçersiz.";
        }
    }

    public static JSONObject get(String sunucu, String yol, String cihaz) throws IOException {
        return istek(sunucu, yol, "GET", null, null, CIHAZ_BASLIGI, cihaz);
    }

    public static JSONObject post(String sunucu, String yol, JSONObject govde, String oturum, String cihaz) throws IOException {
        return istek(sunucu, yol, "POST", govde, oturum, CIHAZ_BASLIGI, cihaz);
    }

    /** Oturumla (Bearer) istek: uygulamanın bütün ekranları. govde null ise GET. */
    public static JSONObject oturumla(String sunucu, String yol, String yontem, JSONObject govde, String oturum) throws IOException {
        return istek(sunucu, yol, yontem, govde, oturum, CIHAZ_BASLIGI, null);
    }

    /** Uygulama anahtarıyla (X-Cihaz) istek: bildirim yoklama, sefer konumu. */
    public static JSONObject uygulama(String sunucu, String yol, JSONObject govde, String anahtar) throws IOException {
        return istek(sunucu, yol, govde == null ? "GET" : "POST", govde, null, UYGULAMA_BASLIGI, anahtar);
    }

    private static JSONObject istek(String sunucu, String yol, String yontem, JSONObject govde,
                                    String oturum, String baslik, String cihaz) throws IOException {
        String sorun = adresSorunu(sunucu);
        if (sorun != null) throw new Hata(0, sorun);
        HttpURLConnection b = (HttpURLConnection) new URL(sunucu.replaceAll("/+$", "") + yol).openConnection();
        b.setRequestMethod(yontem);
        b.setConnectTimeout(15000);
        b.setReadTimeout(20000);
        b.setRequestProperty("Accept", "application/json");
        if (oturum != null && !oturum.isEmpty()) b.setRequestProperty("Authorization", "Bearer " + oturum);
        if (cihaz != null && !cihaz.isEmpty()) b.setRequestProperty(baslik, cihaz);
        if (govde != null) {
            byte[] veri = govde.toString().getBytes(StandardCharsets.UTF_8);
            b.setDoOutput(true);
            b.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            b.setFixedLengthStreamingMode(veri.length);
            try (OutputStream o = b.getOutputStream()) { o.write(veri); }
        }
        int durum = b.getResponseCode();
        InputStream g = durum >= 400 ? b.getErrorStream() : b.getInputStream();
        String metin = g == null ? "" : oku(g);
        b.disconnect();
        JSONObject j;
        try { j = metin.isEmpty() ? new JSONObject() : new JSONObject(metin); } catch (JSONException e) { j = new JSONObject(); }
        if (durum >= 400) {
            String m = j.optString("error", "");
            throw new Hata(durum, m.isEmpty() ? "Sunucu hatası (" + durum + ")" : m, j);
        }
        return j;
    }

    private static String oku(InputStream g) throws IOException {
        try (InputStream in = g; ByteArrayOutputStream o = new ByteArrayOutputStream()) {
            byte[] tampon = new byte[8192];
            int n;
            while ((n = in.read(tampon)) > 0) o.write(tampon, 0, n);
            return o.toString("UTF-8");
        }
    }
}
