package org.egitimevi.aile;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Uygulamanın arayüz takımı. Bütün ekranlar bu parçalarla kurulur; böylece her yerde
 * aynı ölçü, renk ve davranış olur (sitedeki kart, düğme, form alanı, etiket, boş
 * kutu, yükleniyor ve hata kalıplarının karşılığı). Dış kütüphane yok.
 */
public final class Arayuz {
    private Arayuz() { }

    public enum Dugme { BIRINCIL, IKINCIL, HAYALET, TEHLIKE }
    public enum Etiket { ANA, YESIL, KIRMIZI, TURUNCU, MAVI, GRI, BORDO }

    /* ---------------- yazı ---------------- */

    public static TextView yazi(Context c, CharSequence s, int boySp, int renkId) {
        TextView t = new TextView(c);
        t.setText(s);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, boySp);
        t.setTextColor(Tema.renk(c, renkId));
        t.setTypeface(Tema.GOVDE);
        t.setLineSpacing(0, 1.25f);
        return t;
    }

    /** Sayfa başlığı: serif, büyük (sitedeki hero başlığı). */
    public static TextView baslik(Context c, CharSequence s) {
        TextView t = yazi(c, s, 27, R.color.yazi);
        t.setTypeface(Tema.BASLIK);
        t.setLineSpacing(0, 1.1f);
        return t;
    }

    /** Kart ya da bölüm başlığı. */
    public static TextView altBaslik(Context c, CharSequence s) {
        TextView t = yazi(c, s, 17, R.color.yazi);
        t.setTypeface(Tema.KALIN);
        return t;
    }

    /** Büyük harfli küçük bölüm etiketi ("BUGÜN", "SERVİS"). */
    public static TextView bolumEtiketi(Context c, CharSequence s) {
        TextView t = yazi(c, s.toString().toUpperCase(java.util.Locale.forLanguageTag("tr-TR")), 12, R.color.soluk);
        t.setTypeface(Tema.KALIN);
        t.setLetterSpacing(0.08f);
        return t;
    }

    public static TextView metin(Context c, CharSequence s) { return yazi(c, s, 15, R.color.yazi); }

    public static TextView soluk(Context c, CharSequence s) { return yazi(c, s, 13, R.color.soluk); }

    /* ---------------- yerleşim ---------------- */

    public static LinearLayout dikey(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    public static LinearLayout yatay(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        return l;
    }

    /** Alt alta dizilen parçalar arasında boşluk bırakarak ekler. */
    public static void ekle(LinearLayout ust, View v, int ustBoslukDp) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = Tema.dp(ust.getContext(), ustBoslukDp);
        ust.addView(v, lp);
    }

    /** Kaydırılabilir sayfa gövdesi; içine dikey bir gövde koyar ve onu döndürür. */
    public static LinearLayout sayfaGovdesi(Context c, ScrollView[] kaydirma) {
        ScrollView s = new ScrollView(c);
        s.setFillViewport(true);
        s.setClipToPadding(false);
        LinearLayout g = dikey(c);
        int p = Tema.dp(c, 18);
        g.setPadding(p, Tema.dp(c, 8), p, Tema.dp(c, 28));
        s.addView(g, new FrameLayout.LayoutParams(-1, -2));
        if (kaydirma != null && kaydirma.length > 0) kaydirma[0] = s;
        return g;
    }

    /** Kart: beyaz (koyuda koyu gri) zemin, ince çizgi, yuvarlak köşe. */
    public static LinearLayout kart(Context c) {
        LinearLayout k = dikey(c);
        k.setBackground(Tema.zemin(c, Tema.renk(c, R.color.kart), Tema.R_ORTA, Tema.renk(c, R.color.cizgi), 1));
        int p = Tema.dp(c, 16);
        k.setPadding(p, p, p, p);
        k.setElevation(Tema.dp(c, 1));
        return k;
    }

    /** İnce ayırıcı çizgi. */
    public static View ayirici(Context c) {
        View v = new View(c);
        v.setBackgroundColor(Tema.renk(c, R.color.cizgi));
        v.setLayoutParams(new LinearLayout.LayoutParams(-1, Math.max(1, Tema.dp(c, 1))));
        return v;
    }

    /* ---------------- düğmeler ---------------- */

    public static TextView dugme(Context c, CharSequence s, Dugme tur) {
        TextView b = new TextView(c);
        b.setText(s);
        b.setGravity(Gravity.CENTER);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        b.setTypeface(Tema.KALIN);
        b.setMinHeight(Tema.dp(c, 50));
        int yp = Tema.dp(c, 12), xp = Tema.dp(c, 18);
        b.setPadding(xp, yp, xp, yp);
        int zemin, yazi;
        switch (tur) {
            case BIRINCIL: zemin = Tema.renk(c, R.color.ana); yazi = Tema.renk(c, R.color.ustune_yazi); break;
            case IKINCIL: zemin = Tema.renk(c, R.color.ana_acik); yazi = Tema.renk(c, R.color.ana_koyu); break;
            case TEHLIKE: zemin = Tema.renk(c, R.color.kirmizi_zemin); yazi = Tema.renk(c, R.color.kirmizi_yazi); break;
            default: zemin = Color.TRANSPARENT; yazi = Tema.renk(c, R.color.ana); break;
        }
        b.setTextColor(yazi);
        b.setBackground(Tema.dalgali(c, Tema.zemin(c, zemin, Tema.R_KUCUK, 0, 0), Tema.R_KUCUK));
        b.setClickable(true);
        b.setFocusable(true);
        return b;
    }

    /** Düğmeyi meşgul/serbest yapar (istek sürerken iki kez basılmasın). */
    public static void mesgul(TextView b, boolean mesgul) {
        b.setEnabled(!mesgul);
        b.setAlpha(mesgul ? 0.55f : 1f);
    }

    /** Yuvarlak simge düğmesi (üst çubuk). */
    public static FrameLayout simgeDugme(Context c, int simge, CharSequence aciklama) {
        FrameLayout f = new FrameLayout(c);
        int b = Tema.dp(c, 44);
        f.setLayoutParams(new LinearLayout.LayoutParams(b, b));
        GradientDrawable z = new GradientDrawable();
        z.setShape(GradientDrawable.OVAL);
        z.setColor(Color.TRANSPARENT);
        GradientDrawable m = new GradientDrawable();
        m.setShape(GradientDrawable.OVAL);
        m.setColor(0xFFFFFFFF);
        f.setBackground(new android.graphics.drawable.RippleDrawable(
            ColorStateList.valueOf(Tema.renk(c, R.color.ana_cizgi)), z, m));
        ImageView i = ikon(c, simge, R.color.yazi, 24);
        f.addView(i, new FrameLayout.LayoutParams(Tema.dp(c, 24), Tema.dp(c, 24), Gravity.CENTER));
        f.setContentDescription(aciklama);
        f.setClickable(true);
        f.setFocusable(true);
        return f;
    }

    public static ImageView ikon(Context c, int simge, int renkId, int boyDp) {
        ImageView i = new ImageView(c);
        i.setImageResource(simge);
        i.setImageTintList(ColorStateList.valueOf(Tema.renk(c, renkId)));
        i.setLayoutParams(new LinearLayout.LayoutParams(Tema.dp(c, boyDp), Tema.dp(c, boyDp)));
        return i;
    }

    /* ---------------- form ---------------- */

    /** Etiketli giriş alanı; altında hata yazısı yeri. */
    public static final class Alan {
        public final LinearLayout kok;
        public final EditText kutu;
        public final TextView hata;

        Alan(LinearLayout kok, EditText kutu, TextView hata) { this.kok = kok; this.kutu = kutu; this.hata = hata; }

        public String deger() { return kutu.getText().toString().trim(); }
        public String hamDeger() { return kutu.getText().toString(); }

        public void hataGoster(CharSequence s) {
            hata.setText(s);
            hata.setVisibility(TextUtils.isEmpty(s) ? View.GONE : View.VISIBLE);
            kutu.setActivated(!TextUtils.isEmpty(s));
        }
    }

    public static Alan alan(Context c, CharSequence etiket, CharSequence ipucu, int tur) {
        LinearLayout k = dikey(c);
        TextView e = yazi(c, etiket, 14, R.color.yazi);
        e.setTypeface(Tema.ORTA);
        k.addView(e);
        EditText kutu = new EditText(c);
        kutu.setSingleLine((tur & InputType.TYPE_TEXT_FLAG_MULTI_LINE) == 0);
        kutu.setInputType(tur);
        if ((tur & InputType.TYPE_TEXT_VARIATION_PASSWORD) != 0) kutu.setTransformationMethod(PasswordTransformationMethod.getInstance());
        kutu.setHint(ipucu);
        kutu.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        kutu.setTextColor(Tema.renk(c, R.color.yazi));
        kutu.setHintTextColor(Tema.renk(c, R.color.soluk));
        kutu.setTypeface(Tema.GOVDE);
        int p = Tema.dp(c, 14);
        kutu.setPadding(p, Tema.dp(c, 12), p, Tema.dp(c, 12));
        kutu.setMinHeight(Tema.dp(c, 50));
        StateListDrawable z = new StateListDrawable();
        z.addState(new int[] { android.R.attr.state_activated },
            Tema.zemin(c, Tema.renk(c, R.color.kart_ust), Tema.R_KUCUK, Tema.renk(c, R.color.kirmizi), 2));
        z.addState(new int[] { android.R.attr.state_focused },
            Tema.zemin(c, Tema.renk(c, R.color.kart_ust), Tema.R_KUCUK, Tema.renk(c, R.color.ana), 2));
        z.addState(new int[] {}, Tema.zemin(c, Tema.renk(c, R.color.kart_ust), Tema.R_KUCUK, Tema.renk(c, R.color.cizgi_koyu), 1));
        kutu.setBackground(z);
        ekle(k, kutu, 6);
        TextView h = yazi(c, "", 13, R.color.kirmizi);
        h.setVisibility(View.GONE);
        ekle(k, h, 4);
        return new Alan(k, kutu, h);
    }

    /* ---------------- liste ---------------- */

    /** Liste satırı: solda simge (ya da avatar), başlık ve alt yazı, sağda etiket ya da ok. */
    public static LinearLayout satir(Context c, View sol, CharSequence baslik, CharSequence alt, View sag) {
        LinearLayout s = yatay(c);
        s.setMinimumHeight(Tema.dp(c, 64));
        int p = Tema.dp(c, 14);
        s.setPadding(p, Tema.dp(c, 10), p, Tema.dp(c, 10));
        if (sol != null) {
            LinearLayout.LayoutParams lp = sol.getLayoutParams() instanceof LinearLayout.LayoutParams
                ? (LinearLayout.LayoutParams) sol.getLayoutParams() : new LinearLayout.LayoutParams(-2, -2);
            lp.rightMargin = Tema.dp(c, 14);
            s.addView(sol, lp);
        }
        LinearLayout orta = dikey(c);
        TextView b = yazi(c, baslik, 16, R.color.yazi);
        b.setTypeface(Tema.ORTA);
        orta.addView(b);
        if (!TextUtils.isEmpty(alt)) {
            TextView a = soluk(c, alt);
            orta.addView(a);
        }
        s.addView(orta, new LinearLayout.LayoutParams(0, -2, 1f));
        if (sag != null) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
            lp.leftMargin = Tema.dp(c, 10);
            s.addView(sag, lp);
        }
        return s;
    }

    /** Dokunulabilir satır: dalgalanır, okla biter. */
    public static LinearLayout tiklananSatir(Context c, View sol, CharSequence baslik, CharSequence alt, View sag, View.OnClickListener tik) {
        LinearLayout s = satir(c, sol, baslik, alt, sag != null ? sag : ikon(c, R.drawable.ik_geri, R.color.soluk, 18));
        if (sag == null) s.getChildAt(s.getChildCount() - 1).setRotation(180);
        s.setBackground(Tema.dalgali(c, new GradientDrawable(), Tema.R_KUCUK));
        s.setClickable(true);
        s.setFocusable(true);
        s.setOnClickListener(tik);
        return s;
    }

    /** Avatar: baş harfler, kişiye özgü renk (site: avatar()). */
    public static TextView avatar(Context c, String ad, String anahtar, int boyDp) {
        TextView t = new TextView(c);
        t.setText(Tema.basHarfler(ad));
        t.setGravity(Gravity.CENTER);
        t.setTextColor(Color.WHITE);
        t.setTypeface(Tema.KALIN);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, boyDp * 0.36f);
        GradientDrawable z = new GradientDrawable();
        z.setShape(GradientDrawable.OVAL);
        z.setColor(Tema.avatarRengi(anahtar != null ? anahtar : ad));
        t.setBackground(z);
        t.setLayoutParams(new LinearLayout.LayoutParams(Tema.dp(c, boyDp), Tema.dp(c, boyDp)));
        t.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        return t;
    }

    /** Etiket türünün zemin ve yazı rengi. */
    private static int[] etiketRenkleri(Etiket tur) {
        switch (tur) {
            case YESIL: return new int[] { R.color.yesil_zemin, R.color.yesil_yazi };
            case KIRMIZI: return new int[] { R.color.kirmizi_zemin, R.color.kirmizi_yazi };
            case TURUNCU: return new int[] { R.color.turuncu_zemin, R.color.turuncu_yazi };
            case MAVI: return new int[] { R.color.mavi_zemin, R.color.mavi_yazi };
            case GRI: return new int[] { R.color.gri_zemin, R.color.soluk };
            case BORDO: return new int[] { R.color.bordo, R.color.ustune_yazi };
            default: return new int[] { R.color.ana_acik, R.color.ana_koyu };
        }
    }

    /** Durum etiketi (hap): Yaptı yeşil, Yapmadı kırmızı gibi. */
    public static TextView etiket(Context c, CharSequence s, Etiket tur) {
        int[] r = etiketRenkleri(tur);
        TextView t = yazi(c, s, 12, r[1]);
        t.setTypeface(Tema.KALIN);
        t.setBackground(Tema.zemin(c, Tema.renk(c, r[0]), 99, 0, 0));
        int x = Tema.dp(c, 10), y = Tema.dp(c, 4);
        t.setPadding(x, y, x, y);
        return t;
    }

    /* ---------------- durumlar ---------------- */

    public static View yukleniyor(Context c) {
        FrameLayout f = new FrameLayout(c);
        ProgressBar p = new ProgressBar(c);
        p.setIndeterminateTintList(ColorStateList.valueOf(Tema.renk(c, R.color.ana)));
        f.addView(p, new FrameLayout.LayoutParams(Tema.dp(c, 40), Tema.dp(c, 40), Gravity.CENTER));
        f.setMinimumHeight(Tema.dp(c, 220));
        f.setContentDescription("Yükleniyor");
        return f;
    }

    /** Boş durum: simge, başlık, açıklama ve isteğe bağlı düğme (sitedeki bosKutu). */
    public static LinearLayout bosDurum(Context c, int simge, CharSequence baslik, CharSequence aciklama, View dugme) {
        LinearLayout k = dikey(c);
        k.setGravity(Gravity.CENTER_HORIZONTAL);
        int p = Tema.dp(c, 24);
        k.setPadding(p, Tema.dp(c, 40), p, Tema.dp(c, 40));
        FrameLayout daire = new FrameLayout(c);
        GradientDrawable z = new GradientDrawable();
        z.setShape(GradientDrawable.OVAL);
        z.setColor(Tema.renk(c, R.color.ana_acik));
        daire.setBackground(z);
        daire.addView(ikon(c, simge, R.color.ana, 30), new FrameLayout.LayoutParams(Tema.dp(c, 30), Tema.dp(c, 30), Gravity.CENTER));
        k.addView(daire, new LinearLayout.LayoutParams(Tema.dp(c, 68), Tema.dp(c, 68)));
        TextView b = altBaslik(c, baslik);
        b.setGravity(Gravity.CENTER);
        ekle(k, b, 16);
        if (!TextUtils.isEmpty(aciklama)) {
            TextView a = yazi(c, aciklama, 14, R.color.soluk);
            a.setGravity(Gravity.CENTER);
            ekle(k, a, 6);
        }
        if (dugme != null) ekle(k, dugme, 18);
        return k;
    }

    /** Hata kutusu: ne olduğu ve "Yeniden dene". */
    public static LinearLayout hataKutusu(Context c, CharSequence mesaj, Runnable yeniden) {
        TextView d = null;
        if (yeniden != null) {
            d = dugme(c, "Yeniden dene", Dugme.IKINCIL);
            d.setOnClickListener(v -> yeniden.run());
        }
        return bosDurum(c, R.drawable.ik_uyari, "Bir sorun oldu", mesaj, d);
    }

    /** Uyarı/bilgi şeridi (sitedeki .msg). */
    public static TextView serit(Context c, CharSequence s, Etiket tur) {
        int[] r = etiketRenkleri(tur);
        TextView t = yazi(c, s, 14, r[1]);
        t.setBackground(Tema.zemin(c, Tema.renk(c, r[0]), Tema.R_KUCUK, 0, 0));
        int p = Tema.dp(c, 14);
        t.setPadding(p, Tema.dp(c, 12), p, Tema.dp(c, 12));
        return t;
    }
}
