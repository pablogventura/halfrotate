# Pasos manuales para publicar HalfRotate v1.1.0

Todo lo automatizable ya está hecho en el repo. Completá estos pasos en las consolas web.

## Artefactos listos

| Artefacto | Ubicación |
|-----------|-----------|
| AAB firmado (Play) | `app/build/outputs/bundle/release/app-release.aab` |
| Keystore + contraseñas (backup) | `~/.halfrotate-secrets/` |
| Screenshots EN/ES | `store-assets/` y `fastlane/metadata/android/` |
| Política de privacidad | https://pablogventura.github.io/halfrotate/ |

## Google Play Console

1. **Crear app** (si no existe): package `dev.pablo.halfrotate`, categoría Tools.
2. **Ficha de la tienda** — textos en `fastlane/metadata/android/en-US/` y `es-ES/`:
   - Icono, feature graphic, screenshots (ya en repo)
   - Descripción corta/larga desde `short_description.txt` / `full_description.txt`
3. **Data safety**: sin recolección ni compartición de datos.
4. **Foreground service (Android 14+)**: tipo **Special use**, `screen_rotation_filter`. Subir vídeo ~30 s (requisito de Google).
5. **Content rating**: cuestionario IARC → Everyone.
6. **Política de privacidad**: `https://pablogventura.github.io/halfrotate/`
7. **Production** → nueva versión → subir `app-release.aab` → enviar a revisión.

## F-Droid (mismo día que Play)

1. Verificar tag `v1.1.0` en GitHub.
2. Abrir issue en https://gitlab.com/fdroid/rfp/-/issues/new
3. Copiar contenido de [`docs/FDROID_RFP.md`](FDROID_RFP.md)
4. Mencionar metadata en `metadata/fdroid/dev.pablo.halfrotate.yml`

## Recordatorios

- Play y F-Droid usan **claves distintas** — los usuarios no pueden cambiar de canal sin reinstalar.
- Guardá `~/.halfrotate-secrets/` en un gestor de contraseñas o copia cifrada externa.
- GPL: enlace a https://github.com/pablogventura/halfrotate en la ficha y en la app (About).
