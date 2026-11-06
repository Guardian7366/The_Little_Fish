# La fosa y God Fish

Guía de la entrega autorizada de God Fish, implementada sobre `8ab5965`. [Encargo y manifiestos](design/jefe-final/TAREA-CODEX.md)

## Recorrido

El arco norte del mercado lleva a (24,44) del mapa interno 6. Desde la señal contigua (23,44), Enter permite regresar antes del encuentro. El cuello sur en (24,38) exige Rocky e Iron en la mochila. Al cruzarlo se cierra la puerta temporal situada detrás. El jugador conserva sus coordenadas mientras la cámara revela el ojo y el titán se dirige a D. El control vuelve antes de la primera advertencia. La primera introducción tiene cuatro páginas y el reintento de la misma sesión presenta la revelación y la última página.

La victoria otorga una llave y 500 EXP. La llave aparece en (24,32) y puede recogerse tras liberar un espacio en la mochila. Acércate a la puerta de (26,12) mirando hacia ella y usa la llave desde la mochila. Avanza por el pasillo hasta la salida en (26,5) para activar el desenlace y los créditos. Si el jugador cae durante el encuentro, **Retry** lo devuelve al mercado con sus recursos recuperados y el intento de God Fish reiniciado.

## Cómo jugar

- Antes de entrar, guarda y recupera vida y agua en la esmeralda del mercado. Lleva Rocky e Iron en la mochila. Si falta alguno, vuelve desde la señal del vestíbulo con **Enter**.
- Muévete con **WASD**, ataca con **Enter**, dispara una burbuja con **F** y mantén **Espacio** para cubrirte mirando hacia el ataque. Suelta y pulsa **Enter** para avanzar cada página de la introducción y para iniciar un nuevo golpe.
- Pulsa **C** para abrir la mochila, elige con **WASD** y confirma con **Enter** para equipar Rocky o Iron o usar un consumible. Pulsa **C** para volver al combate si sigues en la mochila.
- La barra segmentada muestra la fase y la condición para abrir su defensa. Al cumplirla, golpea al titán hasta el siguiente umbral. El golpe que alcanza un umbral inicia la siguiente fase protegida.

## Las seis fases

| Fase | Tramo de HP | Qué hace God Fish y cómo abrir la defensa |
| --- | ---: | --- |
| 1 · Verde petróleo | 650 → 540 | Marca un carril y barre con el tridente. Sal del carril por un costado. Equipa Rocky y golpea los dos anclajes enlazados, uno a cada lado. Cada anclaje roto corta su enlace. |
| 2 · Azul | 540 → 430 | Marca las trayectorias de tres burbujas sucesivas. Esquiva, equipa Iron y golpea la rejilla. Luego alinéate con el núcleo descubierto y dispárale con **F**. Si se agota el agua, pasa por el respiradero iluminado en (30,35). |
| 3 · Violeta | 430 → 320 | Ponte junto al altar del sur, en (24,36), para atraer la embestida. Cuando aparezca el carril y se fije el rumbo, sal por un lado. La defensa cae cuando el tridente queda clavado en el altar. Si falla, puedes volver a intentarlo. |
| 4 · Malva | 320 → 210 | Deja espacio alrededor del sello de invocación. Derrota primero al Picarón y después a Fishtella, que aparece tras caer el primero. Esquiva los barridos del titán entre invocaciones. La segunda derrota rompe el último enlace. |
| 5 · Carmesí | 210 → 105 | God Fish arroja el tridente y el escudo y alterna sus puños. Esquiva el impacto y ataca el puño mientras quede plantado y marcado. Cuenta un golpe o una burbuja por lado. El indicador registra izquierda y derecha por separado. |
| 6 · Ámbar | 105 → 0 | Tras dos puños dirigidos, levanta ambos para lanzar la onda central. Nada hacia cualquiera de las dos franjas azules laterales. Cuando se agote, apunta al ojo expuesto con golpes o burbujas y esquiva sus golpes cortos anunciados. |

## Balance

God Fish conserva 650 HP totales. El daño efectivo tiene un piso de 8 por impacto válido y un intervalo de 18 actualizaciones. Cada apertura termina en su umbral. Los golpes sobre la barrera conservan la vida del jefe. Se omitió el Enfadillo opcional de la fase 1. Los dos esbirros de la fase 4 no generan premios repetibles.

Los avisos principales duran entre 54 y 88 actualizaciones a 60 Hz, entre 0,9 y 1,47 segundos. El golpe corto del jefe exhausto avisa durante 50 actualizaciones. Los puños de fase 5 se sostienen durante 100 actualizaciones tras el impacto. El daño base de los ataques principales va de 7 a 13 antes de defensa y guardia. La onda central deja tres columnas transitables a cada lado. Si el jugador se queda sin agua en la fase 2, el respiradero en (30,35) repone hasta dos unidades.

## Persistencia

El punto de descanso del mercado sigue siendo el checkpoint normal. Los intentos activos y las cinemáticas no se guardan. Los archivos anteriores al rediseño que estaban en plena pelea pasan al vestíbulo de la arena con una fase 1 nueva y recursos recuperados. Un guardado anterior al combate conserva la posición cuando sigue siendo transitable y permite volver al mercado para buscar equipo. Una victoria anterior conserva su puerta, llave pendiente y salida, incluso cuando la posición de la llave antigua queda fuera del nuevo suelo. Cargar deja intacto el archivo fuente.

## Evidencia

Pasaron las 34 suites de `build.ps1 -Test`. El recorrido usa un jugador de nivel 10 con el escudo inicial, Rocky e Iron. Se cargaron seis guardados generados con las fuentes de `8ab5965`, incluidos combate activo, llave pendiente y salida consumida. La prueba opcional contra el `MyGame.jar` original quedó omitida porque ese ejecutable no estaba disponible. La prueba de arte comparó los 43 PNG empaquetados con las fuentes y verificó dimensiones, transparencia y anclas de 24 atlas con 291 celdas. El registro de la suite completa queda en `build/final-verification.txt`. Las 16 capturas a 960 × 576 quedan en `build/final-preview/` y el JAR de prueba en `build/TheLittleFish.jar`. El recorrido automatizado usa las entradas del juego desde el mercado hasta los créditos y se repitió con el JAR empaquetado. Incluye muerte y reintento, los seis objetivos, recogida de llave y la puerta final. La revisión manual de dificultad y ritmo corresponde al usuario.
