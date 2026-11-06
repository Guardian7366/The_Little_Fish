# God Fish · arte de combate del titán calamar

El manto puntiagudo, las aletas y los seis tentáculos cortos identifican al calamar. El ojo verde es el único rasgo facial. En las fases 5 y 6 se estrecha bajo el ceño del manto. Lleva el tridente en su mano derecha (a la izquierda de la pantalla en reposo) y el escudo con un pez verde en la otra. El escudo físico viaja con el brazo. El campo de invulnerabilidad se dibuja en una capa independiente.

[Paletas y siluetas](preview/fases.png) · [Poses de ataque](preview/ataques.png) · [Perfiles, puños y golpe doble](preview/perfiles.png) · [Proyectiles, armas y mecanismos](preview/efectos.png) · [Arte de la fosa](../fosa/ARTE-FOSA.md)

## Lectura de los atlas

- Los PNG de assets tienen fondo transparente y píxeles nativos. manifest.json define tamaño de celda, ancla, columnas y filas de cada atlas, en orden desde cero. El rectángulo de cada cuadro empieza en columna × ancho y fila × alto. No recortar las celdas por transparencia.
- El jefe ocupa celdas de 144 × 128 con ancla [72, 63] en el centro corporal. Dibujar con interpolación de vecino más cercano a la escala de los tiles del juego (3× por defecto). Colocar el aura en el mismo punto del mundo con su propia ancla [80, 72]. Dibujar el aura como contorno superpuesto sin tapar el ojo.
- El reposo es frontal. Las filas left y right giran el manto, el ojo y los hombros en tres cuartos. Las filas de ataque colocan armas o puños en la dirección correspondiente. Las acciones con fila front se reutilizan en todas las direcciones. No espejar toda la celda, para conservar mano y escudo. Conservar dimensiones y posición de combate por separado del tamaño del PNG.
- Las columnas windup, impact, hold y recover son poses. Su duración la controla el estado del ataque. Mostrar el aviso antes de habilitar daño y retirar la colisión al acabar el golpe. Reposo alterna idle-a e idle-b. Usar exposed al perder la barrera y hit al recibir un golpe válido.
- Las marcas de peligro, las decoraciones y el aura son capas visuales. Las colisiones salen de la arena y del estado del combate. Si se reutiliza una marca como segmento de carril u onda, repetirla y orientarla con el patrón de daño real. Las marcas de refugio señalan zonas que quedan libres.

## Fases

| Fase y paleta | Condición para abrir la defensa | Atlas de ataque y efectos |
| --- | --- | --- |
| 1. Verde petróleo | Rocky rompe los dos anclajes de piedra. Cada anclaje mantiene un enlace visible mientras siga entero. | 1-sweep, mecanismo stone-anchor, marcas tether, sweep y stone. Usar la estela del barrido sobre el carril marcado. Si la implementación usa un Enfadillo lateral, reutilizar su arte. |
| 2. Azul | Iron abre la rejilla. La burbuja del jugador enciende el núcleo expuesto. El respiradero de recarga solo aparece si el sistema de recursos lo requiere. | 2-volley, mecanismos iron-grille, water-core y refill-vent. Proyectil volley para disparos del jefe, water-core para el brillo e impacto en el núcleo. Diferenciar el disparo del jugador con su sprite habitual. |
| 3. Violeta | El jugador atrae la embestida al altar y esquiva. Si falla, el jefe recupera el arma y vuelve a intentar. Si acierta, el altar se quiebra y el tridente queda clavado. | 3-charge y 3-tridente-clavado. Usar armas/clavado en la posición del altar solo mientras el jefe lleva la pose lodged. Retirar el objeto al recuperar. Marcas sweep y trident-dust. |
| 4. Malva | Invoca primero a un Picarón y después a un Fishtella. Cada derrota elimina un enlace y debilita el aura. Con ambos enlaces cortados, queda expuesto. | 4-cast, mecanismo summon-seal y marcas summon, summon-hatch, tether. Usar los sprites aprobados de ambos enemigos y 4-sweep como barrido complementario de esta fase. |
| 5. Carmesí | Arroja tridente y escudo. Golpea con una mano a la vez. Mientras queda plantada, la marca brillante en el puño indica dónde acertar con un golpe o burbuja. Cada mano cuenta por separado. | 4-desarme, armas/tridente-suelto y armas/escudo-suelto, 5-fist-l y 5-fist-r, marcas fist y hand-seal. El tridente y el escudo del suelo son utilería. |
| 6. Ámbar | Dos golpes dirigidos preceden a un golpe doble y su onda. Los refugios laterales quedan libres. Después cae exhausto y expone el ojo. | 6-fist-l, 6-fist-r, 6-slam, 6-final, marcas slam y safe, mecanismo safe-refuge, proyectil ground-wave. weak-windup y weak-impact cubren un golpe corto del titán exhausto. defeat cierra el ojo y baja el manto. Completar la salida con despawn y retirar todos los ataques. |

Las seis filas de fx/aura se asignan por fase. Activar pulse-a/pulse-b mientras el jefe es invulnerable, usar crack al perder un anclaje o enlace y reproducir break/fade al completar la condición de vulnerabilidad. phase refuerza la transición sin depender del color. Tras abrir la defensa, mantenerla abierta hasta alcanzar el umbral de daño de esa fase. En la fase 5, los puños atacados deben conservar su avance en el intento actual.

## Revisión al integrar

1. Probar que el ojo, el tridente y el pez del escudo se leen sobre el fondo oscuro y con el aura superpuesta. Verificar la ausencia de armas en las manos desde la fase 5.
2. Probar las cuatro direcciones de barrido, embestida y puños. La marca de carril y los efectos de contacto deben coincidir con el ataque.
3. Verificar con Rocky, Iron y burbujas que los mecanismos reaccionan al objeto indicado. Mostrar cada cambio de estado y comprobar que un fallo permite reintentar.
4. Comprobar que los dos esbirros aparecen en secuencia y que el segundo aviso no tapa el anterior. Limpiar esbirros, proyectiles, aura y armas de suelo al salir o reiniciar el intento.
5. Probar que la onda deja libres los refugios que anuncia, que el ojo queda expuesto tras el golpe doble y que la derrota limpia todo daño residual.

## Desplazamiento del titán

Los seis atlas boss/nado-1 a boss/nado-6 añaden 96 cuadros. Cada fila es una dirección: down, left, right, up. Las cuatro columnas alternan extensión y empuje de los tentáculos. Los perfiles usan la perspectiva de tres cuartos. La fila up conserva el ojo visible y eleva el manto mientras los tentáculos quedan rezagados. Se mantienen la celda de 144 × 128, el ancla [72, 63] y la escala ×3. [Vista de cuatro secuencias](preview/nado.png).

Para integrar, copiar los seis atlas al classpath bajo final/god-fish/boss/ y registrarlos en GodFishArt. Avanzar una columna por distancia real recorrida durante aproximación o flanqueo; como valor inicial, usar 8–12 píxeles lógicos por cuadro. La dirección sale del desplazamiento aplicado, no del objetivo deseado. Con avance cero, usar reposo. Aviso, golpe, puño plantado, embestida, desarme, apertura, retorno y derrota conservan sus poses propias y tienen prioridad sobre el ciclo. Mantener ancla, colisión y temporización de combate independientes del movimiento interno del dibujo. Preparar las celdas una vez fuera del bucle.
