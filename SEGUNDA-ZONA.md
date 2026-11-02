# Segunda zona renovada

Implementación del plan aprobado. Se mantienen los nueve Greeny y seis Enfadillos, las conexiones, las habitaciones extra y sus recompensas. No cambian los atributos, proyectiles ni cadencia del Enfadillo restaurado. Tampoco cambian los sprites originales, la portada, el mapa inicial o los mapas posteriores.

## Recorrido

Refugio sur con entrada y esmeralda originales. Primer encuentro al este con un Greeny, un Enfadillo y una isla de roca para romper su línea de tiro. Un circuito de regreso conecta este tramo con el refugio de Tuti. El ramal occidental conduce a la primera habitación extra; el centro separa los encuentros de dos tiradores; un desvío oriental conduce a la segunda habitación extra. El tramo norte combina los enemigos restantes y desemboca en la salida original a la tercera zona.

Los Greeny tienen territorios locales. Las islas sólidas de coral proporcionan cobertura; la vegetación decorativa sigue siendo transitable. Tuti mantiene su diálogo original. Hay dos detalles opcionales que se examinan mirando hacia arriba con Enter: una piedra humorística al oeste y una marca de recuerdos al norte. No se abren automáticamente.

Se extiende el fondo marino por capas de la primera zona, con una variación de tono discreta. El recordatorio de cobertura aparece cerca del primer tirador durante seis segundos acumulados de juego activo; después desaparece. No se usa el GIF de portada durante el gameplay.

## Partidas anteriores

El guardado incluye una revisión de la distribución de la segunda zona. Al cargar una partida de la versión anterior que estaba en esa zona, el pez aparece en la esmeralda (14,41). Se conservan estadísticas, inventario, enemigos derrotados, salud de supervivientes, cofres y resto del mundo. Los supervivientes se colocan en sus nuevas ubicaciones y los objetos sueltos de esa zona se reúnen cerca de la esmeralda. Las partidas guardadas en otras zonas mantienen la posición del jugador.

Cargar no reescribe el archivo; la adaptación se conserva al guardar de nuevo. Conviene conservar la copia previa para volver a una versión con mapas anteriores. Las reglas de reaparición al cambiar de área siguen siendo las del juego.

## Validación

- Todas las casillas transitables conectadas. Recorridos comprobados con la caja de colisión real del jugador hacia esmeralda, Tuti, tres salidas y dos secretos.
- Verificación de alcance, alineación y línea de tiro contra los puntos protegidos de entrada, descanso, conversación y retorno de habitaciones.
- Nueve Greeny simulados durante7200actualizaciones cada uno: ninguno abandona su territorio y el refugio conserva la salud del jugador.
- Secretos sin activación automática, recordatorio de cobertura con duración limitada e imágenes del motor para revisar composición y coberturas.
- Migración del layout anterior, conservación de derrotas y recompensas, rechazo de revisiones desconocidas, roundtrip exacto del nuevo guardado y preservación de partidas en otras zonas.
- Prueba adicional con guardado generado por el ejecutable anterior real: conserva17monedas, enemigo herido y otro derrotado; reanuda en la esmeralda nueva.
- Suites completas pasan: combate37, audio55, funcionamiento16, historia42, Enfadillo32 y guardado49. Recursos12798aserciones, primera zona87925, menú552971. Segunda zona68118aserciones finales (principalmente repeticiones de límites, no escenarios distintos).

Tras la suite completa se limitó a seis segundos el recordatorio de cobertura, se recompiló y se repitió la suite de segunda zona. Se revisaron capturas generadas por el motor, no se afirma haber completado una partida manual de la zona. Dificultad y ritmo necesitan la valoración del jugador.

Registros locales: build/second-zone-verification.txt, build/second-zone-final-test.txt, build/second-zone-preview. El generador tools/build_second_zone.py permite reproducir el mapa; el juego no necesita Python.

Entrega directa en la carpeta TheLittleFish-MenuMarino, con proyecto actualizado en TheLittleFish-Proyecto-MenuMarino. No se sustituyen partidas ni se generan ZIP nuevos: los ZIP anteriores siguen representando sus entregas originales.
