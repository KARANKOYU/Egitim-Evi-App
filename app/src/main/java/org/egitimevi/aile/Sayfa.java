package org.egitimevi.aile;

import android.view.View;
import android.widget.FrameLayout;

import org.json.JSONObject;

/**
 * Uygulamanın bir ekranı. AnaEkran sayfaları yığında tutar: git() yenisini üste koyar,
 * geri tuşu çıkarır. Sayfa görünümünü bir kez kurar (olustur), üste döndüğünde
 * gorundu() çağrılır (ör. veriyi tazelemek için).
 */
public abstract class Sayfa {
    protected AnaEkran e;
    View gorunum;

    /** Üst çubukta görünen başlık. */
    public abstract String baslik();

    /** Sayfanın görünümü (bir kez çağrılır; yenile() yeniden kurar). */
    protected abstract View olustur();

    /** Sayfa yeniden görünür olduğunda (başka sayfadan dönülünce). */
    protected void gorundu() { }

    /** true dönerse geri tuşunu sayfa kendisi karşıladı. */
    protected boolean geriBas() { return false; }

    /** Giriş gibi ekranlar üst ve alt çubuğu gizler. */
    protected boolean cubuksuz() { return false; }

    /** Üst çubukta "yenile" düğmesi gösterilsin mi. */
    protected boolean yenilenir() { return false; }

    /** Yenile: varsayılan olarak görünümü baştan kurar. */
    protected void yenile() { e.sayfayiYenile(this); }

    /**
     * Sunucudan veri isteyip çizen sayfa: önce "yükleniyor", sonra içerik; hata olursa
     * nedeni ve "Yeniden dene". Üst çubuktaki yenile düğmesi veriyi yeniden ister.
     */
    public abstract static class Veri extends Sayfa {
        protected FrameLayout kap;

        /** İstenecek API yolu (ör. "/api/notifications"). */
        protected abstract String adres();

        /** Gelen cevaptan görünümü kurar. */
        protected abstract View ciz(JSONObject j);

        @Override
        protected View olustur() {
            kap = new FrameLayout(e);
            yukle();
            return kap;
        }

        @Override protected boolean yenilenir() { return true; }

        @Override protected void yenile() { yukle(); }

        protected void yukle() {
            if (kap.getChildCount() == 0) kap.addView(Arayuz.yukleniyor(e));
            Ag.get(e, adres(), j -> {
                kap.removeAllViews();
                kap.addView(ciz(j), new FrameLayout.LayoutParams(-1, -1));
            }, h -> {
                kap.removeAllViews();
                kap.addView(Arayuz.hataKutusu(e, h.getMessage(), this::yukle), new FrameLayout.LayoutParams(-1, -1));
            });
        }
    }
}
