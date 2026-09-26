package org.egitimevi.aile;

import java.util.ArrayList;
import java.util.List;

/**
 * Kişinin rolüne göre alt çubuktaki sekmeler ve bildirim bağlantılarının hangi ekranı
 * açacağı. Rol ekranları eklendikçe burası genişler (sitedeki 06-menu.js karşılığı).
 */
public final class Sekmeler {
    private Sekmeler() { }

    public static List<AnaEkran.Sekme> icin(AnaEkran e) {
        List<AnaEkran.Sekme> l = new ArrayList<>();
        l.add(new AnaEkran.Sekme("Ana sayfa", R.drawable.ik_ev, AnaSayfa::new));
        l.add(new AnaEkran.Sekme("Bildirimler", R.drawable.ik_bildirim, BildirimlerSayfasi::new));
        l.add(new AnaEkran.Sekme("Ayarlar", R.drawable.ik_ayar, AyarlarSayfasi::new));
        return l;
    }

    /**
     * Bildirim bağlantısı ("#/odevler", "/okul/?k=...#/servis?c=...") hangi ekranı açar.
     * Bilinmeyen bağlantıda bir şey yapılmaz (bildirim zaten okundu).
     */
    public static void baglantiAc(AnaEkran e, String baglanti) {
        if (baglanti == null) return;
        String sayfa = baglanti.replaceAll("^.*#/", "").replaceAll("\\?.*$", "");
        switch (sayfa) {
            case "profil":
                e.git(new AyarlarSayfasi());
                break;
            default:
                break;
        }
    }
}
