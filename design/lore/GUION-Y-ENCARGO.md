# The Little Fish · guion de las memorias de D

Base comprobada: `a6f2788`, combate final y esbirros aprobados. Este documento contiene spoilers. Los textos de pantalla están en inglés para seguir el idioma predominante del juego. El canon y las indicaciones de integración están en español.

## Canon y orden de revelación

D es un joven que depositó sus memorias en un juego sencillo sobre un pez naranja que debía vencer al jefe de las profundidades. El juego vinculó esas memorias con el pez y empezó a reconocerlo como D. El jugador descubre primero el rechazo por la apariencia y la forma de ser, luego la confianza traicionada y, en otro hilo, la compañía de una gata adoptada. La gata envejeció y murió por causas naturales. Su muerte y la traición son pérdidas distintas. Mantener anónimas a la persona y a la gata. Los recuerdos pertenecen a D. Los NPC son testigos imperfectos del juego y cada uno conserva una imagen distinta.

El laberinto representa años de adversidades. God Fish encarna cómo D llegó a ver la vida misma: algo que debía derrotar para recuperar la paz. La victoria permite abandonar esa idea. D reconoce que todavía puede vivir, querer y encontrar belleza sin borrar lo perdido. La costa, el sonido de las olas y la última mirada a la foto cierran esa transformación. La gata vive en la foto y en el recuerdo.

**Lectura obligatoria y lectura opcional.** God Fish reconoce a D en la introducción. El desenlace confirma la conexión con el juego y la pérdida de la gata, incluso si el jugador omitió todos los secretos. La traición, el rechazo y los detalles de la adopción se reconstruyen con los refugios y los escritos. Ninguna pista supone haber leído otra. Repetir motivos con detalles que cambian su significado: una silla vacía, una luz encendida, una puerta, dos huellas y unas manos vacías.

## Voces del recorrido

Cada elemento numerado de una secuencia equivale a una página. Usar la caja actual y ajustar saltos de línea al ancho real. El prefijo de hablante se usa solo donde está escrito. La barra vertical separa páginas en las tablas.

| Fuente | Páginas |
| --- | --- |
| Shadow, grupo 0 | You move like a fish. For a moment, I thought you were someone else. \| Keep your guard up. You can strike or shield yourself. \| Look in the side rooms. What you find there may help you further down. |
| Shadow, grupo 1 | Rest at an emerald when you need to. \| This place keeps things better than people do. Even things they'd rather forget. |
| Shadow, grupo 2 | Strange. I knew your name before you had a face, D. |
| Tuti | They used to leave a place at the edge of the room. \| Funny how you can be invited in and still end up by the door. |
| Coco | If you find sand again, sit for a while. \| The sea sounds different when you stop trying to get somewhere. |
| Candle | The stone gets colder from here. \| Someone used to warm their hands over this flame before going on. |
| BigRock, todas sus instancias | That hand again. I remember the weight of it. |
| TV Supply, saludo del escaparate | TV SUPPLY. Still on the air. |
| TV Supply, pie opcional del escaparate | A recorded voice: Leave the light on. I'll be back. |

Usar el texto de TV en una zona ya disponible del escaparate y sin pasos extra en compra o venta. Si no cabe el pie, omitirlo. La señal de vuelta, el descanso, los precios y las confirmaciones conservan su función. NPC_Square tiene diálogo legado pero no se instancia en AssetSetter de esta base. Conservarlo fuera del recorrido y sin crear otro refugio.

## Refugios del mapa interno 5

Mantener el disparador, el orden de grupos, la recompensa y la ayuda comprobada de cada sala. La última página de cada secuencia es funcional. Cuando el reto ya esté resuelto, conservar el flujo actual y usar las líneas posteriores solo en los grupos existentes que las admitan.

| NPC | Páginas del primer grupo |
| --- | --- |
| Chicken | Pio. D used to leave one light on after dark. \| A small shadow would curl up in the warm patch. \| Try both lanterns. |
| Flower | D asked me to put aside a second dose. \| When D returned, the bottle was still sealed. \| The Greeny guards the remedy. |
| Spiky | They made room until they saw D's face. \| D learned to sleep near the wall, where no one had to move over. \| Use the walls when crossing. |
| Ghost | Two sets of footsteps used to stop at this door. \| One day, I heard the other set carry on past it. \| I heard the promise they made to D. They were making it to someone else. \| Use the pedestal. Watch the marks, then follow them. |
| Hongui | D stood in front of me when they laughed. \| They laughed when D spoke, too. Afterward, D hardly said a word. \| Face two attacks with your shield. You may also defeat the Picaron. |
| Pump | D brought home something small from the rain. \| It slept in a basket that used to hold my seeds. \| Cut both dry bindings with your weapon. |
| Handy | D kept knocking long after the bolt went quiet. \| I could make a stronger tool. The door still wasn't mine to open. \| Equip Rocky. Both latches need two hits. |
| Ic | The small prints came more slowly every winter. \| D waited by the cold step until they caught up. \| Slide to the two marks. Rough stone stops the slide. |

**Grupos posteriores existentes.** Handy grupo 1: “Keep your hands steady, D. You can still open what's ahead.” Ic grupo 1: “The ice has thawed. I can still tell you where the prints were.”

## Inscripciones, secretos y murales

En `EventHandler.setDialogue()`, usar los identificadores actuales y mantener sus coordenadas, orientación y requisitos. Los grupos 15–17 se editan en `TempleExtension.INSCRIPTIONS`, que alimenta EventHandler. Las pistas hablan mediante objetos y huellas. Solo la estatua del centinela alude directamente a la intención de seguir.

| Grupo y punto | Páginas |
| --- | --- |
| 3 · primer mapa, sonrisa | A smile was scratched into the stone. \| The same hand tried three times to get it right. |
| 4 · primer mapa, marca D | One letter survived the current. D. \| The groove fits the edge of your fin. |
| 5 · cartel | NO FISHING. \| Underneath, in smaller letters: Please leave the orange one alone. |
| 6 · rastro borrado | Two names were scratched here. \| One was rubbed out. Another was written in the same space. |
| 7 · sombra | The wall is scratched at shoulder height. \| Whoever made the marks came back taller each year. |
| 8 · cuarto tramo, marcas | Two marks lead to a door. \| On the other side, only one trail leads away. |
| 9 · cuarto tramo, flecha | Someone wrote GO BACK. \| The arrow still points deeper. |
| 10 · cuarto tramo, forma familiar | The carving shows a bowl by a window. \| Your first thought is that someone forgot to fill it. |
| 11 · marca de herramienta | The last tool mark points toward the refuge. \| A small print crosses it, as if someone came to fetch the worker home. |
| 12 · mural de los peces | The first panel shows a single fish. \| The next shows the same fish carrying a light it did not have before. |
| 13 · mural de D | The D was cut through an older picture. \| Beneath the letter, the orange fish still has its original outline. |
| 14 · mural de pesos | Four weights guarded the way. One light stayed on behind them. \| The old lamp and fish marks hide a seam. |
| 15 · Velo | I left room on the bench after they stopped coming. |
| 16 · Fauce | At the end she needed help getting to the window. I carried her there. |
| 17 · Centinela | My hands were empty when I left. I could still turn the handle. |

**Textos de objeto y sistema.** Quitar las alusiones sueltas de `OBJ_Chest` y `OBJ_Key`. Cofre con botín: “You found {item}!” después de la entrega confirmada. Cofre vacío: “It's empty.” Llave fuera de lugar: “There is no lock for this key here.” El éxito al abrir conserva su confirmación. En `EventHandler` grupo 2, usar “For a moment, the passage feels unfamiliar.” solo en el evento de distorsión existente. El descanso informa recuperación y guardado con precisión. Mantener las notificaciones de daño, inventario lleno, consumibles, nivel, controles y objetivos del jefe como texto funcional. Si un mensaje funcional ya combina una línea de lore y una mecánica, separar ambas o conservar la información mecánica íntegra.

## God Fish · introducción

Sustituir las cuatro páginas de `MON_GodFish` y conservar la cuarta como diálogo del reintento abreviado.

1. “There you are, D.”
2. “You gave the little fish one task. Then you gave it everything you remembered.”
3. “You kept swimming down, as if peace were something you could win from me.”
4. “Come closer, D. Show me what you came here for.”

Conservar encuadre, recorrido, pausa, sonido y entrega de control aprobados. Los ataques, teletransporte, fase 4, contacto y ritmo del combate conservan sus reglas. Las indicaciones del HUD son mecánicas y siguen disponibles.

## Desenlace · la costa

### Diálogo después de la puerta final

En `OBJ_Exit.setDialogues()`, sustituir el intercambio anterior por estas páginas. El eco es la voz del juego que conserva las memorias de D. Puede mantenerse el identificador de código anterior para compatibilidad. En pantalla se nombra “Echo”, sin atribuirle una identidad humana.

1. “D: The water's quiet. I thought I'd feel different.”
2. “Echo: This was a game about a fish. Then you put your memories here.”
3. “Echo: When the fish moved, I thought it was you.”
4. “D: I kept looking for a way to put it all back.”
5. “D: She used to sleep by the window. The cat I brought home from the rain.”
6. “D: She grew old. One morning she stopped breathing in her sleep.”
7. “D: I remember holding her. Then I remember how empty my hands felt.”
8. “D: I can carry something again. I don't have to leave her here.”

### Plano y últimas líneas

En `CutsceneManager.scene_Ending()`, usar la salida existente. Tras el diálogo, fundir a negro y pasar a una escena no jugable, en tierra. Atardecer, horizonte marino, olas suaves. D aparece de espaldas o se muestran solo sus manos. Sostiene una foto de su gata al borde del encuadre. Dejar un primer momento con solo las olas. Luego mostrar una línea por pausa y dar tiempo para leer.

1. “I looked at her photograph one last time.”
2. “The tide was coming in.”
3. “The light on the water was beautiful. I stayed to watch it.”

Mantener la foto visible durante la primera línea. Al final, bajar la foto fuera del encuadre y dejar que el mar ocupe el plano. Sostener el atardecer unos segundos antes del título y los créditos. Preservar las atribuciones y agradecimientos existentes en créditos, separándolos del epílogo. No identificar a la gata por los nombres de los créditos. Evitar un letrero que explique el significado de God Fish.

**Recursos de la costa.** Este encargo entrega guion y dirección, no arte ni audio. Producir una ilustración original en pixel art con una capa discreta de oleaje, una silueta de manos y una foto ilustrada de la gata. Usar una composición 320 × 192 a escala ×3, o un equivalente compatible con la escena. La foto muestra una gata dibujada, sin asumir color ni rasgos personales. Usar un bucle propio de olas o un recurso que el usuario haya autorizado. Revisar arte y sonido con el usuario antes de publicar el final. Si no están listos, implementar el flujo con una maqueta claramente identificada para revisión y conservar el pase a créditos.

## Integración y aceptación

- Aplicar este guion sobre `a6f2788` o su descendiente. Revisar los puntos de entrada actuales y mantener el orden de eventos. Los textos del documento son el contenido de pantalla. Se permiten ajustes de puntuación y saltos de línea para la caja, conservando lo que revela cada pista.
- Mantener la carga y el avance del texto fuera del render. Conservar guardados, disparadores, estados de refugio y pausa. Verificar cómo se restauran `dialogueSet` y `dialogueIndex` de NPC en guardados anteriores cuando se acorten o amplíen grupos. Tratar índices antiguos sin bloquear el avance ni resetear el mundo. No añadir flags de lectura ni requisitos para el final.
- Verificar cada grupo y mural por su entrada normal, incluidas las ayudas de puzzle, objetos y textos posteriores. Leer una partida directa al final y otra que visite los refugios en otro orden. El final debe cerrar en ambas y entregar una segunda lectura a la foto. Revisar ancho, altura y legibilidad a resolución lógica y en pantalla completa.
- Ejecutar `build.ps1 -Test`. Actualizar las expectativas textuales de StoryRegression si corresponde y añadir una comprobación del avance hasta créditos si la secuencia gana nuevos estados. Probar entrada, reintento abreviado, puerta final, fundido, foto, audio y créditos con una partida aislada. Entregar una lista de puntos narrativos comprobados, capturas de la costa y la ruta del JAR de prueba. La revisión de guion, arte y ritmo sigue correspondiendo al usuario.
