# Tres refugios de D · Chicken, Flower y Spiky

Implementación sobre 6d1bd4c. El 4 de noviembre de 2026, el usuario confirmó tras jugar que las tres salas funcionan como esperaba y autorizó su publicación. [Encargo y manifest](design/salas-extras-1-3/TAREA-CODEX.md)

## Parejas y recorrido

Los tres NPC conservan sus clases e IDs. Los cofres conservan los slots 1, 2 y 3 del mapa 5 y sus objetos originales. Se utilizan los 38 PNG del paquete con escala ×3 y vecino más cercano. Se verificaron dimensiones, transparencia real y copia idéntica de los archivos.

| Recinto | Puerta exterior | Llegada y regreso en mapa 5 | NPC | Cofre |
|---|---|---|---|---|
| Chicken | Mapa 0, (28,18) | Llegada (10,40), regreso (9,40) | (11,39) | (20,41), Linterna |
| Flower | Mapa 1, (9,34) | Llegada (39,40), regreso (40,40) | (39,42) | (28,37), poción de +5 |
| Spiky | Mapa 1, (39,23) | Llegada (39,29), regreso (40,29) | (39,28) | (34,32), cama portátil |

En Chicken se encienden con Enter los faroles de (12,40) y (16,36), mirando hacia ellos desde una casilla contigua. Ambos órdenes abren el portón en (18,41). Cada luz conserva su estado y añade un resplandor local. La mesa crea un desvío corto y el retorno queda fuera del portón.

En Flower, el NPC queda tras la pantalla de entrada y un solo Greeny patrulla el claro de (32,36) a (37,40). Conserva su vida, daño y recompensa originales. Su derrota abre (31,37) y queda registrada junto con la EXP, para que volver al recinto no repita el encuentro.

En Spiky, dos Enfadillos estacionarios ocupan (32,26) y (34,30). Solo esos ejemplares usan el nuevo comportamiento en el mapa 5. El camino inicial pasa por (36,26), rodea el muro por (27,28) y cruza (30,30). Se probaron salidas a (35,25) y (31,31) sin daño. Al lado del cofre, Enter mirando a la derecha desde (35,31) abre el atajo de (36,31). También funciona después de derrotar a los dos centinelas. Para permitir la esquiva con el movimiento normal, sus burbujas viajan a 5 píxeles por tick durante hasta 48 ticks. Los otros Enfadillos conservan su configuración.

## Aislamiento y guardado

ExtraRooms define ocho IDs y rectángulos fijos. La selección usa la casilla del jugador dentro de esos límites. La cámara centra cada recinto y el fondo propio completa los márgenes. Terreno, actores, cofres, luces, proyectiles, partículas y colisiones se filtran por el recinto activo. Las demás salas no avanzan sus animaciones, ataques ni muertes. El mapa completo y el minimapa muestran el rectángulo de la sala visitada, con el indicador del jugador ajustado a ese recorte. El fundido conserva el recinto de origen hasta cambiar de mapa y posición juntos.

La revisión independiente del guardado conserva dos bits de faroles, la derrota de Greeny, el atajo y las dos derrotas de centinelas. El estado de cada cofre queda separado. Un inventario lleno conserva el premio y permite regresar. Cargar un guardado anterior adapta un cofre ya abierto a reto terminado y recoloca a quien quedó en un muro o en el lado cerrado del portón. La recolocación usa la entrada segura de la misma sala. La carga deja intacto el archivo de origen.

Se preserva extraStage.txt y la geometría transitable de los otros cinco recintos. La variante extraRefugios.txt cambia solo los tres recintos autorizados y sus paredes contiguas. Las entradas de Pump, Hongui, Ghost, Ic y Handy conservan sus destinos, objetos y cofres.

## Validación

Pasaron los 26 grupos de la suite completa mediante build.ps1 -Test. Las nuevas pruebas comprueban las ocho parejas puerta/sala y sus regresos, las recompensas y la accesibilidad de los otros cinco cofres, los dos órdenes de faroles, el combate y la EXP única de Greeny, las dos esquivas sin matar, la variante con centinelas derrotados y el atajo persistente. Se guardó a mitad de los faroles, durante el combate de Flower, antes del atajo de Spiky y con un centinela derrotado.

Las 32 esquinas se compararon por píxeles al introducir actores y terreno de otra sala. Se verificaron el mapa completo, el minimapa y que los actores y proyectiles ajenos no se dibujaran ni actualizaran. Se probaron los tres cofres a través de la colisión del jugador con inventario lleno y se recargaron después de cobrar el premio.

Se generaron seis partidas con las fuentes de 6d1bd4c, una con cofre cerrado y otra con premio cobrado para cada sala. Se comprobaron la recompensa, la apertura del reto y la recolocación en la misma sala sin reescribir la partida. Las fixtures están bajo build/refugios-baseline/. La importación opcional contra MyGame.jar quedó omitida porque el original no estaba disponible.

La comprobación de conservación verificó los 334 recursos originales y el JAR frente a ca4b3ad. Las huellas del JAR raíz, la configuración y las partidas personales fueron iguales antes y después de las pruebas. El resultado de la ejecución está en build/refugios-verification.txt. El ejecutable de prueba está en build/TheLittleFish.jar. Las capturas están en build/refugios-preview/, con los sufijos -npc.png, -esquina.png y -cofre.png para chicken, flower y spiky. Las esquinas incluyen el minimapa. La revisión jugable del usuario aprobó la entrega.
