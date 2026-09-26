package org.egitimevi.aile;

import android.content.Intent;
import android.net.Uri;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONObject;

/**
 * Hesap aç: herkes aynı (yetişkin) hesabı açar; veli, öğretmen ya da müdür olmak
 * girişten sonra "+ Ekle" ile olur. Hesap, e-postaya gelen bağlantıya 24 saat içinde
 * tıklanınca açılır. Öğrenci ve servisçi hesabını okul açar.
 */
public class KayitSayfasi extends Sayfa {
    private Arayuz.Alan ad, kullanici, eposta, sifre, telefon, tc;
    private CheckBox onay;
    private DogrulamaSorusu soru;
    private TextView gonder;
    private LinearLayout govde;

    @Override public String baslik() { return "Hesap aç"; }
    @Override protected boolean cubuksuz() { return true; }

    @Override
    protected View olustur() {
        ScrollView[] s = new ScrollView[1];
        govde = Arayuz.sayfaGovdesi(e, s);
        govde.setPadding(Tema.dp(e, 22), Tema.dp(e, 36), Tema.dp(e, 22), Tema.dp(e, 32));
        TextView geri = Arayuz.dugme(e, "← Girişe dön", Arayuz.Dugme.HAYALET);
        geri.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        geri.setOnClickListener(v -> e.kapat());
        govde.addView(geri, new LinearLayout.LayoutParams(-2, -2));
        Arayuz.ekle(govde, Arayuz.baslik(e, "Hesap aç"), 10);
        Arayuz.ekle(govde, Arayuz.yazi(e, "Herkes aynı hesabı açar: veli, öğretmen ya da müdür. Girişten sonra sağ üstteki "
            + "+ Ekle ile çocuğunu eklersin, okuluna öğretmen olarak katılırsın ya da okulunu açtırırsın.", 15, R.color.soluk), 8);

        LinearLayout kart = Arayuz.kart(e);
        ad = Arayuz.alan(e, "Adın ve soyadın", "Ayşe Yılmaz", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME
            | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        kart.addView(ad.kok);
        kullanici = Arayuz.alan(e, "Kullanıcı adı", "ayse.yilmaz", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        Arayuz.ekle(kart, kullanici.kok, 12);
        eposta = Arayuz.alan(e, "E-posta", "ornek@eposta.com", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        Arayuz.ekle(kart, eposta.kok, 12);
        telefon = Arayuz.alan(e, "Telefon (ülke koduyla)", "+90 532 123 45 67", InputType.TYPE_CLASS_PHONE);
        telefon.kutu.setText("+90 ");
        Arayuz.ekle(kart, telefon.kok, 12);
        sifre = Arayuz.alan(e, "Şifre", "", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        GirisSayfasi.sifreGoster(sifre);
        Arayuz.ekle(kart, sifre.kok, 12);
        Arayuz.ekle(kart, Arayuz.soluk(e, "En az 8 karakter; büyük harf, küçük harf, rakam ve özel karakter (! ? . * gibi)."), 4);
        tc = Arayuz.alan(e, "T.C. kimlik no (isteğe bağlı)", "", InputType.TYPE_CLASS_NUMBER);
        Arayuz.ekle(kart, tc.kok, 12);

        onay = new CheckBox(e);
        onay.setText("Aydınlatma metnini okudum; kişisel verilerimin bu metne göre işlenmesini kabul ediyorum.");
        onay.setTextColor(Tema.renk(e, R.color.yazi));
        onay.setTextSize(14);
        onay.setButtonTintList(android.content.res.ColorStateList.valueOf(Tema.renk(e, R.color.ana)));
        Arayuz.ekle(kart, onay, 14);
        TextView metin = Arayuz.dugme(e, "Aydınlatma metnini oku", Arayuz.Dugme.HAYALET);
        metin.setTextSize(14);
        metin.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        metin.setOnClickListener(v -> KvkkSayfasi.metniAc(e));
        kart.addView(metin);

        soru = new DogrulamaSorusu(e);
        soru.goster(true);
        Arayuz.ekle(kart, soru.kok, 12);

        gonder = Arayuz.dugme(e, "Hesabımı aç", Arayuz.Dugme.BIRINCIL);
        gonder.setOnClickListener(v -> gonder());
        Arayuz.ekle(kart, gonder, 18);
        Arayuz.ekle(govde, kart, 18);
        return s[0];
    }

    private void gonder() {
        for (Arayuz.Alan a : new Arayuz.Alan[] { ad, kullanici, eposta, telefon, sifre, tc }) a.hataGoster(null);
        if (!onay.isChecked()) { e.bildir("Devam etmek için aydınlatma metnini onayla."); return; }
        JSONObject g = new JSONObject();
        try {
            g.put("fullName", ad.deger()).put("username", kullanici.deger()).put("email", eposta.deger())
                .put("phone", telefon.deger()).put("password", sifre.hamDeger()).put("tc", tc.deger()).put("kvkkOnay", true);
            soru.ekle(g);
        } catch (Exception x) { return; }
        Arayuz.mesgul(gonder, true);
        Ag.post(e, "/api/register", g, j -> {
            govde.removeAllViews();
            LinearLayout b = Arayuz.bosDurum(e, R.drawable.ik_posta, "E-postana bak",
                j.optString("message", "E-postana bir bağlantı gönderdik. Hesabını açmak için 24 saat içinde ona tıkla."),
                null);
            TextView don = Arayuz.dugme(e, "Girişe dön", Arayuz.Dugme.BIRINCIL);
            don.setOnClickListener(v -> e.kapat());
            Arayuz.ekle(b, don, 20);
            govde.addView(b);
        }, h -> {
            Arayuz.mesgul(gonder, false);
            String alan = h.govde.optString("alan");
            switch (alan) {
                case "ad": ad.hataGoster(h.getMessage()); break;
                case "kullaniciAdi": kullanici.hataGoster(h.getMessage()); break;
                case "email": eposta.hataGoster(h.getMessage()); break;
                case "telefon": telefon.hataGoster(h.getMessage()); break;
                case "sifre": sifre.hataGoster(h.getMessage()); break;
                case "tc": tc.hataGoster(h.getMessage()); break;
                case "bot": soru.hata(h.getMessage()); soru.getir(); break;
                default: e.bildir(h.getMessage());
            }
        });
    }

    /** Tarayıcıda açılacak site adresi (aydınlatma metni gibi uzun belgeler). */
    static void siteAc(AnaEkran e, String yol) {
        try { e.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Ayarlar.sunucu(e) + yol))); }
        catch (Exception x) { e.bildir("Bağlantıyı açacak tarayıcı yok."); }
    }
}
