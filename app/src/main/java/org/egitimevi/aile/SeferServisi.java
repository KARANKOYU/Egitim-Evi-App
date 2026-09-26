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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Servisçinin seferi sürerken konumu gönderen ön plan servisi. Servisçi sitede
 * seferi başlatınca site köprüyle (Kopru.seferBasladi) bunu açar; ekran kapansa da
 * uygulama arkaya alınsa da konum gider. Bildirim çubuğunda "Sefer sürüyor" yazar.
 * Konum birkaç saniyede bir (araç dururken 20 saniyede bir) gönderilir; sunucu seferi
 * kapatınca (409) ya da anahtar geçersizse servis kendini durdurur. Konum yalnızca
 * uygulama anahtarıyla gider; hesaba giriş vermez.
 */
public class SeferServisi extends Service implements LocationListener {
    private static final String KANAL = "sefer";
    private static final int BILDIRIM_NO = 2;
    private static final long EN_AZ_ARALIK = 5000, NABIZ = 20000;
    private static final float EN_AZ_MESAFE = 30f;

    private final Handler ana = new Handler(Looper.getMainLooper());
    private final ExecutorService arka = Executors.newSingleThreadExecutor();
    private LocationManager lm;
    private Location son, sonGiden;
    private long sonGonderim;
    private final Runnable nabiz = new Runnable() {
        @Override public void run() {
            if (son != null && System.currentTimeMillis() - sonGonderim >= NABIZ) gonder(son);
            ana.postDelayed(this, NABIZ);
        }
    };

    public static void baslat(Context c, String seferId) {
        UygulamaAyar.seferYaz(c, seferId);
        c.startForegroundService(new Intent(c, SeferServisi.class));
    }

    public static void durdur(Context c) {
        UygulamaAyar.seferYaz(c, "");
        c.stopService(new Intent(c, SeferServisi.class));
    }

    @Override
    public int onStartCommand(Intent i, int f, int id) {
        Notification b = bildirim();
        if (Build.VERSION.SDK_INT >= 29) startForeground(BILDIRIM_NO, b, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        else startForeground(BILDIRIM_NO, b);
        if (UygulamaAyar.sefer(this).isEmpty() || !UygulamaAyar.anahtarVar(this) || !konumIzni()) {
            stopSelf();
            return START_NOT_STICKY;
        }
        if (lm == null) {
            lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            try {
                if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, EN_AZ_ARALIK, 10f, this, Looper.getMainLooper());
                }
                if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, EN_AZ_ARALIK, 10f, this, Looper.getMainLooper());
                }
            } catch (SecurityException e) {
                stopSelf();
                return START_NOT_STICKY;
            }
            ana.postDelayed(nabiz, NABIZ);
        }
        return START_NOT_STICKY;
    }

    private boolean konumIzni() {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onLocationChanged(Location l) {
        /* Ağdan gelen kaba konum, yakın zamanda gelmiş daha doğru GPS konumunu ezmesin. */
        if (son != null && LocationManager.NETWORK_PROVIDER.equals(l.getProvider())
            && LocationManager.GPS_PROVIDER.equals(son.getProvider()) && l.getTime() - son.getTime() < 15000) return;
        son = l;
        long simdi = System.currentTimeMillis();
        boolean hareket = sonGiden == null || l.distanceTo(sonGiden) > EN_AZ_MESAFE;
        if (simdi - sonGonderim >= EN_AZ_ARALIK && (hareket || simdi - sonGonderim >= NABIZ)) gonder(l);
    }

    /* Eski Android sürümleri bu üç işlevi çağırır; yenilerinde varsayılanları var. */
    @SuppressWarnings("deprecation")
    @Override public void onStatusChanged(String p, int s, Bundle e) { }
    @Override public void onProviderEnabled(String p) { }
    @Override public void onProviderDisabled(String p) { }

    private void gonder(Location l) {
        sonGonderim = System.currentTimeMillis();
        sonGiden = l;
        String sefer = UygulamaAyar.sefer(this), anahtar = UygulamaAyar.anahtar(this), sunucu = Ayarlar.sunucu(this);
        if (sefer.isEmpty() || anahtar.isEmpty()) { ana.post(this::stopSelf); return; }
        arka.execute(() -> {
            try {
                JSONObject g = new JSONObject().put("seferId", sefer).put("enlem", l.getLatitude())
                    .put("boylam", l.getLongitude()).put("dogruluk", Math.round(l.getAccuracy()));
                Api.uygulama(sunucu, "/api/cihaz/servis-konum", g, anahtar);
            } catch (Api.Hata h) {
                /* Sefer bitti ya da başka servisçiye geçti (409), anahtar geçersiz (401/403), sefer yok (404). */
                if (h.durum == 409 || h.durum == 401 || h.durum == 403 || h.durum == 404) {
                    UygulamaAyar.seferYaz(this, "");
                    ana.post(this::stopSelf);
                }
            } catch (Exception ignored) {
                /* İnternet yok: bir sonraki konumda yeniden denenir. */
            }
        });
    }

    private Notification bildirim() {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm.getNotificationChannel(KANAL) == null) {
            NotificationChannel k = new NotificationChannel(KANAL, "Servis seferi", NotificationManager.IMPORTANCE_LOW);
            k.setDescription("Sefer sürerken servisin konumu velilere gönderilirken görünür.");
            nm.createNotificationChannel(k);
        }
        PendingIntent ac = PendingIntent.getActivity(this, 1, new Intent(this, AnaEkran.class)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_IMMUTABLE);
        return new Notification.Builder(this, KANAL)
            .setSmallIcon(R.drawable.bildirim_simge)
            .setContentTitle("Sefer sürüyor")
            .setContentText("Servisin konumu servisteki öğrencilerin velilerine gönderiliyor.")
            .setOngoing(true)
            .setContentIntent(ac)
            .build();
    }

    @Override
    public void onDestroy() {
        ana.removeCallbacks(nabiz);
        if (lm != null) lm.removeUpdates(this);
        arka.shutdown();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent i) { return null; }
}
