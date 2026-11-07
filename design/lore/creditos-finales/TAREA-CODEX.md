# The Little Fish · revisión del cierre y los créditos

Base jugable de referencia: 3c8bac8. El usuario autorizó publicar este paquete de revisión con las tres exportaciones pixeladas de su foto. Usar esas exportaciones y conservar la identificación visual de la gata. La implementación jugable queda para revisión posterior. El fondo de despedida se trabajó con ImageGen integrado sobre las dos referencias fotográficas del usuario. El GIF anima esa imagen con desplazamientos independientes en nubes y agua; es una animación de una imagen fija.

## Qué revisar

- [Maqueta de créditos y despedida](revision/Ver%20creditos.html). Abrir el HTML y pulsar Reproducir. El selector permite revisar las 14 escenas, el título y la despedida. La maqueta usa sprites y tiles originales de la copia disponible. Los recorridos son coreografías de muestra. El botón de la maqueta vuelve al inicio de la presentación.
- [Foto recogible](foto/foto-item.png) · [Foto de la cinemática](foto/foto-cinematica.png) · [Retrato de despedida](foto/foto-despedida.png). La versión pequeña recorta el rostro para que se lea en la arena. Las otras dos mantienen la pose y el entorno del original.
- [GIF de despedida](pantalla-final/fondo.gif) · [Composición de despedida](revision/pantalla-final.png). El GIF es un fondo limpio, 320 × 192, 48 cuadros de 100 ms, bucle continuo. El retrato, el agradecimiento y el botón son capas independientes.
- [Letra y tiempos](creditos.json). Contiene los 14 bloques y las 71 líneas traducidas al inglés. Los 14 PNG de `dibujos/` son dibujos blancos irregulares sobre transparencia. Cada tarjeta tiene una silueta distinta del personaje que se ve en su toma. En los refugios se dibuja al NPC anfitrión, y en el mercado a TV.

## Secuencia del juego

Mantener el paseo y el oleaje aprobados. Al recoger la foto usar primero los últimos diálogos de D y Echo, y después la foto con las líneas del epílogo que ya existen. Al concluir ese plano, fundir a negro gradualmente. Arrancar entonces los créditos y su reloj de 120 s. La pantalla negra entre partes se usa como transición visual, no como interacción.

| Tiempo desde el inicio de créditos | Pantalla |
| --- | --- |
| 0–4 s | The Little Fish en grande, centrado. Oscurecer rápido en los últimos 320 ms. |
| 4–118,5 s | Volver a mostrar imagen desde negro en 320 ms. Las 14 tarjetas de `creditos.json` cambian en los tiempos fijados. Marco blanco a la izquierda con la escena en movimiento, letra inglesa a la derecha y dibujo blanco abajo. |
| 118,5–120 s | Tras terminar la última tarjeta, fundir progresivamente a negro. |
| Desde 120 s | Desde negro, revelar en aproximadamente 1,7 s la última pantalla con mar y nubes en movimiento, retrato al centro, Thanks for playing y botón Play Again debajo. Mantenerla hasta recibir una acción deliberada. |

La pista instrumental será nueva y se entregará aparte. Preparar un punto de entrada para arrancarla con el título y sincronizar el reloj de las tarjetas. Durante pruebas sin pista, usar el mismo reloj en silencio. La letra se muestra en pantalla. Se conserva el cambio de tercera a primera persona a partir de «I wanted a good life», siguiendo el texto del usuario. Cuando llegue la música, ajustar la alineación fina de las tarjetas a la melodía conservando la duración solicitada de dos minutos y el orden. Los créditos de autoría y agradecimientos vigentes se mantienen disponibles. Colocar las atribuciones necesarias en una banda discreta del título o de las tarjetas, sin tapar letra ni ampliar los 120 s.

## Montaje de los microescenarios

| Orden | Zona | Referencia para la captura jugable |
| --- | --- | --- |
| 1 | Primera zona | Primer tramo abierto. El pez pasa un Normalillo que patrulla y gira al extremo de su ruta. |
| 2 | Refugio de Chicken | Refugio de Chicken. Chicken alterna reposo. El pez cruza por los faroles y uno se ilumina al pasar. |
| 3 | Refugio de Flower | Refugio de Flower. El pez rodea el claro y el guardián recorre un tramo corto. Flower permanece junto al remedio. |
| 4 | Refugio de Spiky | Refugio de Spiky. El pez pasa de una cobertura a otra. Mostrar un proyectil que recorre una línea despejada. |
| 5 | Primera zona de Picarón | Tercera zona principal. Picarón persigue dentro de su sector, anticipa un ataque y el pez se aparta. |
| 6 | Refugio de Pump | Refugio de Pump. El pez avanza entre macetas y corta uno de los amarres con una pose de ataque. |
| 7 | Refugio de Ghost | Refugio de Ghost. El pez pasa por el pedestal y tres marcas se encienden en secuencia. |
| 8 | Refugio de Hongui | Refugio de Hongui. Un Picarón anuncia el golpe y el pez lo bloquea mirando al atacante. |
| 9 | Primera zona de Sharky | Quinta zona principal. Sharky prepara y atraviesa un carril, y el pez lo esquiva lateralmente. |
| 10 | Refugio de Handy | Refugio de Handy. El pez golpea un cerrojo con Rocky, con la animación del equipo original. |
| 11 | Refugio de Ic | Refugio de Ic. El pez se desliza hasta una marca y se detiene en la piedra rugosa. |
| 12 | Primera zona de Planta | Templo de la séptima zona. Planta señala y extiende una liana. El pez sale del carril antes del contacto. |
| 13 | TV Supply | Mercado. El pez se acerca al mostrador y TV enciende su pantalla. La toma mantiene la vista en el mercado. |
| 14 | Arena de God Fish | Fosa final. Mostrar la pose de aviso y un barrido claro de God Fish, con el pez a distancia. Cerrar con el ojo visible. |

- Usar el arte que ya se muestra en juego, incluidos los NPC rediseñados y la versión vigente de God Fish. Los primeros encuentros de Picarón, Sharky y Planta son los de la ruta principal. Confirmar en la copia actual los mapas y variantes de sprites. Las carpetas `revision/sprites` son dependencias de la maqueta de revisión y no se copian al juego.
- Preparar escenas deterministas a partir de los recursos y encuadres de cada zona. Usar sus mapas o recortes de tiles y sprites por estado. Simular posiciones y animaciones mediante secuencias explícitas de espera, marcha, aviso, ataque y recuperación. Para combate, hacer que el pez esquive o bloquee según lo especificado. Encerrar las tomas en su propio estado de presentación. No cargar una partida ni simular el mapa jugable para reproducirlas. Los actores de créditos no modifican HP, inventario, enemigos persistentes, puertas ni eventos.
- El mismo reloj selecciona escena, bloque de letra y PNG de dibujo. Al cambiar tarjeta, inicializar juntos los tres y reiniciar el reloj local de esa toma. El marco blanco enmascara el dibujo de gameplay. Usar vecino más cercano. Ajustar la línea más larga al ancho real y permitir un salto visual en la misma página. Los márgenes del dibujo no se superponen con la letra.
- Conservar la foto pequeña en el punto real de recogida. Usar el retrato mediano sobre la playa durante el epílogo y el grande únicamente en la despedida. En la pantalla de 960 × 576, referencia: retrato (336,39) a 288 × 221, por encima del sol, título centrado cerca de y=410, botón (397,465) de 166 × 39. El fondo GIF se escala con vecino más cercano. Precargar/decodificar una sola vez y reproducir con su cadencia. El botón se construye en el juego, con foco y una pulsación nueva de Enter o clic. Como primera opción, Play Again vuelve al menú principal conservando la partida y dejando Nueva partida disponible. Limpiar controles, música y estado temporal al volver.
- Verificar el recorrido completo, incluyendo que el botón no se active con Enter retenido desde el diálogo, que el tema solo arranque una vez cuando exista, que las 14 tarjetas se vean en el orden dado, que los cambios no corten texto y que se pueda volver a iniciar desde el menú. Revisar al tamaño normal y en pantalla completa. Dejar la implementación jugable lista para revisión antes de publicar sus cambios.

## Fondo fotográfico

Prompt de ImageGen integrado usado para la base:

> Photo-based image edit, prepare the clean background plate for a 5:3 landscape 960x576 game ending. Use the second attached reference (golden sun, waves and wet sand) as the actual photographic basis and extend its left and right naturally to a wide view. Preserve the feeling of camera footage, textures, perspective and distinct wave fronts. Use the first attached reference only for color inspiration: dark blue-teal water and mauve rose upper clouds, mix with the warm golden soft glow of the second. Horizon about 46% from top, sun half peeking just above the horizon around 36% across the screen, wet sand at very bottom, waves breaking softly toward camera. Locked tripod viewpoint, no people, no silhouettes, no signs, no typography, no frame, no collage. Keep clouds naturally textured and subtle. Output realistic photographic plate, it will be downsampled separately to deliberately pixelated 320x192.

## Criterio de acabado para la revisión jugable

La maqueta solo fija la composición, la cadencia de las tarjetas y la intención de cada toma. Para implementar en Codex, construir cada escenario a partir de un recorte reconocible del mapa vigente, con sus paredes, suelos, decoración y un elemento propio de la zona. No reutilizar las plantas rectangulares ni los movimientos aproximados de la maqueta. Dibujar cada sprite con su tamaño, ancla y orientación originales. Mantener la trayectoria y el estado del actor al pasar de aviso a impacto y recuperación. En las tomas tranquilas, encuadrar una acción propia del refugio o del mercado.

Verificar las 14 tomas a velocidad normal. En cada una se debe distinguir el lugar, entender qué hace el pez y ver completar al menos una acción del personaje o mecanismo protagonista. En los ataques, comprobar entrada, aviso, impacto visible, esquive o bloqueo y recuperación. Los actores no se atraviesan, no cambian de posición entre cuadros y no muestran el lado contrario al movimiento. Validar que la ilustración de la tarjeta coincida con el personaje que aparece en la toma y que las 14 se vean una sola vez.

Usar Purisa del juego para The Little Fish, la letra, Thanks for playing y Play Again. Reutilizar el tamaño y contraste del menú donde funcione. Medir la caja de la letra con las métricas de la fuente real en la resolución lógica y ajustar los saltos de línea para dejar libre el espacio del dibujo. Las fuentes de sistema que aparecían en la primera maqueta eran de referencia.

## Puntos de integración en la playa aprobada

En la base 3c8bac8, CoastEpilogue carga foto-item y foto para el paseo y el epílogo. Cambiar solo sus recursos de foto por las versiones de este paquete y adaptar sus dimensiones de dibujado sin cambiar la hitbox de recogida. CutsceneManager controla la secuencia a partir de ENDING_COAST. Añadir ahí el fundido y los nuevos estados posteriores. Mantener el manejo de entradas y los contratos de guardado de la playa. Revisar LoreRegression y las pruebas de playa existentes antes de actualizar las expectativas que dependan del cierre anterior.
