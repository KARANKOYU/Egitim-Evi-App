package org.egitimevi.aile;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Uygulamanın tek ekranı. Üstte başlık çubuğu (geri ya da hesap düğmesi, başlık,
 * yenile, bildirimler), ortada sayfa, altta kişinin rolüne göre sekmeler.
 * Her sekmenin kendi sayfa yığını vardır; geri tuşu yığından çıkarır.
 *
 * Akış: oturum yoksa giriş; oturum varsa aydınlatma metni onayı ve zorunlu şifre
 * değişikliği bittikten sonra rolün sekmeleri. Oturum düşerse (401) giriş ekranı.
 */
public class AnaEkran extends Activity {
    static final String BAGLANTI = "baglanti";
    static volatile boolean onde;

    /** Alt çubukta bir sekme: adı, simgesi ve kök sayfası. */
    public static final class Sekme {
        final String ad;
        final int simge;
        final Supplier<Sayfa> kok;

        public Sekme(String ad, int simge, Supplier<Sayfa> kok) { this.ad = ad; this.simge = simge; this.kok = kok; }
    }

    private static final Object MESAJ = new Object();
    private final Handler ana = new Handler(Looper.getMainLooper());
    private LinearLayout ustCubuk, altCubuk;
    private FrameLayout icerik;
    private TextView baslikYazi, rozet, mesaj;
    private FrameLayout geriDugme, yenileDugme, bildirimDugme, hesapDugme;

    private final List<Sekme> sekmeler = new ArrayList<>();
    private final List<List<Sayfa>> yiginlar = new ArrayList<>();
    private final List<Sayfa> girisYigini = new ArrayList<>();
    private boolean girisKipi = true;
    private int aktif;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        iskeletKur();
        akisiBaslat();
        bildirimdenAc(getIntent());
        /* Android 13 ve üstünde geri hareketi bu çağrıyla gelir; eskilerde onBackPressed. */
        if (Build.VERSION.SDK_INT >= 33) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT, this::geri);
        }
    }

    /* ======================= iskelet ======================= */

    private void iskeletKur() {
        FrameLayout kok = new FrameLayout(this);
        kok.setBackgroundColor(Tema.renk(this, R.color.zemin));
        LinearLayout duzen = Arayuz.dikey(this);
        kok.addView(duzen, new FrameLayout.LayoutParams(-1, -1));

        ustCubuk = Arayuz.yatay(this);
        int yp = Tema.dp(this, 6);
        ustCubuk.setPadding(yp, yp, yp, yp);
        ustCubuk.setMinimumHeight(Tema.dp(this, 60));
        geriDugme = Arayuz.simgeDugme(this, R.drawable.ik_geri, "Geri");
        geriDugme.setOnClickListener(v -> geri());
        ustCubuk.addView(geriDugme);
        hesapDugme = new FrameLayout(this);
        hesapDugme.setLayoutParams(new LinearLayout.LayoutParams(Tema.dp(this, 44), Tema.dp(this, 44)));
        hesapDugme.setClickable(true);
        hesapDugme.setFocusable(true);
        hesapDugme.setOnClickListener(v -> hesapMenusu());
        ustCubuk.addView(hesapDugme);
        baslikYazi = Arayuz.yazi(this, "", 20, R.color.yazi);
        baslikYazi.setTypeface(Tema.BASLIK);
        baslikYazi.setSingleLine(true);
        baslikYazi.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(0, -2, 1f);
        blp.leftMargin = Tema.dp(this, 8);
        ustCubuk.addView(baslikYazi, blp);
        yenileDugme = Arayuz.simgeDugme(this, R.drawable.ust_yenile, "Yenile");
        yenileDugme.setOnClickListener(v -> { Sayfa s = ust(); if (s != null) s.yenile(); });
        ustCubuk.addView(yenileDugme);
        bildirimDugme = Arayuz.simgeDugme(this, R.drawable.ik_bildirim, "Bildirimler");
        rozet = Arayuz.yazi(this, "", 11, R.color.ustune_yazi);
        rozet.setTypeface(Tema.KALIN);
        rozet.setGravity(Gravity.CENTER);
        rozet.setBackground(Tema.zemin(this, Tema.renk(this, R.color.ana), 99, Tema.renk(this, R.color.zemin), 2));
        rozet.setPadding(Tema.dp(this, 5), 0, Tema.dp(this, 5), 0);
        rozet.setMinWidth(Tema.dp(this, 20));
        rozet.setVisibility(View.GONE);
        FrameLayout.LayoutParams rlp = new FrameLayout.LayoutParams(-2, Tema.dp(this, 20), Gravity.TOP | Gravity.END);
        rlp.topMargin = Tema.dp(this, 3);
        rlp.rightMargin = Tema.dp(this, 1);
        bildirimDugme.addView(rozet, rlp);
        bildirimDugme.setOnClickListener(v -> git(new BildirimlerSayfasi()));
        ustCubuk.addView(bildirimDugme);
        duzen.addView(ustCubuk, new LinearLayout.LayoutParams(-1, -2));

        icerik = new FrameLayout(this);
        duzen.addView(icerik, new LinearLayout.LayoutParams(-1, 0, 1f));

        altCubuk = Arayuz.yatay(this);
        altCubuk.setBackgroundColor(Tema.renk(this, R.color.kart));
        altCubuk.setElevation(Tema.dp(this, 8));
        altCubuk.setPadding(0, Tema.dp(this, 6), 0, Tema.dp(this, 6));
        duzen.addView(altCubuk, new LinearLayout.LayoutParams(-1, -2));

        mesaj = Arayuz.yazi(this, "", 14, R.color.zemin);
        mesaj.setBackground(Tema.zemin(this, Tema.renk(this, R.color.yazi), Tema.R_KUCUK, 0, 0));
        int mp = Tema.dp(this, 14);
        mesaj.setPadding(mp, Tema.dp(this, 12), mp, Tema.dp(this, 12));
        mesaj.setVisibility(View.GONE);
        mesaj.setElevation(Tema.dp(this, 10));
        FrameLayout.LayoutParams mlp = new FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM);
        mlp.setMargins(Tema.dp(this, 16), 0, Tema.dp(this, 16), Tema.dp(this, 96));
        kok.addView(mesaj, mlp);

        setContentView(kok);
        kok.setOnApplyWindowInsetsListener((v, ic) -> {
            int[] b = cubukBosluklari(ic);
            duzen.setPadding(0, b[0], 0, b[1]);
            return ic;
        });
    }

    /** Durum ve gezinme çubuklarının (ve klavyenin) kapladığı üst ve alt boşluk. */
    @SuppressWarnings("deprecation")
    private static int[] cubukBosluklari(WindowInsets ic) {
        if (Build.VERSION.SDK_INT >= 30) {
            android.graphics.Insets s = ic.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.ime());
            return new int[] { s.top, s.bottom };
        }
        return new int[] { ic.getSystemWindowInsetTop(), ic.getSystemWindowInsetBottom() };
    }

    /* ======================= akış ======================= */

    /** Oturumun durumuna göre doğru yerden başlar. */
    void akisiBaslat() {
        if (!Oturum.acik(this)) { girisGoster(); return; }
        if (!Oturum.kvkkGuncel(this)) { tekSayfa(new KvkkSayfasi()); return; }
        if (Oturum.kisi(this).optBoolean("sifreDegismeli")) { tekSayfa(new SifreSayfasi(true)); return; }
        uygulamayiKur();
    }

    void girisGoster() { tekSayfa(new GirisSayfasi()); }

    /** Giriş, onay ve zorunlu şifre gibi çubuksuz akış: kendi yığınıyla. */
    void tekSayfa(Sayfa s) {
        girisKipi = true;
        girisYigini.clear();
        girisYigini.add(s);
        goster(s, true);
    }

    /** Giriş ya da rol değişiminden sonra sunucunun oturum cevabı. */
    void oturumAc(JSONObject cevap) {
        Oturum.yaz(this, cevap);
        akisiBaslat();
    }

    private void uygulamayiKur() {
        girisKipi = false;
        girisYigini.clear();
        sekmeler.clear();
        yiginlar.clear();
        sekmeler.addAll(Sekmeler.icin(this));
        for (int i = 0; i < sekmeler.size(); i++) yiginlar.add(new ArrayList<>());
        aktif = 0;
        altCubukCiz();
        Sayfa kok = sekmeler.get(0).kok.get();
        yiginlar.get(0).add(kok);
        goster(kok, true);
        hesapDugmesiCiz();
        rozetTazele();
        cihazKaydet();
        Bildirimler.zamanla(this);
    }

    /** Hesap bilgisi değişti (portal, çocuk seçimi): sekmeler baştan kurulur. */
    public void yenidenKur() { uygulamayiKur(); }

    /* ======================= gezinme ======================= */

    private List<Sayfa> yigin() { return girisKipi ? girisYigini : yiginlar.get(aktif); }

    Sayfa ust() {
        List<Sayfa> y = girisKipi ? girisYigini : (yiginlar.isEmpty() ? null : yiginlar.get(aktif));
        return y == null || y.isEmpty() ? null : y.get(y.size() - 1);
    }

    /** Yeni sayfayı üste koyar. */
    public void git(Sayfa s) {
        yigin().add(s);
        goster(s, true);
    }

    /** Üstteki sayfayı kapatır (ör. kaydettikten sonra). */
    public void kapat() {
        List<Sayfa> y = yigin();
        if (y.size() > 1) {
            y.remove(y.size() - 1);
            Sayfa s = y.get(y.size() - 1);
            goster(s, false);
            s.gorundu();
        }
    }

    void geri() {
        Sayfa s = ust();
        if (s != null && s.geriBas()) return;
        if (yigin().size() > 1) { kapat(); return; }
        if (!girisKipi && aktif != 0) { sekmeSec(0); return; }
        finish();
    }

    void sekmeSec(int i) {
        if (girisKipi || i < 0 || i >= sekmeler.size()) return;
        List<Sayfa> y = yiginlar.get(i);
        if (i == aktif) while (y.size() > 1) y.remove(y.size() - 1);
        aktif = i;
        if (y.isEmpty()) y.add(sekmeler.get(i).kok.get());
        goster(y.get(y.size() - 1), false);
        altCubukCiz();
    }

    /** Sayfanın görünümünü baştan kurar (Sayfa.yenile varsayılanı). */
    void sayfayiYenile(Sayfa s) {
        s.gorunum = null;
        if (s == ust()) goster(s, false);
    }

    private void goster(Sayfa s, boolean ileri) {
        s.e = this;
        if (s.gorunum == null) s.gorunum = s.olustur();
        if (s.gorunum.getParent() instanceof FrameLayout) ((FrameLayout) s.gorunum.getParent()).removeView(s.gorunum);
        icerik.removeAllViews();
        icerik.addView(s.gorunum, new FrameLayout.LayoutParams(-1, -1));
        s.gorunum.setAlpha(0f);
        s.gorunum.setTranslationX(Tema.dp(this, ileri ? 28 : -28));
        s.gorunum.animate().alpha(1f).translationX(0).setDuration(220).setInterpolator(new DecelerateInterpolator(1.6f)).start();

        boolean cubuksuz = girisKipi || s.cubuksuz();
        ustCubuk.setVisibility(cubuksuz ? View.GONE : View.VISIBLE);
        altCubuk.setVisibility(cubuksuz || sekmeler.size() < 2 ? View.GONE : View.VISIBLE);
        baslikYazi.setText(s.baslik());
        boolean derin = yigin().size() > 1;
        geriDugme.setVisibility(derin ? View.VISIBLE : View.GONE);
        hesapDugme.setVisibility(derin ? View.GONE : View.VISIBLE);
        yenileDugme.setVisibility(s.yenilenir() ? View.VISIBLE : View.GONE);
        bildirimDugme.setVisibility(s instanceof BildirimlerSayfasi ? View.GONE : View.VISIBLE);
        setTitle(s.baslik());
    }

    private void altCubukCiz() {
        altCubuk.removeAllViews();
        for (int i = 0; i < sekmeler.size(); i++) {
            final int no = i;
            Sekme sk = sekmeler.get(i);
            boolean secili = i == aktif;
            LinearLayout k = Arayuz.dikey(this);
            k.setGravity(Gravity.CENTER_HORIZONTAL);
            k.setPadding(0, Tema.dp(this, 4), 0, Tema.dp(this, 4));
            FrameLayout hap = new FrameLayout(this);
            if (secili) hap.setBackground(Tema.zemin(this, Tema.renk(this, R.color.ana_acik), 99, 0, 0));
            hap.addView(Arayuz.ikon(this, sk.simge, secili ? R.color.ana : R.color.soluk, 22),
                new FrameLayout.LayoutParams(Tema.dp(this, 22), Tema.dp(this, 22), Gravity.CENTER));
            k.addView(hap, new LinearLayout.LayoutParams(Tema.dp(this, 60), Tema.dp(this, 32)));
            TextView ad = Arayuz.yazi(this, sk.ad, 12, secili ? R.color.ana : R.color.soluk);
            ad.setTypeface(secili ? Tema.KALIN : Tema.ORTA);
            ad.setSingleLine(true);
            ad.setGravity(Gravity.CENTER_HORIZONTAL);
            Arayuz.ekle(k, ad, 3);
            k.setClickable(true);
            k.setFocusable(true);
            k.setContentDescription(sk.ad + (secili ? ", seçili" : ""));
            k.setOnClickListener(v -> sekmeSec(no));
            altCubuk.addView(k, new LinearLayout.LayoutParams(0, -2, 1f));
        }
    }

    /** Sol üstte kişinin avatarı: portallar (yetişkin) ya da ayarlar. */
    private void hesapDugmesiCiz() {
        hesapDugme.removeAllViews();
        JSONObject k = Oturum.kisi(this);
        TextView a = Arayuz.avatar(this, k.optString("fullName"), k.optString("id"), 36);
        hesapDugme.addView(a, new FrameLayout.LayoutParams(Tema.dp(this, 36), Tema.dp(this, 36), Gravity.CENTER));
        hesapDugme.setContentDescription("Hesabım ve portallarım");
    }

    private void hesapMenusu() {
        JSONObject k = Oturum.kisi(this);
        if (k.optBoolean("yetiskin") || k.optBoolean("rolSatiri")) PortalSecici.goster(this);
        else git(new AyarlarSayfasi());
    }

    /* ======================= genel hatalar ve mesaj ======================= */

    /** Oturumla ilgili genel durumlar burada karşılanır; true ise ekran bir şey yapmasın. */
    boolean genelHata(Api.Hata h) {
        JSONObject g = h.govde;
        if (h.durum == 401 && Oturum.acik(this)) {
            oturumuBitir();
            bildir("Oturumunun süresi doldu. Yeniden giriş yap.");
            return true;
        }
        if (g != null && g.optBoolean("kvkkGerek")) {
            try { Oturum.tazele(this, new JSONObject().put("kvkkGuncel", false)); } catch (Exception ignored) { }
            tekSayfa(new KvkkSayfasi());
            return true;
        }
        if (g != null && g.optBoolean("sifreDegismeli")) {
            tekSayfa(new SifreSayfasi(true));
            return true;
        }
        return false;
    }

    /** Alt tarafta birkaç saniye görünen kısa mesaj. */
    public void bildir(CharSequence s) {
        if (TextUtils.isEmpty(s)) return;
        mesaj.setText(s);
        mesaj.setVisibility(View.VISIBLE);
        mesaj.setAlpha(0f);
        mesaj.animate().alpha(1f).setDuration(160).start();
        mesaj.announceForAccessibility(s);
        ana.removeCallbacksAndMessages(MESAJ);
        ana.postAtTime(() -> mesaj.animate().alpha(0f).setDuration(200).withEndAction(() -> mesaj.setVisibility(View.GONE)).start(),
            MESAJ, android.os.SystemClock.uptimeMillis() + 3800);
    }

    /* ======================= oturum ======================= */

    /** Çıkış: uygulama anahtarı ve oturum sunucuda kapatılır, telefonda silinir. */
    public void cikis() {
        final String sunucu = Ayarlar.sunucu(this), oturum = Oturum.anahtar(this), cihaz = UygulamaAyar.anahtar(this);
        Ag.arkada(() -> {
            try { if (!cihaz.isEmpty()) Api.uygulama(sunucu, "/api/cihaz/sil", new JSONObject(), cihaz); } catch (Exception ignored) { }
            try { Api.oturumla(sunucu, "/api/logout", "POST", new JSONObject(), oturum); } catch (Exception ignored) { }
        });
        oturumuBitir();
    }

    private void oturumuBitir() {
        SeferServisi.durdur(this);
        Bildirimler.iptal(this);
        UygulamaAyar.cik(this);
        Oturum.kapat(this);
        sekmeler.clear();
        yiginlar.clear();
        rozet.setVisibility(View.GONE);
        girisGoster();
    }

    /** Bildirimleri telefona getirmek için uygulama anahtarı (sunucu destekliyorsa). */
    private void cihazKaydet() {
        if (UygulamaAyar.anahtarVar(this)) return;
        JSONObject g = new JSONObject();
        try {
            g.put("ad", Build.MANUFACTURER + " " + Build.MODEL).put("platform", "android")
                .put("surum", getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (Exception ignored) { }
        Ag.post(this, "/api/cihaz", g, j -> {
            String a = j.optString("cihazAnahtari", "");
            if (a.matches("^[0-9a-f]{64}$")) {
                UygulamaAyar.anahtarYaz(this, a);
                Bildirimler.zamanla(this);
                bildirimIzniIste();
            }
        }, h -> { /* sunucu bu ucu bilmiyorsa telefon bildirimi olmaz; uygulama yine çalışır */ });
    }

    private void bildirimIzniIste() {
        Bildirimler.kanalKur(this);
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
            != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] { android.Manifest.permission.POST_NOTIFICATIONS }, 13);
        }
    }

    /** Üst çubuktaki zil: okunmamış bildirim sayısı. */
    void rozetTazele() {
        if (girisKipi || !Oturum.acik(this)) return;
        Ag.get(this, "/api/notifications", j -> {
            int n = j.optInt("unread", 0);
            rozet.setText(n > 99 ? "99+" : String.valueOf(n));
            rozet.setVisibility(n > 0 ? View.VISIBLE : View.GONE);
            bildirimDugme.setContentDescription(n > 0 ? "Bildirimler, " + n + " okunmamış" : "Bildirimler");
        }, h -> { });
    }

    /** Bildirim çubuğundan gelindiyse bildirimler sayfası açılır. */
    private void bildirimdenAc(Intent i) {
        if (i == null || i.getStringExtra(BAGLANTI) == null || girisKipi) return;
        git(new BildirimlerSayfasi());
    }

    @Override
    protected void onNewIntent(Intent i) {
        super.onNewIntent(i);
        setIntent(i);
        bildirimdenAc(i);
    }

    @Override
    protected void onResume() {
        super.onResume();
        onde = true;
        if (!girisKipi) rozetTazele();
    }

    @Override
    protected void onPause() {
        onde = false;
        super.onPause();
    }

    /* Android 12 ve altı (13 ve üstünde onCreate'te kaydedilen geri çağrısı çalışır). */
    @SuppressWarnings("deprecation")
    @android.annotation.SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() { geri(); }
}
