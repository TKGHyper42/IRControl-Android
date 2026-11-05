# IR Control Testing — Android

Aplicación de pruebas del emisor infrarrojo mediante la API oficial de Android `ConsumerIrManager`, sin root ni permisos de red.

**Versión:** 2.0.0 · **compileSdk / targetSdk:** API 37 (Android 17) · **minSdk:** API 24 (Android 7.0) · **Licencia:** MIT.

## Novedades de la v2.0

- Interfaz renovada: negro profundo, paneles grafito, acentos magenta y tipografía de alto contraste.
- Nombre visible **IR Control Testing** y cabecera sin identificación personal.
- Ícono vectorial de onda IR para el launcher, con variante adaptativa y monocromática.
- Se conserva el motor de transmisión de v1.1 para no alterar el comportamiento que ya funciona.

## Controles y límites

- Detección del emisor con `ConsumerIrManager`; la ROM debe exponer la interfaz IR.
- Portadora elegible entre frecuencias anunciadas por Android; respaldo de 38 kHz.
- Ciclo de actividad ajustable de **5 a 20 %**. Este control NO aumenta la potencia óptica instantánea ni la corriente del LED.
- Ráfagas IR de 100 ms, transmitidas en sesiones de 5, 15 o 30 segundos.
- Prueba rápida de 100 ms.
- Pausa de 3 segundos entre sesiones, apagado automático y detención al salir de la aplicación.
- La portadora IR está modulada: no se ofrece un modo de luz continuamente encendida.

## Descargar y probar la APK

**[Descarga la APK más reciente en GitHub Releases](https://github.com/TKGHyper42/IRControl-Android/releases/latest)**

Cada nueva versión que compile correctamente en **main** tendrá su propio lanzamiento con una APK de nombre como **IRControl-Android-v2.0.0-API37.apk**.

Como respaldo, también se publica el artefacto **IRControl-APK** en [GitHub Actions · Build and Publish IR Control APK](https://github.com/TKGHyper42/IRControl-Android/actions/workflows/build-apk.yml).

La APK está firmada con una clave de depuración generada en GitHub Actions, no con un certificado de publicación estable. Si cambia la firma entre versiones, Android podría requerir desinstalar la versión previa antes de instalar la nueva.

## Cómo publicar una nueva versión

1. Incrementa **versionName** y **versionCode** en app/build.gradle. Por ejemplo, versionName '2.1.0' y versionCode 4.
2. Sube el cambio a **main**.
3. GitHub Actions compila la APK y crea automáticamente el lanzamiento de la nueva versión.
4. Si el tag de la versión ya existe, el lanzamiento anterior se conserva: incrementa la versión para publicar otra.

El workflow también admite ejecución manual desde GitHub Actions. Los lanzamientos solo se crean si la compilación ha concluido correctamente.

## Seguridad

No dirijas el emisor a los ojos. Detén la aplicación si el equipo se calienta. Los topes temporales son medidas precautorias, no una garantía térmica. Las pausas entre ráfagas dependen del controlador y del sistema.

## Código

La interfaz y la transmisión están en `app/src/main/java/com/tkghyperx/ircontrol/MainActivity.java`; los íconos son recursos vectoriales XML en `app/src/main/res`.

Licencia MIT.
