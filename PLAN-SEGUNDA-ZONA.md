# Plan de segunda zona — posterior a validación de Enfadillo

Estado: planificación; no se modificaron mapas ni código. El usuario confirmó que Enfadillo funciona como esperaba y autorizó continuar, planificando primero si aún faltaba concretar el diseño.

## Base comprobada

Mapa stage2, índice1,50x50. Nueve Greeny y seis Enfadillos. Entrada(9,41), esmeralda(14,41), Tuti(23,34), salida(39,7) hacia mapa2(22,7). Dos accesos extra bidireccionales: (9,34) ↔ mapa5(40,40), y (39,23) ↔ mapa5(40,29). Mantener estas coordenadas y sus destinos para reducir cambios en conexiones. Mantener los cofres/recompensas de las habitaciones extra. Tuti conserva su sprite y diálogo original. No asumir que el comentario narrativo implica un rompecabezas de rescate.

## Objetivo

Enseñar a leer líneas de tiro y usar cobertura mientras se explora. Enfadillo conserva exactamente la mecánica validada: estacionario, intervalo120ticks, alcance detección4casillas, disparo cardinal con alineación16px, proyectil original. No añadir daño, cadencia ni enemigos nuevos para subir dificultad.

## Distribución propuesta

1. Refugio sur alrededor de entrada/esmeralda: sin enemigos ni líneas de tiro hacia el punto de aparición o guardado.
2. Primer tramo al este del refugio: 1Greeny y 1Enfadillo en encuentros separados. Mostrar al tirador antes de cruzar su línea. Isla de roca sólida y camino alternativo para observar, rodear, defenderse o atacar en su pausa.
3. Ramal occidental hacia acceso extra(9,34): 2Greeny y 1Enfadillo separados; recorrido opcional que vuelve a conectar cerca de Tuti, evitando un retorno largo por el mismo pasillo.
4. Arrecife central alrededor de Tuti y el ascenso: 3Greeny y 2Enfadillos en dos encuentros, nunca todos activos alrededor del jugador simultáneamente. Tuti queda en un bolsillo seguro, sin disparos entrando al terminar su diálogo. Coberturas interrumpen las líneas de tiro antes de las esquinas de entrada.
5. Ramal oriental hacia acceso extra(39,23): 1Greeny y 1Enfadillo. Desvío reconocible por vegetación/coral, con acceso libre sin una herramienta futura.
6. Tramo norte hacia salida(39,7): 2Greeny y 1Enfadillo. Reúne movimiento, lectura de tiro y combate con espacio para retroceder. Salida libre, sin obligación de eliminar toda la población.

Total conservado:9Greeny+6Enfadillos. Definir rectángulos de deambulación de Greeny en el plano de implementación para impedir que abandonen sus encuentros. No superponer líneas de Enfadillo sin cobertura y ruta de escape. El combate y los proyectiles existentes no se alteran para resolver un problema del mapa: primero se corrige la colocación.

## Exploración y aspecto

Cambiar los corredores largos repetidos por ensanchamientos, islas de arrecife y conexiones de retorno. Conservar las dos habitaciones extra y sus recompensas; añadir dos detalles opcionales de entorno (uno humorístico, otro una huella de recuerdos sin imponer explicaciones nuevas). Usar señales discretas en coral/arena, sin marcar cada secreto con flechas.

Extender el fondo marino Java2D aprobado de la primera zona con una variación sutil de profundidad/decoración. No usar el GIF de portada en gameplay. Sprites originales intactos. Paredes claramente sólidas y decoraciones claramente transitables. Recordatorio contextual breve cerca del primer Enfadillo, sin diálogo automático; evitar repetir tutoriales básicos ya aprendidos.

## Orden de implementación

1. Registrar versión estable y generar vista del plano: suelos, coberturas, refugios y conexiones.
2. Verificar accesibilidad real de ambas salidas extra, salida principal, descanso y Tuti sin equipo futuro.
3. Ubicar9Greeny+6Enfadillos respetando distribución; comprobar cada línea de tiro y territorio con lógica real. Ajustar geometría antes de decorar.
4. Añadir señales, detalles opcionales y variación del fondo.
5. Añadir revisión de layout de zona2 al guardado: conservar estadísticas, inventario, derrotas y resto del mundo; reubicar con seguridad en la esmeralda a quien cargue dentro del mapa anterior; adaptar supervivientes de zona2 sin resucitar muertos. No reescribir archivo al cargar. Las partidas de otras zonas mantienen su ubicación.
6. Comprobar regresiones, rutas reales, guardado anterior/nuevo, distribución sostenida, refugios fuera de tiro y todas las conexiones; revisar imágenes de encuentros antes de entregar.

## Criterios y alcance

Sin cambios en la portada, primera zona, zona3, jefe, atributos de enemigos ni mecánica restaurada. Evitar daño inevitable al aparecer, salir de una habitación o cerrar una conversación. Las recompensas opcionales no bloquean la historia. No se promete un balance definitivo sin prueba del usuario. No se agregan jefes ni cinemáticas en esta ronda.
