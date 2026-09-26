package org.egitimevi.aile;

import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONObject;

/**
 * Aydınlatma metni onayı: metin güncellenince herkesten yeniden istenir (sunucu
 * kvkkGerek der). Özet burada, metnin tamamı sitede açılır. Onay verilmeden
 * uygulama kullanılamaz; kişi çıkış yapabilir.
 */
public class KvkkSayfasi extends Sayfa {
    private CheckBox onay;
    private TextView kabul;

    @Override public String baslik() { return "Aydınlatma metni"; }
    @Override protected boolean cubuksuz() { return true; }

    static void metniAc(AnaEkran e) { KayitSayfasi.siteAc(e, "/kvkk.html"); }

    @Override
    protected View olustur() {
        ScrollView[] s = new ScrollView[1];
        LinearLayout g = Arayuz.sayfaGovdesi(e, s);
        g.setPadding(Tema.dp(e, 22), Tema.dp(e, 48), Tema.dp(e, 22), Tema.dp(e, 32));
        g.addView(Arayuz.bolumEtiketi(e, "Kişisel verilerin korunması"));
        Arayuz.ekle(g, Arayuz.baslik(e, "Aydınlatma metni güncellendi"), 8);
        Arayuz.ekle(g, Arayuz.yazi(e, "Devam etmeden önce yeni metni okuyup onaylaman gerekiyor.", 15, R.color.soluk), 8);

        LinearLayout kart = Arayuz.kart(e);
        String[][] maddeler = {
            { "Veri sorumlusu", "Bu sistemi kullanan okuldur. Eğitim Evi bir yazılımdır; veriler okulun sunucusunda durur." },
            { "Ne işlenir", "Ad, kullanıcı adı, e-posta ve telefon; öğrencide okul kayıtları (ödev, not, devamsızlık, servis)." },
            { "Kim görür", "Herkes yetkisi kadar: öğrenci kendini, veli kendi çocuğunu, öğretmen ders verdiği sınıfları." },
            { "Paylaşım", "Veriler reklam, analiz ya da satış için hiçbir üçüncü tarafa aktarılmaz." },
            { "Haklarım", "Bilgi isteme, düzeltme ve silme gibi haklarını okula başvurarak kullanırsın." }
        };
        for (int i = 0; i < maddeler.length; i++) {
            TextView b = Arayuz.yazi(e, maddeler[i][0], 15, R.color.yazi);
            b.setTypeface(Tema.KALIN);
            Arayuz.ekle(kart, b, i == 0 ? 0 : 14);
            Arayuz.ekle(kart, Arayuz.yazi(e, maddeler[i][1], 14, R.color.soluk), 2);
        }
        TextView tam = Arayuz.dugme(e, "Metnin tamamını oku", Arayuz.Dugme.IKINCIL);
        tam.setOnClickListener(v -> metniAc(e));
        Arayuz.ekle(kart, tam, 18);
        Arayuz.ekle(g, kart, 18);

        onay = new CheckBox(e);
        onay.setText("Aydınlatma metnini okudum; kişisel verilerimin bu metne göre işlenmesini kabul ediyorum.");
        onay.setTextColor(Tema.renk(e, R.color.yazi));
        onay.setTextSize(15);
        onay.setButtonTintList(android.content.res.ColorStateList.valueOf(Tema.renk(e, R.color.ana)));
        Arayuz.ekle(g, onay, 16);
        kabul = Arayuz.dugme(e, "Onayla ve devam et", Arayuz.Dugme.BIRINCIL);
        kabul.setOnClickListener(v -> onayla());
        Arayuz.ekle(g, kabul, 14);
        TextView cik = Arayuz.dugme(e, "Çıkış yap", Arayuz.Dugme.HAYALET);
        cik.setOnClickListener(v -> e.cikis());
        Arayuz.ekle(g, cik, 4);
        return s[0];
    }

    private void onayla() {
        if (!onay.isChecked()) { e.bildir("Devam etmek için onay kutusunu işaretle."); return; }
        Arayuz.mesgul(kabul, true);
        JSONObject g = new JSONObject();
        try { g.put("onay", true); } catch (Exception x) { return; }
        Ag.post(e, "/api/kvkk-onay", g, j -> {
            Oturum.tazele(e, j);
            e.akisiBaslat();
        }, h -> { Arayuz.mesgul(kabul, false); e.bildir(h.getMessage()); });
    }
}
