# Nueva cuarta zona — 2026-11-01

## Recorrido
Tercera zona -> Profundidades (nueva cuarta) -> antigua cuarta (ahora quinta).
Mapa interno 7, 50x50. Se mantienen los IDs 0-6, sus recursos, personajes y eventos originales. La salida (20,44) de la tercera conduce a (25,7) del nuevo mapa; salida nueva (25,45) conduce a la entrada anterior (25,23) de mapa 3, conservando musica y tipo de area.

Llegada segura con esmeralda de descanso/guardado (25,8), primer paso de Rocky, encuentro individual, bifurcacion izquierda/derecha, atajo central, rincon lateral, circuito inferior, segundo paso de Rocky y camara final con dos Picarones separados. Sin puertas a habitaciones adicionales.

## Contenido
Ocho Picarones: (25,17), (12,25), (37,25), (8,34), (17,33), (34,33), (20,41), (32,41). Mismas estadisticas y ataque; deteccion cercana, territorio de cuatro casillas por eje y regreso sin curacion. Navegacion y visibilidad usan el mapa activo. Otros enemigos y mapas anteriores conservan su comportamiento.
Ocho piedras en cuatro pares: paso inicial (25/26,11), desvio (8/9,30), atajo (25/26,27), paso final (25/26,35). Los dos pares opcionales no impiden llegar a la salida.
Tres secretos: (7,37), (29,29), (40,33), mirando arriba y pulsando Enter. Textos ambiguos sobre marcas que continuan, una flecha contradictoria y una figura familiar.
Mismos tiles azul marino, corales pegados a paredes y capas profundas de tercera zona. No se duplican las imagenes del fondo ni se cargan archivos durante el dibujo.

## Compatibilidad
Campo fourthZoneRevision (UID/esquema conservados). Las partidas anteriores inicializan solo el nuevo mapa, sin reubicar al jugador ni modificar el archivo al cargar. Las nuevas partidas guardadas conservan enemigos derrotados/heridos y piedras destruidas. Validacion de mapas contra recursos realmente cargados. Nuevos territorios se configuran tambien al cargar y al repoblar enemigos en transiciones segun la regla existente del juego.

## Verificacion
build.ps1 -Test -OriginalJar ../original/MyGame/MyGame.jar
FourthZoneRegression comprueba conectividad, piedras obligatorias/opcionales, desplazamiento con la colision real del jugador hacia salida/secretos, ocho territorios/regresos, entrada y salida seguras, secretos manuales y genera vistas en build/fourth-zone-preview.
StoryRegression comprueba las dos transiciones y el nuevo punto de guardado. SaveRegression comprueba migracion sin reescribir, ubicacion anterior preservada, poblacion inicial y progreso guardado de mapa 7. Resto de suites de combate, audio, titulo, zonas anteriores, recursos y guardado original incluidas.
Validacion visual mediante capturas del motor; falta la prueba manual del usuario. No se afirma una medicion de FPS en su equipo.
