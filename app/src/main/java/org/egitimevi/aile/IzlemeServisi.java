package org.egitimevi.aile;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Arka planda çalışan servis (bildirim çubuğunda görünür; Android bunu
 * şart koşar). Velinin seçtiği aralıkla konum alır: Wi-Fi'deyken sık, mobil
 * veride seyrek. İnternet yokken konumlar kuyrukta birikir, bağlanılan ilk
 * anda gönderilir. Ekran süresi 15 dakikada bir, ayarlar 30 dakikada bir
 * sunucudan tazelenir. Hiçbir uygulamayı kapatmaz ya da kilitlemez.
 */
public class IzlemeServisi extends Service implements LocationListener {
    private static final String KANAL = "izleme";
    private static final int BILDIRIM_NO = 7;
    private static final long DAKIKA = 60 * 1000;

    private HandlerThread is;
    private Handler el;
    private LocationManager lm;
    private int istenenDk = -1;
    private long sonKullanim = 0, sonAyar = 0;

    public static void baslat(Context c) {
        if (!Ayarlar.bagli(c)) return;
        Intent i = new Intent(c, IzlemeServisi.class);
        c.startForegroundService(i);
    }

    public static void durdur(Context c) { c.stopService(new Intent(c, IzlemeServisi.class)); }

    @Override
    public void onCreate() {
        super.onCreate();
        is = new HandlerThread("aile-izleme");
        is.start();
        el = new Handler(is.getLooper());
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /* Servis konum servisidir: konum izni yoksa başlamaz (ana ekran izni ister). */
        if (!Ayarlar.bagli(this) || !konumIzniVar()) { stopSelf(); return START_NOT_STICKY; }
        Notification b = bildirim();
        if (Build.VERSION.SDK_INT >= 29) startForeground(BILDIRIM_NO, b, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        else startForeground(BILDIRIM_NO, b);
        el.removeCallbacksAndMessages(null);
        el.post(this::tur);
        return START_STICKY;   // sistem kapatırsa yeniden başlasın
    }

    /** Her dakika: aralığı ağ türüne göre ayarla, zamanı geleni gönder. */
    private void tur() {
        try {
            konumIsteginiAyarla();
            long simdi = System.currentTimeMillis();
            if (simdi - sonAyar > 30 * DAKIKA) ayarlariTazele();
            if (Ayarlar.kullanimAcik(this) && simdi - sonKullanim > 15 * DAKIKA) kullanimGonder();
            konumlariGonder();
        } catch (Exception e) {
            Ayarlar.hata(this, e.getMessage());
        }
        el.postDelayed(this::tur, DAKIKA);
    }

    private boolean konumIzniVar() {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /** "wifi", "mobil" ya da "" (bağlantı yok) */
    private String agTuru() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Network n = cm.getActiveNetwork();
        NetworkCapabilities nc = n == null ? null : cm.getNetworkCapabilities(n);
        if (nc == null || !nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) return "";
        if (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || nc.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) return "wifi";
        return "mobil";
    }

    private void konumIsteginiAyarla() {
        if (!Ayarlar.konumAcik(this) || !konumIzniVar()) {
            if (istenenDk != -1) { lm.removeUpdates(this); istenenDk = -1; }
            return;
        }
        int dk = "mobil".equals(agTuru()) || agTuru().isEmpty() ? Ayarlar.mobilAraligi(this) : Ayarlar.wifiAraligi(this);
        if (dk == istenenDk) return;
        lm.removeUpdates(this);
        istenenDk = dk;
        long ms = dk * DAKIKA;
        try {
            if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, ms, 0f, this, Looper.getMainLooper());
            }
            if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, ms, 0f, this, Looper.getMainLooper());
            }
        } catch (SecurityException e) {
            istenenDk = -1;
        }
    }

    /** Yeni konum: aynı aralık içinde iki sağlayıcıdan gelirse daha doğrusu tutulur. */
    @Override
    public void onLocationChanged(Location l) {
        long son = Ayarlar.sonKonum(this);
        long aralik = Math.max(1, istenenDk) * DAKIKA;
        if (l.getTime() - son < aralik * 0.8) return;
        try {
            JSONObject k = new JSONObject();
            k.put("enlem", l.getLatitude());
            k.put("boylam", l.getLongitude());
            k.put("dogruluk", l.hasAccuracy() ? Math.round(l.getAccuracy()) : null);
            k.put("zaman", l.getTime());
            k.put("ag", agTuru());
            k.put("pil", pil());
            Kuyruk.ekle(this, k);
            Ayarlar.konumAlindi(this, l.getTime());
        } catch (Exception e) {
            Ayarlar.hata(this, e.getMessage());
        }
        el.post(this::konumlariGonder);
    }

    private int pil() {
        BatteryManager bm = (BatteryManager) getSystemService(Context.BATTERY_SERVICE);
        return bm == null ? -1 : bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
    }

    /** Kuyruktaki konumlar 500'erli gönderilir; gönderilen silinir. */
    private void konumlariGonder() {
        if (agTuru().isEmpty() || Kuyruk.boyut(this) == 0) return;
        try {
            for (int tur = 0; tur < 10 && Kuyruk.boyut(this) > 0; tur++) {
                JSONArray parca = Kuyruk.bastan(this, 500);
                JSONObject g = new JSONObject().put("konumlar", parca);
                Api.post(Ayarlar.sunucu(this), "/api/aile/cihaz/konum", g, null, Ayarlar.cihazAnahtari(this));
                Kuyruk.sil(this, parca.length());
                Ayarlar.gonderildi(this, System.currentTimeMillis());
            }
        } catch (Api.Hata h) {
            baglantiKoptuMu(h);
        } catch (Exception e) {
            Ayarlar.hata(this, e.getMessage());
        }
    }

    private void kullanimGonder() {
        if (agTuru().isEmpty() || !Kullanim.izinVar(this)) return;
        try {
            JSONObject g = new JSONObject().put("gunler", Kullanim.sonGunler(this, 2));
            Api.post(Ayarlar.sunucu(this), "/api/aile/cihaz/kullanim", g, null, Ayarlar.cihazAnahtari(this));
            sonKullanim = System.currentTimeMillis();
            Ayarlar.gonderildi(this, sonKullanim);
        } catch (Api.Hata h) {
            baglantiKoptuMu(h);
        } catch (Exception e) {
            Ayarlar.hata(this, e.getMessage());
        }
    }

    private void ayarlariTazele() {
        if (agTuru().isEmpty()) return;
        try {
            JSONObject a = Api.get(Ayarlar.sunucu(this), "/api/aile/cihaz/ayar", Ayarlar.cihazAnahtari(this)).getJSONObject("ayar");
            Ayarlar.ayariYaz(this, a.optInt("wifiDk", 5), a.optInt("mobilDk", 15),
                a.optBoolean("konumAcik", true), a.optBoolean("kullanimAcik", true));
            sonAyar = System.currentTimeMillis();
            istenenDk = -2;   // aralık değişmiş olabilir: yeniden kur
        } catch (Api.Hata h) {
            baglantiKoptuMu(h);
        } catch (Exception e) {
            Ayarlar.hata(this, e.getMessage());
        }
    }

    /** Sunucu cihazı tanımıyorsa (veli ya da okul bağlantıyı kaldırdı) servis durur. */
    private void baglantiKoptuMu(Api.Hata h) {
        Ayarlar.hata(this, h.getMessage());
        if (h.durum == 401 || h.durum == 403) {
            Ayarlar.cik(this);
            Kuyruk.temizle(this);
            stopSelf();
        }
    }

    private Notification bildirim() {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm.getNotificationChannel(KANAL) == null) {
            NotificationChannel k = new NotificationChannel(KANAL, "Aile paylaşımı", NotificationManager.IMPORTANCE_LOW);
            k.setDescription("Konum ve ekran süresi velinle paylaşılırken görünür.");
            nm.createNotificationChannel(k);
        }
        PendingIntent ac = PendingIntent.getActivity(this, 0, new Intent(this, AnaEkran.class), PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder b = new Notification.Builder(this, KANAL);
        return b.setSmallIcon(R.drawable.bildirim_simge)
            .setContentTitle("Eğitim Evi Aile")
            .setContentText("Konumun ve ekran süren velinle paylaşılıyor.")
            .setContentIntent(ac)
            .setOngoing(true)
            .build();
    }

    @Override
    public void onDestroy() {
        try { lm.removeUpdates(this); } catch (Exception ignored) { }
        el.removeCallbacksAndMessages(null);
        is.quitSafely();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    /* Eski Android sürümleri için boş gövdeler */
    @Override public void onProviderEnabled(String p) { istenenDk = -2; }
    @Override public void onProviderDisabled(String p) { }
    @SuppressWarnings("deprecation") @Override public void onStatusChanged(String p, int s, android.os.Bundle b) { }
}
