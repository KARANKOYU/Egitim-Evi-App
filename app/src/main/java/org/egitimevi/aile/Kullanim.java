package org.egitimevi.aile;

import android.app.AppOpsManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Process;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Ekran süresi: son 7 günün her günü için hangi uygulama önde kaç dakika
 * açık kaldı. Android'in kullanım istatistiklerinden okunur (kullanıcının
 * Ayarlar > Kullanım erişimi'nden izin vermesi gerekir). Ana ekran
 * (başlatıcı), sistem arayüzü ve bu uygulama sayılmaz.
 */
public final class Kullanim {
    private Kullanim() { }

    public static boolean izinVar(Context c) {
        AppOpsManager ops = (AppOpsManager) c.getSystemService(Context.APP_OPS_SERVICE);
        int m = Build.VERSION.SDK_INT >= 29
            ? ops.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), c.getPackageName())
            : ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), c.getPackageName());
        return m == AppOpsManager.MODE_ALLOWED;
    }

    /** { gunler: [ { gun: "2026-09-26", uygulamalar: [ { paket, ad, dakika } ] } ] } */
    public static JSONArray sonGunler(Context c, int gunSayisi) throws JSONException {
        JSONArray gunler = new JSONArray();
        if (!izinVar(c)) return gunler;
        UsageStatsManager usm = (UsageStatsManager) c.getSystemService(Context.USAGE_STATS_SERVICE);
        PackageManager pm = c.getPackageManager();
        Set<String> atla = atlanacaklar(c, pm);
        SimpleDateFormat bicim = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);

        for (int g = gunSayisi - 1; g >= 0; g--) {
            Calendar bas = Calendar.getInstance();
            bas.add(Calendar.DAY_OF_MONTH, -g);
            bas.set(Calendar.HOUR_OF_DAY, 0);
            bas.set(Calendar.MINUTE, 0);
            bas.set(Calendar.SECOND, 0);
            bas.set(Calendar.MILLISECOND, 0);
            long basMs = bas.getTimeInMillis();
            long bitMs = Math.min(basMs + 24L * 60 * 60 * 1000, System.currentTimeMillis());
            Map<String, Long> sure = onPlandaKalma(usm, basMs, bitMs);

            JSONArray uygulamalar = new JSONArray();
            for (Map.Entry<String, Long> e : sure.entrySet()) {
                if (atla.contains(e.getKey())) continue;
                long dakika = Math.round(e.getValue() / 60000.0);
                if (dakika < 1) continue;
                JSONObject u = new JSONObject();
                u.put("paket", e.getKey());
                u.put("ad", ad(pm, e.getKey()));
                u.put("dakika", dakika);
                uygulamalar.put(u);
            }
            JSONObject gun = new JSONObject();
            gun.put("gun", bicim.format(bas.getTime()));
            gun.put("uygulamalar", uygulamalar);
            gunler.put(gun);
        }
        return gunler;
    }

    /** Olaylardan hesap: uygulama öne geldi - arkaya gitti arası süre. */
    private static Map<String, Long> onPlandaKalma(UsageStatsManager usm, long bas, long bit) {
        Map<String, Long> toplam = new HashMap<>();
        Map<String, Long> acilis = new HashMap<>();
        UsageEvents olaylar = usm.queryEvents(bas, bit);
        UsageEvents.Event o = new UsageEvents.Event();
        while (olaylar.hasNextEvent()) {
            olaylar.getNextEvent(o);
            String p = o.getPackageName();
            int t = o.getEventType();
            if (t == UsageEvents.Event.ACTIVITY_RESUMED) {
                acilis.put(p, o.getTimeStamp());
            } else if (t == UsageEvents.Event.ACTIVITY_PAUSED || t == UsageEvents.Event.ACTIVITY_STOPPED) {
                Long a = acilis.remove(p);
                if (a != null && o.getTimeStamp() > a) toplam.put(p, getOr(toplam, p) + (o.getTimeStamp() - a));
            }
        }
        /* Hâlâ açık olan uygulama: şimdiye (ya da gün sonuna) kadar say. */
        for (Map.Entry<String, Long> e : acilis.entrySet()) {
            if (bit > e.getValue()) toplam.put(e.getKey(), getOr(toplam, e.getKey()) + (bit - e.getValue()));
        }
        return toplam;
    }

    private static long getOr(Map<String, Long> m, String k) {
        Long v = m.get(k);
        return v == null ? 0 : v;
    }

    private static Set<String> atlanacaklar(Context c, PackageManager pm) {
        Set<String> s = new HashSet<>();
        s.add(c.getPackageName());
        s.add("com.android.systemui");
        s.add("android");
        Intent ev = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
        for (ResolveInfo r : pm.queryIntentActivities(ev, 0)) s.add(r.activityInfo.packageName);
        return s;
    }

    private static String ad(PackageManager pm, String paket) {
        try {
            ApplicationInfo ai = pm.getApplicationInfo(paket, 0);
            CharSequence a = pm.getApplicationLabel(ai);
            return a == null ? paket : a.toString();
        } catch (PackageManager.NameNotFoundException e) {
            return paket;
        }
    }
}
