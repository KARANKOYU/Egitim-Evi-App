# gradle/wrapper/KLASOR.md

Gradle sarmalayıcısı (Gradle Wrapper): `gradlew`/`gradlew.bat`'ın çalıştırdığı küçük başlatıcı `gradle-wrapper.jar` ve hangi
Gradle sürümünün (8.14.3) nereden indirileceğini söyleyen `gradle-wrapper.properties`.

## Bu dosya ne yapar?

Bu depoyu derlemek için bilgisayara Gradle kurmak gerekmez. Kökteki `gradlew` (Windows'ta `gradlew.bat`) bu klasördeki jar'ı
çalıştırır; jar `gradle-wrapper.properties`'e bakar, istenen Gradle sürümü bilgisayarda yoksa bir kez indirir ve derlemeyi
onunla yapar. Böylece projeyi derleyen herkes (ve her bilgisayar) aynı Gradle sürümünü kullanır. Betiklerin anlatımı
[../../KLASOR.md](../../KLASOR.md)'de.

## İçinde neler var?

### `gradle-wrapper.jar`

Gradle'ın ürettiği ikili dosya (43.764 bayt); sarmalayıcının kendisi. Elle yazılmaz, açılıp okunmaz, düzenlenmez. İçinde
Gradle'ın kendisi yoktur: yalnız ayar dosyasını okuyup doğru dağıtımı indiren ve başlatan küçük bir program. Depoya girer
(sarmalayıcının çalışması için gerekli); `.gitignore` `*.jar`'ı dışlamaz.

### `gradle-wrapper.properties`

| Ayar | Değer | Anlamı |
|---|---|---|
| `distributionBase` | `GRADLE_USER_HOME` | İndirilen dağıtımın açılacağı kök: kullanıcının Gradle klasörü (Windows'ta genelde kullanıcı klasöründe `.gradle`) |
| `distributionPath` | `wrapper/dists` | O kökün altındaki yer |
| `distributionUrl` | `https\://services.gradle.org/distributions/gradle-8.14.3-all.zip` | İndirilecek Gradle: 8.14.3, `all` dağıtımı (kaynak ve belgeleriyle) |
| `networkTimeout` | `10000` | İndirmede 10 saniyelik ağ zaman aşımı |
| `validateDistributionUrl` | `true` | Dağıtım adresinin doğrulanması açık (Gradle'ın kendi ürettiği değer) |
| `zipStoreBase`, `zipStorePath` | `GRADLE_USER_HOME`, `wrapper/dists` | İnen zip'in saklandığı yer |

`distributionUrl`'deki `\:` properties biçiminin kaçışıdır, adresin parçası değil.

## Kimle konuşur?

- **Çağıran:** kökteki `gradlew` ve `gradlew.bat` (`-jar "$APP_HOME/gradle/wrapper/gradle-wrapper.jar"`),
  [../../KLASOR.md](../../KLASOR.md).
- **Okuduğu:** `gradle-wrapper.properties`.
- **İnternet:** dağıtım yoksa `services.gradle.org`. `--offline` ile çalışırken dağıtım önceden inmiş olmalı.
- **Uyum:** kök `build.gradle`'daki Android eklentisi (AGP 8.13.0) bu Gradle sürümüyle çalışır; biri yükselirse öbürü de
  gözden geçirilir.

## Nasıl çalışır (adım adım)?

```
./gradlew <görev>
  ─► java -jar gradle/wrapper/gradle-wrapper.jar <görev>
       gradle-wrapper.properties ─► gradle-8.14.3-all.zip
       GRADLE_USER_HOME/wrapper/dists/gradle-8.14.3-all/... var mı?
          var ─► o Gradle'ı başlat
          yok ─► indir (10 sn ağ zaman aşımı), aç, başlat   (--offline'da bu adım yapılamaz)
  ─► Gradle settings.gradle'dan başlayıp görevi çalıştırır
```

## Dikkat!

- **Gradle sürümünü elle değiştirme; görevle değiştir.** Sürüm yükseltmek için `./gradlew wrapper --gradle-version <sürüm>`
  çalıştırılır: hem `.properties`'i hem gerekirse jar'ı ve betikleri günceller. Yalnız `distributionUrl`'yi değiştirmek de
  çalışır ama jar ve betikler eski kalır.
- **İndirilen zip'in özeti denetlenmiyor.** Dosyada `distributionSha256Sum` satırı yok; Gradle'ın yayımladığı SHA-256 özetini
  bu satıra eklemek, bozuk ya da değiştirilmiş bir dağıtımın kullanılmasını engeller (öneri; bugün yok).
- **Java sürümü:** Gradle 8.14.3 çok yeni Java sürümlerinde (ör. 25) açılmaz; bu projede `JAVA_HOME` JDK 21'i gösterir
  ([../../TANITIM.md](../../TANITIM.md), "Derleme ve deneme").
- **Jar'ı silme ya da `.gitignore`'a ekleme.** Silinirse `gradlew` çalışmaz.

## Testleri

- Otomatik test yok. Sarmalayıcının çalıştığı her derlemede görülür: `./gradlew --version` Gradle 8.14.3 yazmalı.
- Bu belge yazılırken Gradle çalıştırılmadı.

## Son durum

- İki dosya da `34b45f1 commit 1` (2026-09-26) ile geldi ve o günden beri değişmedi.
- Açık iş: `distributionSha256Sum` önerisi (yukarıda); kod değiştirilmedi.
- Planlı işlerde Gradle sürümünü değiştiren bir madde yok.
