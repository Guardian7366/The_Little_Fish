# Primera zona renovada: comprobaciones del 31 de octubre de 2026

Compilada en Windows con JDK 25 y compatibilidad Java 17. Entrega de contenido sobre la versión de audio corregida, sin cambiar `Sound.java`.

## Resultado

| Comprobación | Resultado |
|---|---|
| Combate | 37 comprobaciones pasan: guardia, parry, niveles, proyectiles, inventario y equipo |
| Audio | 55 comprobaciones pasan, incluidas las pruebas que bloquean el dispositivo simulado y verifican que el juego no espere |
| Funcionamiento general | 16 comprobaciones pasan: entrada de teclado, reinicio, pausa y cambios de mapa |
| Historia y progreso | 42 escenarios pasan: portales, descansos, cofres, NPC, herramientas, rompecabezas y jefe hasta créditos |
| Guardado | 41 comprobaciones pasan, incluidas adaptación de la primera zona, conservación de enemigos derrotados y rechazo transaccional de datos inválidos |
| Recursos | 291 PNG conservan sus píxeles; 280 consultas de rutas coinciden con búsqueda en anchura; recorte de terreno y cachés pasan |
| Zona nueva | Todo el suelo transitable está conectado; ambas salidas y los dos secretos se recorren usando la caja de colisión real del jugador |
| Distribución de enemigos | 7.200 actualizaciones por enemigo, equivalentes a dos minutos a 60 Hz, con retrocesos repetidos: ninguno sale de su área; el refugio queda seguro |
| Capas | Cinco cuadros diferentes en bucle; dibujar no avanza la animación; pausa detiene su actualización; recorte coincide con referencia completa |
| Zonas posteriores | Las capas nuevas no alteran los píxeles del terreno de los otros seis mapas |

La suite nueva realiza 87.926 aserciones, principalmente repeticiones de los mismos límites durante la simulación: no son 87.926 escenarios distintos. La suite de recursos realiza 13.930 aserciones; el total varía con los caminos del mapa, no con el número de pruebas.

Se ejecutaron todas las suites sobre la implementación inicial. Tras el ajuste final del recordatorio de descanso, la condición de orientación de secretos y la reutilización del objeto de transparencia, se recompiló y repitió la suite de primera zona con éxito.

## Compatibilidad comprobada con ejecutables anteriores

- El JAR original genera una partida antigua que la suite de guardado importa correctamente.
- La entrega `TheLittleFish-AudioCorregido` generó un guardado real sin el nuevo campo de revisión del mapa. El ejecutable nuevo lo cargó, colocó al pez en la esmeralda, conservó 17 monedas, 3 puntos de experiencia, un enemigo derrotado, otro herido y un cofre abierto en otra zona. El guardado posterior y su recarga también pasaron.
- Leer una partida anterior no reescribe su archivo. La adaptación se guarda solamente al volver a guardar.
- Se compararon los recursos contra el JAR original: 331 de 332 archivos permanecen idénticos. El único recurso modificado es `maps/stage1.txt`. Todos los PNG y WAV originales permanecen idénticos.

## Revisión visual e interactiva

Se revisaron imágenes producidas por el motor para el refugio, combate, guardia, disparo, encuentro oriental, cueva y ambos rincones opcionales. Se abrió el juego en una carpeta de pruebas independiente y se comprobó el inicio de partida, la apertura y cierre del inventario, el guardado en la esmeralda, la salida de su diálogo, el menú de opciones y el cierre del proceso.

El fondo usa cinco imágenes de profundidad y cinco de vegetación, de 384 × 256 píxeles: 3.932.160 bytes de almacenamiento de píxeles, aproximadamente 3,75 MiB, más el pequeño coste de los objetos Java. Se crean una sola vez por sesión. Cada cuadro dura 30 actualizaciones y el ciclo completo dura 2,5 segundos a 60 Hz. No es un GIF cargado en cada cuadro: es una animación equivalente dibujada y almacenada por Java2D.

## Límites de la verificación

Las pruebas de rutas y combate no sustituyen una partida humana completa por la nueva zona ni una evaluación de dificultad y ritmo. No se afirma compatibilidad con todos los equipos ni ausencia absoluta de fallos. La regla original de reaparición de enemigos al cambiar de área sigue vigente. Los demás escenarios y el combate final se verificaron automáticamente; no se repitió aquí una partida manual completa hasta los créditos.

## Repetir

`Compilar.cmd -Test` compila y ejecuta las suites. Para verificar también la importación contra el original, ejecutar `build.ps1 -Test -OriginalJar "C:\ruta\MyGame.jar"` en PowerShell. Los archivos e imágenes de prueba quedan bajo `build/` y no se distribuyen como partidas del juego.
