package org.egitimevi.aile;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/**
 * Tarih ve saat yazımı, Türkiye saatiyle (sitedeki tarihSaat, tarihGun gibi):
 * "az önce", "5 dk önce", "14:32", "Dün 09:10", "12 Eylül", "12 Eylül 2025".
 */
public final class Zaman {
    public static final ZoneId TURKIYE = ZoneId.of("Europe/Istanbul");
    private static final Locale TR = Locale.forLanguageTag("tr-TR");
    private static final DateTimeFormatter SAAT = DateTimeFormatter.ofPattern("HH:mm", TR);
    private static final DateTimeFormatter GUN_AY = DateTimeFormatter.ofPattern("d MMMM", TR);
    private static final DateTimeFormatter GUN_AY_YIL = DateTimeFormatter.ofPattern("d MMMM yyyy", TR);
    private static final DateTimeFormatter GUN_AY_HAFTA = DateTimeFormatter.ofPattern("d MMMM, EEEE", TR);

    private Zaman() { }

    /** ISO zaman ("2026-09-26T17:23:37.000Z") -> Türkiye saatinde; bozuksa null. */
    public static ZonedDateTime oku(String iso) {
        if (iso == null || iso.isEmpty()) return null;
        try { return Instant.parse(iso).atZone(TURKIYE); } catch (Exception e) { return null; }
    }

    /** Bildirim ve mesaj zamanı: yakınsa göreli, uzaksa tarih. */
    public static String goreli(String iso) {
        ZonedDateTime z = oku(iso);
        if (z == null) return "";
        ZonedDateTime simdi = ZonedDateTime.now(TURKIYE);
        long dk = Duration.between(z, simdi).toMinutes();
        if (dk < 1) return "az önce";
        if (dk < 60) return dk + " dk önce";
        long gun = ChronoUnit.DAYS.between(z.toLocalDate(), simdi.toLocalDate());
        if (gun == 0) return z.format(SAAT);
        if (gun == 1) return "Dün " + z.format(SAAT);
        if (z.getYear() == simdi.getYear()) return z.format(GUN_AY);
        return z.format(GUN_AY_YIL);
    }

    /** Liste bölüm başlığı: "Bugün", "Dün", "12 Eylül, Cuma". */
    public static String gunBasligi(String iso) {
        ZonedDateTime z = oku(iso);
        if (z == null) return "";
        long gun = ChronoUnit.DAYS.between(z.toLocalDate(), LocalDate.now(TURKIYE));
        if (gun == 0) return "Bugün";
        if (gun == 1) return "Dün";
        return z.format(GUN_AY_HAFTA);
    }

    /** "2026-09-30" -> "30 Eylül 2026, Çarşamba" (gün adıyla, sitedeki tarihGun). */
    public static String gun(String tarih) {
        try { return LocalDate.parse(tarih).format(DateTimeFormatter.ofPattern("d MMMM yyyy, EEEE", TR)); }
        catch (Exception e) { return tarih == null ? "" : tarih; }
    }

    /** Bugünün tarihi, ana sayfa başlığı için: "26 Eylül, Cumartesi". */
    public static String bugun() { return LocalDate.now(TURKIYE).format(GUN_AY_HAFTA); }

    /** Günün saatine göre selam. */
    public static String selam() {
        int s = LocalTime.now(TURKIYE).getHour();
        if (s < 6) return "İyi geceler";
        if (s < 12) return "Günaydın";
        if (s < 18) return "İyi günler";
        return "İyi akşamlar";
    }
}
