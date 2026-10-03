# app/src/debug/res/xml/KLASOR.md

Yalnız deneme (debug) paketine giren tek dosya `ag_guvenligi.xml`: ana kaynaktaki aynı adlı dosyanın yerine geçer ve
şifresiz (http) bağlantıya izin verir; evdeki ya da öykünücüdeki deneme sunucusuna bağlanabilmek için.

## Bu dosya ne yapar?

Gradle her yapı türü için `src/main`'i o türün kendi klasörüyle birleştirir. `src/debug/` yalnız `assembleDebug` ile
derlenen deneme paketine girer; aynı adlı bir kaynak varsa `src/main`'dekinin yerine geçer. Bu klasörde tek böyle kaynak
var.

Yayın paketi yalnız https ile bağlanır ([../../../main/res/xml/KLASOR.md](../../../main/res/xml/KLASOR.md)). Ama geliştirirken
sunucu bilgisayarda https'siz çalışır: öykünücüden `http://10.0.2.2:3200` (öykünücüde `10.0.2.2` bilgisayarın kendisidir),
evdeki telefondan `http://192.168.x.x:…`. Bu dosya deneme paketinde o bağlantıyı açar; yayın paketine hiç girmez.

## İçinde neler var?

### `ag_guvenligi.xml`

```
<network-security-config xmlns:tools="http://schemas.android.com/tools">
    <base-config cleartextTrafficPermitted="true" tools:ignore="InsecureBaseConfiguration" />
</network-security-config>
```

- `cleartextTrafficPermitted="true"` — bütün adreslere http'ye Android düzeyinde izin.
- `tools:ignore="InsecureBaseConfiguration"` — lint'in "bütün trafiğe şifresiz izin verilmiş" uyarısını yalnız bu dosyada
  susturur (bilerek yapıldı, deneme paketi).
- Yorumu: "Yalnızca deneme (debug) paketi: evdeki deneme sunucusuna http ile bağlanılabilsin. `Api.java` yine de http'ye
  yalnızca yerel ağ adreslerinde (localhost, 10.x, 172.16-31.x, 192.168.x) izin verir; internetteki adreslere https ile
  gidilir."

Yani deneme paketinde iki kat var: Android her adrese http'yi açar, ama uygulamanın kendi kodu
([Api](../../../main/java/org/egitimevi/aile/Api.md), `adresSorunu`) http'yi yalnız yerel ağ adreslerinde kabul eder.

## Kimle konuşur?

- **Gradle/AGP:** `debug` yapı türünde bu dosya `src/main/res/xml/ag_guvenligi.xml`'in yerine geçer
  ([../../../../KLASOR.md](../../../../KLASOR.md)).
- **Manifest** aynı adı gösterir (`android:networkSecurityConfig="@xml/ag_guvenligi"`); hangi dosyanın geleceğine yapı türü
  karar verir.
- **Kod:** [Api](../../../main/java/org/egitimevi/aile/Api.md) — `adresSorunu` (yerel ağ sınırı). Sunucu adresi
  [Ayarlar](../../../main/java/org/egitimevi/aile/Ayarlar.md)'dan gelir.

## Nasıl çalışır (adım adım)?

```
./gradlew assembleDebug
  res birleşir: src/main/res/xml/ag_guvenligi.xml (false)  +  src/debug/res/xml/ag_guvenligi.xml (true)
                ─► deneme paketinde true olanı kalır
uygulama http://10.0.2.2:3200'e istek atar
  Api.adresSorunu: "10." ile başlıyor ─► yerel, geçer
  Android ağ katmanı: cleartextTrafficPermitted=true ─► bağlanır
uygulama http://ornek.com'a istek atmak isterse
  Api.adresSorunu ─► "İnternetteki sunucuya yalnızca https:// ile bağlanılır." (istek hiç gitmez)
```

## Dikkat!

- **Deneme paketini kimseye dağıtma.** Android düzeyinde her adrese http açıktır; tek koruma `Api`'nin metin denetimi.
  `Api` adresin ana bilgisayar adını yalnız yazıyla denetler: `10.` ile başlayan bir alan adı (ör. `10.ornek.com`) de yerel
  sayılır ([Api](../../../main/java/org/egitimevi/aile/Api.md)). Yayın paketinde bu sorun yok, çünkü Android http'yi orada
  hiç açmaz.
- **Bugün deneme paketinde sunucu adresini seçen bir ekran yok.** Uygulama varsayılan olarak `https://egitimevi.org`'a
  gider; deneme sunucusuna gitmesi için adresin "aile" ayar dosyasına yazılmış olması gerekir. Bunu yapan tek yer "Çocuğun
  telefonu" ekranının sunucu kutusudur ([Ayarlar](../../../main/java/org/egitimevi/aile/Ayarlar.md),
  [AileEkrani](../../../main/java/org/egitimevi/aile/AileEkrani.md)). Yani bu dosyanın açtığı http yolu bugün yalnız o yoldan
  kullanılabiliyor. O ekrana da ancak varsayılan sunucuda (`egitimevi.org`) öğrenci olarak girilmişken Ayarlar'daki "Bu
  telefonu velimle paylaş"tan (ya da telefon bağlıyken izleme servisinin bildiriminden) ulaşılır; ekran dışa kapalıdır
  (`exported="false"`). Sonuç: yeni kurulmuş bir deneme paketi, uygulamanın kendi ekranlarıyla deneme sunucusuna bugün
  bağlanamaz. Sunucu seçme penceresi `commit 5`'te vardı (yalnız deneme paketinde, ilk açılışta), `commit 6`'da kalktı;
  ondan kalan `UygulamaAyar.sunucuSecildi` bayrağını bugün kimse yazmıyor.
- Bu klasöre başka kaynak koyarsan o da yalnız deneme paketine girer; yayın paketinde bulunması gereken hiçbir şeyi buraya
  koyma.

## Testleri

- Otomatik test yok. Lint bu dosyada `InsecureBaseConfiguration`'ı bilerek susturur; yayın paketinin lint'i ana dosyayı
  denetler.
- Elle (öykünücü, deneme paketi): bilgisayarda deneme sunucusu `3200`'de açıkken uygulamayı `http://10.0.2.2:3200`'e
  bağla; istekler gitmeli. Aynı adres yayın paketinde reddedilmeli.
- Bu belge yazılırken öykünücü ya da sunucu çalıştırılmadı.

## Son durum

- Dosya `3875db7 commit 4` (2026-09-26) ile eklendi ve o günden beri değişmedi. Aynı commit ana kaynaktaki
  `ag_guvenligi.xml`'i `false`'a çekti; http izni o güne kadar bütün paketlerde açıktı, bu dosyayla yalnız deneme paketine
  kaldı.
- Açık iş: deneme paketinde sunucu adresini seçen ekranın olmaması (yukarıda; kod değiştirilmedi).
- Planlı işlerde bu dosyaya dokunan bir madde yok; "Android yerel uygulama" işinin her aşaması öykünücüde deneme
  sunucusuna (`10.0.2.2:3200`) karşı denenmeyi istiyor, bu dosya o denemelerin ön koşulu.
