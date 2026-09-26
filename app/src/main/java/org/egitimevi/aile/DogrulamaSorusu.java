package org.egitimevi.aile;

import android.text.InputType;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONObject;

/**
 * Sitedeki "doğrulama sorusu" (ör. 4 + 7 = ?): kayıtta ve şifre sıfırlamada her zaman,
 * girişte yalnızca hatalı denemeden sonra sorulur. Soru sunucudan gelir.
 */
final class DogrulamaSorusu {
    final LinearLayout kok;
    private final AnaEkran e;
    private final TextView soru;
    private final Arayuz.Alan cevap;
    private String id = "";

    DogrulamaSorusu(AnaEkran e) {
        this.e = e;
        kok = Arayuz.dikey(e);
        LinearLayout satir = Arayuz.yatay(e);
        soru = Arayuz.yazi(e, "Doğrulama sorusu yükleniyor...", 15, R.color.yazi);
        soru.setTypeface(Tema.ORTA);
        satir.addView(soru, new LinearLayout.LayoutParams(0, -2, 1f));
        TextView baska = Arayuz.dugme(e, "Başka soru", Arayuz.Dugme.HAYALET);
        baska.setMinHeight(Tema.dp(e, 40));
        baska.setOnClickListener(v -> getir());
        satir.addView(baska);
        kok.addView(satir);
        cevap = Arayuz.alan(e, "Cevap", "Sayıyla yaz", InputType.TYPE_CLASS_NUMBER);
        Arayuz.ekle(kok, cevap.kok, 6);
    }

    void getir() {
        cevap.kutu.setText("");
        Ag.get(e, "/api/challenge", j -> {
            id = j.optString("id");
            soru.setText("Doğrulama: " + j.optString("soru"));
        }, h -> soru.setText("Soru alınamadı: " + h.getMessage()));
    }

    void goster(boolean g) {
        kok.setVisibility(g ? View.VISIBLE : View.GONE);
        if (g && id.isEmpty()) getir();
    }

    boolean gorunur() { return kok.getVisibility() == View.VISIBLE; }

    void hata(String s) { cevap.hataGoster(s); }

    /** Gövdeye challengeId ve challengeAnswer ekler. */
    void ekle(JSONObject g) throws org.json.JSONException {
        g.put("challengeId", id);
        String c = cevap.deger();
        g.put("challengeAnswer", c.matches("^\\d{1,3}$") ? Integer.parseInt(c) : -1);
    }
}
