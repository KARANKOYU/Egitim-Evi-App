package org.egitimevi.aile;

import android.app.AlertDialog;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Portallar (sitede sol menünün üstündeki liste): "Öğretmen · Test Ortaokulu",
 * "Müdür · Deneme Anadolu", "Veli · Zeynep Şahin". Dokununca o portala geçilir
 * (sunucu yeni oturum açar). Altta "+ Ekle".
 */
final class PortalSecici {
    private PortalSecici() { }

    static void goster(AnaEkran e) {
        ScrollView kaydir = new ScrollView(e);
        LinearLayout g = Arayuz.dikey(e);
        int p = Tema.dp(e, 18);
        g.setPadding(p, p, p, Tema.dp(e, 12));
        kaydir.addView(g);
        JSONObject k = Oturum.kisi(e);
        LinearLayout ust = Arayuz.yatay(e);
        ust.addView(Arayuz.avatar(e, k.optString("fullName"), k.optString("id"), 44));
        LinearLayout ad = Arayuz.dikey(e);
        ad.setPadding(Tema.dp(e, 12), 0, 0, 0);
        ad.addView(Arayuz.altBaslik(e, k.optString("fullName")));
        String alt = k.optString("email").isEmpty() ? k.optString("username") : k.optString("email");
        ad.addView(Arayuz.soluk(e, alt));
        ust.addView(ad, new LinearLayout.LayoutParams(0, -2, 1f));
        g.addView(ust);
        Arayuz.ekle(g, Arayuz.bolumEtiketi(e, "Portalların"), 20);
        LinearLayout liste = Arayuz.dikey(e);
        Arayuz.ekle(g, liste, 6);

        AlertDialog d = new AlertDialog.Builder(e).setView(kaydir).create();
        satirlar(e, liste, d::dismiss);
        TextView ekle = Arayuz.dugme(e, "+ Ekle", Arayuz.Dugme.IKINCIL);
        ekle.setOnClickListener(v -> { d.dismiss(); e.git(new EkleSayfasi()); });
        Arayuz.ekle(g, ekle, 14);
        TextView ayar = Arayuz.dugme(e, "Ayarlar", Arayuz.Dugme.HAYALET);
        ayar.setOnClickListener(v -> { d.dismiss(); e.git(new AyarlarSayfasi()); });
        Arayuz.ekle(g, ayar, 4);
        d.show();
        if (d.getWindow() != null) {
            d.getWindow().setBackgroundDrawable(Tema.zemin(e, Tema.renk(e, R.color.kart), Tema.R_BUYUK, 0, 0));
        }
    }

    /** Portal satırlarını kutuya çizer; oturumda liste yoksa sunucudan ister. */
    static void satirlar(AnaEkran e, LinearLayout kap, Runnable kapat) {
        JSONArray p = Oturum.portallar(e);
        if (p.length() > 0) { ciz(e, kap, p, kapat); return; }
        kap.addView(Arayuz.soluk(e, "Yükleniyor..."));
        Ag.get(e, "/api/kisilikler", j -> {
            JSONArray l = new JSONArray();
            JSONArray roller = j.optJSONArray("roller"), cocuklar = j.optJSONArray("cocuklar");
            try {
                for (int i = 0; roller != null && i < roller.length(); i++) {
                    JSONObject r = roller.getJSONObject(i);
                    l.put(new JSONObject().put("tur", "rol").put("id", r.optString("id"))
                        .put("ad", "principal".equals(r.optString("rol")) ? "Müdür" : "Öğretmen")
                        .put("alt", r.optString("okulAdi")).put("girilebilir", r.optBoolean("girilebilir", true))
                        .put("aktif", r.optString("id").equals(j.optString("aktif"))));
                }
                for (int i = 0; cocuklar != null && i < cocuklar.length(); i++) {
                    JSONObject c = cocuklar.getJSONObject(i);
                    l.put(new JSONObject().put("tur", "veli").put("id", c.optString("id")).put("ad", "Veli")
                        .put("alt", c.optString("ad")).put("okulAdi", c.optString("okulAdi")).put("girilebilir", true));
                }
            } catch (Exception ignored) { }
            ciz(e, kap, l, kapat);
        }, h -> { kap.removeAllViews(); kap.addView(Arayuz.soluk(e, h.getMessage())); });
    }

    private static void ciz(AnaEkran e, LinearLayout kap, JSONArray l, Runnable kapat) {
        kap.removeAllViews();
        if (l.length() == 0) {
            kap.addView(Arayuz.soluk(e, "Henüz bir portalın yok. + Ekle ile başla."));
            return;
        }
        for (int i = 0; i < l.length(); i++) {
            JSONObject p = l.optJSONObject(i);
            String tur = p.optString("tur"), ad = p.optString("ad"), alt = p.optString("alt");
            boolean veli = "veli".equals(tur);
            /* Veli portalında olmak: oturum yetişkin hesabında (rol satırında değil) ve bu çocuk seçili. */
            boolean aktif = p.optBoolean("aktif")
                || (veli && !Oturum.kisi(e).optBoolean("rolSatiri") && p.optString("id").equals(Oturum.seciliCocuk(e)));
            View sol = veli ? Arayuz.avatar(e, alt, p.optString("id"), 36)
                : Arayuz.ikon(e, "Müdür".equals(ad) ? R.drawable.ik_mudur : R.drawable.ik_ogretmen, R.color.ana, 26);
            View sag = aktif ? Arayuz.etiket(e, "Buradasın", Arayuz.Etiket.YESIL)
                : !p.optBoolean("girilebilir", true) ? Arayuz.etiket(e, "Onay bekliyor", Arayuz.Etiket.TURUNCU) : null;
            String altYazi = veli && !p.optString("okulAdi").isEmpty() ? p.optString("okulAdi") : "";
            String baslik = ad + " · " + alt;
            if (!p.optBoolean("girilebilir", true) || aktif) {
                kap.addView(Arayuz.satir(e, sol, baslik, altYazi, sag));
            } else {
                kap.addView(Arayuz.tiklananSatir(e, sol, baslik, altYazi, sag, v -> {
                    if (kapat != null) kapat.run();
                    gec(e, tur, p.optString("id"));
                }));
            }
        }
    }

    /** Portala geçiş: sunucu yeni oturum açar; veli portalında çocuk seçilir. */
    static void gec(AnaEkran e, String tur, String id) {
        JSONObject g = new JSONObject();
        try { g.put("tur", tur).put("id", id); } catch (Exception x) { return; }
        Ag.post(e, "/api/kisilik/gec", g, j -> {
            if ("veli".equals(tur)) Oturum.cocukSec(e, id);
            e.oturumAc(j);
        }, h -> e.bildir(h.getMessage()));
    }
}
