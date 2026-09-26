package org.egitimevi.aile;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Sitenin uygulamayla konuştuğu köprü: sayfada window.EgitimEviUygulama olarak görünür.
 * Yalnızca uygulamanın kendi sunucusundan açılmış sayfa kullanabilir (AnaEkran başka
 * adresleri hiç açmaz; yine de her çağrıda bakılır). İşlevler bilerek az: uygulama
 * anahtarını saklamak, sefer konumunu başlatıp durdurmak, çocuğun telefonu ekranını
 * açmak ve dosya kaydetmek. Oturum (giriş) hiçbir zaman buraya gelmez.
 */
final class Kopru {
    private static final int EN_BUYUK_DOSYA = 25 * 1024 * 1024;
    private final AnaEkran ekran;

    Kopru(AnaEkran ekran) { this.ekran = ekran; }

    private boolean guvenli() { return ekran.sayfaGuvenli; }

    @JavascriptInterface
    public String surum() {
        try { return ekran.getPackageManager().getPackageInfo(ekran.getPackageName(), 0).versionName; }
        catch (Exception e) { return ""; }
    }

    @JavascriptInterface
    public boolean anahtarVar() { return guvenli() && UygulamaAyar.anahtarVar(ekran); }

    /** Site girişten sonra POST /api/cihaz ile aldığı anahtarı verir (64 onaltılık hane). */
    @JavascriptInterface
    public void anahtarKaydet(String anahtar) {
        if (!guvenli() || anahtar == null || !anahtar.matches("^[0-9a-f]{64}$")) return;
        UygulamaAyar.anahtarYaz(ekran, anahtar);
        Bildirimler.zamanla(ekran);
        ekran.runOnUiThread(ekran::bildirimIzniIste);
    }

    /** Çıkış: anahtar unutulur, sefer ve bildirim yoklaması durur. */
    @JavascriptInterface
    public void cikis() {
        if (!guvenli()) return;
        SeferServisi.durdur(ekran);
        UygulamaAyar.cik(ekran);
        Bildirimler.iptal(ekran);
    }

    @JavascriptInterface
    public void seferBasladi(String seferId) {
        if (!guvenli() || seferId == null || !seferId.matches("^[A-Za-z0-9_-]{1,60}$")) return;
        ekran.runOnUiThread(() -> ekran.seferBaslat(seferId));
    }

    @JavascriptInterface
    public void seferBitti() {
        if (!guvenli()) return;
        SeferServisi.durdur(ekran);
    }

    /** Öğrencinin "Bu telefonu velimle paylaş" düğmesi: çocuğun telefonu (Aile) ekranı. */
    @JavascriptInterface
    public void aileAc() {
        if (!guvenli()) return;
        ekran.runOnUiThread(() -> ekran.startActivity(new Intent(ekran, AileEkrani.class)));
    }

    /**
     * Sitenin tarayıcıda "indir" dediği dosya (blob) uygulamada buradan kaydedilir:
     * İndirilenler/Eğitim Evi klasörüne. Dönüş: "ok", "buyuk" ya da "hata".
     */
    @JavascriptInterface
    public String dosyaKaydet(String ad, String tur, String base64) {
        if (!guvenli() || base64 == null) return "hata";
        if (base64.length() > EN_BUYUK_DOSYA / 3 * 4 + 8) return "buyuk";
        byte[] veri;
        try { veri = Base64.decode(base64, Base64.DEFAULT); } catch (IllegalArgumentException e) { return "hata"; }
        String dosya = temizAd(ad);
        String mime = tur != null && tur.matches("^[a-z]+/[A-Za-z0-9.+-]{1,80}$") ? tur : "application/octet-stream";
        try {
            if (Build.VERSION.SDK_INT >= 29) {
                ContentValues d = new ContentValues();
                d.put(MediaStore.Downloads.DISPLAY_NAME, dosya);
                d.put(MediaStore.Downloads.MIME_TYPE, mime);
                d.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Eğitim Evi");
                Uri u = ekran.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, d);
                if (u == null) return "hata";
                try (OutputStream o = ekran.getContentResolver().openOutputStream(u)) {
                    if (o == null) return "hata";
                    o.write(veri);
                }
            } else {
                File k = ekran.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                if (k == null) return "hata";
                try (FileOutputStream o = new FileOutputStream(new File(k, dosya))) { o.write(veri); }
            }
        } catch (Exception e) {
            return "hata";
        }
        ekran.runOnUiThread(() -> Toast.makeText(ekran, "Kaydedildi: İndirilenler/Eğitim Evi/" + dosya, Toast.LENGTH_LONG).show());
        return "ok";
    }

    /** Dosya adında klasör ayıracı, denetim ve yön karakterleri olmasın; en çok 120 karakter. */
    static String temizAd(String ad) {
        String s = ad == null ? "" : ad.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}\\u202A-\\u202E\\u2066-\\u2069]", "_").trim();
        s = s.replaceAll("^\\.+", "");
        if (s.isEmpty()) s = "dosya";
        return s.length() > 120 ? s.substring(s.length() - 120) : s;
    }
}
