# Encargo para Codex · La fosa y God Fish

Base visual `063e0fa`. Implementar la arena final, la introducción hablada y las seis fases como un solo encuentro. El arte ya está preparado. Esta guía define el recorrido y las reglas de combate. Los manifiestos definen las coordenadas de los atlas, las anclas y los nombres de archivo.

- [Fosa, suelo y capas](fosa/ARTE-FOSA.md) · [Manifiesto de la fosa](fosa/manifest.json)
- [Sprites revisados y efectos de God Fish](god-fish/ARTE-GOD-FISH.md) · [Manifiesto de combate](god-fish/manifest.json)

## Punto de partida comprobado

La introducción existe. `EventHandler.GodFish()` se dispara en el mapa interno 6, casilla (25,25). `CutsceneManager.scene_GodFish()` cierra la entrada con una puerta temporal en (25,26), usa un `PlayerDummy` mientras mueve el encuadre con la posición del jugador y despierta al God Fish dormido. Presenta cinco páginas de diálogo en `MON_GodFish.setDialogue()` y devuelve el control con la pista 23. El texto original pregunta a D si de verdad quiere seguir. Reimaginar esa escena y preservar ese reconocimiento.

La entrada desde el mercado 10 deja al jugador en (24,44) del mapa 6. `Market` ofrece regreso desde (23,44) antes del encuentro. God Fish conserva el slot 87 y la identidad de registro `monster:god-fish`. La puerta de llave y la salida del final están en (26,12) y (26,5) respectivamente en el trazado anterior. Al vencerlo, hoy se registra `Progress.GodFishDefeated`, cae la llave y vuelve la pista 22. `OBJ_Exit` activa la cinemática de cierre existente.

## Arena · geometría y recorrido

1. Mantener el mapa interno 6 y las transiciones desde y hacia el mercado. Ubicar un vestíbulo sur con la llegada y la señal de vuelta, seguido de un umbral único que inicia la escena. La ruta a la salida queda al norte, tras la puerta de llave. Conservar coordenadas de entrada y regreso si el mapa lo permite. Si cambian las demás, usar una sola fuente de coordenadas para eventos, posiciones de escena, puertas y migración.
2. Comprobar que el jugador tiene Rocky e Iron antes de sellar el encuentro. Si una partida anterior llegó sin alguno, señalar el requisito y conservar el regreso al mercado y al templo para recuperarlo. No gastar ni retirar esas herramientas al usarlas. Centro amplio de basalto con espacio para el jefe, la embestida y la separación del jugador. Dejar dos bandas laterales transitables con refugios para la última onda. Usar bordes sólidos y vacío con colisión, incluidos proyectiles y desplazamientos rápidos. Poner ruinas pequeñas en el perímetro y reservar el centro para combate.
3. Ubicar dos anclajes de piedra accesibles por flancos. Poner la rejilla y el núcleo en un punto desde el que se pueda disparar desde suelo transitable una vez abierta. Reservar un altar con una trayectoria recta de embestida y una escapatoria lateral. Los sellos de invocación deben quedar visibles, fuera de la entrada y con separación del jugador. El suelo junto a los puños plantados permite contacto melee desde un costado seguro.
4. Mostrar solo los mecanismos relevantes en cada fase. Mostrar objetivo, aviso y salida segura dentro de la vista útil durante el ataque. Validar a resolución lógica 960 × 576 con las colisiones reales. Probar esquives a ambos lados, movimiento de God Fish con su cuerpo completo y acceso al botín y a la puerta tras la victoria.
5. Dibujar fondo de fosa, acantilados y sedimento según la guía. Mantener el contraste de mecanismos, ojo, emblema y avisos incluso con la luz de zona. Cargar y recortar atlas al preparar recursos, usar vecino más cercano y anclas del manifiesto. Dibujar sin I/O, mutaciones de estado ni escalados de imágenes por cuadro. Dejar los PNG fuente en design y empaquetar los recursos que consume Java bajo src/main/resources.

## Cinemática introductoria · «El fondo recuerda»

Reutilizar la caja de diálogo, letra, avance con Enter y efectos de audio existentes. La escena sucede dentro de la fosa jugable y usa el titán de fase 1. El ojo lleva la actuación. El personaje no tiene boca.

| Beat | Puesta en escena |
| --- | --- |
| 1. El umbral | Al cruzar el acceso interior por primera vez en el intento, cerrar el paso con el sonido de puerta. Bloquear movimiento, ataques y menús mientras dura la escena. Mantener visible al pez, inmóvil y mirando hacia el centro. |
| 2. La presencia | Desplazar el encuadre suavemente hacia el titán durante unos 0,8–1,2 s con cámara separada de la posición real del jugador. Mantener el contorno a oscuras durante unos instantes y revelar primero el ojo verde. |
| 3. El reconocimiento | Revelar el manto, tridente y escudo de fase 1. Sostener una composición donde el cuadro de diálogo no tape el ojo. El titán habla a D directamente. Una página por línea de abajo. |
| 4. El desafío | Tras la última página, iluminar el contorno del aura y mostrar los dos enlaces a las piedras. Volver al encuadre jugable en unos 0,5–0,8 s. Arrancar la pista 23 una vez, devolver el control y arrancar fase 1 con una breve pausa de lectura antes del primer aviso de barrido. |

**Texto propuesto en inglés, como el diálogo original**

1. “So you’ve finally arrived, D.”
2. “The temple remembers what you left behind.”
3. “You swam all this way to hear it again?”
4. “Very well. Come closer, if you still want to know.”

El pasado de D queda insinuado. No convertir el diálogo en una explicación de la tragedia ni en un tutorial. La introducción utiliza los sprites de reposo y las capas existentes. Hacer el oscurecimiento con una máscara de escena y componer el ojo a partir del cuadro nativo si hace falta.

En la implementación, reutilizar `cutsceneState` y el avance por actualización. Separar encuadre, espera y texto de la simulación de combate. No desplazar al jugador para mover la cámara ni introducir un NPC guardable para simularlo. Consumir Enter al entrar y al salir. Enter durante las tomas sin diálogo no cambia el estado del combate. La puerta, la pista y el inicio del combate tienen una única activación. Interrumpir con reinicio, cambio de mapa o carga debe limpiar cámara, caja de diálogo, entradas y cierre temporal. Si falta el jefe al intentar iniciar, devolver control y dejar accesible el retorno.

## Combate · un ciclo legible por fase

Cada fase tiene un estado protegido, un objetivo y una apertura que dura hasta consumir su tramo de vida. La fase siguiente empieza protegida con su paleta y señal de transición. Una instancia de daño no salta fases. Mantener una barra total con umbrales visibles y ajustar duración, daño y ventanas de aviso tras una prueba de recorrido. Como punto inicial, dividir los 650 puntos originales en seis tramos cercanos; respetar los bloqueos aunque el jugador llegue con equipo avanzado.

| Fase | Secuencia durante la barrera | Apertura y reglas |
| --- | --- | --- |
| 1. Verde petróleo | Barridos de tridente por un carril marcado. Dos anclajes agrietados conectados al aura. | Rocky rompe ambos anclajes. Cada uno roto elimina su enlace. Ajustar la fase para que el jugador pueda cruzar entre objetivos. El Enfadillo lateral queda fuera de la primera integración, como opcional en la guía visual. |
| 2. Azul | Salvas cortas de burbujas del titán con aviso de trayectoria y pausas entre salvas. Rejilla metálica protege al núcleo. | Iron rompe la rejilla. Una burbuja del jugador debe tocar el núcleo con línea de visión. La rejilla permanece abierta durante la fase. Si agotar agua impediría completar el objetivo, activar una fuente de recarga en un lugar alcanzable. |
| 3. Violeta | Marca de embestida y pausa para apuntar. Al iniciar, se fija el rumbo. El jugador atrae el ataque al altar y se aparta. | Solo el choque del tridente con el altar abre la defensa. Si falla o golpea un muro, recuperar el arma y permitir otra oportunidad. Si acierta, mostrar el tridente clavado y la pose sin él. Recuperarlo al pasar a fase 4. |
| 4. Malva | Invocar primero a un Picarón y, tras su derrota, a un Fishtella. Anticipar cada aparición con sello y vínculo visible. Usar barridos malva como amenaza complementaria. | Cada derrota elimina un vínculo. Invocar una vez por esbirro por intento, con separación y sin añadir al siguiente mientras vive el anterior. Ambos fuera abren la defensa. Usar el comportamiento y sprites existentes sin modificar sus encuentros en otras zonas. |
| 5. Carmesí | Desarme. Tridente y escudo quedan a un lado como utilería. Alternar golpes de izquierda y derecha con avisos distintos. | Después del impacto queda una mano plantada y marcada. Un golpe válido o burbuja sobre esa mano cuenta como objetivo. Registrar ambas por separado, con indicador que confirme cada acierto. No contar el mismo lado dos veces. |
| 6. Ámbar | Dos golpes dirigidos y después el golpe doble. Señalar onda y refugios laterales antes de aplicar el daño. | Tras el golpe doble, el titán se hunde exhausto, cae la barrera y se abre el punto débil del ojo. Puede hacer golpes cortos con aviso. El ojo queda vulnerable hasta el golpe final. Mostrar derrota, cerrar el ojo y disipar la silueta. |

### Reglas transversales

- Resolver los impactos de melee y proyectil por el mismo criterio de fase. La barrera bloquea daño y reacciones normales. El núcleo y los puños cuentan como objetivos, con colisiones alineadas a los sprites visibles. El rectángulo transparente de una celda no es un área de impacto.
- Unir posición de avisos, geometría y ejecución desde el mismo plan de ataque. Usar preparación, impacto, sostén y recuperación como estados claros. Mantener aviso antes del daño, pausa para reaccionar y límites de alcance. Una onda o proyectil que cruza varias casillas debe comprobar los segmentos recorridos para no atravesar bordes.
- Mantener efectos, proyectiles, esbirros, puerta y armas de suelo bajo propiedad del encuentro. Al cambiar de fase retirar peligros incompatibles. En derrota, muerte o abandono, limpiar temporales y restaurar el estado de entrada que corresponda. Solo abrir el acceso a la salida tras victoria. La llave, experiencia y bandera de victoria se resuelven una vez. Retirar solo la puerta temporal del encuentro, no puertas del resto del mapa.
- En una muerte o reintento nuevo, restablecer fase 1 y mecanismos. La introducción puede abreviarse en reintentos de la misma sesión a la revelación y la última página, con el mismo cierre y devolución de control. La primera visita de una partida debe mostrarla completa.

## Guardados y regresiones

- Auditar `SaveLoad`, `SaveSnapshot`, `WorldEntityRegistry` y `MarketSaveRegression`. El formato actual conoce `bossBattleOn`, `godFishDefeated` e `inRage` del jefe anterior. Preservar identidad de entidad y migrar estados antes de aplicar el snapshot. Si la geometría cambió, comprobar posición y trasladar a una casilla segura de la misma arena cuando haga falta.
- Guardado legado antes del combate: conservar progreso y posición válida, con regreso al mercado e introducción disponible. Combate legado activo: convertirlo a un reintento limpio antes del umbral, con equipo, inventario y progreso general conservados. Victoria legada: mantener al jefe ausente y la puerta, llave o desenlace en su estado correspondiente sin volver a entregar recompensas. Comprobar en particular la partida que aún debe recoger la llave.
- Si el juego admite guardar en combate fuera de la cinemática, persistir fase, vida y objetivos cumplidos. Al cargar, retomar desde preparación segura de esa fase con temporales reconstruidos de forma coherente. El punto de descanso del mercado es la ruta normal. No añadir un punto de guardado en la arena solo para este encuentro. Probar que un guardado anterior válido todavía carga. Nunca guardar a mitad de la introducción o durante una transición parcial.
- Añadir casos de regresión al flujo normal para entrada y retorno de mercado, arranque único de escena, avance de diálogo y Enter retenido, barrera y condiciones de las seis fases, proyectiles frente a bordes, muerte y reintento, llave única y salida original. Incluir casos de guardado precombate, activo y victoria. Probar también falta de agua en fase 2 y embestida fallida en fase 3.
- Ejecutar `build.ps1 -Test` con partidas aisladas. Hacer un recorrido jugable desde el mercado hasta los créditos y capturar la revelación del titán, cada objetivo y los refugios de la onda. Entregar resultados de pruebas, capturas, ruta al JAR de build y decisiones de balance que se hayan tomado. Dejar la aprobación de la versión jugable al usuario.

## Orden de implementación

1. Arena y rutas con colisiones y compatibilidad de guardado.
2. Recursos de God Fish e introducción completa, con un punto único de inicio del encuentro.
3. Máquina de estados y objetivos de fases 1–4. Después desarme, puños, onda y derrota de fases 5–6.
4. Recorrido completo y regresiones de guardado, audio, reinicios, mercado y final.
