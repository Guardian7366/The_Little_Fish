# Contratos y recursos de diseño

La compilación usa directamente `lore/creditos-finales/creditos.json`, `dibujos/`, `foto/`, `pantalla-final/fondo.gif` y `tema-instrumental.wav`. Estas rutas son fuentes de la versión vigente y deben acompañar un checkout.

El resto de las carpetas conserva guías y manifiestos de zonas y enemigos. Sus recursos de ejecución se encuentran en `src/main/resources/`. Las rutas `assets/` de esos manifiestos identifican las entregas de origen. Las copias antiguas, las maquetas y las vistas de revisión se recuperan desde el historial de Git.

Las pruebas de conservación usan el manifiesto de quinta y `src/test/resources/prepared-assets.properties` para comprobar los bytes de los recursos vigentes. La letra canónica continúa en `lore/GUION-Y-ENCARGO.md` y la cronología de las 14 tarjetas en `lore/creditos-finales/creditos.json`.
