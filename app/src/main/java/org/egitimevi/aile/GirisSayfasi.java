package org.egitimevi.aile;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Giriş. Veli, öğretmen ve müdür e-posta ya da kullanıcı adıyla girer; öğrenci ve
 * servisçi okulunu seçip kullanıcı adıyla girer (aynı kullanıcı adı başka okulda da
 * olabilir). Hatalı denemeden sonra doğrulama sorusu çıkar. E-postası olan hesaba
 * ikinci adımda kod gider (KodSayfasi).
 */
public class GirisSayfasi extends Sayfa {
    private Arayuz.Alan kimlik, sifre, okulAra;
    private LinearLayout okulKutusu, okulSonuclari;
    private TextView okulSecili, okulAc, giris;
    private DogrulamaSorusu soru;
    private String okulKisaAd = "";
    private final Runnable aramaIsi = this::okulAra;

    @Override public String baslik() { return "Giriş"; }
    @Override protected boolean cubuksuz() { return true; }

    @Override
    protected View olustur() {
        ScrollView[] s = new ScrollView[1];
        LinearLayout g = Arayuz.sayfaGovdesi(e, s);
        g.setPadding(Tema.dp(e, 22), Tema.dp(e, 40), Tema.dp(e, 22), Tema.dp(e, 32));

        g.addView(marka());

        LinearLayout kart = Arayuz.kart(e);
        kart.setPadding(Tema.dp(e, 20), Tema.dp(e, 22), Tema.dp(e, 20), Tema.dp(e, 22));
        kart.addView(Arayuz.altBaslik(e, "Giriş yap"));

        kimlik = Arayuz.alan(e, "E-posta ya da kullanıcı adı", "ornek@eposta.com",
            InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        Arayuz.ekle(kart, kimlik.kok, 16);

        sifre = Arayuz.alan(e, "Şifre", "", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        sifreGoster(sifre);
        sifre.kutu.setImeOptions(EditorInfo.IME_ACTION_DONE);
        sifre.kutu.setOnEditorActionListener((v, a, o) -> { if (a == EditorInfo.IME_ACTION_DONE) { gir(); return true; } return false; });
        Arayuz.ekle(kart, sifre.kok, 12);

        okulAc = Arayuz.dugme(e, "Öğrenci ya da servisçiysen okulunu seç", Arayuz.Dugme.HAYALET);
        okulAc.setTextSize(14);
        okulAc.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        okulAc.setOnClickListener(v -> okulKutusunuAc(true));
        Arayuz.ekle(kart, okulAc, 4);
        okulKutusu = okulKutusuKur();
        okulKutusu.setVisibility(View.GONE);
        Arayuz.ekle(kart, okulKutusu, 4);

        soru = new DogrulamaSorusu(e);
        soru.goster(false);
        Arayuz.ekle(kart, soru.kok, 12);

        giris = Arayuz.dugme(e, "Giriş yap", Arayuz.Dugme.BIRINCIL);
        giris.setOnClickListener(v -> gir());
        Arayuz.ekle(kart, giris, 18);

        TextView unuttum = Arayuz.dugme(e, "Şifremi unuttum", Arayuz.Dugme.HAYALET);
        unuttum.setOnClickListener(v -> e.git(new SifremiUnuttumSayfasi()));
        Arayuz.ekle(kart, unuttum, 4);
        Arayuz.ekle(g, kart, 24);

        LinearLayout hesap = Arayuz.kart(e);
        hesap.addView(Arayuz.altBaslik(e, "Hesabın yok mu?"));
        Arayuz.ekle(hesap, Arayuz.yazi(e, "Veli, öğretmen ve müdür kendi hesabını açar. Öğrenci ve servisçi hesabını okul açar; "
            + "kullanıcı adını ve ilk şifreni okulundan al.", 14, R.color.soluk), 6);
        TextView ac = Arayuz.dugme(e, "Hesap aç", Arayuz.Dugme.IKINCIL);
        ac.setOnClickListener(v -> e.git(new KayitSayfasi()));
        Arayuz.ekle(hesap, ac, 14);
        Arayuz.ekle(g, hesap, 14);

        TextView not = Arayuz.soluk(e, "Bilgilerin yalnızca Eğitim Evi sunucusunda tutulur; reklam ya da satış için kimseyle paylaşılmaz.");
        not.setGravity(Gravity.CENTER);
        Arayuz.ekle(g, not, 18);
        return s[0];
    }

    /** Uygulamanın simgesi ve adı. */
    private View marka() {
        LinearLayout m = Arayuz.dikey(e);
        m.setGravity(Gravity.CENTER_HORIZONTAL);
        /* Başlatıcı simgesi ve sitedeki simgeyle aynı çizim (bacalı ev, aralık kapı). */
        ImageView i = new ImageView(e);
        i.setImageResource(R.drawable.simge_marka);
        i.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);   // altındaki "Eğitim Evi" yazısı okunur
        m.addView(i, new LinearLayout.LayoutParams(Tema.dp(e, 72), Tema.dp(e, 72)));
        TextView ad = Arayuz.baslik(e, "Eğitim Evi");
        ad.setTextSize(32);
        ad.setGravity(Gravity.CENTER);
        Arayuz.ekle(m, ad, 14);
        TextView alt = Arayuz.yazi(e, "Ödev, not, servis ve okuldan haberler tek yerde.", 15, R.color.soluk);
        alt.setGravity(Gravity.CENTER);
        Arayuz.ekle(m, alt, 4);
        return m;
    }

    /** Şifre kutusunun altında "Göster/Gizle". */
    static void sifreGoster(Arayuz.Alan a) {
        TextView g = Arayuz.yazi(a.kok.getContext(), "Şifreyi göster", 13, R.color.ana);
        g.setTypeface(Tema.ORTA);
        g.setPadding(0, Tema.dp(a.kok.getContext(), 6), 0, Tema.dp(a.kok.getContext(), 2));
        g.setOnClickListener(v -> {
            boolean gizli = a.kutu.getTransformationMethod() instanceof PasswordTransformationMethod;
            a.kutu.setTransformationMethod(gizli ? HideReturnsTransformationMethod.getInstance() : PasswordTransformationMethod.getInstance());
            a.kutu.setSelection(a.kutu.getText().length());
            g.setText(gizli ? "Şifreyi gizle" : "Şifreyi göster");
        });
        a.kok.addView(g);
    }

    /* ---------------- okul seçimi ---------------- */

    private LinearLayout okulKutusuKur() {
        LinearLayout k = Arayuz.dikey(e);
        okulSecili = Arayuz.serit(e, "", Arayuz.Etiket.MAVI);
        okulSecili.setVisibility(View.GONE);
        okulSecili.setOnClickListener(v -> { okulKisaAd = ""; okulSecili.setVisibility(View.GONE); okulAra.kok.setVisibility(View.VISIBLE); });
        k.addView(okulSecili);
        okulAra = Arayuz.alan(e, "Okulun", "Okulunun adını yaz", InputType.TYPE_CLASS_TEXT);
        okulAra.kutu.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void afterTextChanged(Editable s) {
                okulAra.kutu.removeCallbacks(aramaIsi);
                okulAra.kutu.postDelayed(aramaIsi, 350);
            }
        });
        Arayuz.ekle(k, okulAra.kok, 8);
        okulSonuclari = Arayuz.dikey(e);
        k.addView(okulSonuclari);
        return k;
    }

    private void okulKutusunuAc(boolean ac) {
        okulKutusu.setVisibility(ac ? View.VISIBLE : View.GONE);
        okulAc.setVisibility(ac ? View.GONE : View.VISIBLE);
        if (ac) okulAra.kutu.requestFocus();
    }

    private void okulAra() {
        String q = okulAra.deger();
        if (q.length() < 2) { okulSonuclari.removeAllViews(); return; }
        String yol;
        try { yol = "/api/okul-adres/ara?q=" + URLEncoder.encode(q, StandardCharsets.UTF_8.name()); } catch (Exception x) { return; }
        Ag.get(e, yol, j -> {
            okulSonuclari.removeAllViews();
            JSONArray l = j.optJSONArray("okullar");
            if (l == null || l.length() == 0) {
                Arayuz.ekle(okulSonuclari, Arayuz.soluk(e, "Bu adla bir okul bulunamadı."), 8);
                return;
            }
            for (int i = 0; i < l.length() && i < 6; i++) {
                JSONObject o = l.optJSONObject(i);
                String ad = o.optString("ad"), yer = (o.optString("ilce") + " / " + o.optString("il")).replaceAll("^ / | / $", "");
                okulSonuclari.addView(Arayuz.tiklananSatir(e, Arayuz.ikon(e, R.drawable.ik_okul, R.color.ana, 22), ad, yer, null, v -> {
                    okulKisaAd = o.optString("kisaAd");
                    okulSecili.setText(ad + "  ·  değiştir");
                    okulSecili.setVisibility(View.VISIBLE);
                    okulAra.kok.setVisibility(View.GONE);
                    okulSonuclari.removeAllViews();
                }));
            }
        }, h -> { });
    }

    /* ---------------- giriş ---------------- */

    private void gir() {
        kimlik.hataGoster(null);
        sifre.hataGoster(null);
        if (kimlik.deger().isEmpty()) { kimlik.hataGoster("Kullanıcı adını ya da e-posta adresini yaz."); return; }
        if (sifre.hamDeger().isEmpty()) { sifre.hataGoster("Şifreni yaz."); return; }
        JSONObject g = new JSONObject();
        try {
            g.put("kimlik", kimlik.deger()).put("password", sifre.hamDeger()).put("uygulama", true);
            if (!okulKisaAd.isEmpty()) g.put("okul", okulKisaAd);
            if (soru.gorunur()) soru.ekle(g);
        } catch (Exception x) { return; }
        Arayuz.mesgul(giris, true);
        giris.setText("Giriş yapılıyor...");
        Ag.post(e, "/api/login", g, j -> {
            Arayuz.mesgul(giris, false);
            giris.setText("Giriş yap");
            if (j.optBoolean("twoFactor")) {
                e.git(new KodSayfasi(j.optString("challengeId"), j.optString("mesaj")));
            } else if (!j.optString("token").isEmpty()) {
                e.oturumAc(j);
            }
        }, h -> {
            Arayuz.mesgul(giris, false);
            giris.setText("Giriş yap");
            JSONObject b = h.govde;
            String alan = b.optString("alan");
            if (b.optBoolean("okulSec")) okulKutusunuAc(true);
            if (b.optBoolean("soruGerekli")) { soru.goster(true); soru.getir(); }
            if ("sifre".equals(alan)) sifre.hataGoster(h.getMessage());
            else if ("bot".equals(alan)) soru.hata(h.getMessage());
            else if ("kimlik".equals(alan)) kimlik.hataGoster(h.getMessage());
            else e.bildir(h.getMessage());
        });
    }
}
