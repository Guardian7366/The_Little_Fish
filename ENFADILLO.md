# Enfadillo restaurado en la segunda zona

Los seis ejemplares existentes de la segunda zona permanecen en sus ubicaciones. Ya no deambulan ni persiguen; tampoco se desplazan por retroceso. Los ejemplares de la tercera zona conservan su comportamiento anterior. No se rediseñaron mapas ni se cambiaron sprites, vida, defensa, daño por contacto, experiencia o botín.

Disparan la burbuja naranja original cuando el jugador está alineado horizontal o verticalmente, dentro de cuatro casillas y sin obstáculos delante. Cadencia inicial: dos segundos de juego activo, con una burbuja por enemigo como máximo. El sonido proEffect.wav (ya existente) se solicita únicamente después de registrar el disparo mediante el controlador asíncrono de audio existente.

La burbuja conserva velocidad 8 y ataque 3. Su duración pasa de20 a30 actualizaciones, para cubrir el alcance de detección: máximo240píxeles/5casillas. Colisión de16x16centrada en el dibujo. Comprueba todo su trayecto píxel a píxel: bordes del mapa, terreno sólido, puertas/objetos sólidos, obstáculos destructibles, NPC y otros enemigos vivos. Los escombros no la bloquean. No causa daño a aliados.

Impactar al jugador consume la burbuja incluso durante invulnerabilidad. Defensa/parry usan la lógica de combate actual; el parry neutraliza el proyectil, no lo refleja. El jugador puede destruirlo con un ataque. Un disparo ya lanzado puede continuar tras morir su dueño, pero termina por impacto o duración. Al cambiar de mapa se limpia; fuera del juego activo no avanza. No se persiste un proyectil en vuelo al guardar: los enemigos reconstruidos pueden disparar normalmente.

No se reutiliza la función antigua checkShootOrNot: la cadencia local usa un contador saturado independiente y el disparo solo se activa si hay un hueco disponible. Un espacio lleno no crea burbujas invisibles, no reproduce sonido y no consume la oportunidad de disparar después.

Validación: EnemyShotRegression cubre repetición de disparos, registro/audio único, inmovilidad, daño, espacio lleno/recuperación, línea de tiro bloqueada, impactos contra pared/puerta/roca, escombros, parry en cuatro direcciones, invulnerabilidad, pausa, cambio de mapa, límites, muerte del dueño, ataque contra burbuja, retroceso, conservación de movimiento en la tercera zona y guardado/carga de los seis enemigos. Pasan también todas las suites anteriores de combate, audio, recursos, funcionamiento, historia, primera zona, menú y guardado. Registro: build/enemy-shot-verification.txt. Son comprobaciones automáticas; el ritmo y dificultad quedan para la prueba del jugador.

Se actualizó directamente el JAR de outputs/TheLittleFish-MenuMarino, sin sustituir partidas ni generar nuevos ZIP. Se conserva una copia previa en work/menu-repair/TheLittleFish-antes-enfadillo.jar. Los ZIP anteriores no incluyen esta restauración.
