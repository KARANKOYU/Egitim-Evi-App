package org.egitimevi.aile;

import android.text.InputType;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONObject;

/**
 * Şifre değiştirme. zorunlu: okulun ya da yöneticinin verdiği şifreyle girilmiş;
 * kişi kendi şifresini koymadan devam edemez (çubuksuz, çıkış dışında yol yok).
 * Şifre değişince bu telefon dışındaki bütün oturumlar kapanır.
 */
public class SifreSayfasi extends Sayfa {
    private final boolean zorunlu;
    private Arayuz.Alan eski, yeni, tekrar;
    private TextView kaydet;

    public SifreSayfasi(boolean zorunlu) { this.zorunlu = zorunlu; }

    @Override public String baslik() { return "Şifre değiştir"; }
    @Override protected boolean cubuksuz() { return zorunlu; }

    @Override
    protected View olustur() {
        ScrollView[] s = new ScrollView[1];
        LinearLayout g = Arayuz.sayfaGovdesi(e, s);
        if (zorunlu) {
            g.setPadding(Tema.dp(e, 22), Tema.dp(e, 48), Tema.dp(e, 22), Tema.dp(e, 32));
            Arayuz.ekle(g, Arayuz.baslik(e, "Kendi şifreni belirle"), 0);
            Arayuz.ekle(g, Arayuz.yazi(e, "Sana verilen şifreyle girdin. Bu şifreyi başkaları da bilebilir; devam etmeden önce "
                + "yalnızca senin bildiğin bir şifre belirle.", 15, R.color.soluk), 8);
        }
        boolean guclu = Oturum.kisi(e).optBoolean("yetiskin") || Oturum.kisi(e).optBoolean("rolSatiri");
        LinearLayout kart = Arayuz.kart(e);
        eski = Arayuz.alan(e, zorunlu ? "Sana verilen şifre" : "Şu anki şifren", "", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        kart.addView(eski.kok);
        yeni = Arayuz.alan(e, "Yeni şifre", "", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        GirisSayfasi.sifreGoster(yeni);
        Arayuz.ekle(kart, yeni.kok, 12);
        Arayuz.ekle(kart, Arayuz.soluk(e, guclu
            ? "En az 8 karakter; büyük harf, küçük harf, rakam ve özel karakter (! ? . * gibi)."
            : "En az 8 karakter; en az bir harf ve bir rakam."), 4);
        tekrar = Arayuz.alan(e, "Yeni şifre (tekrar)", "", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        Arayuz.ekle(kart, tekrar.kok, 12);
        kaydet = Arayuz.dugme(e, "Şifreyi kaydet", Arayuz.Dugme.BIRINCIL);
        kaydet.setOnClickListener(v -> kaydet());
        Arayuz.ekle(kart, kaydet, 18);
        Arayuz.ekle(g, kart, zorunlu ? 18 : 4);
        if (zorunlu) {
            TextView cik = Arayuz.dugme(e, "Çıkış yap", Arayuz.Dugme.HAYALET);
            cik.setOnClickListener(v -> e.cikis());
            Arayuz.ekle(g, cik, 10);
        }
        return s[0];
    }

    private void kaydet() {
        eski.hataGoster(null);
        yeni.hataGoster(null);
        tekrar.hataGoster(null);
        if (eski.hamDeger().isEmpty()) { eski.hataGoster("Şifreni yaz."); return; }
        if (!yeni.hamDeger().equals(tekrar.hamDeger())) { tekrar.hataGoster("İki yeni şifre aynı değil."); return; }
        Arayuz.mesgul(kaydet, true);
        JSONObject g = new JSONObject();
        try { g.put("old", eski.hamDeger()).put("new", yeni.hamDeger()); } catch (Exception x) { return; }
        Ag.post(e, "/api/password", g, j -> {
            Oturum.tazele(e, j);
            e.bildir("Şifren değişti. Öbür cihazlardaki oturumlar kapandı.");
            if (zorunlu) e.akisiBaslat(); else e.kapat();
        }, h -> {
            Arayuz.mesgul(kaydet, false);
            if ("eski".equals(h.govde.optString("alan"))) eski.hataGoster(h.getMessage());
            else yeni.hataGoster(h.getMessage());
        });
    }
}
