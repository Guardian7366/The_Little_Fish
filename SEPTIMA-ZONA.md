# Séptima zona · Templo sumergido

Implementación sobre f160c37. El 4 de noviembre de 2026, el usuario confirmó tras jugar que el templo y los rediseños de Planta y Secret funcionaban como esperaba y autorizó la publicación. [Encargo y arte original](design/septima-zona/TAREA-CODEX.md)

## Recorrido y combate

El mapa interno 4 conserva la entrada (24,42) desde el mapa 8, el retorno (24,44) y la salida (24,5) hacia el mapa 6. Conserva el descanso original y añade uno al sur. El circuito pasa primero por una Planta aislada y luego ofrece cámaras con Planta y Fishtella. Se integraron los 71 PNG del paquete, tres murales de D y tres capas con desplazamiento independiente. Las lámparas iluminan los puntos de lectura y empuje. La linterna amplía la visibilidad de los tramos oscuros.

Planta avisa durante 36 ticks y tira hasta 24 ticks usando colisiones reales. Se puede cortar la liana con el ataque normal mientras el jugador está enganchado. El parry frontal y una obstrucción también interrumpen el ataque. Secret huye durante 180 ticks, deja hasta dos señuelos visuales y descansa durante 90 ticks. Cada ejemplar de los nichos oeste y este concede 100 EXP una vez por partida.

## Empujes y guardado

Cada empuje válido completa una casilla y la roca se fija en su placa. Cada carril tiene un pasillo lateral conectado en sus extremos. El portón comprueba las cuatro identidades y sus posiciones exactas.

| Galería | Roca inicial | Placa |
|---|---|---|
| Noroeste | (11,15) | (11,10) |
| Noreste | (35,12) | (40,12) |
| Suroeste | (13,31) | (8,31) |
| Sureste | (36,30) | (36,35) |

La revisión 1 del templo guarda los empujes completos, las parejas, los dos relieves y las derrotas individuales. Al cargar una partida anterior se conservan las posiciones válidas y se mueve al embarcadero (24,42) a quien aparezca en terreno incompatible. Se trasladan las placas resueltas y se respeta el portón ya abierto o el avance al jefe. Un Secret ausente o derrotado en su slot anterior se conserva como derrotado. Si una partida antigua ya había repoblado ese slot, el formato carece de evidencia para determinar si se cobró su EXP. La carga conserva el archivo de origen.

## Validación y capturas

Pasaron las 22 suites de build.ps1 -Test. La prueba del templo recorrió las 24 órdenes mediante movimiento y colisiones, verificó empujes desde los lados y el frente, pasos laterales, guardados parciales, el portón y las transiciones. También ejercitó la liana, el encuentro mixto, los dos relieves, el cansancio y la recompensa al guardar y cargar. Se probaron dos partidas generadas desde las fuentes anteriores al templo, una con una placa resuelta y otra con el portón abierto y el jugador en el jefe. La comprobación opcional contra el MyGame.jar original se omitió porque no estaba disponible.

El registro está en build/temple-verification.txt y el ejecutable de prueba en build/TheLittleFish.jar. Las capturas generadas en build/temple-preview/ incluyen landing-fixed.png (sin linterna), landing-lantern.png (con linterna), mural-d.png, vine-aim.png, vine-extend.png, vine-retract.png, mixed.png, secret-tired.png, plate-active.png, gate-closed.png y gate-open.png.
