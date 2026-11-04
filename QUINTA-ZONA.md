# Quinta zona y Sharky: implementación y verificación

Fecha: **1 de noviembre de 2026**. Base: `ca4b3ad` más los cambios locales de la primera implementación, todavía sin publicar. Encargo: [TAREA-CODEX.md](design/quinta-zona/TAREA-CODEX.md), [DISENO.md](design/quinta-zona/DISENO.md), [ARTE.md](design/quinta-zona/ARTE.md) y [manifest](design/quinta-zona/assets/manifest.json). **El usuario probó y aprobó el comportamiento de Sharky, el recorrido general y la ambientación.** El usuario aprobó la revisión 2, con Iron obligatorio y las puertas adicionales corregidas, y autorizó su publicación el 4 de noviembre de 2026.

## Revisión 2: Iron obligatorio y puertas

- Se conserva el recorrido aprobado. Solo el corredor final pasa de tres a dos casillas de ancho, con dos bloques metálicos nuevos en (16,43) y (16,44), slots 21–22. El terreno sólido cierra sus extremos. No hay condición de inventario en la salida ni bloqueo invisible: romper un bloque abre físicamente un hueco transitable, que sigue abierto al cambiar de equipo.
- Permanecen los siete metales opcionales previos, la práctica inicial iluminada sin ataques, el atajo y los dos bloques Rocky. El último coral luminoso se sitúa en (15,44) para iluminar la barrera. No cambia el sistema de iluminación.
- Las puertas de Iron (9,5) y mejora de arma (14,40) muestran el tile original **086**. La salida conserva **068**. Eventos, destinos, retornos, salas y cofres no se modifican.
- El fondo aprobado, sus capas, criaturas, partículas, PNG preparados y código de Sharky permanecen idénticos a los de la primera prueba del usuario. `references/ambiente-generado.png` sigue siendo solo referencia y no se carga en el juego.
- Capturas actuales: `build/fifth-correction-preview/barrier-intact.png`, `barrier-open.png`, `iron-door-086.png` y `weapon-door-086.png`.

## Sharky, implementado primero de forma aislada

- Máquina de estados: acecho con patrulla corta a velocidad 1, preparación de 30 ticks, embestida cardinal a velocidad 8 con máximo de 240 píxeles, recuperación de 45 ticks, aturdimiento por choque/parry de 75 y regreso lento sin curación. Tras la recuperación hay al menos 90 ticks antes de otro ataque. El usuario aprobó este comportamiento en su prueba; no se modifica en la revisión 2.
- Detección a cinco casillas, franja frontal de media casilla, cuerpo completamente visible en pantalla y línea despejada. La dirección se fija al terminar la preparación. Los timers y overlays solo avanzan durante juego activo.
- Barrido píxel a píxel contra terreno, bordes, objetos sólidos, NPC, obstáculos destructibles y aliados vivos. Los overlays no amplían la caja de 42×42.
- Se adaptaron tanto el contacto del jugador como el de Sharky: daño únicamente durante una embestida, una sola resolución por pasada, incluso con invulnerabilidad o guardia. Parry frontal interrumpe y aturde; fallar no dibuja estrellas.
- Golpes y retroceso interrumpen el ataque; muerte, carga, recarga de mapa y repoblación no conservan ataques transitorios. Un bloqueo del regreso por un cuerpo provoca reintentos sin teletransporte ni curación.
- Coordinación por encuentro para los dos Sharkys finales; los encuentros independientes pueden preparar a la vez. Se conservan vida 20, ataque 9, defensa 0, experiencia 28 y drops.
- Los ocho sprites originales se conservan. Se integraron exactamente los 18 overlays preparados, a 96×96 y desplazamiento (-24,-24).

## Mapa y herramientas

Mapa interno **3**, 50×50. Sin mapas nuevos ni cambios en los IDs 0–7, mapa 5 o sus recompensas. Llegada (25,23), descanso (25,20), salida (9,44), puerta de Iron (9,5) y puerta de arma (14,40) conservadas, con retornos originales.

Hay seis Sharkys y tres Picarones. Se mantienen los slots de Sharky 40–45 y Picaron 33,36,39; se retiran 46–48 y 34,35,37,38. Los territorios se configuran al colocar y al cargar. Picaron incorpora el mapa 3 sin cambiar su selección de comportamiento en otras zonas.

El recorrido tiene refugio, cámara de presentación al este, brazo norte, aproximación occidental a Iron, circuito inferior y cámara final. Dos descensos mantienen rutas alternativas. Un desvío tranquilo conserva el acceso a la mejora de arma.

Nueve bloques metálicos: los siete opcionales anteriores (dos de práctica, dos de atajo y tres en el avance inferior), más los dos de la barrera obligatoria final. Dos piedras Rocky protegen un bolsillo opcional. Cada metal conserva tres golpes de Iron; Rocky sigue siendo otra herramienta. Las ayudas son contextuales, sin equipamiento automático. Un detalle opcional tras la primera pared se examina con Enter mirando arriba; no abre diálogo por proximidad.

La geometría concreta y los slots están en `FifthZone`; [tools/build_fifth_zone.py](tools/build_fifth_zone.py) reproduce exclusivamente `stage4.txt`. El plano de diseño sigue siendo una referencia esquemática, no el mapa final.

## Arte, fondo e iluminación

Se copiaron los **32 PNG del paquete** a `src/main/resources/quinta/`, con los mismos bytes y hashes. Ningún sprite o WAV original fue sustituido. El nuevo muro sólido usa el tile 88; corales transitables e indestructibles, 89–92. No se recolorea el tile 048 ni se altera la tabla original de recursos.

El fondo usa las capas nativas preparadas con parallax, partículas a un cuadro cada 30 ticks y dos criaturas decorativas a 15/18 píxeles de pantalla por segundo, con contraste reducido. Se cargan en caché, sin decodificar en draw y sin colisión.

Doce corales proporcionan luz fija, radio inicial 2,75 casillas. Una máscara del mundo a resolución nativa aplica oclusión por terreno sólido y se reconstruye al cambiar la revisión del mapa. La oscuridad ambiental se ajustó de alfa 215 a **165/255** tras inspeccionar las capturas, para conservar lectura entre luces. El centro de una fuente usa alfa 30. La linterna mantiene su radio **400**; se combina por mínimo de oscuridad, sin sumar alpha. No se oscurece el HUD ni se muestra «Day» en esta zona. Las otras áreas conservan su ciclo.

La oclusión fija considera paredes del terreno, no sombras móviles de enemigos ni cambios en metal/piedra. No se afirma iluminación física. El coste de píxeles de la caché nueva de luz y fondo es aproximadamente **2.689.408 bytes** (2,57 MiB, estimación de almacenamiento de píxeles; excluye objetos Java, sprites y resto del motor).

## Guardados

`fifthZoneRevision=2` identifica la barrera obligatoria. El campo sigue siendo compatible con su ausencia (valor 0) y con la primera implementación local (valor 1). Se mantienen `serialVersionUID=1` y versión de esquema 1. La preparación y validación del mundo termina antes de publicarlo en memoria.

Al migrar una revisión 0:

- Solo si el jugador estaba en mapa 3, se reubica en (25,23). Las partidas en otros mapas conservan su posición.
- Los enemigos retenidos conservan vida, muerte/ausencia y slot, pero los supervivientes pasan a las ubicaciones nuevas. Los siete slots retirados no se sustituyen por enemigos nuevos.
- Se conservan los obstáculos o escombros de slots 4,5,11–13, reubicados; se retiran las piedras 6–10. Los cuatro metales nuevos de la primera implementación usan 17–20. La revisión 2 añade únicamente los slots 21–22. No se regeneran al cargar guardados de revisión 2.
- Se conservan inventario, equipo, cofres originales abiertos y progreso ajeno a mapa 3. Los objetos sueltos de este mapa se reúnen junto al refugio.
- Sharky reanuda en acecho con espera inicial de 90 ticks; no se serializa una embestida a medias. Cargar no reescribe el archivo; la revisión se registra al guardar explícitamente.

Cambiar de área conserva la regla previa de repoblación de enemigos. Las revisiones desconocidas y actores inválidos se rechazan sin sustituir el mundo activo. Los guardados nuevos no deben llevarse de vuelta a ejecutables anteriores.

Al migrar revisiones anteriores a 2 se inicializa la barrera nueva. Una partida de revisión 1 dentro del metal, sobre el borde del corredor eliminado o detrás de la barrera se coloca en (17,44), en el lado desde el que puede volver por Iron. Se reubican también los objetos sueltos que quedarían incrustados; los demás conservan su posición. Enemigos heridos/derrotados y obstáculos opcionales conservan su progreso.

Si el guardado anterior ya estaba en la siguiente zona (mapa 4) o en el jefe (mapa 6), se conserva ese avance inicializando los dos bloques como escombros reales: podrá volver sin quedar encerrado. Es una adaptación del progreso anterior, no una condición nueva en el evento de salida. Si el cofre de Iron figuraba abierto pero Iron ya no estaba en el inventario, se vuelve a ofrecer la recompensa en ese mismo cofre; no se añade ni equipa ningún objeto automáticamente. La lectura no modifica el archivo original.

## Pruebas ejecutadas

Se ejecutó `./build.ps1 -Test` con JDK 25 y `--release 17`: **17 suites pasan**. Log actual de la corrección: `build/fifth-correction-verification.txt`; el log de la primera implementación sigue en `build/fifth-verification.txt`. Los errores de corrupción/versión mostrados durante los tests son rechazos esperados.

| Suite o comprobación | Resultado real |
|---|---|
| Sharky aislado | 1.261 aserciones: aviso a 2/5 casillas, guardia/parry cardinal, impacto único, colisiones, pausa y muerte congelada durante diálogo, regreso, independencia de encuentros y pareja escalonada |
| Quinta zona | 8.867 aserciones: prueba global sin bypass por las 23 transiciones reales, obstáculos opcionales tratados como abiertos; barrido de posibles huecos con colisión real; obtener Iron, volver y equiparlo, golpes reales, salida sin Iron equipado, puertas y cofre lleno con barrera intacta |
| Guardados de quinta | 214 aserciones: migración de revisiones 0/1 a 2, guardado nuevo, barrera destruida y cambio de área, rollback, heridas/derrotas, escombros, cofre abierto sin Iron y posiciones afectadas. Matriz sintética siempre activa y comparación adicional con archivos anteriores reales |
| Arte preparado | 32 PNG comprobados contra manifest: bytes/SHA-256, dimensiones y alpha |
| Iluminación | Oclusión, radio 400, mínimo de alpha, pausa y caché estable; fixture de carga con 40 luces y 32 Sharkys, incluyendo ataques activos |
| Suites existentes | Combate 37; recursos 13.012; audio 55; funcionamiento 16; historia 44; primera zona 87.923; menú 552.971; Enfadillo 32; segunda 68.118; tercera y cuarta pasan; guardado general 50 |

Los números grandes son aserciones, muchas repetidas, no partidas independientes. Se actualizaron las expectativas de población (88 enemigos totales y **27 tiles interactivos**, dos más por la barrera), y se excluyó el mapa 3 del contrato de «sin fondo nuevo» porque su rediseño inicial fue intencionado. Los 291 PNG originales siguen comprobándose por separado del arte nuevo.

Los fixtures antiguos se generaron compilando **fuentes del commit `ca4b3ad`**, con su clase original de serialización, sin reconstruir ningún JAR. Reproducción: `./tools/verify_fifth_baseline.ps1`, seguido de `./build.ps1 -Test`. Si faltan esos fixtures, la suite informa que omite la comparación contra la base real. Todo queda bajo `build/`.

Antes de corregir la revisión 1 local se conservaron sus clases y se generaron siete guardados reales en `build/fifth-revision1-fixtures/` mediante `tools/fixtures/FifthRevisionOneFixture.java`. Cubren nueva barrera, suelo eliminado, lado de salida, refugio con cofre abierto sin Iron y partidas en otros mapas. El generador exige clases de revisión 1; no sirve compilarlo contra la revisión 2 para recrear el pasado. Si esos archivos locales faltan, se informa la omisión; la matriz sintética de ocho casos (incluido mapa 6) se ejecuta siempre, sin depender de ellos.

`python tools/verify_fifth_resources.py` pasó: **334 blobs de recursos/JAR de la base sin cambios**, con normalización Git para texto; únicamente `stage4.txt` está excluido por ser el mapa rediseñado. Incluye el JAR raíz y los PNG/WAV originales. No se ejecutó la importación contra `MyGame.jar` del autor: ese ejecutable externo no está incluido; la comparación real realizada aquí es con la base autorizada `ca4b3ad`.

## Capturas y coste medido

- `build/sharky-preview/`: seis capturas aisladas con los dibujadores reales del motor, sin filtro oscuro para revisar los overlays: preparación, embestida, fallo, parry, choque y encuentros independientes.
- `build/fifth-zone-preview/`: mapa y veinte capturas del motor en diez puntos, con y sin linterna. Se revisaron visualmente mapa, refugio, primera cámara, preparación y comparación con linterna.
- Las capturas y logs están bajo `build/`, ignorado por Git; se reproducen con las suites, no se presentan como archivos publicados.

Medición histórica de la primera implementación aprobada, en Java2D headless, JDK 25, 180 cuadros tras 30 de calentamiento (`build/fifth-light-stress-final.txt`; no corresponde a una medición nueva de estas correcciones):

| Fixture | Update p50 / p95 | Dibujo p50 / p95 |
|---|---|---|
| Quinta con linterna, 12 fuentes y 9 enemigos | 0,023 / 0,084 ms | 6,312 / 7,264 ms |
| Estrés: 40 fuentes y 32 Sharkys; 124 ticks de ataque acumulados | 0,016 / 0,115 ms | 6,729 / 7,319 ms |

Son tiempos de esos fixtures, no FPS de una ventana interactiva ni una garantía de rendimiento en todos los equipos. La caché de imágenes no crece durante el dibujo repetido. Los datos no miden la memoria total del proceso.

## Entrega aprobada

El resultado está en **`build/TheLittleFish.jar`**. El JAR de la raíz conserva SHA-256 `dd17d1702600f95986dd1e09cb65ff9301dc0298abc921747b6103c097c96654`; `Jugar.cmd` sigue prefiriéndolo. Para probar el resultado, ejecutar desde una carpeta aislada `java -jar "C:\Users\dasip\Proyectos\Juegos-Creados\TheLittleFish\build\TheLittleFish.jar"`, sin copiar partidas personales originales; utilizar copias si se desea probar una migración.

La revisión 2 aprobada incluye la barrera obligatoria, las dos puertas restauradas y la migración de guardados descrita arriba.
