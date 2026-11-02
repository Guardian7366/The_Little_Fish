# Cambios de esta entrega

## Primera zona renovada — 31 de octubre de 2026

- Nueva distribución del arrecife inicial: refugio, cuatro áreas de combate, un circuito occidental y un desvío al sur. Se conservan los seis Greeny, sus atributos y recompensas.
- Territorios locales para evitar que los enemigos se acumulen o invadan el refugio. Los demás mapas conservan su comportamiento anterior.
- Dos detalles opcionales: una marca de recuerdo y una piedra sonriente. Se examinan con Enter mirando hacia ellos; no interrumpen automáticamente la exploración.
- Recordatorios contextuales de movimiento, ataque, defensa y disparo que desaparecen tras probar cada acción. Shadow y la habitación extra original permanecen.
- Fondo marino pixelado: cinco cuadros de animación lenta, dos capas con desplazamiento relativo y pequeñas partículas fuera del terreno jugable. Caché fija de aproximadamente 3,75 MiB; sin nuevas lecturas de archivos por cuadro.
- Adaptación de partidas de la primera zona anterior, conservando estadísticas, enemigos derrotados, objetos y progreso de las demás zonas.
- Ningún PNG ni WAV original fue modificado. Cambia `maps/stage1.txt`; el fondo nuevo se dibuja con geometría Java2D.

Los apartados siguientes documentan la estabilización previa. Su sección antigua de audio se complementa y corrige con [CORRECCION-AUDIO.md](CORRECCION-AUDIO.md).

## Partidas y progreso

- Se guardan destreza y defensa correctamente y se reconstruye el objeto de salida `Exit`.
- El guardado nuevo incluye mapa, posición, área, equipo y linterna, inventario, cofres, obstáculos destruidos, rocas y placas, estado de enemigos y progreso del jefe.
- Los objetos tienen identificadores estables; los nombres antiguos siguen reconocidos para importar partidas.
- La carga reconstruye y valida los datos antes de cambiar el mundo activo. Un archivo incompleto, una versión desconocida o un objeto inválido produce un error y conserva la partida en memoria.
- Se escribe primero un archivo temporal y se mantiene un respaldo del guardado válido anterior. Importar `save.dat` no modifica ese archivo.
- Reintentar conserva el progreso del mundo. Una partida nueva limpia objetos, proyectiles, equipo y estados anteriores.

Los enemigos comunes mantienen la regla original de reaparecer al cambiar de área. Guardar su estado en un punto de descanso no elimina esa regla. Las rocas de los rompecabezas ya no vuelven a su posición inicial al cruzar entre áreas.

## Combate, inventario y controles

- Soltar Espacio cancela la guardia incluso estando quieto. Se conserva la ventana de parry y la reducción del bloqueo.
- Un bloqueo con defensa alta ya no cura por producir daño negativo.
- Las burbujas hacen daño desde el nivel 1; se conserva la fórmula original desde el nivel 2.
- Una recompensa grande de experiencia concede todos los niveles cuyos umbrales se han alcanzado.
- Un objeto rechazado por inventario lleno permanece en el mundo; un cofre conserva su recompensa.
- Sin espacio para un proyectil no se consume agua. Cambiar de mapa libera el disparo pendiente y permite volver a disparar.
- P pausa como indica la ayuda y Y sigue funcionando. R recarga el mapa activo.
- Perder el foco libera movimiento, defensa y disparo. Enter no queda pendiente para saltarse un diálogo posterior o confirmar una opción al abrir el menú.

## Funcionamiento interno

- Se reconstruyó el proyecto y se añadió una compilación reproducible con pruebas, además de un JAR con punto de entrada ejecutable.
- El bucle conserva una cadencia de 60 actualizaciones por segundo y espera entre ellas. Se eliminó la espera activa continua.
- Las imágenes se comparten mediante una caché. Las vistas de mapa se generan cuando hacen falta, usando el mismo gestor de terreno.
- El audio utiliza un número limitado de voces y cierra los recursos; la ausencia de un dispositivo de audio no impide jugar.
- La búsqueda de caminos usa costes acumulados y evita el escaneo repetido de todos los obstáculos por cada casilla.
- Diálogos, compras, sueño, transiciones, cinemáticas, muerte y temporizadores avanzan con la simulación. Dibujar la pantalla no ejecuta esas acciones.
- Las preferencias se escriben al cambiarlas, en lugar de escribir el archivo en cada dibujo del menú.

## Contenido conservado y trabajo futuro

Se mantienen los 291 PNG, los 28 WAV y los siete mapas del JAR original. La población inicial comprobada es de **87 enemigos**, incluidos el jefe y los enemigos secretos. No se añadieron zonas, jefes, diálogos ni sprites.

Esta entrega estabiliza y reorganiza sistemas concretos. `Entity` todavía reúne muchas responsabilidades, existen capacidades fijas y gran parte del contenido se define en código. La futura expansión necesitará continuar esa separación. El selector de apariencias incompleto heredado no se convirtió en una función nueva. El peso del paquete sigue alrededor de 97,6 MB: conservar los WAV originales conserva también la mayor parte de ese peso.
