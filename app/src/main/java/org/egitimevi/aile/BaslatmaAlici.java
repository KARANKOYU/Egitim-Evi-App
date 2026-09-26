package org.egitimevi.aile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Telefon açılınca ya da uygulama güncellenince: çocuğun telefonu servisi yeniden başlar,
 *  bildirim yoklaması yeniden kurulur. Yarıda kalmış sefer sürdürülmez (servisçi yeniden başlatır). */
public class BaslatmaAlici extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent i) {
        String a = i.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(a) || Intent.ACTION_MY_PACKAGE_REPLACED.equals(a)) {
            IzlemeServisi.baslat(c);
            UygulamaAyar.seferYaz(c, "");
            Bildirimler.zamanla(c);
        }
    }
}
