# IR Control Testing

App Android para controlar el emisor infrarrojo mediante `ConsumerIrManager`.

## Funciones

- Detección del emisor IR.
- Frecuencia portadora configurable según el hardware.
- Ciclo de actividad ajustable del 5 al 20 %.
- Temporizador de 5, 15 o 30 segundos.
- Prueba rápida de 100 ms.
- Pausa de 3 segundos entre sesiones y apagado al salir.

## Descargar

[Releases](https://github.com/TKGHyper42/IRControl-Android/releases) · [Builds de prueba](https://github.com/TKGHyper42/IRControl-Android/actions/workflows/build-apk.yml)

## Compilar

Requiere JDK 17 y Android SDK API 37.

```bash
gradle :app:assembleDebug
```

## Licencia

[MIT](LICENSE)
