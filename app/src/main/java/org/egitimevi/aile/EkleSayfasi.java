package org.egitimevi.aile;

import android.content.Intent;
import android.net.Uri;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONObject;

/**
 * "+ Ekle": Veli (çocuğun veli koduyla çocuğunu ekler), Öğretmen (kişi kodunu okulun
 * müdürüne verir), Müdür (yöneticiyle iletişime geçip kodunu verir; okulunu yönetici açar).
 */
public class EkleSayfasi extends Sayfa {
    @Override public String baslik() { return "Ekle"; }

    @Override
    protected View olustur() {
        ScrollView[] s = new ScrollView[1];
        LinearLayout g = Arayuz.sayfaGovdesi(e, s);
        g.addView(Arayuz.baslik(e, "Ne eklemek istiyorsun?"));
        Arayuz.ekle(g, secenek(R.drawable.ik_veli, "Veli", "Çocuğunun veli kodunu gir; ödevini, notunu, servisini gör.",
            v -> e.git(new Veli())), 18);
        Arayuz.ekle(g, secenek(R.drawable.ik_ogretmen, "Öğretmen", "Kişi kodunu okulunun müdürüne ver; seni okula ekler.",
            v -> e.git(new Kod(false))), 12);
        Arayuz.ekle(g, secenek(R.drawable.ik_okul, "Müdür", "Okulunu Eğitim Evi'ne açtır; yönetici okulunu açıp seni müdür yapar.",
            v -> e.git(new Kod(true))), 12);
        return s[0];
    }

    private View secenek(int simge, String ad, String aciklama, View.OnClickListener tik) {
        LinearLayout k = Arayuz.kart(e);
        FrameLayout daire = new FrameLayout(e);
        daire.setBackground(Tema.zemin(e, Tema.renk(e, R.color.ana_acik), 16, 0, 0));
        daire.addView(Arayuz.ikon(e, simge, R.color.ana, 26), new FrameLayout.LayoutParams(Tema.dp(e, 26), Tema.dp(e, 26), Gravity.CENTER));
        daire.setLayoutParams(new LinearLayout.LayoutParams(Tema.dp(e, 52), Tema.dp(e, 52)));
        LinearLayout satir = Arayuz.tiklananSatir(e, daire, ad, aciklama, null, tik);
        satir.setPadding(0, 0, 0, 0);
        k.addView(satir);
        ((TextView) ((LinearLayout) satir.getChildAt(1)).getChildAt(0)).setTextSize(18);
        k.setOnClickListener(tik);
        return k;
    }

    /* ---------------- Veli: çocuğu ekle ---------------- */

    public static class Veli extends Sayfa {
        private Arayuz.Alan kod;
        private TextView ekle;

        @Override public String baslik() { return "Çocuğunu ekle"; }

        @Override
        protected View olustur() {
            ScrollView[] s = new ScrollView[1];
            LinearLayout g = Arayuz.sayfaGovdesi(e, s);
            g.addView(Arayuz.baslik(e, "Veli kodunu gir"));
            Arayuz.ekle(g, Arayuz.yazi(e, "Kodu çocuğunun okulundan alırsın (giriş bilgisi kâğıdında ya da öğrencinin Ayarlar'ında). "
                + "Birden çok çocuğun varsa her birini ayrı ekle.", 15, R.color.soluk), 8);
            LinearLayout kart = Arayuz.kart(e);
            kod = Arayuz.alan(e, "Veli kodu (15 karakter)", "Ab3#k Qx9+m Pt7?z",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            kod.kutu.setTypeface(Tema.KOD);
            kart.addView(kod.kok);
            Arayuz.ekle(kart, Arayuz.soluk(e, "Büyük/küçük harfe dikkat et; boşluklar önemli değil."), 4);
            ekle = Arayuz.dugme(e, "Çocuğumu ekle", Arayuz.Dugme.BIRINCIL);
            ekle.setOnClickListener(v -> ekle());
            Arayuz.ekle(kart, ekle, 16);
            Arayuz.ekle(g, kart, 18);
            return s[0];
        }

        private void ekle() {
            String k = KisiKodu.sade(kod.hamDeger());
            kod.hataGoster(null);
            if (k.length() != 15) { kod.hataGoster("Veli kodu 15 karakterdir."); return; }
            Arayuz.mesgul(ekle, true);
            JSONObject g = new JSONObject();
            try { g.put("code", k); } catch (Exception x) { return; }
            Ag.post(e, "/api/kisilik/cocuk", g, j -> Ag.get(e, "/api/me", me -> {
                Oturum.tazele(e, me);
                e.bildir(j.optString("message", "Çocuğun eklendi."));
                e.yenidenKur();
            }, h -> e.yenidenKur()), h -> { Arayuz.mesgul(ekle, false); kod.hataGoster(h.getMessage()); });
        }
    }

    /* ---------------- Öğretmen ve Müdür: kişi kodu ---------------- */

    public static class Kod extends Sayfa.Veri {
        private final boolean mudur;

        public Kod(boolean mudur) { this.mudur = mudur; }

        @Override public String baslik() { return mudur ? "Okulunu açtır" : "Öğretmen olarak katıl"; }
        @Override protected String adres() { return "/api/kisilikler"; }
        @Override protected boolean yenilenir() { return false; }

        @Override
        protected View ciz(JSONObject j) {
            ScrollView[] s = new ScrollView[1];
            LinearLayout g = Arayuz.sayfaGovdesi(e, s);
            String kod = j.optString("kisiKodu", j.optString("ogretmenKodu", ""));
            g.addView(Arayuz.baslik(e, "Kişi kodun"));
            Arayuz.ekle(g, Arayuz.yazi(e, mudur
                ? "Yöneticimize okulunun adını ve bu kodu ver. Okulunu ve adresini açıp seni müdür yapar; okul portalların arasında görünür."
                : "Bu kodu okulunun müdürüne ver. Müdür kodu girince seni okula öğretmen olarak ekler; okul portalların arasında görünür.",
                15, R.color.soluk), 8);
            LinearLayout kart = Arayuz.kart(e);
            kart.addView(KisiKodu.kutu(e, kod, () -> e.bildir("Kod kopyalandı.")));
            Arayuz.ekle(kart, Arayuz.soluk(e, "Kod bir kez kullanılır; seni ekleyince yenilenir. Kodu yalnızca vermek istediğin kişiye ver."), 12);
            TextView yeni = Arayuz.dugme(e, "Yeni kod üret", Arayuz.Dugme.HAYALET);
            yeni.setOnClickListener(v -> {
                Arayuz.mesgul(yeni, true);
                Ag.post(e, "/api/kisilik/kod", null, x -> { e.bildir("Yeni kod üretildi; eskisi artık geçmez."); yukle(); },
                    h -> { Arayuz.mesgul(yeni, false); e.bildir(h.getMessage()); });
            });
            Arayuz.ekle(kart, yeni, 8);
            Arayuz.ekle(g, kart, 16);
            if (mudur) {
                LinearLayout il = Arayuz.kart(e);
                il.addView(Arayuz.altBaslik(e, "Yöneticiye ulaş"));
                LinearLayout yer = Arayuz.dikey(e);
                Arayuz.ekle(il, yer, 8);
                Ag.get(e, "/api/site", site -> iletisimCiz(yer, site.optJSONObject("iletisim")), h -> iletisimCiz(yer, null));
                Arayuz.ekle(g, il, 14);
            }
            return s[0];
        }

        private void iletisimCiz(LinearLayout yer, JSONObject il) {
            yer.removeAllViews();
            String ep = il == null ? "" : il.optString("eposta"), tel = il == null ? "" : il.optString("telefon");
            if (ep.isEmpty() && tel.isEmpty()) {
                yer.addView(Arayuz.soluk(e, "İletişim bilgisi sitenin alt bilgisinde duruyor."));
                return;
            }
            if (!ep.isEmpty()) {
                yer.addView(Arayuz.tiklananSatir(e, Arayuz.ikon(e, R.drawable.ik_posta, R.color.ana, 22), ep, "E-posta yaz", null,
                    v -> ac(Uri.parse("mailto:" + ep + "?subject=" + Uri.encode("Okulumu Eğitim Evi'ne açtırmak istiyorum")))));
            }
            if (!tel.isEmpty()) {
                yer.addView(Arayuz.tiklananSatir(e, Arayuz.ikon(e, R.drawable.ik_telefon, R.color.ana, 22), tel, "Ara", null,
                    v -> ac(Uri.parse("tel:" + tel.replaceAll("[^0-9+]", "")))));
            }
        }

        private void ac(Uri u) {
            try { e.startActivity(new Intent(Intent.ACTION_VIEW, u)); } catch (Exception x) { e.bildir("Bunu açacak uygulama yok."); }
        }
    }
}
