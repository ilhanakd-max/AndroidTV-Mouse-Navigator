# AndroidTV-Mouse-Navigator

Android TV kumandasıyla tüm sistem üzerinde gezinebilen fare imleci sağlayan **TvMouseNavigator** uygulaması. Bu depo, Android TV cihazlarında çalışan ve yön tuşlarıyla kontrol edilen bir overlay imleci sunan tam teşekküllü bir Android Studio projesi içerir.

## Proje Hakkında
- **Minimum SDK:** 21
- **Hedef SDK:** 34
- **Dil:** Kotlin + Jetpack Compose
- **Paket adı:** `com.ilhanakd.tvnavigator`
- **Proje adı:** `TvMouseNavigator`

Uygulama; bir foreground servis, erişilebilirlik servisi ve Compose tabanlı bir overlay katmanı kullanarak her uygulamanın üzerinde çalışan küçük bir imleç oluşturur.

### Temel Özellikler
- Overlay izni isteme ve kullanıcı onayı sonrası servisi başlatma
- Foreground servis üzerinden sistem genelinde Compose ile çizilen yüzen imleç
- DPAD yön tuşları ile yumuşak (25px adım) hareket ve sınır kontrolleri
- OK/ENTER tuşu ile MotionEvent tabanlı dokunma simülasyonu
- BACK tuşu ile servisi kapatma ve uygulamadan çıkma
- Leanback ana ekranlarına uygun TV başlatıcı desteği

## Servislerin Etkinleştirilmesi
1. Uygulamayı Android TV cihazınızda açın.
2. **Servisi Başlat** düğmesine basın.
3. İstenirse overlay iznini verin.
4. Uygulama erişilebilirlik servisinin etkin olduğundan emin olun. Eğer kapalıysa, **Servisi Başlat** düğmesi sizi erişilebilirlik ayarlarına yönlendirir.
5. İzinler tamamlandıktan sonra imleç tüm ekranlarda görünür.

## Projeyi Derlemek
Makinenizde JDK 17 bulunduğundan emin olun. Ardından:

```bash
./gradlew assembleDebug
```

> Not: Depoda Gradle wrapper JAR dosyası bulunmaz. Gerekirse `gradle wrapper` komutuyla kendi makinenizde oluşturabilirsiniz.

## Lisans
Bu proje [MIT Lisansı](LICENSE) ile lisanslanmıştır.
