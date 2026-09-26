package org.egitimevi.aile;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Kişi kodu (öğrencide veli kodu): 15 karakter; ekranda 5'erli gruplar hâlinde,
 * boşlukla ayrılmış gösterilir ("Ab3#k Qx9+m Pt7?z"). Kopyala ham kodu (boşluksuz)
 * panoya koyar. Girişte yalnızca boşluklar silinir; büyük/küçük harf önemlidir.
 */
public final class KisiKodu {
    private KisiKodu() { }

    public static String bicim(String kod) {
        if (kod == null) return "";
        String k = sade(kod);
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < k.length(); i++) {
            if (i > 0 && i % 5 == 0) b.append(' ');
            b.append(k.charAt(i));
        }
        return b.toString();
    }

    /** Yazılan ya da yapıştırılan koddan boşlukları (ve eski biçimin tirelerini değil) atar. */
    public static String sade(String kod) { return kod == null ? "" : kod.replaceAll("\\s+", ""); }

    /** Kod kutusu + Kopyala düğmesi. */
    public static LinearLayout kutu(Context c, String kod, Runnable kopyalandi) {
        LinearLayout k = Arayuz.dikey(c);
        k.setGravity(Gravity.CENTER_HORIZONTAL);
        TextView t = Arayuz.yazi(c, bicim(kod), 26, R.color.yazi);
        t.setTypeface(Tema.KOD);
        t.setTextIsSelectable(true);
        t.setGravity(Gravity.CENTER);
        t.setLetterSpacing(0.06f);
        t.setBackground(Tema.zemin(c, Tema.renk(c, R.color.ana_acik), Tema.R_KUCUK, Tema.renk(c, R.color.ana_cizgi), 1));
        int p = Tema.dp(c, 14);
        t.setPadding(p, Tema.dp(c, 16), p, Tema.dp(c, 16));
        t.setContentDescription("Kod: " + harfHarf(sade(kod)));
        k.addView(t, new LinearLayout.LayoutParams(-1, -2));
        TextView kopyala = Arayuz.dugme(c, "Kopyala", Arayuz.Dugme.IKINCIL);
        kopyala.setOnClickListener(v -> {
            ClipboardManager pano = (ClipboardManager) c.getSystemService(Context.CLIPBOARD_SERVICE);
            if (pano != null) pano.setPrimaryClip(ClipData.newPlainText("Kişi kodu", sade(kod)));
            if (kopyalandi != null) kopyalandi.run();
        });
        Arayuz.ekle(k, kopyala, 10);
        return k;
    }

    /** Ekran okuyucu için kod harf harf okunsun ("A, b, 3, diyez..."). */
    private static String harfHarf(String k) {
        StringBuilder b = new StringBuilder();
        for (char ch : k.toCharArray()) {
            if (b.length() > 0) b.append(", ");
            switch (ch) {
                case '!': b.append("ünlem"); break;
                case '?': b.append("soru işareti"); break;
                case '#': b.append("diyez"); break;
                case '*': b.append("yıldız"); break;
                case '+': b.append("artı"); break;
                case '-': b.append("tire"); break;
                default: b.append(Character.isUpperCase(ch) ? "büyük " + ch : String.valueOf(ch));
            }
        }
        return b.toString();
    }
}
