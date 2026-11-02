# Primera zona: arrecife de los recuerdos

Implementación aprobada el 30 de octubre de 2026. Base estable: `fb86bad`, etiqueta `primera-zona-base-estable`. Esta entrega desarrolla la primera zona; no es todavía la edición definitiva completa.

## Recorrido y límites

- Refugio de arena (10–16, 28–34): nacimiento, esmeralda (13,31), Shadow (15,29). Sin enemigos.
- Laguna inicial (19–25, 28–34): un Greeny, espacio para acercarse, atacar y retirarse.
- Paso norte (19–25, 20–24): un Greeny, acceso con espacio para observarlo y practicar guardia frontal. Sin puerta que exija usar una técnica específica.
- Laguna oriental (30–36, 19–24): un Greeny visible desde el acceso; espacio para disparar o acercarse. Se conserva el consumo de agua y el regreso al punto de recuperación.
- Encuentro final (31–38, 27–33): dos Greeny, separados al aparecer. Salida original (38,25) a la zona 2, sin bloqueo obligatorio de combate.
- Desvío occidental (10–15, 19–24): un Greeny y un rincón de recuerdos; vuelve a conectar con el camino principal.
- Desvío sur: pequeño santuario humorístico opcional, anunciado por coral violeta.
- Cueva original (28,18): mantiene su conexión con la habitación extra y su recompensa original. La decoración orienta sin marcar un objetivo obligatorio.

Los seis Greeny originales conservan sprites, vida, daño, experiencia y botín. Sus territorios separan encuentros. Mantienen su movimiento aleatorio original: no se añade persecución global ni se acumulan enemigos desde habitaciones anteriores. No se añade experiencia ni equipo de poder superior a esta zona.

## Orden de trabajo

1. Crear el mapa y comprobar accesibilidad con colisiones reales, incluidos caminos opcionales y ambas salidas.
2. Distribuir los seis enemigos, limitar su deambulación y verificar retroceso, muerte y restauración.
3. Añadir dos detalles interactivos opcionales y recordatorios breves de teclas, sin detener el juego automáticamente. Mantener los diálogos de Shadow.
4. Añadir profundidad con un fondo pixelado de cinco cuadros, movimiento relativo a la cámara y detalles de primer plano fuera del centro de combate. Crear y reutilizar cuadros en memoria; no abrir archivos durante cada dibujo.
5. Adaptar partidas anteriores de forma transaccional. Si estaban en la zona 1, reanudar en la esmeralda; recolocar solamente actores de esa zona que lo necesiten. Conservar enemigos derrotados, estadísticas, objetos, cofres y progreso de las demás zonas. No modificar el archivo hasta guardar de nuevo.
6. Ejecutar regresiones de combate, recursos, audio, transiciones, historia y guardado, y pruebas nuevas de rutas, territorios, adaptación y capas. Revisar visualmente antes de empaquetar.

## Criterios de entrega

Todas las salidas y secretos son accesibles sin equipo adicional. El refugio permanece libre de enemigos incluso tras varios minutos. Los recordatorios no bloquean controles ni abren diálogos solos. No hay lectura de recursos ni operaciones de audio nuevas en el dibujo. Todos los PNG originales permanecen intactos. Juego y proyecto se entregan en carpetas nuevas, sin sustituir versiones anteriores ni incluir partidas de pruebas.

## Referencias

Inspiración de enseñanza por situaciones: entrevistas de Nintendo sobre Super Mario Bros., https://iwataasks.nintendo.com/interviews/wii/nsmb/0/3/ . Referencia del principio de desplazamiento relativo y repetición de capas: https://docs.godotengine.org/en/4.5/tutorials/2d/2d_parallax.html . Se aplica el principio en Java2D; no se cambia el motor ni se incorporan recursos de esos juegos.
