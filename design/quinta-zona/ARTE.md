# Arte para integrar: contrato

## Archivos de producción candidatos
32 PNG pequeños, con SVG editable y fuentes de rectángulos en assets/pixel-sources.json. **No se cargan aún en el juego.** Todos en píxeles enteros, sin suavizado. Manifest con dimensiones, alpha y SHA-256. Nuevos archivos: ningún sprite/tile original se sobrescribe.

- tiles/abyss-wall.png: 16x16 opaco, borde azul-violeta oscuro. Usarlo como NUEVO tile exclusivo del mapa 3; no recolorear 048 globalmente. Su colisión al integrarlo es sólida. Esta propiedad se configura en código/tabla, no en PNG.
- tiles/glow-coral-{top,bottom,left,right}.png: 16x16 opacos, fondo EXACTO #3b1d80 y motas #8036af. El nombre indica la PARED a la que se adhiere el coral. top cuelga de arriba; bottom nace de abajo. Decoración transitable, indestructible y no recogible. Solo emite luz fija; nada de daño, curación ni activación.
- background/far.png: 320x192 opaco, fondo índigo-violeta y roca lejana. background/ridges.png: mismo tamaño con transparencia. Repetibles con parallax, bordes exteriores despejados. Usar filtros nearest-neighbor a 3x. Sin efectos de blur.
- background/particles-0..4.png: cinco frames transparentes 320x192. Un frame cada 30 ticks (2Hz). Cachear y superponer, no decodificar por cuadro.
- background/angler.png (72x40) y longfin.png (72x36): siluetas decorativas transparentes de criaturas abisales. Máximo dos visibles, muy lentas (por ejemplo 12–20 píxeles de pantalla/segundo); sin ojos brillantes, colisión o lógica de enemigo. Movimientos independientes de Sharky. Desvanecer si el contraste amenaza la legibilidad.
- sharky/prepare-{right,down,left,up}-{0,1}.png y charge-{...}-{0,1}.png: overlays transparentes 32x32. El sprite ORIGINAL de 16x16 va centrado en (8,8). Preparación: burbujas delante del hocico. Embestida: estela detrás. **No son reemplazos del cuerpo ni nuevas poses completas**. La lectura inicial propuesta se apoya en orientación/pausa/overlays; la boca original ya tiene dientes. Si al probar falta claridad, solicitar a ChatGPT nuevas poses completas, no inventarlas en programación.
- sharky/stunned-{0,1}.png: estrellas lavanda arriba del cuerpo. Independiente de orientación. Solo choque/parry; recuperación de una pasada fallida usa cuerpo original sin estrellas.

## Coordenadas y dibujo
WorldX/Y originales apuntan a esquina del cuerpo de 48x48. Dibujar overlay escalado a 96x96 en (worldX-cameraX-24, worldY-cameraY-24). NO ampliar solidArea/attackArea por el tamaño del overlay. Mantener x/y y sprites base, animación de dos frames. Rotaciones ya exportadas: no rotar todo Sharky en runtime.
Timings orientativos en manifest (prepare 8ticks/frame, charge 6, stunned 12). Animación avanza en update y se congela fuera de juego activo; draw no cambia frame/estado.

## Luz
El PNG brillante representa el coral; su halo visible real debe implementarse en Lighting. No basta con dibujar un círculo de color encima de una pantalla oscura. Preparar máscara radial reutilizable; combinar con linterna sin duplicar oscuridad, recortar a pantalla y revisar con/sin linterna. No mover ni añadir brillo al HUD. Si se usa una máscara de luz independiente, evaluar fuga de luz por paredes finas; la fuente puede limitarse a su cámara.
El coral tiene extremo lavanda/cian sin blanco puro masivo. No usar estos tonos como señal de daño de enemigos. La linterna del jugador sigue siendo útil entre fuentes.

## Referencias / previews
references/ambiente-generado.png es SOLO guía de tono, generada con image_gen y referencias de suelo/Sharky; no usar como textura gigante, spritesheet, fondo fijo ni mapa. La imagen generada es más detallada que el juego: los PNG nativos pequeños son la entrega integrable. Prompt y procedencia en references/PROCEDENCIA.md.
preview/ambiente.png combina las capas nativas preparadas, sin filtro de noche del juego ni UI: muestra composición, no visibilidad final garantizada.
preview/sharky.png muestra sprites ORIGINALES con overlays de preparación, embestida y aturdimiento. No importar el tablero completo como recurso.
preview/tiles.png muestra los originales de referencia y los tiles nuevos ampliados sin suavizado.
references/recorrido.svg es esquema de cámaras y flujo; references/sharky-estados.svg comunica los estados, no especifica sprites ni duración final.

## Validación realizada en esta entrega
Dimensiones y alpha de 32 PNG, inventario y hashes del manifest; inspección de tablero y ambiente. No se compiló el juego, no se probaron ataques, rendimiento, iluminación ni guardados. Esa validación pertenece a Codex tras integrar. El usuario aún debe valorar los recursos nuevos y sensación del combate; no registrar estos como arte jugable aprobado por separado.
