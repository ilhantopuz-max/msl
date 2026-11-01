# 🌸 Masal'ın Dünyası — 1. Sınıf Türkçe Okuma-Yazma Uygulaması

7 yaş (ilkokul 1. sınıf) çocukları için, **tamamen çevrimdışı**, **reklamsız** ve **internet izni olmayan**
Android uygulaması. Akış: **Alfabe → Heceleme → Kelime/Cümle okuma**. Her bölüm bir öncekini bitirince açılır.

## Özellikler
- **29 harf**: büyük + küçük harf, sevimli resim (emoji), sesli telaffuz, **yazılış animasyonu** (kontür çizimi)
- Harf alıştırmaları: "Dinle, bu hangi harf?", "Bu resim hangi harfle başlıyor?", "Şu resmi seç"
- **Heceleme**: açık hece (ba, be…) → kapalı hece (al, em…) → 3 harfli → 4 harfli; "harfleri birleştir" oyunu
- **Okuma**: kelime–resim eşleştirme, kısa cümleler, **sesli okuma** (isteğe bağlı mikrofon kontrolü)
- Yıldız / "Aferin!" ödül animasyonu ve sesi, **günlük hedef**
- **İlerleme kilidi**: ders bitmeden sonraki açılmaz (ebeveyn istenirse tüm kilitleri açabilir)
- **Ebeveyn paneli** (basit toplama sorusuyla korunur): rapor, günlük hedef, tema, ses, mikrofon, sıfırlama
- "Tekrar et" ve "Önceki derse dön" butonları, sesli yönergeler
- Telefon + tablet (dikey/yatay) uyumlu, açık/koyu/sistem tema
- Güvenlik: `INTERNET` izni yok, analitik/reklam yok, veriler yalnızca cihazda

## Proje yapısı
```
OkuYazma/
├─ .github/workflows/build-apk.yml   ← otomatik APK
├─ settings.gradle.kts, build.gradle.kts, gradle.properties
└─ app/
   ├─ build.gradle.kts
   └─ src/main/
      ├─ AndroidManifest.xml
      ├─ res/ (values, drawable/ic_launcher.xml, raw/ ← ses dosyaları buraya)
      └─ java/com/okuyazma/app/
         ├─ MainActivity.kt
         ├─ data/   Content.kt (harf, hece, kelime, cümle içeriği) · Store.kt (ilerleme/ayarlar)
         ├─ audio/  Speaker.kt (ses dosyası veya TTS) · Reader (ses tanıma)
         └─ ui/     App.kt (gezinme) · Screens.kt (ana ekran, harita, ebeveyn)
                    Lessons.kt (ders akışı, sesli okuma) · Quiz.kt (alıştırma motoru)
                    Tracer.kt (yazılış animasyonu) · Components.kt · Theme.kt
```

## Kurulum ve APK alma

### Yol 1 — GitHub Actions (bilgisayar kurmadan)
1. Bu klasörü yeni bir GitHub deposuna yükleyin (`main` dalı).
2. **Actions** sekmesi → **Build APK** → çalışmanın bitmesini bekleyin (≈3-5 dk).
3. Çalışmanın sayfasında **Artifacts → MasalinDunyasi-debug-apk** dosyasını indirin, zip'ten `app-debug.apk` çıkarın.
4. Telefona/tablete aktarın, "bilinmeyen kaynaklardan yüklemeye izin ver" deyip kurun.
5. Sürüm yayınlamak için: `git tag v1.0.0 && git push --tags` → APK otomatik **Releases** sayfasına eklenir.

### Yol 2 — Android Studio
`File ▸ Open` ile klasörü açın, Gradle senkronize olsun, ▶ ile çalıştırın. (JDK 17 gerekir.)

### Yol 3 — Komut satırı
Gradle 8.7 + JDK 17 kuruluysa: `gradle assembleDebug` → `app/build/outputs/apk/debug/app-debug.apk`
(İsterseniz `gradle wrapper` ile wrapper üretip repoya ekleyebilirsiniz.)

> Not: Debug APK, debug anahtarıyla imzalıdır ve doğrudan kurulabilir. Play Store için kendi keystore'unuzla
> imzalı release paketi gerekir.

## Ses (seslendirme)
Varsayılan olarak cihazın **Türkçe Text-to-Speech** motoru kullanılır (çevrimdışı çalışması için
*Ayarlar ▸ Genel yönetim ▸ Metin okuma* içinde Türkçe ses verisini indirin).

**Profesyonel çocuk sesi eklemek için:** kayıtları `app/src/main/res/raw/` içine kopyalayın.
Dosya adı bulunursa TTS yerine o dosya çalınır, kod değişikliği gerekmez.

| İçerik | Dosya adı | Örnek |
|---|---|---|
| Harf | `l_<harf>` | `l_a.mp3`, `l_b.mp3` |
| Hece | `s_<hece>` | `s_ba.mp3`, `s_al.mp3` |
| Kelime | `w_<kelime>` | `w_elma.mp3` |
| "Aferin" | `aferin` | `aferin.mp3` |

Türkçe karakterler: **ç→cc, ğ→gg, ı→ii, ö→oo, ş→ss, ü→uu** (örn. `l_cc.mp3` = Ç, `w_kuss.mp3` = kuş).
Dosya adları küçük harf, yalnızca a-z, 0-9 ve `_` içerebilir.

## Sesli okuma (mikrofon) hakkında
Ebeveyn panelinden açılana kadar **kapalıdır**. Açıldığında Android'in konuşma tanıma servisi kullanılır;
cihazda çevrimdışı Türkçe konuşma paketi yoksa ses işlenmek üzere servis sağlayıcısına gidebilir.
Tam çevrimdışı garanti için paketi indirin veya özelliği kapalı bırakın.

## Ekran görüntüleri
Uygulamayı çalıştırıp görüntüleri `docs/screenshots/` klasörüne ekleyin ve buraya bağlayın:
```
![Ana ekran](docs/screenshots/home.png)
![Harf dersi](docs/screenshots/letter.png)
![Alıştırma](docs/screenshots/quiz.png)
```

## Geliştirme fikirleri
- Resimler: emoji yerine `res/drawable` içine sevimli çizimler (çok eski cihazlarda bazı emojiler görünmeyebilir)
- Gerçek yazım sırası: harf başına SVG yolu verisi ile `LetterTracer` genişletilebilir
- Daha fazla kelime/cümle: `data/Content.kt` içine eklemek yeterlidir
- Harf sesi (fonem) kayıtları: `l_*.mp3` olarak eklenirse "be" yerine "b" sesi çalınır
