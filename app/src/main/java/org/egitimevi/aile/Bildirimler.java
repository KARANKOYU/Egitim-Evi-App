package org.egitimevi.aile;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Telefon bildirimleri. Uygulama Firebase kullanmadığı için sunucuya kendisi sorar:
 * normalde 15 dakikada bir (Android'in izin verdiği en sık düzenli iş), okulun servis
 * saatlerinde dakikada bir ("servise bindi" gibi bildirimler gecikmesin). Sunucu yalnız
 * son görülenden sonraki bildirimleri verir (imleç); ilk sorguda eskiler gelmez.
 * Uygulama açıkken bildirim çubuğuna yazılmaz: site kendi bildirimlerini gösterir.
 */
public final class Bildirimler {
    static final String KANAL = "bildirimler";
    private static final int IS_DUZENLI = 101, IS_HIZLI = 102;
    private static final long ONBES_DK = 15 * 60 * 1000L;
    private static final TimeZone TURKIYE = TimeZone.getTimeZone("Europe/Istanbul");

    private Bildirimler() { }

    /** Anahtar varsa işleri kurar: düzenli (15 dk) ve servis saatindeysek bir dakika sonrasına tek seferlik. */
    public static void zamanla(Context c) { zamanla(c, false); }

    /** zincir: yoklama işinin kendisi çağırır; bekleyen tek seferlik iş olsa da yenisi kurulur. */
    static void zamanla(Context c, boolean zincir) {
        JobScheduler js = (JobScheduler) c.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (js == null) return;
        if (!UygulamaAyar.anahtarVar(c)) { iptal(c); return; }
        ComponentName is = new ComponentName(c, BildirimIsi.class);
        if (js.getPendingJob(IS_DUZENLI) == null) {
            js.schedule(new JobInfo.Builder(IS_DUZENLI, is)
                .setPeriodic(ONBES_DK)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .build());
        }
        if (servisSaatinde(UygulamaAyar.servisSaatleri(c)) && (zincir || js.getPendingJob(IS_HIZLI) == null)) {
            js.schedule(new JobInfo.Builder(IS_HIZLI, is)
                .setMinimumLatency(60 * 1000L)
                .setOverrideDeadline(3 * 60 * 1000L)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .build());
        }
    }

    public static void iptal(Context c) {
        JobScheduler js = (JobScheduler) c.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (js != null) { js.cancel(IS_DUZENLI); js.cancel(IS_HIZLI); }
    }

    /** "HH:MM" -> gün içindeki dakika; bozuksa -1. */
    private static int dakika(String s) {
        if (s == null || !s.matches("^[0-2]\\d:[0-5]\\d$")) return -1;
        return Integer.parseInt(s.substring(0, 2)) * 60 + Integer.parseInt(s.substring(3));
    }

    /** Şu an (Türkiye saatiyle) okulun sabah ya da akşam servis saatinde miyiz? (10 dk pay) */
    static boolean servisSaatinde(String json) {
        if (json == null || json.isEmpty()) return false;
        try {
            JSONObject s = new JSONObject(json);
            Calendar t = Calendar.getInstance(TURKIYE);
            int simdi = t.get(Calendar.HOUR_OF_DAY) * 60 + t.get(Calendar.MINUTE);
            String[][] araliklar = { { "sabahBas", "sabahBit" }, { "aksamBas", "aksamBit" } };
            for (String[] a : araliklar) {
                int bas = dakika(s.optString(a[0])), bit = dakika(s.optString(a[1]));
                if (bas >= 0 && bit > bas && simdi >= bas - 10 && simdi <= bit + 60) return true;
            }
        } catch (Exception ignored) { }
        return false;
    }

    /** Sunucuya sorar, yenileri gösterir. Ağ işi: arka iş parçacığında çağrılır. */
    static void yokla(Context c) {
        String anahtar = UygulamaAyar.anahtar(c);
        if (anahtar.isEmpty()) return;
        String imlec = UygulamaAyar.imlec(c);
        String yol = "/api/cihaz/bildirimler" + (imlec.isEmpty() ? "" : "?son=" + kodla(imlec));
        try {
            JSONObject d = Api.uygulama(Ayarlar.sunucu(c), yol, null, anahtar);
            JSONArray liste = d.optJSONArray("bildirimler");
            if (liste != null && !imlec.isEmpty() && !AnaEkran.onde) {
                for (int i = 0; i < liste.length() && i < 20; i++) goster(c, liste.optJSONObject(i));
            }
            JSONObject saatler = d.optJSONObject("servisSaatleri");
            UygulamaAyar.imlecYaz(c, d.optString("imlec", imlec), saatler == null ? "" : saatler.toString());
        } catch (Api.Hata h) {
            /* Anahtar iptal edilmiş (çıkış, hesap silindi): unut ve dur. */
            if (h.durum == 401 || h.durum == 403) { UygulamaAyar.cik(c); iptal(c); }
        } catch (IOException ignored) {
            /* İnternet yok: bir sonraki işte yeniden denenir. */
        }
    }

    private static String kodla(String s) {
        try { return URLEncoder.encode(s, StandardCharsets.UTF_8.name()); } catch (Exception e) { return ""; }
    }

    static void kanalKur(Context c) {
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null || nm.getNotificationChannel(KANAL) != null) return;
        NotificationChannel k = new NotificationChannel(KANAL, "Bildirimler", NotificationManager.IMPORTANCE_DEFAULT);
        k.setDescription("Ödev, mesaj, servis ve okul bildirimleri.");
        nm.createNotificationChannel(k);
    }

    private static void goster(Context c, JSONObject b) {
        if (b == null) return;
        String metin = b.optString("metin", "").trim();
        if (metin.isEmpty()) return;
        kanalKur(c);
        Intent ac = new Intent(c, AnaEkran.class)
            .putExtra(AnaEkran.BAGLANTI, b.optString("baglanti", ""))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int no = b.optString("id", metin).hashCode();
        PendingIntent pi = PendingIntent.getActivity(c, no, ac, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        Notification n = new Notification.Builder(c, KANAL)
            .setSmallIcon(R.drawable.bildirim_simge)
            .setContentTitle("Eğitim Evi")
            .setContentText(metin)
            .setStyle(new Notification.BigTextStyle().bigText(metin))
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build();
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        try { if (nm != null) nm.notify(no, n); } catch (SecurityException ignored) { /* bildirim izni yok */ }
    }
}
