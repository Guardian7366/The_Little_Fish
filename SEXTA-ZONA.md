# Sexta zona y Fishtella

Implementación del encargo de `c7ae5e8`, verificada el 4 de noviembre de 2026. El usuario probó la nueva zona y Fishtella, confirmó que funcionaban como esperaba y aprobó la entrega para publicación. [Encargo y arte](design/sexta-zona/TAREA-CODEX.md)

## Combate

Fishtella usa los doce PNG de 16 × 16 del paquete. Patrulla a velocidad 1 y prepara un pulso al ver al jugador dentro de su sector. El primer aviso es horizontal. Cada ciclo completo alterna de eje. El aviso dura 45 ticks, el pulso 10 y el núcleo queda expuesto 60. Son 0,75 s, 0,17 s y 1 s a 60 actualizaciones por segundo. La vida inicial es 45, la defensa baja de 10 a 2 y se conservan el ataque 10, la recompensa de 30 EXP y el drop original. Las apariciones anteriores de MON_Tella conservan su comportamiento y su identificador de guardado.

El aviso discontinuo y el pulso continuo comparten la misma lista de casillas. Cada brazo alcanza hasta cuatro casillas, se detiene ante el primer sólido y puede acortarse si aparece una obstrucción. Romper un obstáculo durante la carga no amplía el pulso. La caja del jugador determina el impacto. La guardia se evalúa desde el origen eléctrico y el parry no desplaza al enemigo. Fishtella no aplica daño por contacto. Un cambio de posición cancela un pulso que ya había fijado su origen. Cargar, recargar o repoblar el área reinicia el ciclo. Pausa y diálogo lo congelan.

## Recorrido

El mapa 8 mide 50 × 50. El refugio lleva a una cámara con un solo Fishtella. Después se abren dos ramales con Sharky, un taller opcional con Fishtella y una moneda, y un atajo de Iron en el lado de entrada. Dos bloques en (24,20) y (25,20) ocupan todo el ancho del paso obligatorio. Las paredes separan ambos lados. El encuentro mixto viene después de la barrera. La última bifurcación separa los encuentros y termina en un tramo tranquilo.

La quinta sale al refugio del mapa 8. El retorno desde el refugio llega a (17,44) de la quinta, del lado que permite volver al cofre de Iron. La salida de la sexta llega a (24,42) del mapa 4. El retorno desde el mapa 4 aparece en (25,6) del mapa 8, con camino de vuelta a la salida aunque el metal siga intacto. Las cuatro llegadas quedan separadas de los disparadores. El mapa 4 pasa a ser la séptima zona del recorrido y conserva su ID.

La sexta usa las capas, criaturas y máscara de iluminación de la quinta. La geometría y los corales del mapa 8 determinan su propia distribución de luz. Las señales se dibujan después de la oscuridad y antes del HUD para conservar su contraste. [Generador del mapa](tools/build_sixth_zone.py)

## Guardados y pruebas

`sixthZoneRevision=1` inicializa solo los actores, la moneda y los obstáculos del mapa 8 en partidas anteriores. Mantiene las posiciones, el inventario y el progreso de los mapas anteriores. Si falta Iron y su cofre figuraba abierto, el cofre vuelve a ofrecerlo durante la migración o al regresar a la quinta con la barrera de sexta intacta. La obtención y el equipamiento siguen siendo manuales. La barrera y el atajo se guardan como obstáculos o escombros según su estado. Cargar conserva los bytes del archivo.

`./build.ps1 -Test` completó las 20 suites. La prueba aislada cubre la coincidencia de franjas, el corte por muros y metal, el impacto único, el parry frontal, los 60 ticks de núcleo y la pausa. La prueba del mapa recorre las cuatro transiciones, comprueba el acceso al cofre, descarta un bypass con las 25 transiciones y el atajo abierto, prueba las colisiones a intervalos de cuatro píxeles y cruza con el arma normal después de romper el metal con Iron. También verifica un contraataque cuerpo a cuerpo tras apartarse y volver al núcleo, y una esquiva que evita las dos amenazas del primer encuentro mixto.

Se probaron tres archivos generados con el guardado anterior a la sexta en los mapas 3, 4 y 6. Se comprobó que cargar no reescribe sus bytes, que el mapa nuevo se inicializa y que las bajas antiguas se conservan. Las pruebas incluyen el recorrido de salida con la barrera cerrada desde el lado al que regresa una partida avanzada. El JAR original externo para la prueba opcional de importación no estaba disponible. Registro local en `build/sixth-verification.txt`.

El ejecutable de prueba está en `build/TheLittleFish.jar`. Las capturas del motor están en `build/sixth-preview/` y se regeneran con la suite. Incluyen aviso, descarga y núcleo del primer Fishtella, encuentro mixto y paso de Iron con y sin linterna.
