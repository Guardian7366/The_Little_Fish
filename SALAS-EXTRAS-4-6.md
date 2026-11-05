# Tres refugios de D · Pump, Hongui y Ghost

Implementación sobre e982c07. El 5 de noviembre de 2026, el usuario confirmó tras jugar que las tres salas funcionan como esperaba y autorizó su publicación. [Encargo y manifest](design/salas-extras-4-6/TAREA-CODEX.md)

## Parejas y recursos

Se integraron los 43 PNG del paquete con escala ×3 y vecino más cercano. Sus dimensiones, transparencia y bytes se comprobaron contra el inventario. Los NPC conservan sus clases e IDs. Los cofres permanecen en sus slots originales del mapa 5.

| Sala | Puerta exterior en mapa 2 | Llegada en mapa 5 | Regreso en mapa 5 | NPC | Cofre |
|---|---|---|---|---|---|
| Pump | (39,13) | (10,29) | (9,29) | (11,28) | Slot 4, (20,30), poción +5 |
| Hongui | (9,17) | (10,19) | (9,19) | (11,18) | Slot 6, (21,15), Super Shield |
| Ghost | (19,16) | (39,19) | (40,19) | (39,18) | Slot 5, (29,21), Rocky |

El terreno, los fondos, los NPC y los props usan el paquete preparado. Se mantuvieron los límites de ExtraRooms y sus filtros de cámara, simulación, colisiones, partículas, proyectiles y mapas. Se verificó que todas las casillas visibles de Chicken, Flower, Spiky, Ic y Handy coinciden con la base e982c07.

## Retos y decisiones de integración

Pump tiene dos ataduras en (14,27) y (17,30). Un golpe cuerpo a cuerpo de cualquiera de las cuatro armas actuales las corta y deja restos sin colisión. No producen EXP ni objetos. Ambas abren el portón en (19,31). Se desplazó el paso una fila respecto al primer trazado de implementación para evitar que el cofre obstaculizara la entrada. El retorno queda en el lado del NPC.

Hongui ofrece un único Picarón en el slot 77. Conserva su ataque, vida y EXP originales y patrulla el claro de seis por cinco casillas, entre (13,15) y (18,19). El dosel y el muro separan al NPC. Se requieren dos impactos distintos resueltos con guardia frontal o parry. Un indicador se enciende por cada bloqueo. Cada ciclo solo puede registrarse una vez. La espera con Space, la invulnerabilidad y los impactos traseros no cuentan. Dos guardias normales o dos parries con el escudo inicial abren el portón de (19,21).

Derrotar al Picarón completa también el reto, incluso antes del primer bloqueo. La victoria por guardia deja vivo al enemigo y no añade EXP. Su derrota posterior concede los 15 EXP normales una vez y se conserva al volver o cargar. El cofre se reubicó en (21,15), fuera de su alcance. Se comprobaron 300 ticks en ese nicho sin daño.

Ghost funciona con el arma y el escudo iniciales. Enter mirando arriba desde (37,19) activa el pedestal. La secuencia fija es triángulo, rombo y cuadrado. Cada símbolo se muestra durante 60 ticks, seguido de una pausa de 20 ticks, a 60 Hz. Se puede repetir sin borrar el prefijo aprendido. Durante la observación se suspende el registro de respuestas y se mantiene el movimiento.

Los símbolos están en (34,16), (37,16) y (35,20). Una respuesta cuenta cuando la caja de colisión completa entra en su casilla. Permanecer quieto no añade respuestas. Un error borra el prefijo. Salir y volver conserva el aprendizaje, y reentrar en una piedra vuelve a evaluarse según el siguiente símbolo esperado. Completar abre (31,21). No hay enemigos ni piedras destructibles en el archivo. El portón de regreso permanece disponible.

## Persistencia

ExtraRooms usa revisión 2. Los bits 0 a 5 de las primeras tres salas conservan su significado. Los campos nuevos guardan la máscara de dos ataduras, bloqueos 0 a 2, derrota independiente del Picarón y prefijo de memoria 0 a 3. El estado de cada cofre sigue separado del reto.

Los guardados anteriores inicializan los nuevos retos según sus cofres. Un premio ya cobrado completa su reto, conserva el inventario y evita repetir la nueva baja de Hongui. Se recoloca al jugador solo si la posición quedó incompatible, siempre dentro de la misma sala. Se comprobaron seis partidas generadas con las fuentes de e982c07 y la conservación de faroles, Greeny, centinelas y atajo anteriores. Cargar deja intactos los archivos de origen. La demostración y los contadores transitorios de ataque se cancelan al cargar.

## Validación y capturas

Pasaron las 28 suites de build.ps1 -Test. Las pruebas cubren las tres entradas y retornos, los dos órdenes de corte con las cuatro armas, los dos bloqueos, dos parries, la muerte antes o después de bloquear y la EXP única. También se verificaron demostración, pausas, errores, espera sobre símbolos, prefijos parciales, carga durante la observación, regreso parcial y solución sin equipo opcional.

Se comprobaron cofres con inventario lleno mediante la interacción del jugador, premios cobrados y las 32 esquinas de las ocho salas. Las imágenes de terreno y actores ajenos no alteraron el recinto, el mapa completo ni el minimapa. Las pruebas de las tres primeras salas siguieron pasando. Se rechazaron estados de progreso fuera de rango sin aplicar una carga parcial.

El registro completo está en build/refugios-4-6-verification.txt. La ampliación final de las pruebas de doble parry y regreso parcial de Ghost se ejecutó además en build/refugios-4-6-focused.txt. Las partidas históricas están en build/later-refugios-baseline/. La importación opcional contra MyGame.jar se omitió porque ese original no estaba disponible.

Las capturas están en build/refugios-4-6-preview/. Para pump, hongui y ghost se entregan los sufijos -npc.png, -reto.png, -cofre.png y -esquina.png. Ghost incluye también demostracion-0, demostracion-2 y demostracion-1. El ejecutable de prueba está en build/TheLittleFish.jar. La revisión jugable del usuario aprobó la entrega.
