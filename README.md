# The Little Fish — menú marino y primera zona renovada

Esta entrega desarrolla la primera mejora de contenido sobre la base reconstruida y corregida: un nuevo recorrido inicial, encuentros distribuidos por áreas, dos rincones opcionales para descubrir y un fondo marino animado por capas. Conserva todos los sprites originales, las mecánicas, las siete zonas y el desenlace. La edición definitiva completa sigue siendo un objetivo posterior.

Lee [PRIMERA-ZONA.md](PRIMERA-ZONA.md) para conocer el diseño y [VALIDACION-PRIMERA-ZONA.md](VALIDACION-PRIMERA-ZONA.md) para las comprobaciones. La corrección de las pausas de audio está incluida.

El menú incorpora el GIF marino aprobado, con escalado sin suavizado, proporciones conservadas y textos originales con mayor contraste. Detalles y comprobaciones: [MENU-MARINO.md](MENU-MARINO.md).

Actualizaci�n actual: [segunda zona renovada](SEGUNDA-ZONA.md), con coberturas para Enfadillo, encuentros distribuidos, secretos y adaptaci�n de partidas anteriores.

## Jugar

1. Extrae por completo el paquete del juego en una carpeta propia.
2. Abre `Jugar.cmd`. Necesitas Java 17 o posterior instalado y disponible en el equipo.
3. Elige una partida nueva o carga una partida desde el menú.

También puedes ejecutar `java -jar TheLittleFish.jar` desde esa carpeta. La distribución no incluye Java. Los archivos de partida y configuración se escriben en la carpeta desde la que se inicia el juego; `Jugar.cmd` utiliza su propia carpeta.

| Acción | Tecla |
|---|---|
| Moverse | WASD |
| Confirmar, interactuar o atacar | Enter |
| Disparar burbuja | F |
| Defender y hacer parry | Espacio |
| Inventario y equipo | C |
| Pausar o continuar | P o Y |
| Opciones | Esc |
| Mapa | M |
| Minimapa | X |

Los controles de desarrollo heredados siguen disponibles: T muestra información de depuración, G alterna el modo invulnerable y R recarga el mapa actual.

## Guardar y recuperar una partida

Para trasladar una partida de la versión corregida, **copia** `save-v1.dat` a la nueva carpeta y elige LOAD GAME. Si estaba en la primera zona anterior, se reanuda en la esmeralda del mapa renovado con el progreso conservado. Si estaba en otra zona, se mantiene allí. Esta adaptación no modifica el archivo hasta que guardes otra vez. Conserva la carpeta anterior; no lleves el nuevo guardado de vuelta a una versión con el mapa antiguo.

Para experimentar el nuevo inicio con sus encuentros completos, elige NEW GAME en una carpeta sin tu partida personal. Se mantienen las reglas originales de reaparición de enemigos al cambiar de área.

Interactúa con un punto de descanso esmeralda usando Enter. El formato nuevo guarda en `save-v1.dat`. Desde el segundo guardado válido, `save-v1.dat.bak` conserva el guardado válido anterior. La carga valida el archivo antes de sustituir la partida en memoria; si falla, aparece un error.

Para importar una partida antigua, **copia** su `save.dat` a una carpeta del juego que todavía no tenga `save-v1.dat` y selecciona cargar. El archivo antiguo permanece intacto. Al guardar en un punto de descanso se crea el formato nuevo. El formato antiguo no incluía la posición ni todo el estado del mundo: se reanuda al inicio con los atributos, inventario y objetos recuperables. No es posible recuperar información que nunca se guardó.

Para restaurar el respaldo, cierra el juego, conserva una copia del `save-v1.dat` actual y copia `save-v1.dat.bak` con el nombre `save-v1.dat`. No hay un botón de restauración automática. Si existe un archivo nuevo dañado, el juego muestra el error; no carga silenciosamente el formato antiguo.

## Compilar y comprobar

El proyecto requiere un **JDK 17 o posterior**, con `jar`, `javac` y `java` disponibles en su carpeta `bin`. No necesita bibliotecas externas ni descargar dependencias.

- `Compilar.cmd`: genera `build/TheLittleFish.jar`.
- `Compilar.cmd -Test`: compila, ejecuta las pruebas y genera el juego.
- En PowerShell: `./build.ps1 -Test`.
- Para incluir además la prueba de importación contra el ejecutable original: `./build.ps1 -Test -OriginalJar "C:\ruta\MyGame.jar"`.

La compilación entregada se produjo con JDK 25 y `--release 17`. El paquete recibido no contenía fuentes Java: esta reconstrucción partió del JAR mediante CFR 0.152. Los archivos recuperados no son los fuentes originales del autor.

Lee [CAMBIOS.md](CAMBIOS.md), [VALIDACION.md](VALIDACION.md) y [ARQUITECTURA.md](ARQUITECTURA.md) para conocer el alcance, las comprobaciones y los límites de esta entrega.

Actualización: [corrección de pausas de audio e instalación conservando partidas](CORRECCION-AUDIO.md).
