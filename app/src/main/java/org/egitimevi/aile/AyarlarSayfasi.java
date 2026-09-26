package org.egitimevi.aile;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONObject;

/**
 * Ayarlar: hesap bilgisi, kişi kodu (öğrencide veli kodu), portallar, bildirim izni,
 * şifre, çocuğun telefonu (öğrenci), site bağlantıları, sürüm ve çıkış.
 */
public class AyarlarSayfasi extends Sayfa {
    @Override public String baslik() { return "Ayarlar"; }

    @Override
    protected View olustur() {
        ScrollView[] s = new ScrollView[1];
        LinearLayout g = Arayuz.sayfaGovdesi(e, s);
        JSONObject k = Oturum.kisi(e);
        String rol = k.optString("role");

        LinearLayout profil = Arayuz.kart(e);
        LinearLayout ust = Arayuz.yatay(e);
        ust.addView(Arayuz.avatar(e, k.optString("fullName"), k.optString("id"), 56));
        LinearLayout ad = Arayuz.dikey(e);
        ad.setPadding(Tema.dp(e, 14), 0, 0, 0);
        TextView isim = Arayuz.altBaslik(e, k.optString("fullName"));
        isim.setTextSize(19);
        ad.addView(isim);
        String rolAdi = AnaSayfa.rolAdi(rol);
        String okul = k.optString("schoolName");
        ad.addView(Arayuz.soluk(e, (rolAdi.isEmpty() ? "Hesabım" : rolAdi) + (okul.isEmpty() ? "" : " · " + okul)));
        String kimlik = k.optString("email").isEmpty() ? k.optString("username") : k.optString("email");
        if (!kimlik.isEmpty()) ad.addView(Arayuz.soluk(e, kimlik));
        ust.addView(ad, new LinearLayout.LayoutParams(0, -2, 1f));
        profil.addView(ust);
        g.addView(profil);

        /* Öğrencinin veli kodu: velisiyle paylaşır. */
        if ("student".equals(rol) && !k.optString("code").isEmpty()) {
            Arayuz.ekle(g, Arayuz.bolumEtiketi(e, "Veli kodun"), 22);
            LinearLayout kart = Arayuz.kart(e);
            kart.addView(Arayuz.yazi(e, "Velin bu kodla seni Eğitim Evi'nde ekler. Kodu yalnızca velinle paylaş.", 14, R.color.soluk));
            Arayuz.ekle(kart, KisiKodu.kutu(e, k.optString("code"), () -> e.bildir("Kod kopyalandı.")), 12);
            Arayuz.ekle(g, kart, 8);
        }

        Arayuz.ekle(g, Arayuz.bolumEtiketi(e, "Hesap"), 22);
        LinearLayout hesap = Arayuz.kart(e);
        hesap.setPadding(0, Tema.dp(e, 4), 0, Tema.dp(e, 4));
        boolean yetiskin = k.optBoolean("yetiskin") || k.optBoolean("rolSatiri");
        if (yetiskin) {
            hesap.addView(satir(R.drawable.ik_grup, "Portallarım", "Veli, öğretmen ve müdür portalların arasında geç", v -> PortalSecici.goster(e)));
            hesap.addView(Arayuz.ayirici(e));
            hesap.addView(satir(R.drawable.ik_ekle, "Ekle", "Çocuğunu ekle, öğretmen olarak katıl ya da okulunu açtır", v -> e.git(new EkleSayfasi())));
            hesap.addView(Arayuz.ayirici(e));
        }
        hesap.addView(satir(R.drawable.ik_kilit, "Şifre değiştir", null, v -> e.git(new SifreSayfasi(false))));
        Arayuz.ekle(g, hesap, 8);

        Arayuz.ekle(g, Arayuz.bolumEtiketi(e, "Telefon"), 22);
        LinearLayout tel = Arayuz.kart(e);
        tel.setPadding(0, Tema.dp(e, 4), 0, Tema.dp(e, 4));
        boolean izin = Build.VERSION.SDK_INT < 33
            || e.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        tel.addView(satir(R.drawable.ik_bildirim, "Telefon bildirimleri",
            izin ? (UygulamaAyar.anahtarVar(e) ? "Açık: 15 dakikada bir, servis saatlerinde dakikada bir bakılır" : "Açık")
                : "Kapalı: dokun ve izin ver", v -> bildirimAyari()));
        if ("student".equals(rol)) {
            tel.addView(Arayuz.ayirici(e));
            tel.addView(satir(R.drawable.ik_konum, "Bu telefonu velimle paylaş",
                Ayarlar.bagli(e) ? "Bağlı: konum ve ekran süresi velinle paylaşılıyor" : "Konumunu ve ekran süreni velin görsün",
                v -> e.startActivity(new Intent(e, AileEkrani.class))));
        }
        Arayuz.ekle(g, tel, 8);

        Arayuz.ekle(g, Arayuz.bolumEtiketi(e, "Eğitim Evi"), 22);
        LinearLayout site = Arayuz.kart(e);
        site.setPadding(0, Tema.dp(e, 4), 0, Tema.dp(e, 4));
        site.addView(satir(R.drawable.ik_okul, "Siteyi aç", "Uygulamada olmayan işler için (Excel aktarımı, roller, ders programı)",
            v -> KayitSayfasi.siteAc(e, "/")));
        site.addView(Arayuz.ayirici(e));
        site.addView(satir(R.drawable.ik_belge, "Aydınlatma metni", null, v -> KvkkSayfasi.metniAc(e)));
        site.addView(Arayuz.ayirici(e));
        site.addView(satir(R.drawable.ik_soru, "Sık sorulan sorular", null, v -> KayitSayfasi.siteAc(e, "/sss/sss.html")));
        Arayuz.ekle(g, site, 8);

        TextView cik = Arayuz.dugme(e, "Çıkış yap", Arayuz.Dugme.TEHLIKE);
        cik.setOnClickListener(v -> new AlertDialog.Builder(e)
            .setTitle("Çıkış yapılsın mı?")
            .setMessage("Bu telefonda bildirimler de durur.")
            .setPositiveButton("Çıkış yap", (d, w) -> e.cikis())
            .setNegativeButton("Vazgeç", null)
            .show());
        Arayuz.ekle(g, cik, 26);
        String surum = "";
        try { surum = e.getPackageManager().getPackageInfo(e.getPackageName(), 0).versionName; } catch (Exception ignored) { }
        TextView alt = Arayuz.soluk(e, "Eğitim Evi " + surum);
        alt.setGravity(android.view.Gravity.CENTER);
        Arayuz.ekle(g, alt, 14);
        return s[0];
    }

    private View satir(int simge, String ad, String alt, View.OnClickListener tik) {
        return Arayuz.tiklananSatir(e, Arayuz.ikon(e, simge, R.color.ana, 22), ad, alt, null, tik);
    }

    private void bildirimAyari() {
        if (Build.VERSION.SDK_INT >= 33
            && e.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            e.requestPermissions(new String[] { android.Manifest.permission.POST_NOTIFICATIONS }, 13);
            return;
        }
        Intent i = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, e.getPackageName());
        try { e.startActivity(i); }
        catch (Exception x) { e.startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + e.getPackageName()))); }
    }

    @Override
    protected void gorundu() { yenile(); }
}
