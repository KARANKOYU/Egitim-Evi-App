package org.egitimevi.aile;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Layout;
import android.text.Selection;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Kişi kodu (öğrencide veli kodu): 16 karakter; ekranda tireyle ayrılmış 4'erli dört
 * grup hâlinde gösterilir ("Ab3#-kQx9-+mPt-7?zR"). Tire kodun karakteri değildir,
 * ayırıcıdır. Kopyala da tireli biçimi panoya koyar. Girişte boşluklar ve tireler
 * silinir; büyük/küçük harf önemlidir. Kod yazılan kutuda tire kendiliğinden gelir
 * ({@link #kutuyaBagla}).
 */
public final class KisiKodu {
    public static final int UZUNLUK = 16;
    public static final String ORNEK = "Ab3#-kQx9-+mPt-7?zR";

    /* Kodun karakterleri ve geçerli bir kod (sunucudaki KISI_KODU_DESENI): ilk karakter harf;
       büyük harf, küçük harf, rakam ve işaretin her birinden en az biri. */
    private static final String KARAKTER = "A-HJKMNP-Za-km-np-z2-9!?#*+=";
    private static final Pattern GECERLI = Pattern.compile(
        "(?=.*[A-HJKMNP-Z])(?=.*[a-km-np-z])(?=.*[2-9])(?=.*[!?#*+=])[A-HJKMNP-Za-km-np-z][" + KARAKTER + "]{15}");
    /* Yapıştırılan metinde kodun aranma sırası (sitedeki kisiKoduAyikla ile aynı): ekrandaki
       tireli biçim, başka ayırıcılarla 4'erli gruplar, 16 bitişik karakter. Adayın iki yanında
       kod karakteri olmamalı. */
    private static final Pattern[] ARAMA;
    static {
        String g = "([" + KARAKTER + "]{4})", bas = "(?:^|[^" + KARAKTER + "])", son = "(?![" + KARAKTER + "])";
        ARAMA = new Pattern[] {
            Pattern.compile(bas + g + "-" + g + "-" + g + "-" + g + son),
            Pattern.compile(bas + g + "[ -]+" + g + "[ -]+" + g + "[ -]+" + g + son),
            Pattern.compile(bas + "([" + KARAKTER + "]{16})" + son)
        };
    }

    private KisiKodu() { }

    public static String bicim(String kod) {
        if (kod == null) return "";
        String k = sade(kod);
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < k.length(); i++) {
            if (i > 0 && i % 4 == 0) b.append('-');
            b.append(k.charAt(i));
        }
        return b.toString();
    }

    /** Yazılan ya da yapıştırılan koddan ayırıcıları (boşluk, tire, benzerleri) atar. */
    public static String sade(CharSequence kod) {
        if (kod == null) return "";
        StringBuilder b = new StringBuilder(kod.length());
        for (int i = 0; i < kod.length(); i++) {
            char ch = kod.charAt(i);
            if (!ayiriciMi(ch)) b.append(ch);
        }
        return b.toString();
    }

    /* Ayırıcı (sitedeki 05-giris.js ve sunucudaki ortak.js ile aynı küme): boşluklar (bölünmez
       ve dar boşluk da), tire ve benzerleri, görünmez karakterler. Java'nın isWhitespace'i
       U+001C..U+001F denetim karakterlerini de boşluk sayar; sitedeki \s saymaz, küme aynı kalsın. */
    private static boolean ayiriciMi(char ch) {
        return (Character.isWhitespace(ch) && (ch < 0x1C || ch > 0x1F)) || Character.isSpaceChar(ch) || tireMi(ch) || gorunmezMi(ch);
    }

    /* Tire ve kopyalanınca tire kılığına giren benzerleri: U+2010..U+2015 kısa/uzun çizgiler,
       U+2212 eksi, U+FE63 küçük tire, U+FF0D tam genişlikli tire. */
    private static boolean tireMi(char ch) {
        return ch == '-' || (ch >= 0x2010 && ch <= 0x2015) || ch == 0x2212 || ch == 0xFE63 || ch == 0xFF0D;
    }

    /* Görünmez karakterler: U+00AD yumuşak tire, U+200B..U+200D sıfır genişlikli karakterler,
       U+2060 sözcük birleştirici, U+FEFF. */
    private static boolean gorunmezMi(char ch) {
        return ch == 0x00AD || (ch >= 0x200B && ch <= 0x200D) || ch == 0x2060 || ch == 0xFEFF;
    }

    /**
     * Yapıştırılan metindeki tam kod ("Veli kodu: Ab3#-kQx9-+mPt-7?zR" içinden yalnız kod);
     * tam ve geçerli bir kod yoksa null.
     */
    public static String ayikla(CharSequence metin) {
        if (metin == null) return null;
        String k = sade(metin);
        if (k.length() < UZUNLUK) return null;
        if (k.length() == UZUNLUK) return GECERLI.matcher(k).matches() ? k : null;
        /* Görünmez karakterler atılır, tire benzerleri '-', boşluklar ' ' olur. */
        StringBuilder b = new StringBuilder(metin.length());
        for (int i = 0; i < metin.length(); i++) {
            char ch = metin.charAt(i);
            if (gorunmezMi(ch)) continue;
            b.append(tireMi(ch) ? '-' : ayiriciMi(ch) ? ' ' : ch);
        }
        String m = b.toString();
        for (Pattern p : ARAMA) {
            Matcher e = p.matcher(m);
            int yer = 0;
            while (yer <= m.length() && e.find(yer)) {
                StringBuilder aday = new StringBuilder(UZUNLUK);
                for (int i = 1; i <= e.groupCount(); i++) aday.append(e.group(i));
                if (GECERLI.matcher(aday).matches()) return aday.toString();
                yer = e.start() + 1;
            }
        }
        return null;
    }

    /** Biçimli metinde n kod karakterinin ardı (aradaki tireler de sayılır). */
    private static int imlecYeri(int n) { return n > 0 ? n + (n - 1) / 4 : 0; }

    /**
     * Kod kutusu: yazarken her 4 karakterden sonra tire kendiliğinden gelir (bir sonraki
     * karakter yazılınca; sonda tire kalmaz), silerken tire de gider. İmleç yazılan ya da
     * silinen yerde kalır. Yalnız bir tire silindiyse (geri tuşu tirenin ardında, Delete
     * önünde) tirenin yanındaki karakter de silinir; yoksa tire hemen geri gelirdi.
     * Tireli, tiresiz ya da boşluklu yapıştırılan kod da biçime girer; 16 karakterden
     * fazlası yazılmaz (yapıştırılanın sığan kısmı alınır). Yapıştırılan metinde tam bir kod
     * varsa ("Veli kodu: ...") kutuda yalnız o kalır.
     * Kutu görünür parola türündedir (EkleSayfasi): klavye harfleri birleştirmeden
     * (composition) tek tek işler, biçimleme klavyenin birleştirdiği sözcüğü bozmaz.
     */
    public static void kutuyaBagla(EditText kutu) {
        kutu.setHint(ORNEK);
        Bicimleyici b = new Bicimleyici();
        kutu.setFilters(new InputFilter[] { b });
        kutu.addTextChangedListener(b);
    }

    private static final class Bicimleyici implements InputFilter, TextWatcher {
        /* Kutuya kendimiz yazarken (biçimlerken) süzgeç ve izleyici karışmaz. */
        private boolean yaziyor;
        /* Silinen tek karakter tireyse: -1 değil, 0 geri tuşu, 1 Delete. */
        private int tireSilme = -1;
        /* Yapıştırılan metindeki tam kod: kutuda yalnız o kalır. */
        private String tamKod;

        @Override public CharSequence filter(CharSequence kaynak, int bas, int son, Spanned hedef, int hBas, int hSon) {
            if (yaziyor) return null;
            CharSequence gelen = kaynak.subSequence(bas, son);
            String kod = gelen.length() >= UZUNLUK ? ayikla(gelen) : null;
            if (kod != null) {
                tamKod = kod;
                return kod;
            }
            String eklenen = sade(gelen);
            int kalan = sade(hedef.subSequence(0, hBas)).length() + sade(hedef.subSequence(hSon, hedef.length())).length();
            if (kalan + eklenen.length() <= UZUNLUK) return null;   // sığıyor: olduğu gibi
            int yer = Math.max(0, UZUNLUK - kalan);
            return eklenen.substring(0, Math.min(yer, eklenen.length()));
        }

        @Override public void beforeTextChanged(CharSequence s, int bas, int adet, int sonra) {
            tireSilme = -1;
            if (yaziyor || adet != 1 || sonra != 0 || bas >= s.length() || !ayiriciMi(s.charAt(bas))) return;
            tireSilme = Selection.getSelectionEnd(s) == bas ? 1 : 0;
        }

        @Override public void onTextChanged(CharSequence s, int bas, int once, int adet) { }

        @Override public void afterTextChanged(Editable e) {
            if (yaziyor) return;
            String ham = e.toString();
            int imlec = Selection.getSelectionEnd(e);
            if (imlec < 0 || imlec > ham.length()) imlec = ham.length();
            String k = sade(ham);
            int n = sade(ham.substring(0, imlec)).length();
            if (tamKod != null) {
                k = tamKod;
                n = k.length();
            } else if (tireSilme == 0 && n > 0) {
                k = k.substring(0, n - 1) + k.substring(n);
                n--;
            } else if (tireSilme == 1 && n < k.length()) {
                k = k.substring(0, n) + k.substring(n + 1);
            }
            tamKod = null;
            tireSilme = -1;
            if (k.length() > UZUNLUK) k = k.substring(0, UZUNLUK);
            if (n > k.length()) n = k.length();
            String yeni = bicim(k);
            yaziyor = true;
            try {
                if (!yeni.equals(ham)) e.replace(0, e.length(), yeni);
                Selection.setSelection(e, Math.min(imlecYeri(n), e.length()));
            } finally {
                yaziyor = false;
            }
        }
    }

    /**
     * Kod kutusu + Kopyala düğmesi (Kopyala tireli biçimi verir). Kod tek satırdadır, dar
     * ekranda küçülür. Yazı boyutu çok büyük seçilmişse (telefonun Ayarlar > Yazı boyutu) kod en
     * küçük boyda da sığmayabilir: o zaman iki satıra geçer (tireden bölünür), sonu gizlenmez.
     */
    public static LinearLayout kutu(Context c, String kod, Runnable kopyalandi) {
        LinearLayout k = Arayuz.dikey(c);
        k.setGravity(Gravity.CENTER_HORIZONTAL);
        TextView t = Arayuz.yazi(c, bicim(kod), 26, R.color.yazi);
        t.setTypeface(Tema.KOD);
        t.setTextIsSelectable(true);
        t.setGravity(Gravity.CENTER);
        t.setLetterSpacing(0.04f);
        t.setMaxLines(1);
        t.setAutoSizeTextTypeUniformWithConfiguration(10, 26, 1, TypedValue.COMPLEX_UNIT_SP);
        t.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override public void onLayoutChange(View v, int s, int u, int sa, int a, int es, int eu, int esa, int ea) {
                Layout y = t.getLayout();
                if (y == null || v.getWidth() <= 0 || (y.getLineCount() <= 1 && y.getLineEnd(0) >= t.length())) return;
                /* Otomatik küçültme en küçük boya inmeden sığmıyorsa henüz bitmemiştir. */
                if (t.getTextSize() > t.getAutoSizeMinTextSize() + 0.5f) return;
                t.removeOnLayoutChangeListener(this);
                t.post(() -> ikiSatiraGec(t, kod));
            }
        });
        t.setBackground(Tema.zemin(c, Tema.renk(c, R.color.ana_acik), Tema.R_KUCUK, Tema.renk(c, R.color.ana_cizgi), 1));
        int p = Tema.dp(c, 12);
        t.setPadding(p, Tema.dp(c, 16), p, Tema.dp(c, 16));
        t.setContentDescription("Kod: " + harfHarf(sade(kod)));
        k.addView(t, new LinearLayout.LayoutParams(-1, -2));
        TextView kopyala = Arayuz.dugme(c, "Kopyala", Arayuz.Dugme.IKINCIL);
        kopyala.setOnClickListener(v -> {
            ClipboardManager pano = (ClipboardManager) c.getSystemService(Context.CLIPBOARD_SERVICE);
            if (pano != null) pano.setPrimaryClip(ClipData.newPlainText("Kişi kodu", bicim(kod)));
            if (kopyalandi != null) kopyalandi.run();
        });
        Arayuz.ekle(k, kopyala, 10);
        return k;
    }

    /**
     * Kod tek satıra sığmadı: ikinci gruptan sonra satır başı konur ("Ab3#-kQx9-" ve
     * "+mPt-7?zR"; satırları telefonun satır kırıcısına bırakınca üçe bölünüp sonu
     * gizlenebiliyordu). Yazı, yarım satır sığacak kadar büyütülür (en çok 26 sp); satır
     * sınırı kalkar, çok dar ekranda da kodun hiçbir yeri gizlenmez. Kopyala ve seçip
     * kopyalama etkilenmez (satır başı boşluk gibi atılır).
     */
    private static void ikiSatiraGec(TextView t, String kod) {
        String b = bicim(kod);
        if (b.length() <= 10) return;
        String ilk = b.substring(0, 10);
        float boy = t.getTextSize();
        float genislik = t.getWidth() - t.getTotalPaddingLeft() - t.getTotalPaddingRight();
        float satir = t.getPaint().measureText(ilk);
        float enBuyuk = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 26, t.getResources().getDisplayMetrics());
        float yeni = satir > 0 && genislik > 0 ? Math.min(enBuyuk, boy * genislik / satir * 0.95f) : boy;
        t.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_NONE);
        t.setMaxLines(Integer.MAX_VALUE);
        t.setText(ilk + "\n" + b.substring(10));
        t.setTextSize(TypedValue.COMPLEX_UNIT_PX, Math.max(boy, yeni));
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
                case '=': b.append("eşittir"); break;
                default: b.append(Character.isUpperCase(ch) ? "büyük " + ch : String.valueOf(ch));
            }
        }
        return b.toString();
    }
}
