package org.egitimevi.aile;

import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONObject;

/**
 * Şifremi unuttum: yetişkin hesabının e-postasına yenileme bağlantısı gider (bağlantı
 * sitede açılır). Öğrenci ve servisçi hesabının şifresini okul yeniler.
 */
public class SifremiUnuttumSayfasi extends Sayfa {
    private Arayuz.Alan eposta;
    private DogrulamaSorusu soru;
    private TextView gonder;
    private LinearLayout govde;

    @Override public String baslik() { return "Şifremi unuttum"; }
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
        Arayuz.ekle(govde, Arayuz.baslik(e, "Şifreni yenile"), 10);
        Arayuz.ekle(govde, Arayuz.yazi(e, "Hesabının e-posta adresini yaz; yeni şifre belirlemen için bir bağlantı gönderelim. "
            + "Öğrenci ve servisçi hesabının şifresini okul yönetimi yeniler.", 15, R.color.soluk), 8);
        LinearLayout kart = Arayuz.kart(e);
        eposta = Arayuz.alan(e, "E-posta", "ornek@eposta.com", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        kart.addView(eposta.kok);
        soru = new DogrulamaSorusu(e);
        soru.goster(true);
        Arayuz.ekle(kart, soru.kok, 12);
        gonder = Arayuz.dugme(e, "Bağlantı gönder", Arayuz.Dugme.BIRINCIL);
        gonder.setOnClickListener(v -> gonder());
        Arayuz.ekle(kart, gonder, 16);
        Arayuz.ekle(govde, kart, 18);
        return s[0];
    }

    private void gonder() {
        eposta.hataGoster(null);
        if (eposta.deger().indexOf('@') < 1) { eposta.hataGoster("Hesabının e-posta adresini yaz."); return; }
        JSONObject g = new JSONObject();
        try { g.put("email", eposta.deger()); soru.ekle(g); } catch (Exception x) { return; }
        Arayuz.mesgul(gonder, true);
        Ag.post(e, "/api/sifre-unuttum", g, j -> {
            govde.removeAllViews();
            LinearLayout b = Arayuz.bosDurum(e, R.drawable.ik_posta, "E-postana bak",
                j.optString("message", "Bu adresle bir hesap varsa yenileme bağlantısı gönderildi. Bağlantı sitede açılır."), null);
            TextView don = Arayuz.dugme(e, "Girişe dön", Arayuz.Dugme.BIRINCIL);
            don.setOnClickListener(v -> e.kapat());
            Arayuz.ekle(b, don, 20);
            govde.addView(b);
        }, h -> {
            Arayuz.mesgul(gonder, false);
            if (h.getMessage() != null && h.getMessage().contains("Doğrulama")) { soru.hata(h.getMessage()); soru.getir(); }
            else eposta.hataGoster(h.getMessage());
        });
    }
}
