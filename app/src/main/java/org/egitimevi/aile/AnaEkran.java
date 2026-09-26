package org.egitimevi.aile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.webkit.GeolocationPermissions;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Uygulamanın ana ekranı: Eğitim Evi sitesi (WebView). Müdür, öğretmen, veli,
 * öğrenci ve servisçi aynı sayfaları kullanır; telefona özgü işler yerel kodda kalır:
 * servisçinin sefer konumu (SeferServisi), bildirim yoklaması (Bildirimler), çocuğun
 * telefonu (AileEkrani), dosya seçme/indirme ve konum izni.
 *
 * Güvenlik: yalnızca uygulamanın sunucusu (aynı köken) içeride açılır; başka her
 * adres (harita, GitHub, tel:, mailto:) telefonun kendi uygulamasına gider. Köprü
 * (window.EgitimEviUygulama) yalnızca kendi kökendeki sayfada çalışır.
 */
public class AnaEkran extends Activity {
    static final String BAGLANTI = "baglanti";
    static volatile boolean onde;

    private static final int DOSYA_SEC = 10, IZIN_KONUM_SAYFA = 11, IZIN_KONUM_SEFER = 12, IZIN_BILDIRIM = 13;
    private static final int ZEMIN = Color.parseColor("#FFF9F5");

    volatile boolean sayfaGuvenli;
    private boolean hataVar;
    private WebView web;
    private LinearLayout hataKutusu;
    private Uri kok;
    private ValueCallback<Uri[]> dosyaGeri;
    private GeolocationPermissions.Callback konumGeri;
    private String konumKoken, bekleyenSefer;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        if (hataAyiklanabilir() && !UygulamaAyar.sunucuSecildi(this)) sunucuSor();
        else kur();
    }

    private boolean hataAyiklanabilir() {
        return (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    /** Yalnızca deneme paketinde: hangi sunucuya bağlanılacağı sorulur (öykünücü: 10.0.2.2). */
    private void sunucuSor() {
        EditText e = new EditText(this);
        e.setSingleLine(true);
        e.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
        e.setText(Ayarlar.sunucu(this).equals(Ayarlar.VARSAYILAN_SUNUCU) ? "http://10.0.2.2:3200" : Ayarlar.sunucu(this));
        new AlertDialog.Builder(this)
            .setTitle("Deneme sunucusu")
            .setView(e)
            .setCancelable(false)
            .setPositiveButton("Bağlan", (d, w) -> {
                String adres = e.getText().toString().trim().replaceAll("/+$", "");
                String sorun = Api.adresSorunu(adres);
                if (sorun != null) { Toast.makeText(this, sorun, Toast.LENGTH_LONG).show(); sunucuSor(); return; }
                Ayarlar.sunucuYaz(this, adres);
                UygulamaAyar.sunucuSecildi(this, true);
                kur();
            })
            .show();
    }

    /* Eski sürümlerin çubuk ve kenar boşluğu arayüzleri sürüm denetimiyle kullanılıyor.
       JavaScript açık: site onsuz çalışmaz; yalnız kendi sunucumuz içeride açılır. */
    @SuppressWarnings("deprecation")
    @SuppressLint("SetJavaScriptEnabled")
    private void kur() {
        kok = Uri.parse(Ayarlar.sunucu(this));
        FrameLayout cerceve = new FrameLayout(this);
        cerceve.setBackgroundColor(ZEMIN);
        web = new WebView(this);
        cerceve.addView(web, new FrameLayout.LayoutParams(-1, -1));
        hataKutusu = hataKutusuKur();
        cerceve.addView(hataKutusu, new FrameLayout.LayoutParams(-1, -1));
        setContentView(cerceve);

        if (Build.VERSION.SDK_INT < 35) {
            getWindow().setStatusBarColor(ZEMIN);
            getWindow().setNavigationBarColor(ZEMIN);
        }
        if (Build.VERSION.SDK_INT >= 30) {
            int a = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
            getWindow().getInsetsController().setSystemBarsAppearance(a, a);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        cerceve.setOnApplyWindowInsetsListener((v, ic) -> {
            int ust, alt;
            if (Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets s = ic.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.ime());
                ust = s.top; alt = s.bottom;
            } else {
                ust = ic.getSystemWindowInsetTop(); alt = ic.getSystemWindowInsetBottom();
            }
            cerceve.setPadding(0, ust, 0, alt);
            return ic;
        });

        WebView.setWebContentsDebuggingEnabled(hataAyiklanabilir());
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setGeolocationEnabled(true);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        s.setSupportMultipleWindows(false);
        s.setUserAgentString(s.getUserAgentString() + " EgitimEviUygulama/" + new Kopru(this).surum());
        web.addJavascriptInterface(new Kopru(this), "EgitimEviUygulama");
        web.setWebViewClient(new Istemci());
        web.setWebChromeClient(new Krom());
        web.setDownloadListener((adres, ua, bicim, tur, boy) -> indir(adres, bicim, tur));
        web.loadUrl(adresi(getIntent()));
    }

    /** Açılış adresi: bildirimden gelindiyse o sayfa, yoksa sitenin kökü. */
    private String adresi(Intent i) {
        String taban = kok.toString().replaceAll("/+$", "");
        String b = i == null ? null : i.getStringExtra(BAGLANTI);
        if (b != null && b.matches("^(/[A-Za-z0-9._~/?=&%-]*)?(#/[A-Za-z0-9._~/?=&%-]*)?$") && !b.isEmpty()) {
            return taban + (b.startsWith("#") ? "/" + b : b);
        }
        return taban + "/";
    }

    @Override
    protected void onNewIntent(Intent i) {
        super.onNewIntent(i);
        setIntent(i);
        if (web != null && i.getStringExtra(BAGLANTI) != null) web.loadUrl(adresi(i));
    }

    boolean kendiKokeni(Uri u) {
        if (u == null || kok == null) return false;
        String s = u.getScheme();
        return s != null && s.equals(kok.getScheme()) && u.getHost() != null && u.getHost().equalsIgnoreCase(kok.getHost())
            && port(u) == port(kok);
    }

    private static int port(Uri u) {
        if (u.getPort() != -1) return u.getPort();
        return "https".equals(u.getScheme()) ? 443 : 80;
    }

    private void disariAc(Uri u) {
        try { startActivity(new Intent(Intent.ACTION_VIEW, u).addCategory(Intent.CATEGORY_BROWSABLE)); }
        catch (ActivityNotFoundException e) { Toast.makeText(this, "Bu bağlantıyı açacak uygulama yok.", Toast.LENGTH_SHORT).show(); }
    }

    private final class Istemci extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
            Uri u = r.getUrl();
            if (kendiKokeni(u)) return false;
            disariAc(u);
            return true;
        }

        @Override
        public void onPageStarted(WebView v, String adres, android.graphics.Bitmap simge) {
            sayfaGuvenli = kendiKokeni(Uri.parse(adres));
            hataVar = false;
        }

        @Override
        public void doUpdateVisitedHistory(WebView v, String adres, boolean yenileme) {
            sayfaGuvenli = kendiKokeni(Uri.parse(adres));
        }

        @Override
        public void onPageFinished(WebView v, String adres) {
            if (!hataVar) hataKutusu.setVisibility(View.GONE);
        }

        @Override
        public void onReceivedError(WebView v, WebResourceRequest r, WebResourceError e) {
            if (r.isForMainFrame()) { hataVar = true; hataKutusu.setVisibility(View.VISIBLE); }
        }
    }

    private final class Krom extends WebChromeClient {
        @Override
        public void onGeolocationPermissionsShowPrompt(String koken, GeolocationPermissions.Callback geri) {
            if (!kendiKokeni(Uri.parse(koken))) { geri.invoke(koken, false, false); return; }
            if (konumIzni()) { geri.invoke(koken, true, false); return; }
            konumGeri = geri;
            konumKoken = koken;
            requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION },
                IZIN_KONUM_SAYFA);
        }

        @Override
        public boolean onShowFileChooser(WebView v, ValueCallback<Uri[]> geri, FileChooserParams p) {
            if (dosyaGeri != null) dosyaGeri.onReceiveValue(null);
            dosyaGeri = geri;
            Intent i = p.createIntent();
            if (p.getMode() == FileChooserParams.MODE_OPEN_MULTIPLE) i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            try {
                startActivityForResult(Intent.createChooser(i, "Dosya seç"), DOSYA_SEC);
            } catch (ActivityNotFoundException e) {
                dosyaGeri = null;
                geri.onReceiveValue(null);
                return false;
            }
            return true;
        }
    }

    /** Sitenin bilet adresli indirmeleri (ör. /api/ek/indir?bilet=...) telefonun indiricisiyle. */
    private void indir(String adres, String bicim, String tur) {
        Uri u = Uri.parse(adres);
        if (!kendiKokeni(u)) { disariAc(u); return; }
        String ad = Kopru.temizAd(URLUtil.guessFileName(adres, bicim, tur));
        DownloadManager.Request r = new DownloadManager.Request(u)
            .setTitle(ad)
            .setMimeType(tur)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        if (Build.VERSION.SDK_INT >= 29) r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Eğitim Evi/" + ad);
        else r.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, ad);
        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        if (dm == null) return;
        dm.enqueue(r);
        Toast.makeText(this, "İndiriliyor: " + ad, Toast.LENGTH_SHORT).show();
    }

    /** Servisçi seferi başlattı (köprüden): konum izni varsa ön plan servisi hemen açılır. */
    void seferBaslat(String seferId) {
        if (konumIzni()) { SeferServisi.baslat(this, seferId); return; }
        bekleyenSefer = seferId;
        requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION },
            IZIN_KONUM_SEFER);
    }

    void bildirimIzniIste() {
        Bildirimler.kanalKur(this);
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] { Manifest.permission.POST_NOTIFICATIONS }, IZIN_BILDIRIM);
        }
    }

    private boolean konumIzni() {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int kod, String[] izinler, int[] sonuclar) {
        super.onRequestPermissionsResult(kod, izinler, sonuclar);
        if (kod == IZIN_KONUM_SAYFA && konumGeri != null) {
            konumGeri.invoke(konumKoken, konumIzni(), false);
            konumGeri = null;
        } else if (kod == IZIN_KONUM_SEFER && bekleyenSefer != null) {
            if (konumIzni()) SeferServisi.baslat(this, bekleyenSefer);
            else Toast.makeText(this, "Konum izni verilmeden servisin yeri velilere gönderilemez.", Toast.LENGTH_LONG).show();
            bekleyenSefer = null;
        }
    }

    @Override
    protected void onActivityResult(int kod, int sonuc, Intent veri) {
        super.onActivityResult(kod, sonuc, veri);
        if (kod != DOSYA_SEC || dosyaGeri == null) return;
        Uri[] secilen = null;
        if (sonuc == RESULT_OK && veri != null) {
            if (veri.getClipData() != null) {
                int n = veri.getClipData().getItemCount();
                secilen = new Uri[n];
                for (int i = 0; i < n; i++) secilen[i] = veri.getClipData().getItemAt(i).getUri();
            } else if (veri.getData() != null) {
                secilen = new Uri[] { veri.getData() };
            }
        }
        dosyaGeri.onReceiveValue(secilen);
        dosyaGeri = null;
    }

    @Override
    public boolean onKeyDown(int tus, KeyEvent o) {
        if (tus == KeyEvent.KEYCODE_BACK && web != null && web.canGoBack()) {
            web.goBack();
            return true;
        }
        return super.onKeyDown(tus, o);
    }

    @Override
    protected void onResume() {
        super.onResume();
        onde = true;
        /* Sayfa "görünür" olur: quizde sekme değiştirme bu olaylarla anlaşılır. */
        if (web != null) web.onResume();
        Bildirimler.zamanla(this);
    }

    @Override
    protected void onPause() {
        onde = false;
        if (web != null) web.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (web != null) web.destroy();
        super.onDestroy();
    }

    private LinearLayout hataKutusuKur() {
        LinearLayout k = new LinearLayout(this);
        k.setOrientation(LinearLayout.VERTICAL);
        k.setGravity(Gravity.CENTER);
        k.setBackgroundColor(ZEMIN);
        int p = Math.round(24 * getResources().getDisplayMetrics().density);
        k.setPadding(p, p, p, p);
        TextView t = new TextView(this);
        t.setText("Eğitim Evi'ne ulaşılamadı. İnternet bağlantını kontrol edip yeniden dene.");
        t.setTextColor(Color.parseColor("#23191A"));
        t.setTextSize(17);
        t.setGravity(Gravity.CENTER);
        k.addView(t);
        Button d = new Button(this);
        d.setText("Yeniden dene");
        d.setAllCaps(false);
        d.setOnClickListener(v -> { if (web != null) web.reload(); });
        k.addView(d);
        k.setVisibility(View.GONE);
        return k;
    }
}
