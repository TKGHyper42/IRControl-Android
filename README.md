# IR Control — Android

Aplicación de código abierto para emitir señales infrarrojas desde cualquier dispositivo Android que exponga un emisor mediante la API oficial `ConsumerIrManager`.

**Versión:** 1.1.0 · **compileSdk / targetSdk:** API 37 (Android 17) · **minSdk:** API 24 (Android 7.0) · **Licencia:** MIT.

## Funciones
- Detección de hardware de infrarrojos por la API de Android, sin root.
- Transmisión mediante ráfagas cortas repetidas.
- Ciclo de actividad ajustable del 5 al 20 % y portadora configurable según lo que reporte el emisor (38 kHz como opción habitual).
- Prueba rápida de 100 ms.
- Temporizador de 5, 15 o 30 segundos, con intervalo de 3 segundos entre sesiones.
- Detención al minimizar la aplicación.

## Descarga de APK
En [Actions](https://github.com/TKGHyper42/IRControl-Android/actions/workflows/build-apk.yml), abre una ejecución correcta de **Build IR Control APK** y descarga el artefacto `IRControl-APK`, que contiene `app-debug.apk`. Es una APK de desarrollo firmada con clave de depuración, no una publicación en Play Store.

## Compilar manualmente
Abre este repositorio desde Android Studio, instala Android SDK API 37 y ejecuta `assembleDebug`. En GitHub Actions se usan Gradle 9.6.0, Android Gradle Plugin 9.4.0 y JDK 17.

## Compatibilidad y límites
- La compatibilidad **no depende de la marca**: requiere que la ROM exponga el servicio `ConsumerIrManager`. En algunas ROM personalizadas el LED IR físico no está disponible para apps.
- El porcentaje ajusta el patrón temporal de pulsos y **no** la intensidad óptica ni la corriente eléctrica del LED.
- Android puede introducir intervalos entre transmisiones. No es una señal continua garantizada.
- Evita exposiciones innecesarias del IR directamente a los ojos y detén la app si el equipo se calienta.
- La aplicación no solicita permisos de red.

## Organización
`app/src/main/java/com/tkghyperx/ircontrol/MainActivity.java`: interfaz y transmisión IR. `.github/workflows/build-apk.yml`: compilación automática.

Copyright © 2026 Ian.>_TKG. Distribuido bajo la [licencia MIT](LICENSE).
