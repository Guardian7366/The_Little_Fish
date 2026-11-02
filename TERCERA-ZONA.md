# Tercera zona — 2026-11-01

Recorrido: llegada segura, primer Picaron, puerta temprana de Rocky (19,16), piedras de aprendizaje (22/23,20), circuito inferior con atajo de roca (27,30/31), Coco, encuentros combinados y salida original (20,44).

Se conservan 6 Enfadillos y 6 Picarones, sprites, recompensas y ataques originales. Enfadillos estacionarios con proyectiles registrados en el mapa actual. Picaron detecta a 5 casillas con visibilidad, abandona a mas de 7 o fuera de un territorio de 4 casillas por eje y regresa sin curarse. Los otros mapas conservan su comportamiento. La direccion de navegacion se actualiza durante persecucion/regreso para evitar oscilaciones en esquinas; los enemigos inactivos no buscan caminos.

Fondo por capas reutilizado, tono profundo y tres siluetas decorativas; sin carga de archivos durante el dibujo. Dimensiones 50x50 conservadas. Ayuda contextual cerca de rocas, secreto en (27,33) mirando a izquierda. Rocky se equipa manualmente.

Guardado: revision de tercera zona; adapta posiciones antiguas, conserva enemigos derrotados, salud y formas destruidas de las piedras. No modifica el archivo al cargar.

Validacion automatizada: suite general y ThirdZoneRegression (cofre accesible antes de Rocky, puerta principal bloqueada, destinos accesibles tras romper rocas, 3600 ticks de territorios, regreso real de Picaron, registro de proyectil en mapa 2, guardado y enemigo derrotado). Previsualizaciones en build/third-zone-preview. No sustituye la prueba manual ni mide FPS en la maquina del jugador.

## Ajuste visual aprobado — 2026-11-01
Suelo original 015, corales 059–065/067 adheridos a paredes, borde azul 014 y entrada 068. Recorrido y colisiones idénticos en las 2500 casillas; sin cambios de enemigos, eventos o guardado. Fondo propio con estratos verticales, luces frías y peces grandes; dos imágenes adicionales reutilizadas (0,75 MiB). Suite completa aprobada; imágenes revisadas en build/third-zone-preview.
