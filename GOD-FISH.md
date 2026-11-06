# La fosa y God Fish

Revisión del combate sobre `e1d17bb`, aprobada por el usuario el 6 de noviembre de 2026. [Encargo revisado](design/jefe-final/REVISION-COMBATE.md). [Encargo y manifiestos](design/jefe-final/TAREA-CODEX.md)

La entrega sobre `d75386b` incorpora los esbirros de fase 4 y el ajuste de contacto del manto. El usuario aprobó ambos cambios y autorizó publicarlos. [Arte y manifiesto de Aguijón y Baluarte](design/jefe-final/esbirros/ARTE-ESBIRROS.md)

## Recorrido

El arco norte del mercado lleva a (24,44) del mapa interno 6. Desde la señal contigua (23,44), Enter permite regresar antes del encuentro. El cuello sur en (24,38) exige Rocky e Iron en la mochila. Al cruzarlo se cierra la puerta temporal situada detrás. El pez nada físicamente desde el umbral hasta la fila 35 mientras la cámara revela al titán. God Fish se dirige a D y el pez conserva la posición final de la escena cuando vuelve el control. El control vuelve antes de la primera advertencia. La primera introducción tiene cuatro páginas y el reintento de la misma sesión presenta la revelación y la última página.

La victoria otorga una llave y 500 EXP. La llave aparece en (24,32) y puede recogerse tras liberar un espacio en la mochila. Acércate a la puerta de (26,12) mirando hacia ella y usa la llave desde la mochila. Avanza por el pasillo hasta la salida en (26,5) para activar el desenlace y los créditos. Si el jugador cae durante el encuentro, **Retry** lo devuelve al mercado con sus recursos recuperados y el intento de God Fish reiniciado.

## Cómo jugar

- Antes de entrar, guarda y recupera vida y agua en la esmeralda del mercado. Lleva Rocky e Iron en la mochila. Si falta alguno, vuelve desde la señal del vestíbulo con **Enter**.
- Muévete con **WASD**, ataca con **Enter**, dispara una burbuja con **F** y mantén **Espacio** para cubrirte mirando hacia el ataque. Suelta y pulsa **Enter** para avanzar cada página de la introducción y para iniciar un nuevo golpe.
- Pulsa **C** para abrir la mochila, elige con **WASD** y confirma con **Enter** para equipar Rocky o Iron o usar un consumible. Pulsa **C** para volver al combate si sigues en la mochila.
- La barra segmentada muestra la fase y la condición para abrir su defensa. Al cumplirla, golpea al titán hasta el siguiente umbral. Al agotarlo, se anuncia el retorno al centro. La fase siguiente espera si ocupas la marca.

## Las seis fases

| Fase | Tramo de HP | Qué hace God Fish y cómo abrir la defensa |
| --- | ---: | --- |
| 1 · Verde petróleo | 650 → 540 | God Fish se acerca y barre con el tridente. Sal del carril por un costado. Equipa Rocky y conecta **tres golpes distintos en cada anclaje**. Las grietas muestran el avance. |
| 2 · Azul | 540 → 430 | Esquiva las salvas y rompe los **tres bloques** con Iron. Dos flanquean el eje de la arena y uno queda al sur. Después dispara con **F** al núcleo separado, al norte. El respiradero de (31,35) se ilumina si falta agua. |
| 3 · Violeta | 430 → 320 | Atrae la embestida a los **tres altares** en (18,23), (31,23) y (29,35). Espera a que se fije el aviso y apártate. Las roturas se conservan y solo el último acierto deja clavado el tridente. |
| 4 · Malva | 320 → 210 | Derrota de uno en uno a **Aguijón, Baluarte, Aguijón y Baluarte**. Aguijón anuncia tres puntas sucesivas y queda expuesto mientras recoge el tridente. Baluarte bloquea los ataques frontales con el escudo alzado. Esquiva su golpe corto y ataca durante la recuperación, cuando planta el escudo. Cada derrota confirmada corta un vínculo. |
| 5 · Carmesí | 210 → 105 | Después de tirar sus armas, God Fish avanza con cada puño. Esquiva el recorrido anunciado y ataca la mano mientras está plantada. Cada mano requiere **diez impactos distintos**. El progreso se conserva entre golpes y puedes completarla a melee. |
| 6 · Ámbar | 105 → 0 | Esquiva dos puños dirigidos y sal de la franja central antes de la onda. Los refugios azules están a ambos lados. Golpea el ojo durante los **cinco segundos** de agotamiento. Si sobrevive, repite los puños y la onda con la vida que le dejaste. |

## Balance

God Fish conserva los **650 HP**, los seis umbrales y el daño base. Se acerca y alterna flancos antes de anunciar su rumbo. El nado cambia de cuadro por cada diez píxeles lógicos recorridos. Mientras God Fish está protegido, tocar el centro del manto cuesta 1 HP y activa la invulnerabilidad habitual. El empujón corto usa una salida transitable hacia fuera o por un costado. Durante una apertura, el cuerpo deja de dañar, empujar y bloquear. Al cerrarse el ojo, la colisión corporal espera si el pez aún está dentro del manto.

Los avisos de los puños duran entre 40 y 44 actualizaciones a 60 Hz. En fase 5 la mano queda plantada durante 140 actualizaciones para admitir varios golpes. Se amplió el barrido y los puños avanzan con el cuerpo sobre una trayectoria comprobada contra el terreno. El retorno avisa durante un segundo y espera si el centro está ocupado.

Aguijón tiene 45 HP y daño base 7. Baluarte tiene 55 HP y daño base 9. Si el suelo próximo al sello está ocupado, la invocación busca un espacio libre junto a él y vuelve a intentarlo si todos los puntos están bloqueados.

## Persistencia

El punto de descanso del mercado sigue siendo el checkpoint normal. Los intentos activos y las cinemáticas no se guardan. Los archivos anteriores al rediseño que estaban en plena pelea pasan al vestíbulo de la arena con una fase 1 nueva y recursos recuperados. Un guardado anterior al combate conserva la posición cuando sigue siendo transitable y permite volver al mercado para buscar equipo. Una victoria anterior conserva su puerta, llave pendiente y salida, incluso cuando la posición de la llave antigua queda fuera del nuevo suelo. Cargar deja intacto el archivo fuente.

## Evidencia

La prueba automatizada conduce al pez con los controles del juego desde el mercado hasta los créditos. Usa un personaje de nivel 10 con el escudo inicial, Rocky, Iron y pociones disponibles. Comprueba el reintento, la presión en las cuatro esquinas, las cuatro invocaciones de Aguijón y Baluarte, sus avisos y sus impactos y dos ondas consecutivas con el daño del ojo conservado. También prueba el cierre norte hasta usar la llave. En la fase 4 verifica los tres centros de Aguijón, el bloqueo frontal de golpes y burbujas, el golpe y la recuperación de Baluarte, la aparición bloqueada y la limpieza al reintentar.

Los seis atlas de nado coinciden con las fuentes recuperadas y añaden 96 celdas con las anclas del manifiesto. Las pruebas de guardado usan dos archivos generados con las fuentes de `e1d17bb`, uno en el vestíbulo y otro tras vencer con la llave pendiente. También se comprueban seis guardados de `8ab5965`. La prueba opcional con `MyGame.jar` se omite cuando ese ejecutable no está disponible.

Pasaron las 34 suites de `build.ps1 -Test` y se repitió el recorrido completo con el JAR empaquetado. La comprobación final incluye el contacto entre ataques, la ausencia de daño y empuje durante la vulnerabilidad y la salida lateral junto a una pared. El registro queda en `build/god-fish-contact-verification.txt`. Las capturas quedan en `build/esbirros-preview/` y el JAR de prueba en `build/TheLittleFish.jar`.
