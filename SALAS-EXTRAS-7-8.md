# Dos refugios finales de D · Handy e Ic

Implementación sobre fe9f548, incorporado desde design/handy-ic. El 5 de noviembre de 2026, el usuario confirmó tras jugar que Handy e Ic funcionan como esperaba y autorizó su publicación. [Encargo y manifest](design/salas-extras-7-8/TAREA-CODEX.md)

## Parejas y recorrido

Los 37 PNG del paquete se copiaron sin modificaciones y se comprobaron sus dimensiones y transparencia. Se usan a escala ×3 con vecino más cercano. Ambas salas conservan el mapa interno 5, los tipos de NPC y los slots de recompensa.

| Sala | Puerta de quinta | Llegada | Regreso | NPC | Cofre |
|---|---|---|---|---|---|
| Handy | (9,5) | (39,9) | (40,9) | (39,10) | Slot 8, (28,11), Iron tipo 10 |
| Ic | (14,40) | (10,9) | (9,9) | (11,10) | Slot 7, (21,10), segunda arma tipo 3, ataque 3, tiempos 3/15 |

Handy tiene dos cerrojos en (32,6) y (37,11). Cada uno cede tras dos golpes distintos de Rocky y deja restos transitables. El mecanismo muestra el lado resuelto y enciende ambos rotores cuando abre el portón de (30,11). El cerrojo cercano queda en una zona iluminada, fuera de la línea de tiro. Dos apliques aportan luz local recortada por muros. Junto a una piedra intacta, aparece un recordatorio si Rocky no está equipado.

El Enfadillo del slot 86 vigila el paso de (36,8) desde (32,8). Usa la variante de burbuja de Spiky. El jugador puede salir lateralmente a (35,7), resolver ambos cerrojos con el enemigo vivo o derrotarlo. La derrota conserva los 7 EXP normales una vez. Los cerrojos no conceden EXP ni objetos. La conversación, los cerrojos y el cofre quedan fuera de la línea de tiro.

Ic tiene un tramo de aprendizaje que parte de (11,9) y llega a (15,9). Desde ese apoyo, los giros en (15,6) y (15,12) llevan a las marcas (13,6) y (18,12). Se pueden activar en cualquier orden y abren el paso de (19,10). El cofre está una casilla más al este que en la base para dejar libre el paso.

El hielo conserva la dirección cardinal a la velocidad normal desde que la caja de colisión entra por completo en una celda. Los apoyos centran la posición cuando la colisión lo permite. Ante un muro queda la última posición válida. Una tecla que seguía pulsada al parar debe soltarse antes de volver a usarse. Perder el foco libera las teclas y deja que el tramo en curso termine por inercia. Pausa, inventario y opciones congelan el movimiento. Cargar, cambiar de mapa o morir cancela el deslizamiento. Al cargar se conserva la posición válida, incluso si se guardó en pleno tramo.

## Persistencia y comprobaciones

ExtraRooms revisión 3 guarda máscaras independientes de cerrojos, golpes parciales y marcas, además de la derrota del centinela. Los estados de los seis retos aprobados y de los cofres permanecen separados. Los guardados de revisión anterior que ya cobraron una recompensa completan su reto al cargar. Si una partida anterior llega desde quinta o un tramo posterior sin Rocky, los cerrojos se inicializan abiertos y se conserva el estado del cofre de Iron. Esta excepción se guarda como progreso. No entrega ningún objeto ni se vuelve a evaluar al salir. En el recorrido nuevo, la tercera zona sigue exigiendo Rocky y el comercio de TV sigue después de la barrera de Iron.

Se generaron ocho partidas desde las fuentes de fe9f548 con casos de cofres cerrados y abiertos, Rocky presente y ausente, y posiciones que quedaron incompatibles. Se conservaron los seis retos previos y los IDs de las recompensas. Las posiciones incompatibles se trasladaron solo a la llegada de su mismo recinto. Cargar no modificó los archivos de origen.

Las pruebas recorrieron las cuatro direcciones desde 8.379 posiciones alcanzables con el portón cerrado y 11.139 con él abierto. En ambos estados se encontraron 69 paradas de deslizamiento, 44 contra muros. Todas las posiciones comprobadas permiten volver al portal. Con el portón abierto también permiten llegar al cofre. Se probaron ambos órdenes, entrada parcial, pulsaciones repetidas y opuestas, menús, pérdida de foco y recarga durante un tramo.

También se verificaron los dos órdenes de cerrojos con golpes reales, rechazo de las otras armas, partida guardada tras un golpe, esquiva, derrota persistente, cofres con inventario lleno y la barrera obligatoria de Iron en quinta. Las 32 esquinas comprobaron el aislamiento de cámara, mapa completo y minimapa. Las casillas visibles de los seis recintos aprobados se compararon con fe9f548 y coinciden.

## Evidencias

El registro de las 30 suites está en build/refugios-7-8-verification.txt. La importación opcional contra MyGame.jar se omitió porque el original no estaba disponible. Los guardados históricos están en build/final-refugios-baseline/. El ejecutable de prueba está en build/TheLittleFish.jar. Las capturas están en build/refugios-7-8-preview/, con los sufijos -npc, -reto, -cofre y -esquina-no, -esquina-ne, -esquina-so y -esquina-se para handy e ic.
