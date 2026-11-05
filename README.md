# IR Control Testing — Android

Aplicación de pruebas del emisor infrarrojo mediante la API oficial de Android \`ConsumerIrManager\`, sin root ni permisos de red.

**Versión:** 2.0.0 · **compileSdk / targetSdk:** API 37 (Android 17) · **minSdk:** API 24 (Android 7.0) · **Licencia:** MIT.

## Novedades de la v2.0

- Interfaz renovada: negro profundo, paneles grafito, acentos magenta y tipografía de alto contraste.
- Nombre visible **IR Control Testing** y cabecera sin identificación personal.
- Ícono vectorial de onda IR para el launcher, con variante adaptativa y monocromática.
- Se conserva el motor de transmisión de v1.1 para no alterar el comportamiento que ya funciona.

## Controles y límites

- Detección del emisor con \`ConsumerIrManager\`; la ROM debe exponer la interfaz IR.
- Portadora elegible entre frecuencias anunciadas por Android; respaldo de 38 kHz.
- Ciclo de actividad ajustable de **5 a 20 %**. Este control NO aumenta la potencia óptica instantánea ni la corriente del LED.
- Ráfagas IR de 100 ms, transmitidas en sesiones de 5, 15 o 30 segundos.
- Prueba rápida de 100 ms.
- Pausa de 3 segundos entre sesiones, apagado automático y detención al salir de la aplicación.
- La portadora IR está modulada: no se ofrece un modo de luz continuamente encendida.

## Descargar y probar la APK

1. Abre [GitHub Actions · Build IR Control APK](https://github.com/TKGHyper42/IRControl-Android/actions/workflows/build-apk.yml).
2. Selecciona una ejecución exitosa de la rama \`main\` para esta versión.
3. Descarga el artefacto **IRControl-APK** e instala \`app-debug.apk\`.

Es una APK de depuración. La compilación de GitHub Actions utiliza JDK 17, Gradle 9.6.0, Android Gradle Plugin 9.4.0 y SDK API 37. Para generar localmente, ejecuta \`gradle :app:assembleDebug\` con las mismas dependencias instaladas.

## Seguridad

No dirijas el emisor a los ojos. Detén la aplicación si el equipo se calienta. Los topes temporales son medidas precautorias, no una garantía térmica. Las pausas entre ráfagas dependen del controlador y del sistema.

## Código

La interfaz y la transmisión están en \`app/src/main/java/com/tkghyperx/ircontrol/MainActivity.java\`; los íconos son recursos vectoriales XML en \`app/src/main/res\`.

Licencia MIT.
