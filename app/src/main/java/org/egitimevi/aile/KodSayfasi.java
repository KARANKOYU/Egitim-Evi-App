package org.egitimevi.aile;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONObject;

/**
 * İki adımlı girişin ikinci adımı: e-postaya gelen 6 haneli kod (5 dakika geçerli,
 * bir kez kullanılır). Altı hane yazılınca kendiliğinden gönderilir. Yeni kod 60
 * saniye sonra istenebilir.
 */
public class KodSayfasi extends Sayfa {
    private String kimlik;
    private final String bilgi;
    private Arayuz.Alan kod;
    private TextView dogrula, tekrar;
    private final Handler ana = new Handler(Looper.getMainLooper());
    private int bekle = 60;

    public KodSayfasi(String kimlik, String bilgi) { this.kimlik = kimlik; this.bilgi = bilgi; }

    @Override public String baslik() { return "Giriş kodu"; }
    @Override protected boolean cubuksuz() { return true; }

    @Override
    protected View olustur() {
        ScrollView[] s = new ScrollView[1];
        LinearLayout g = Arayuz.sayfaGovdesi(e, s);
        g.setPadding(Tema.dp(e, 22), Tema.dp(e, 48), Tema.dp(e, 22), Tema.dp(e, 32));
        TextView geri = Arayuz.dugme(e, "← Geri", Arayuz.Dugme.HAYALET);
        geri.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        geri.setOnClickListener(v -> e.kapat());
        g.addView(geri, new LinearLayout.LayoutParams(-2, -2));
        Arayuz.ekle(g, Arayuz.baslik(e, "E-postana gelen kodu yaz"), 12);
        Arayuz.ekle(g, Arayuz.yazi(e, bilgi == null || bilgi.isEmpty() ? "Giriş kodu e-posta adresine gönderildi." : bilgi,
            15, R.color.soluk), 8);

        LinearLayout kart = Arayuz.kart(e);
        kod = Arayuz.alan(e, "6 haneli kod", "000000", InputType.TYPE_CLASS_NUMBER);
        kod.kutu.setFilters(new InputFilter[] { new InputFilter.LengthFilter(6) });
        kod.kutu.setTextSize(26);
        kod.kutu.setLetterSpacing(0.4f);
        kod.kutu.setGravity(Gravity.CENTER);
        kod.kutu.setTypeface(Tema.KOD);
        kod.kutu.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence x, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence x, int a, int b, int c) { }
            @Override public void afterTextChanged(Editable x) { if (x.length() == 6) gonder(); }
        });
        kart.addView(kod.kok);
        dogrula = Arayuz.dugme(e, "Girişi tamamla", Arayuz.Dugme.BIRINCIL);
        dogrula.setOnClickListener(v -> gonder());
        Arayuz.ekle(kart, dogrula, 16);
        tekrar = Arayuz.dugme(e, "", Arayuz.Dugme.HAYALET);
        tekrar.setOnClickListener(v -> tekrarGonder());
        Arayuz.ekle(kart, tekrar, 4);
        Arayuz.ekle(g, kart, 20);
        Arayuz.ekle(g, Arayuz.soluk(e, "Kod gelmediyse istenmeyen (spam) klasörüne bak. Kod 5 dakika geçerlidir."), 14);
        sayac();
        kod.kutu.requestFocus();
        return s[0];
    }

    private void sayac() {
        ana.removeCallbacksAndMessages(null);
        Runnable r = new Runnable() {
            @Override public void run() {
                if (bekle > 0) {
                    tekrar.setText("Yeni kod " + bekle + " saniye sonra istenebilir");
                    Arayuz.mesgul(tekrar, true);
                    bekle--;
                    ana.postDelayed(this, 1000);
                } else {
                    tekrar.setText("Yeni kod gönder");
                    Arayuz.mesgul(tekrar, false);
                }
            }
        };
        r.run();
    }

    private void gonder() {
        String k = kod.deger();
        if (!k.matches("^\\d{6}$")) { kod.hataGoster("Kod 6 rakamdır."); return; }
        kod.hataGoster(null);
        Arayuz.mesgul(dogrula, true);
        JSONObject g = new JSONObject();
        try { g.put("challengeId", kimlik).put("code", k).put("uygulama", true); } catch (Exception x) { return; }
        Ag.post(e, "/api/login/dogrula", g, j -> {
            ana.removeCallbacksAndMessages(null);
            e.oturumAc(j);
        }, h -> {
            Arayuz.mesgul(dogrula, false);
            kod.hataGoster(h.getMessage());
            kod.kutu.setText("");
        });
    }

    private void tekrarGonder() {
        Arayuz.mesgul(tekrar, true);
        JSONObject g = new JSONObject();
        try { g.put("challengeId", kimlik); } catch (Exception x) { return; }
        Ag.post(e, "/api/login/tekrar", g, j -> {
            kimlik = j.optString("challengeId", kimlik);
            e.bildir(j.optString("mesaj", "Yeni kod gönderildi."));
            bekle = 60;
            sayac();
        }, h -> { Arayuz.mesgul(tekrar, false); e.bildir(h.getMessage()); });
    }

    @Override
    protected boolean geriBas() {
        ana.removeCallbacksAndMessages(null);
        return false;
    }
}
