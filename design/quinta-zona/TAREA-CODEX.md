# Tarea para Codex: Sharky y rediseño de la quinta zona

Lee AGENTS.md, ESTADO_PROYECTO.md, HOJA_RUTA.md, ARQUITECTURA.md y este paquete. La conversación previa se resume aquí; no necesitas reconstruirla. Trabaja con src/, no descompiles el JAR.

## Alcance y responsabilidades
Implementa la propuesta documentada en DISENO.md, integra los PNG según ARTE.md y verifica. ChatGPT preparó el arte; no redibujes personajes ni sustituyas el arte aprobado. Ajustes técnicos menores de coordenadas/tiempos se admiten si conservan la intención. Si necesitas ampliar mapa, sustituir sprites o alterar progresión creativa, documenta el motivo antes de pedir una decisión. Reutiliza los ocho sprites originales de Sharky con las señales superpuestas; el paquete no contiene nuevos cuerpos de Sharky.

El usuario ya aprobó la reimaginación de Sharky. El encargo actual autoriza preparar esta implementación para Codex; el usuario puede iniciar el trabajo enviando el prompt al final de este archivo. No afirmar que esta preparación equivale a código implementado o pruebas pasadas.

## Orden de implementación
1. Sharky: estados acecho, preparación, embestida, recuperación/aturdimiento y regreso. Probar aislado antes de mover contenido. Integrar overlays y respetar sprites originales.
2. Definición de quinta zona (mapa interno 3), usando como referencia el plano propuesto. Conservar IDs 0-7, habitaciones del mapa 5 y conexión de la nueva cuarta (mapa 7). No crear ni reordenar otros mapas.
3. Iluminación fija con coral luminoso, compatible con linterna y oscuridad permanente. Las otras zonas mantienen su ciclo. Preparar máscaras/cache una vez, recortar fuentes a pantalla y medir costo; no cargar/decodificar en draw().
4. Hierro y Rocky: recorridos, obstáculos distinguibles, ayudas no modales y cofres existentes.
5. Migración de partidas antiguas y configuración de territorios al cargar/repoblar, según contratos de guardado.
6. Pruebas, capturas, informe; actualizar ESTADO_PROYECTO, HOJA_RUTA y CAMBIOS con evidencias reales. No sobrescribir JAR raíz, publicar ni crear commits salvo instrucción expresa del usuario en la sesión de implementación.

## Puntos reales del código a revisar
- MON_Sharky actualmente persigue a velocidad 6; daño por contacto en Entity.checkCollision(). No basta con añadir setAction(): impedir daño continuo y controlar knockback, muerte, pausa y retorno.
- MON_Picaron limita comportamiento territorial a mapas 2 y 7. Añadir mapa 3 sin cambiar el resto; su visibilidad debe usar el mapa actual.
- GamePanel.changeArea() repuebla enemigos y limpia proyectiles; no cambiar esa regla incidentalmente.
- TileManager toma dimensiones de stage1 para todos los mapas. Mantener 50x50; ampliación es alternativa pendiente, no requisito.
- Lighting crea darknessFilter centrado en jugador, con alpha casi total sin linterna; para dungeons lo dibuja completo aunque el indicador diga Day. Incorporar fuentes fijas reales, sin oscurecer HUD ni alterar el día exterior.
- OceanLayers y las condiciones de GamePanel/TileManager hoy solo activan capas para mapas 0,1,2,7. Mapa 3 necesita una variante propia; conservar las anteriores.
- AssetSetter contiene los 7 Picarones y 9 Sharkys originales de mapa 3. La nueva propuesta usa 3 y 6; conservar slots estables o migrarlos explícitamente.
- SaveSnapshot/SaveLoad guardan mundo completo; crear revisión de quinta zona sin cambiar UID/esquema por accidente. Diferenciar partida antigua de enemigos/piedras ya derrotados/destruidos. Conservar inventario, cofres abiertos, progreso y partidas en otros mapas. No reescribir al cargar.
- Dos puertas existentes: mapa 3 (9,5) <-> mapa 5 (40,9), y mapa 3 (14,40) <-> mapa 5 (9,9). Cofre de Iron está en mapa 5 (28,11); mejora de arma en (20,10). Conservar recompensas/IDs y emparejar retornos si se reubican entradas en la quinta.
- IT_DestructibleWall requiere arma tipo 10 y vida 3; IT_Rock requiere tipo 4. Mantener su distinción.
- Sound ya fue estabilizado. Usar requestPlay/playSE existentes; no abrir/cerrar clips en el hilo de juego. Sin sonidos nuevos en este paquete.

## Criterios de aceptación
- Sharky prepara ataque visible en pantalla, fija dirección al acabar preparación y no gira durante la pasada. Recorre como máximo su distancia definida, no atraviesa tiles, objetos sólidos, piedras/metales, bordes ni cuerpos aliados. Muestrear todo trayecto con velocidad alta; alpha visual no determina colisión.
- Una embestida produce como máximo un impacto al jugador, incluyendo escudo/parry/invulnerabilidad; recuperación obligatoria. Parry correcto la interrumpe. Fallo y choque tienen ventanas de castigo diferentes. No cura al regresar.
- Nunca atacar desde fuera de pantalla ni desde detrás de un muro. Evitar estados atascados y ataques simultáneos en la pareja final; coordinador solo por encuentro, sin un bloqueo global para todo el mapa.
- Pausa, diálogos, opciones y cinemáticas congelan temporizadores. Morir/recargar/transicionar limpia estados transitorios. Guardar/cargar conserva vida/ausencia de enemigos; reanudar estados transitorios de forma segura y documentada.
- La ruta principal es legible sin linterna; luces fijas no bloquean paso ni se recogen/rompen. Sin sumas de alpha que creen destellos o agujeros duros. Linterna conserva su ventaja y radio actual salvo decisión explícita.
- Cofre de Iron accesible sin Iron; salida segura con inventario lleno. Primera prueba sin ataques. Atajos útiles, desvíos opcionales y salida alcanzable. La mejora de arma original sigue disponible.
- Caminar rutas con colisión real del jugador, probar puertas ida/vuelta y que llegada no re-dispare transición. Refugio y puntos de conversación/cofre fuera de territorios de embestida.
- Migración de guardados de la base, guardado nuevo, enemigos derrotados/heridos y escombros; rollback ante partida inválida, fixtures aislados en build/.
- Ejecutar build.ps1 -Test por afectar sistemas compartidos; ampliar suites específicas de Sharky, quinta zona e iluminación. Comparar zonas anteriores para detectar cambios accidentales. Capturas del motor con y sin linterna y de estados de Sharky.
- Verificar colisiones y alpha/tamaños de recursos al integrarlos. Medir tiempo de actualización/dibujo y memoria bajo muchas luces/enemigos; no afirmar FPS de la PC del usuario sin medición real.

## Prompt para iniciar el trabajo
Implementa Sharky y la quinta zona conforme a design/quinta-zona/TAREA-CODEX.md, DISENO.md, ARTE.md y assets/manifest.json. Respeta AGENTS.md y el arte original. Empieza por Sharky aislado, después mapa, luz, hierro y compatibilidad. No amplíes el alcance ni reemplaces el JAR raíz. Termina con pruebas, capturas y documentación de lo implementado y de cualquier pendiente. La publicación se solicitará aparte.
