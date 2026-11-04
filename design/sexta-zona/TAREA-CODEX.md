# Nueva sexta zona y Fishtella

Preparación artística y encargo de implementación. Base consultada: `6f2f887`, quinta zona aprobada. El usuario aprobó el ciclo de blindaje, descarga y núcleo expuesto y pidió preparar la expansión. La geometría, población y cifras de abajo son propuestas para la primera versión jugable.

## Recorrido

La nueva sexta prolonga el abismo morado. Reutiliza el suelo, corales, bordes, iluminación fija, criaturas lejanas y fondo por capas de la quinta. Mantén 50 × 50. Hay espacio para separar encuentros si el trazado tiene cámaras conectadas y descansos. El motor comparte dimensiones entre mapas, por lo que ampliar un mapa por separado no aporta suficiente valor aquí.

| Tramo | Encuentro inicial | Función |
| --- | --- | --- |
| Refugio iluminado | Ninguno | Llegada desde la quinta, guardado y salida de regreso para recuperar Iron si hace falta |
| Cámara de inducción | 1 Fishtella | Presentación aislada, suelo visible y espacio para esquivar en ambos ejes |
| Bifurcación | 1 Sharky por brazo | Una ruta abierta y otra con cobertura. Se reúnen antes del siguiente tramo |
| Taller lateral opcional | 1 Fishtella | Rodear una columna, leer dónde termina la descarga y encontrar una pequeña recompensa |
| Paso de hierro | Ninguno | Barrera visible entre paredes sólidas. Hay espacio iluminado para equipar Iron y golpear |
| Galería baja | 1 Sharky y 1 Fishtella | Primer encuentro mixto. Entra por cobertura y enfrenta ambos desde posiciones separadas |
| Últimas cámaras | 1 Sharky y 1 Fishtella en cámaras distintas, más 1 Sharky en un desvío | Variar ángulos y distancias, con un tramo tranquilo antes de salir |

Total de partida sugerido: cinco Sharkys y cuatro Fishtellas. Prioriza encuentros individuales y como máximo dos amenazas activas donde se mezclan. Usa los límites territoriales de Sharky y evita cubrir una ruta de evasión con una embestida durante el primer encuentro mixto. El fondo conserva criaturas lejanas con bajo contraste. Agrupa corales alrededor de muros y deja despejados los avisos, pasos y puntos de luz. La linterna sigue siendo útil sin ser requisito.

### Iron y conexiones

- Nueva sexta en el mapa interno **8**. Quinta sigue en el 3 y nueva cuarta en el 7. La antigua sexta conserva el ID 4 y pasa a ser séptima en el recorrido. Los IDs 5 y 6, las salas y el jefe conservan su función.
- Redirige la salida de quinta en (9,44) a la llegada segura del mapa 8. La salida del 8 conserva el destino anterior, mapa 4 en (24,44). El retorno desde el mapa 4 conduce al lado de salida de la nueva sexta. Separa los puntos de aparición de sus disparadores. En la entrada de la sexta, incluye retorno a un punto seguro de la quinta desde el que pueda volver al cofre de Iron.
- Pon dos bloques metálicos en un paso de dos casillas delimitado por muros sólidos. Con ellos intactos, todos los caminos desde la entrada y ambos ramales quedan separados de la salida. Un segundo par opcional puede abrir un atajo, siempre en el mismo lado de la barrera. Usa la colisión y los tres golpes de Iron ya existentes. No basta con comprobar inventario en el portal.
- Conserva abierta la barrera una vez destruida, aunque el jugador desequipe Iron. La zona asume que la quinta enseñó a usarlo. Reutiliza su aviso contextual. Si falta la herramienta, deja un regreso seguro hasta el cofre y conserva la recuperación de Iron de la quinta. No entregues ni equipes la herramienta automáticamente.

## Fishtella

Reutiliza `MON_Tella` con el comportamiento renovado activado en el mapa 8. Conserva el comportamiento de las apariciones antiguas hasta que se trabajen sus zonas. No cambies sus identificadores de guardado, recompensas ni sprites originales de archivo.

1. **Blindado.** Patrulla lentamente en una cámara asignada. El chasis reduce el daño mediante la defensa existente y los golpes siguen haciendo un mínimo de daño. Detecta jugadores cercanos y visibles, dentro del sector.
2. **Carga.** Se detiene y fija dos franjas opuestas, de una casilla de ancho, sobre el eje horizontal o vertical. Marca las casillas con líneas discontinuas. El aviso dura 45 ticks como primera prueba. El primer eje es horizontal y los ciclos completados alternan el eje. El visor y el sprite de carga complementan el aviso del suelo.
3. **Pulso.** Ilumina las mismas franjas en trazo continuo durante unos 10 ticks. Alcance inicial de cuatro casillas en cada sentido. Calcula las casillas al comenzar la carga, corta al primer sólido, incluidos bloques interactivos. Si cambia un obstáculo durante la carga, el pulso puede acortarse, pero nunca extenderse fuera de lo anunciado. Daño máximo de un impacto por pulso. Usa el contrato de daño e invulnerabilidad del jugador. Prueba el escudo frontal desde el origen del pulso y evita que un parry eléctrico active la lógica de la embestida de Sharky.
4. **Núcleo expuesto.** Abre el chasis durante unos 60 ticks. Reduce defensa para que los golpes y proyectiles tengan una ventana clara. Después se cierra y retoma la patrulla. Como valores de prueba, vida 45, defensa 10 cerrada y 2 abierta. Mantén inicialmente daño y recompensa del original y afina con una prueba jugable.

No inicia ataques fuera de pantalla ni a través de obstáculos. El chasis conserva la misma caja en los tres estados. La orientación del cuerpo y el eje eléctrico son independientes. Avisa el eje en el suelo, incluso si el jugador distingue mal los colores. Decide el impacto por intersección de la caja real del jugador con las franjas. El contacto con el cuerpo no debe sumar un golpe genérico al pulso ni castigar el acercamiento al núcleo abierto. Revisa `Entity.checkCollision()` y conserva el comportamiento de los demás enemigos.

No muevas a Fishtella ni recalcules un eje nuevo durante el aviso o el pulso. Si un evento de combate invalida el origen, cancela el ataque completo con su aviso. Un golpe que no desplaza al enemigo no debe reiniciar el temporizador constantemente. Pausa y diálogos congelan el ciclo. Muerte, carga, transición y recarga limpian avisos y ataques activos. El dibujo no avanza la simulación.

## Arte listo para integrar

- [Vista de los doce sprites](fishtella.svg) y [manifest](assets/fishtella/manifest.json). Tres estados por cuatro direcciones, PNG RGBA de 16 × 16. Centro y encuadre comunes. Escala ×3 con vecino más cercano. `carga` se usa también durante el pulso y `nucleo` solo durante la ventana vulnerable.
- Carcasa azul acero con contorno violeta, visor ámbar y núcleo turquesa. La apertura aumenta el área visible del núcleo. Las rotaciones están exportadas. Los archivos son dibujos de cuadrícula exacta basados en la propuesta aprobada, no recortes de la referencia generada.
- Para el aviso y el pulso, dibuja directamente las franjas del mundo. Aviso discontinuo y pulso continuo. Mantén el contraste con suelo morado e iluminación nocturna. No requieren sprites adicionales ni deben afectar al HUD. Conserva las señales originales de Sharky.
- El fondo y la luz de quinta se comparten desde sus recursos activos. Reutiliza imágenes y máscaras cuando proceda, con fuentes y geometría propias de cada mapa. No dupliques archivos ni cargues recursos durante el dibujo.

## Encargo para Codex

Lee AGENTS.md, QUINTA-ZONA.md y este documento. Primero implementa y prueba Fishtella aislado. Después construye la zona y sus transiciones, integra el arte y valida Iron y el guardado.

- Revisa `TileManager`, `EventHandler`, `GamePanel`, `Lighting`, `OceanLayers`, `FifthZone` y `AssetSetter`. Extiende las condiciones específicas de quinta solo cuando corresponda. Para Sharky, habilita la emboscada en mapa 8 y reutiliza sus estados, retorno y limpieza, sin modificar el balance aprobado de quinta.
- Registra una revisión nueva para el mapa 8. En un guardado anterior, inicializa sus entidades y obstáculos sin repoblar enemigos derrotados de los otros mapas. Conserva inventario, posiciones válidas y guardados ya avanzados en mapa 4 o 6. Verifica que el regreso de una partida avanzada al lado de salida del 8 permite volver a su mapa de origen incluso con la barrera cerrada. Documenta cualquier adaptación de progreso. Nunca escribas el guardado al cargar.
- Comprueba que aviso y pulso coinciden, que un muro corta ambos, que el pulso impacta como máximo una vez, que la ventana abierta es aprovechable y que los temporizadores respetan pausa y cambios de mapa.
- Comprueba el recorrido con las colisiones reales. La salida debe ser inaccesible con la barrera intacta desde el lado de entrada, incluso con el atajo abierto. Rocky y el arma normal no rompen el metal. Tras usar Iron, cruza, guarda y carga, cambia de equipo y verifica que la barrera sigue abierta. Recorre las transiciones en ambos sentidos y prueba guardados de quinta y de zonas posteriores con fixtures separados.
- Ejecuta `build.ps1 -Test`, revisa capturas del primer encuentro, el combate mixto y la barrera con y sin linterna, e informa resultados y pendientes de valoración manual. Mantén intactas las partidas personales y el JAR raíz. Actualiza la documentación de estado cuando se haya implementado y probado.

Esta entrega contiene sprites y diseño. La programación, el mapa jugable y el balance quedan para la sesión de Codex.
