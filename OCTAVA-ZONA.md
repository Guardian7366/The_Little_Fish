# Octava zona · Galerías del templo

Implementación sobre b9d1104. El 4 de noviembre de 2026, el usuario confirmó tras jugar que la zona cumple los requisitos y autorizó su publicación. [Encargo y arte](design/octava-zona/TAREA-CODEX.md)

## Recorrido y encuentros

Tras resolver las cuatro parejas de séptima, su salida en (24,5) lleva al mapa 9 en (24,43). El retorno en (24,45) deja al jugador en (24,6) de séptima, fuera del activador. El descanso en (22,41) recupera vida y agua y guarda la partida. El portón en (24,6) protege el único paso al activador (24,5), que lleva al mercado de TV del mapa 10. Desde allí se alcanza la llegada original a la arena, (24,44) del mapa 6. El regreso desde el mercado deja al jugador en la casilla del portón abierto, fuera del activador de ida. Una partida antigua que llegue desde la arena con el portón aún cerrado retorna al sur de ese portón.

Se mantuvieron siete Plantas y cuatro Fishtellas. Las casillas son columna y fila desde cero. Se comprobaron los avisos en estos puntos del recorrido.

| Sector | Detección comprobada | Espacio para esquivar y recuperarse |
|---|---|---|
| Vestíbulo | Planta desde (24,39) | Desplazamiento a (25,39) y retirada al descanso |
| Oeste bajo y alto | Plantas desde (15,35) y (14,25) | Cámaras separadas por un recodo. Ruta de vuelta al descanso recorrida |
| Arcadas del este | Fishtella desde (36,27) y sobre su franja horizontal en (38,25) | Esquivas probadas en (38,26) y (37,27) para ambos ejes. El núcleo se abre tras la descarga |
| Cruce | Planta y Fishtella desde (23,25) | Columna central y entradas separadas por oeste y sur |
| Noroeste | Planta y Fishtella desde (16,13) | Espacio lateral al oeste y pilar desplazado al este |
| Noreste | Plantas desde (34,18) y (42,14), Fishtella desde (32,9) | Tres cámaras con recodos. Antesala norte sin enemigos |

En los puntos de aviso probados hubo hasta dos amenazas. Se recorrieron ambas alas hasta el descanso, se comprobó el recorte de franjas por muros y se probaron las casillas de lectura y los alrededores de los carriles sin activación de ataques. Las tres inscripciones se leen mirando hacia su estatua con Enter. Velo está en (8,22), Fauce en (43,24) y Centinela en (28,9). Cada estatua usa el PNG del paquete y queda sobre el muro de su nicho. El mapa usa la iluminación y las capas de séptima con una máscara calculada para su propia geometría.

## Rompecabezas y partidas

| Carril | Trayecto |
|---|---|
| Oeste bajo | (10,39) a (14,39) |
| Oeste alto | (8,14) a (8,10) |
| Este | (35,32) a (38,32) y luego a (38,36). El cambio de apoyo se hace en (38,31) |

Cada empuje conserva una posición de casilla completa. En el codo solo se acepta el nuevo sentido y el portón se abre con los tres vínculos exactos. El guardado tiene una revisión independiente. La migración instala las tres parejas iniciales de octava y conserva las placas, relieves y recompensas de Secret de séptima. Quien ya llegó al jefe conserva su mapa y posición.

Se ejecutaron 24 suites. Las seis órdenes pasaron con movimiento y colisiones reales. Las pruebas recorrieron los apoyos y comprobaron salida a ambos lados en cada estado, guardado y carga en el codo, portón persistente, reintento, partida nueva y transiciones. Se cargaron tres partidas creadas con las fuentes de b9d1104 en los mapas 3, 4 y 6. Se comprobó que cargar deja intacto el archivo y que se rechazan una roca fuera de su trayecto y un extremo sin el vínculo correcto. La importación opcional contra el MyGame.jar original quedó omitida porque ese archivo no estaba disponible.

## Capturas

Las imágenes se generan en build/octava-preview/. Incluyen velo.png, velo-sin-linterna.png, fauce.png, centinela.png, primer-encuentro.png, encuentro-mixto.png, carril-este-inicial.png, giro.png, carril-este-resuelto.png, porton-cerrado.png y porton-abierto.png. El registro está en build/octava-verification.txt y la compilación jugable en build/TheLittleFish.jar.
