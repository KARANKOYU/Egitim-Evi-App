package org.egitimevi.aile;

import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Bildirimler: yeniden eskiye, gün gün. Okunmamışlar solda noktayla ve kalın.
 * Sayfa açılınca hepsi okundu sayılır (sitedeki gibi). Bildirime dokununca ilgili
 * ekran açılır (Sekmeler.baglantiAc).
 */
public class BildirimlerSayfasi extends Sayfa.Veri {
    @Override public String baslik() { return "Bildirimler"; }
    @Override protected String adres() { return "/api/notifications"; }

    @Override
    protected View ciz(JSONObject j) {
        JSONArray l = j.optJSONArray("notifications");
        if (l == null || l.length() == 0) {
            return Arayuz.bosDurum(e, R.drawable.ik_bildirim, "Bildirim yok",
                "Ödev, mesaj, servis ve okuldan haberler burada görünür.", null);
        }
        ScrollView[] s = new ScrollView[1];
        LinearLayout g = Arayuz.sayfaGovdesi(e, s);
        String sonGun = null;
        LinearLayout kart = null;
        for (int i = 0; i < l.length(); i++) {
            JSONObject b = l.optJSONObject(i);
            String gun = Zaman.gunBasligi(b.optString("createdAt"));
            if (kart == null || !gun.equals(sonGun)) {
                Arayuz.ekle(g, Arayuz.bolumEtiketi(e, gun.isEmpty() ? "Daha eski" : gun), kart == null ? 4 : 18);
                kart = Arayuz.kart(e);
                kart.setPadding(0, Tema.dp(e, 4), 0, Tema.dp(e, 4));
                Arayuz.ekle(g, kart, 8);
                sonGun = gun;
            } else {
                kart.addView(Arayuz.ayirici(e));
            }
            kart.addView(satir(b));
        }
        if (j.optInt("unread", 0) > 0) {
            Ag.post(e, "/api/notifications/read", null, x -> e.rozetTazele(), h -> { });
        }
        return s[0];
    }

    private View satir(JSONObject b) {
        boolean okunmadi = !b.optBoolean("read", true);
        View nokta = new View(e);
        GradientDrawable z = new GradientDrawable();
        z.setShape(GradientDrawable.OVAL);
        z.setColor(okunmadi ? Tema.renk(e, R.color.ana) : 0);
        nokta.setBackground(z);
        nokta.setLayoutParams(new LinearLayout.LayoutParams(Tema.dp(e, 9), Tema.dp(e, 9)));
        String link = b.optString("link", "");
        View.OnClickListener tik = link.isEmpty() ? null : v -> Sekmeler.baglantiAc(e, link);
        LinearLayout s = tik == null
            ? Arayuz.satir(e, nokta, b.optString("text"), Zaman.goreli(b.optString("createdAt")), null)
            : Arayuz.tiklananSatir(e, nokta, b.optString("text"), Zaman.goreli(b.optString("createdAt")), null, tik);
        TextView metin = (TextView) ((LinearLayout) s.getChildAt(1)).getChildAt(0);
        metin.setTypeface(okunmadi ? Tema.KALIN : Tema.GOVDE);
        metin.setTextSize(15);
        return s;
    }
}
