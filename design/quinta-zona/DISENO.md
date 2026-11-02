# Diseño: cuenca abisal morada

## Decisiones del usuario
Conservar mecánicas y sprites originales; Sharky es tiburón de emboscada aprobado. Quinta zona reemplaza el laberinto monótono por cámaras amplias. Mayoría Sharkys, algunos Picarones. Suelo/decoración morados, paredes azul-violeta oscuras con color visible, oscuridad permanente y fuentes luminosas decorativas. Nuevo fondo abisal con criaturas más extrañas. Hierro se obtiene de cofre original y se aprende como Rocky, con usos posteriores. No se programó esta entrega.

## Recorrido propuesto (ajustable al probar colisión)
50x50; coordenadas son propuestas, no cambios del mapa activo. Conservar llegada (25,23), descanso (25,20) y salida (9,44) para simplificar conexiones. Anchos de cámaras orientativos 8–12 casillas, conexiones 3–4; no saturar de piedra, decoración o enemigos.
A: refugio central (21,18)-(29,25). B: primera cámara Sharky (32,17)-(43,27), cobertura con pared lateral y evasión en ambos sentidos.
C: bifurcación: brazo norte hacia puerta Iron (9,5); brazo oeste desde refugio lleva al desvío y a esa misma aproximación. Los dos se reúnen en D, explanada iluminada de Iron (10,16)-(19,24). Puerta Iron puede conservar (9,5) si un camino corto iluminado la enlaza; una reubicación corta es aceptable conservando retorno. Nunca esconder su entrada ni poner Iron en el acceso al propio cofre.
E: circuito inferior (14,28)-(38,36), con atajo de Iron a refugio y desvío a puerta de arma (14,40), conservando cofre original. F: cámara final (18,37)-(32,44), dos Sharkys separados, conduce por tramo libre a salida (9,44). Evitar que el acceso a la puerta de arma o salida reciba ataques antes de ver la cámara.
El plano SVG es esquemático: no da geometría final ni autoriza copiar intersecciones como tiles. Codex concretará coordenadas sin perder secuencia, puertas, descansos y alcance de herramientas.

## Población propuesta
6 Sharkys: uno presentación B; uno brazo norte; uno brazo oeste; uno cámara intermedia E; dos en cámara final F. 3 Picarones: uno desvío opcional; uno E separado del Sharky; uno antesala F. Territorios acotados, sin perseguir de una cámara a otra. Separar los 2 finales y escalonar la preparación para que el jugador pueda leer ataques. Sin aumentar estadísticas iniciales.

## Sharky: contrato de diseño aprobado
Acecho visible y lento, detección cercana con línea despejada, preparación con burbujas y postura/señal, pasada rápida recta, recuperación y regreso. Se fija el objetivo al acabar preparación; no giro durante embestida ni aparición invisible. Fallar permite castigo; pared causa aturdimiento más largo. Escudo frontal y parry útiles. Un impacto por pasada. No persigue por todo el mapa.
Valores para primera prueba, NO balance final aprobado: acecho velocidad 1, alcance detección 5 casillas, preparación 30 ticks (0,5s a 60Hz), pasada velocidad 8 y distancia máxima 5 casillas, recuperación 45 ticks y choque 75 ticks, pausa mínima entre nuevos ataques 90 ticks contados desde terminar recuperación. Conservar vida 20, ataque 9, defensa 0, exp 28 y drops. Probar tiempo de reacción a 2 y 5 casillas antes de fijar cifras. No atacar al jugador a quemarropa sin señal; ajustar selección/recuperación si ocurre.
Se puede mantener direcciones cardinales para respetar los ocho sprites originales; apuntar hacia el eje dominante y requerir una franja frontal que el jugador pueda reconocer. No implementar ataques diagonales sin arte/decisión nueva. El movimiento exacto y detección deben probarse para no introducir embestidas que nunca aciertan ni giros instantáneos imposibles.

## Herramientas
Iron: primera pared pequeña iluminada después del cofre, sin combate; un atajo útil al refugio; otro grupo en avance con pequeño secreto opcional. Aproximadamente 6–8 bloques repartidos, a concretar según ancho de pasos; sin largos muros de golpes. Mantener diferencia visible de piedra vs metal. Un par de piedras Rocky opcionales conserva su uso; no convertirlo en requisito repetido.
Inventario lleno: cofre no consume recompensa y cuarto conserva salida. No equipar automáticamente. Ayuda contextual discreta al acercarse con herramienta incorrecta; se retira al completar.

## Luz y ambientación
Refugio, cruces, puertas, primera pared y presentación de Sharky legibles sin linterna. Separar luces fijas del móvil; fuentes del mundo no bloquean ni cambian combate. Propuesta inicial 8–12 corales luminosos espaciados, radios 2,5–3 casillas; ajustar usando capturas sin linterna, evitar un camino a oscuras totalmente ilegible. Linterna da lectura continua entre islas de luz.
Suelo original 069 y corales 070–079; borde original 048 es referencia pero su tono se vuelve azul-violeta en el tile candidato nuevo. No recolorear recursos globales. Fondo independiente con huecos verticales, roca lejana y criaturas deformes muy discretas; criaturas decorativas sin hitbox. Paleta derivada: suelo #3b1d80, motas #8036af; oscuridad del fondo índigo-violeta, brillos lavanda/cian. Mantener visibilidad de jugador naranja, señales de Sharky, metal y salidas. Evitar rojo brillante de amenaza en peces de fondo.

## Entregas de Codex
1 Sharky aislado con pruebas y capturas. 2 Quinta zona + luz + Iron + migración + pruebas/capturas. Usuario revisa sensación y ritmo antes de dar por terminado; la automatización no sustituye esa valoración.
