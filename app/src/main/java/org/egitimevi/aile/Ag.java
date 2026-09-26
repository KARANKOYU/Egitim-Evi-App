package org.egitimevi.aile;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Ekranların sunucuya istekleri: arka planda gider, cevap ana iş parçacığında döner.
 * Oturumla ilgili genel durumları ekran tek tek düşünmez, AnaEkran karşılar:
 * oturum düştü (401) → giriş; aydınlatma metni güncellendi → onay; şifre değişmeli →
 * şifre ekranı; okula bağlı değil (rolsüz) → portal ekle. Öbür hataları ekran ister
 * kendisi gösterir (olmadi), vermezse alt tarafta kısa mesaj çıkar.
 */
public final class Ag {
    private static final ExecutorService ARKA = Executors.newFixedThreadPool(3);

    public interface Tamam { void ok(JSONObject j); }
    public interface Olmadi { void olmadi(Api.Hata h); }

    private Ag() { }

    public static void get(AnaEkran e, String yol, Tamam t, Olmadi o) { istek(e, yol, "GET", null, t, o); }

    public static void post(AnaEkran e, String yol, JSONObject govde, Tamam t, Olmadi o) {
        istek(e, yol, "POST", govde == null ? new JSONObject() : govde, t, o);
    }

    private static void istek(AnaEkran e, String yol, String yontem, JSONObject govde, Tamam t, Olmadi o) {
        final String sunucu = Ayarlar.sunucu(e), anahtar = Oturum.anahtar(e);
        ARKA.execute(() -> {
            JSONObject cevap = null;
            Api.Hata hata = null;
            try {
                cevap = Api.oturumla(sunucu, yol, yontem, govde, anahtar);
            } catch (Api.Hata h) {
                hata = h;
            } catch (IOException x) {
                hata = new Api.Hata(0, "İnternet bağlantısı yok ya da Eğitim Evi'ne ulaşılamadı.");
            }
            final JSONObject c = cevap;
            final Api.Hata h = hata;
            e.runOnUiThread(() -> {
                if (e.isFinishing() || e.isDestroyed()) return;
                if (h == null) { if (t != null) t.ok(c); return; }
                if (e.genelHata(h)) return;
                if (o != null) o.olmadi(h); else e.bildir(h.getMessage());
            });
        });
    }

    /** Arka planda çalışan kısa iş (ör. oturumsuz istek); sonuç ana iş parçacığında. */
    public static void arkada(Runnable is) { ARKA.execute(is); }
}
