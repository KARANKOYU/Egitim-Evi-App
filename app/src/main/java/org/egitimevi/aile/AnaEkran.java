package org.egitimevi.aile;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Tek ekran. Bağlı değilken: öğrenci hesabıyla giriş ve açık onay.
 * Bağlıyken: izinler (konum her zaman, kullanım erişimi, bildirim, pil) ve
 * son gönderim durumu. Arayüz kodla kurulur; dış kütüphane yok.
 */
public class AnaEkran extends Activity {
    private static final int ANA = Color.parseColor("#D62839");
    private static final int YAZI = Color.parseColor("#23191A");
    private static final int SOLUK = Color.parseColor("#6D5F60");
    private static final int IZIN_KONUM = 1, IZIN_ARKA = 2, IZIN_BILDIRIM = 3;

    private final ExecutorService arka = Executors.newSingleThreadExecutor();
    private final Handler ana = new Handler(Looper.getMainLooper());
    private LinearLayout govde;
    private String sorunNo = "";

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        ScrollView kaydir = new ScrollView(this);
        kaydir.setBackgroundColor(Color.parseColor("#FFF9F5"));
        govde = new LinearLayout(this);
        govde.setOrientation(LinearLayout.VERTICAL);
        int p = dp(20);
        govde.setPadding(p, dp(28), p, dp(40));
        kaydir.addView(govde);
        setContentView(kaydir);
        /* Açık zemin: durum çubuğu simgeleri koyu olsun; içerik çubukların altına girmesin. */
        getWindow().setStatusBarColor(Color.parseColor("#FFF9F5"));
        getWindow().setNavigationBarColor(Color.parseColor("#FFF9F5"));
        if (Build.VERSION.SDK_INT >= 30) {
            int a = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
            getWindow().getInsetsController().setSystemBarsAppearance(a, a);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        kaydir.setOnApplyWindowInsetsListener((v, ic) -> {
            int ust, alt;
            if (Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets s = ic.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.ime());
                ust = s.top; alt = s.bottom;
            } else {
                ust = ic.getSystemWindowInsetTop(); alt = ic.getSystemWindowInsetBottom();
            }
            govde.setPadding(p, ust + dp(16), p, alt + dp(28));
            return ic;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        ciz();
        if (Ayarlar.bagli(this) && konumIzni()) IzlemeServisi.baslat(this);
    }

    private void ciz() {
        govde.removeAllViews();
        baslik("Eğitim Evi Aile");
        if (Ayarlar.bagli(this)) bagliEkran(); else girisEkrani();
    }

    /* ---------------- giriş ---------------- */
    private void girisEkrani() {
        yazi("Bu uygulama telefonunun konumunu ve hangi uygulamayı ne kadar kullandığını velinle paylaşır. "
            + "Velin bunları Eğitim Evi'nde görür; okulun görmez. Veriler 7 gün sonra silinir. "
            + "Hiçbir uygulama kapatılmaz ya da kilitlenmez.", SOLUK, 15);
        EditText sunucu = alan("Sunucu adresi", Ayarlar.sunucu(this), InputType.TYPE_TEXT_VARIATION_URI);
        EditText okul = alan("Okulunun adresi (egitimevi.org/...)", "", InputType.TYPE_CLASS_TEXT);
        EditText kadi = alan("Kullanıcı adı", "", InputType.TYPE_CLASS_TEXT);
        EditText sifre = alan("Şifre", "", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        TextView soru = yazi("Doğrulama sorusu yükleniyor...", YAZI, 15);
        EditText cevap = alan("Cevap", "", InputType.TYPE_CLASS_NUMBER);
        Button yenile = dugme("Başka soru", false);
        yenile.setOnClickListener(v -> soruGetir(sunucu.getText().toString().trim(), soru));
        CheckBox onay = new CheckBox(this);
        onay.setText("Konumumun ve ekran süremin velimle paylaşılacağını okudum, kabul ediyorum.");
        onay.setTextColor(YAZI);
        onay.setTextSize(15);
        govde.addView(onay, bosluk(dp(12)));
        TextView mesaj = yazi("", ANA, 14);
        Button giris = dugme("Giriş yap ve bu telefonu bağla", true);
        giris.setOnClickListener(v -> {
            String adres = sunucu.getText().toString().trim();
            String sorun = Api.adresSorunu(adres);
            if (sorun != null) { mesaj.setText(sorun); return; }
            if (!onay.isChecked()) { mesaj.setText("Devam etmek için paylaşımı kabul etmelisin."); return; }
            if (kadi.getText().toString().trim().isEmpty() || sifre.getText().toString().isEmpty()) {
                mesaj.setText("Kullanıcı adını ve şifreni yaz."); return;
            }
            giris.setEnabled(false);
            mesaj.setText("Bağlanıyor...");
            arka.execute(() -> {
                try {
                    String sonuc = baglan(adres, okul.getText().toString().trim(), kadi.getText().toString().trim(),
                        sifre.getText().toString(), cevap.getText().toString().trim());
                    ana.post(() -> {
                        if (sonuc == null) { ciz(); izinIste(); }
                        else { mesaj.setText(sonuc); giris.setEnabled(true); soruGetir(adres, soru); }
                    });
                } catch (Exception e) {
                    ana.post(() -> { mesaj.setText("Bağlanılamadı: " + e.getMessage()); giris.setEnabled(true); });
                }
            });
        });
        soruGetir(sunucu.getText().toString().trim(), soru);
    }

    private void soruGetir(String adres, TextView soru) {
        if (Api.adresSorunu(adres) != null) { soru.setText("Önce sunucu adresini doğru yaz."); return; }
        arka.execute(() -> {
            try {
                JSONObject s = Api.get(adres, "/api/challenge", null);
                sorunNo = s.optString("id");
                String metin = s.optString("soru");
                ana.post(() -> soru.setText("Doğrulama: " + metin));
            } catch (Exception e) {
                ana.post(() -> soru.setText("Sunucuya ulaşılamadı: " + e.getMessage()));
            }
        });
    }

    /** null: başarılı; yoksa gösterilecek hata. Ağ işi (arka iş parçacığında). */
    private String baglan(String adres, String okul, String kadi, String sifre, String cevap) throws Exception {
        JSONObject g = new JSONObject().put("email", kadi).put("password", sifre)
            .put("challengeId", sorunNo).put("challengeAnswer", cevap.isEmpty() ? 0 : Integer.parseInt(cevap));
        if (!okul.isEmpty()) g.put("okul", okul.toLowerCase(Locale.ROOT));
        JSONObject r;
        try { r = Api.post(adres, "/api/login", g, null, null); }
        catch (Api.Hata h) { return h.getMessage(); }
        if (r.optBoolean("twoFactor")) return "Bu uygulamaya öğrenci hesabıyla girilir.";
        String oturum = r.optString("token");
        JSONObject u = r.optJSONObject("user");
        if (oturum.isEmpty() || u == null) return "Giriş yapılamadı.";
        if (!"student".equals(u.optString("role"))) {
            cikis(adres, oturum);
            return "Bu uygulamaya öğrenci hesabıyla girilir (veli, telefonun sahibi olan çocuğun hesabıyla bağlar).";
        }
        JSONObject c = new JSONObject().put("ad", Build.MANUFACTURER + " " + Build.MODEL).put("platform", "android")
            .put("surum", Build.VERSION.RELEASE).put("onay", true);
        try {
            JSONObject d = Api.post(adres, "/api/aile/cihaz", c, oturum, null);
            Ayarlar.baglan(this, adres, d.getString("cihazAnahtari"), d.optJSONObject("ogrenci") != null
                ? d.getJSONObject("ogrenci").optString("ad") : u.optString("fullName"));
            JSONObject a = d.optJSONObject("ayar");
            if (a != null) Ayarlar.ayariYaz(this, a.optInt("wifiDk", 5), a.optInt("mobilDk", 15),
                a.optBoolean("konumAcik", true), a.optBoolean("kullanimAcik", true));
            return null;
        } catch (Api.Hata h) {
            return h.getMessage();
        } finally {
            cikis(adres, oturum);   // öğrencinin oturumu telefonda kalmaz; yalnızca cihaz anahtarı kalır
        }
    }

    private void cikis(String adres, String oturum) {
        try { Api.post(adres, "/api/logout", new JSONObject(), oturum, null); } catch (Exception ignored) { }
    }

    /* ---------------- bağlıyken ---------------- */
    private void bagliEkran() {
        yazi("Bağlı hesap: " + Ayarlar.ogrenciAdi(this), YAZI, 17).setTypeface(null, Typeface.BOLD);
        yazi("Konumun ve ekran süren velinle paylaşılıyor. Velin gönderme sıklığını seçer: Wi-Fi'deyken "
            + Ayarlar.wifiAraligi(this) + " dakikada bir, mobil veride " + Ayarlar.mobilAraligi(this)
            + " dakikada bir. İnternet yokken konumlar telefonda bekler, bağlanınca gönderilir.", SOLUK, 14);

        altBaslik("İzinler");
        izinSatiri("Konum", konumIzni(), "Konum iznini ver", v -> requestPermissions(new String[] {
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION }, IZIN_KONUM));
        if (Build.VERSION.SDK_INT >= 29) {
            izinSatiri("Konum: her zaman (uygulama kapalıyken de)", arkaKonumIzni(),
                Build.VERSION.SDK_INT >= 30 ? "Ayarlar'da \"Her zaman izin ver\"i seç" : "Her zaman izin ver",
                v -> {
                    if (!konumIzni()) { yazi("Önce konum iznini ver.", ANA, 14); return; }
                    requestPermissions(new String[] { Manifest.permission.ACCESS_BACKGROUND_LOCATION }, IZIN_ARKA);
                });
        }
        izinSatiri("Ekran süresi (kullanım erişimi)", Kullanim.izinVar(this), "Kullanım erişimini aç",
            v -> startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)));
        if (Build.VERSION.SDK_INT >= 33) {
            izinSatiri("Bildirimler", checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED,
                "Bildirim iznini ver", v -> requestPermissions(new String[] { Manifest.permission.POST_NOTIFICATIONS }, IZIN_BILDIRIM));
        }
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        izinSatiri("Arka planda çalışma (pil kısıtlaması yok)", pm.isIgnoringBatteryOptimizations(getPackageName()),
            "Arka planda çalışmasına izin ver", v -> {
                Intent i = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:" + getPackageName()));
                try { startActivity(i); } catch (Exception e) { startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)); }
            });

        altBaslik("Durum");
        SimpleDateFormat s = new SimpleDateFormat("d MMMM HH:mm", new Locale("tr", "TR"));
        long sk = Ayarlar.sonKonum(this), sg = Ayarlar.sonGonderim(this);
        yazi("Son konum: " + (sk == 0 ? "henüz yok" : s.format(new Date(sk))), YAZI, 15);
        yazi("Son gönderim: " + (sg == 0 ? "henüz yok" : s.format(new Date(sg))), YAZI, 15);
        int bekleyen = Kuyruk.boyut(this);
        if (bekleyen > 0) yazi("Gönderilmeyi bekleyen konum: " + bekleyen, YAZI, 15);
        if (!Ayarlar.sonHata(this).isEmpty()) yazi("Son sorun: " + Ayarlar.sonHata(this), ANA, 14);

        Button kaldir = dugme("Bu telefonun bağlantısını kaldır", false);
        kaldir.setOnClickListener(v -> {
            kaldir.setEnabled(false);
            String adres = Ayarlar.sunucu(this), anahtar = Ayarlar.cihazAnahtari(this);
            arka.execute(() -> {
                try { Api.post(adres, "/api/aile/cihaz/sil", new JSONObject(), null, anahtar); } catch (Exception ignored) { }
                ana.post(() -> {
                    IzlemeServisi.durdur(this);
                    Kuyruk.temizle(this);
                    Ayarlar.cik(this);
                    ciz();
                });
            });
        });
    }

    private boolean konumIzni() {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean arkaKonumIzni() {
        return Build.VERSION.SDK_INT < 29
            || checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /** Bağlanınca izinler sırayla istenir: önce konum. */
    private void izinIste() {
        if (!konumIzni()) requestPermissions(new String[] {
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION }, IZIN_KONUM);
    }

    @Override
    public void onRequestPermissionsResult(int kod, String[] izinler, int[] sonuclar) {
        super.onRequestPermissionsResult(kod, izinler, sonuclar);
        if (kod == IZIN_KONUM && konumIzni()) IzlemeServisi.baslat(this);
        ciz();
    }

    /* ---------------- küçük arayüz yardımcıları ---------------- */
    private int dp(int d) { return Math.round(d * getResources().getDisplayMetrics().density); }

    private LinearLayout.LayoutParams bosluk(int ust) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = ust;
        return lp;
    }

    private void baslik(String s) {
        TextView t = yazi(s, ANA, 26);
        t.setTypeface(Typeface.SERIF, Typeface.BOLD);
    }

    private void altBaslik(String s) {
        TextView t = yazi(s, YAZI, 18);
        t.setTypeface(null, Typeface.BOLD);
        ((LinearLayout.LayoutParams) t.getLayoutParams()).topMargin = dp(22);
    }

    private TextView yazi(String s, int renk, int boy) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextColor(renk);
        t.setTextSize(boy);
        t.setLineSpacing(0, 1.2f);
        govde.addView(t, bosluk(dp(8)));
        return t;
    }

    private EditText alan(String etiket, String deger, int tur) {
        yazi(etiket, SOLUK, 13);
        EditText e = new EditText(this);
        e.setSingleLine(true);   // önce: tek satır ayarı şifre gizlemeyi silmesin
        e.setInputType(tur);
        if ((tur & InputType.TYPE_TEXT_VARIATION_PASSWORD) != 0) e.setTransformationMethod(PasswordTransformationMethod.getInstance());
        e.setText(deger);
        e.setTextColor(YAZI);
        govde.addView(e, bosluk(0));
        return e;
    }

    private Button dugme(String s, boolean birincil) {
        Button b = new Button(this);
        b.setText(s);
        b.setAllCaps(false);
        b.setTextSize(16);
        b.setMinHeight(dp(48));
        GradientDrawable z = new GradientDrawable();
        z.setCornerRadius(dp(10));
        z.setColor(birincil ? ANA : Color.parseColor("#F1E6E4"));
        b.setBackground(z);
        b.setTextColor(birincil ? Color.WHITE : YAZI);
        govde.addView(b, bosluk(dp(14)));
        return b;
    }

    private void izinSatiri(String ad, boolean var, String dugmeYazi, View.OnClickListener tik) {
        LinearLayout satir = new LinearLayout(this);
        satir.setOrientation(LinearLayout.VERTICAL);
        satir.setPadding(dp(14), dp(12), dp(14), dp(12));
        GradientDrawable z = new GradientDrawable();
        z.setCornerRadius(dp(10));
        z.setColor(Color.WHITE);
        z.setStroke(dp(1), Color.parseColor("#E7DCDA"));
        satir.setBackground(z);
        TextView t = new TextView(this);
        t.setText((var ? "✓  " : "•  ") + ad + (var ? "" : " — kapalı"));
        t.setTextColor(var ? Color.parseColor("#2F7D32") : YAZI);
        t.setTextSize(15);
        satir.addView(t);
        if (!var) {
            Button b = new Button(this);
            b.setText(dugmeYazi);
            b.setAllCaps(false);
            b.setOnClickListener(tik);
            b.setGravity(Gravity.CENTER);
            satir.addView(b);
        }
        govde.addView(satir, bosluk(dp(8)));
    }

    @Override
    protected void onDestroy() {
        arka.shutdownNow();
        super.onDestroy();
    }
}
