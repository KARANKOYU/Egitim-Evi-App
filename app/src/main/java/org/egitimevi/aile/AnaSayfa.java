package org.egitimevi.aile;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Ana sayfa: selam, tarih ve okul. Portalı olmayan yetişkin için boş ekran ve
 * "+ Ekle"; birden çok portalı olan ama hiçbirine girmemiş yetişkin için portal kartları.
 * Rolün kendi ana sayfası (öğrencinin bugünkü dersleri, velinin çocukları...) rol
 * ekranları eklendikçe Sekmeler'de bu sayfanın yerini alır.
 */
public class AnaSayfa extends Sayfa {
    @Override public String baslik() { return "Eğitim Evi"; }

    @Override
    protected View olustur() {
        ScrollView[] s = new ScrollView[1];
        LinearLayout g = Arayuz.sayfaGovdesi(e, s);
        JSONObject k = Oturum.kisi(e);
        String ad = k.optString("fullName", "").trim();
        String ilk = ad.isEmpty() ? "" : ad.split("\\s+")[0];
        g.addView(Arayuz.bolumEtiketi(e, Zaman.bugun()));
        Arayuz.ekle(g, Arayuz.baslik(e, Zaman.selam() + (ilk.isEmpty() ? "" : ", " + ilk)), 6);
        String okul = k.optString("schoolName");
        String rol = rolAdi(k.optString("role"));
        if (!okul.isEmpty() || !rol.isEmpty()) {
            Arayuz.ekle(g, Arayuz.yazi(e, rol + (okul.isEmpty() ? "" : (rol.isEmpty() ? "" : " · ") + okul), 15, R.color.soluk), 4);
        }

        boolean yetiskinHesabi = k.optBoolean("yetiskin") && !k.optBoolean("rolSatiri");
        JSONArray portallar = Oturum.portallar(e);
        if (yetiskinHesabi && k.optString("role").isEmpty() && portallar.length() == 0 && Oturum.cocuklar(e).length() == 0) {
            TextView ekle = Arayuz.dugme(e, "+ Ekle", Arayuz.Dugme.BIRINCIL);
            ekle.setOnClickListener(v -> e.git(new EkleSayfasi()));
            Arayuz.ekle(g, Arayuz.bosDurum(e, R.drawable.ik_grup, "Henüz bir portalın yok",
                "Başla: çocuğunu ekle, okuluna öğretmen olarak katıl ya da okulunu açtır.", ekle), 20);
            return s[0];
        }
        if (yetiskinHesabi && portallar.length() > 1) {
            Arayuz.ekle(g, Arayuz.bolumEtiketi(e, "Portalların"), 24);
            LinearLayout kart = Arayuz.kart(e);
            kart.setPadding(0, Tema.dp(e, 4), 0, Tema.dp(e, 4));
            PortalSecici.satirlar(e, kart, null);
            Arayuz.ekle(g, kart, 8);
            return s[0];
        }
        Arayuz.ekle(g, Arayuz.serit(e, "Bu sürümde rolüne özel ekranlar hazırlanıyor. Bildirimlerin ve ayarların hazır.",
            Arayuz.Etiket.MAVI), 20);
        return s[0];
    }

    static String rolAdi(String rol) {
        switch (rol) {
            case "student": return "Öğrenci";
            case "parent": return "Veli";
            case "teacher": return "Öğretmen";
            case "principal": return "Müdür";
            case "servisci": return "Servisçi";
            case "admin": return "Yönetici";
            default: return "";
        }
    }
}
