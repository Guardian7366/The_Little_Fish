# Historial de cambios

Este archivo resume los cambios realizados. Los documentos enlazados conservan los detalles y resultados de cada entrega; el estado vigente está en [ESTADO_PROYECTO.md](ESTADO_PROYECTO.md). Las validaciones históricas no se presentan como ejecuciones nuevas.

## Sistema de trabajo documental — 1 de noviembre de 2026

- Creados [AGENTS.md](AGENTS.md), [ESTADO_PROYECTO.md](ESTADO_PROYECTO.md) y [HOJA_RUTA.md](HOJA_RUTA.md) con autorización del usuario: reglas permanentes, base comprobada y prioridades pendientes de aprobación.
- Actualizado [README.md](README.md) con el contenido hasta la nueva cuarta zona, estructura real, flujo ChatGPT–usuario–Codex, migraciones y uso del JAR generado frente al ejecutable de la raíz. Normalizado a UTF-8 para corregir la codificación mixta anterior.
- Ampliado este historial con las entregas posteriores a la primera zona sin sustituir sus informes técnicos.
- Revisión de archivos, enlaces y coherencia documental; sin modificar código, recursos, partidas ni ejecutables. No se ejecutaron compilación ni suites en esta tarea. El usuario aprobó los cinco documentos y autorizó su commit y publicación en GitHub; se actualizan estado y hoja de ruta con esa decisión.

## Respaldo completo en el repositorio — 1 de noviembre de 2026

- Base `296fb57`: «Respaldo completo del juego hasta la nueva cuarta zona». Incorporados fuentes, recursos, pruebas, scripts de compilación y herramientas auxiliares.
- El inventario revisado contiene 89 archivos Java del juego, 13 de pruebas y 332 recursos. El script de compilación ejecuta 12 suites; el archivo de pruebas restante es un auxiliar de importación legada.
- El código disponible procede de la recuperación histórica descrita en [ORIGEN.json](ORIGEN.json) y de su desarrollo posterior; no se identifica como los fuentes originales del autor.

## Nueva cuarta zona de profundidades — 1 de noviembre de 2026

- Añadido el mapa interno 7 entre la tercera y la antigua cuarta zona, conservando los identificadores 0–6 y las conexiones posteriores.
- Ocho Picarones, pasos de Rocky, bifurcaciones, atajo central, descanso y tres secretos; reutilización de los recursos y capas profundas existentes.
- Adaptación de partidas anteriores para inicializar el mapa nuevo sin reubicar al jugador ni escribir al cargar; persistencia de enemigos y piedras.
- [CUARTA-ZONA.md](CUARTA-ZONA.md) documenta las suites y capturas de esa entrega. La prueba manual del usuario permanece pendiente.

## Tercera zona y ajuste visual — 1 de noviembre de 2026

- Recorrido con aprendizaje de Rocky, encuentros combinados, atajo y secreto; conservación de seis Enfadillos y seis Picarones.
- Enfadillos estacionarios; Picarones con detección, visibilidad, territorios y regreso sin curación. Adaptación de partidas a la distribución nueva.
- Ajuste visual aprobado con suelo y corales originales, capas profundas, luces y siluetas, conservando recorrido y colisiones de ese ajuste.
- Detalles y validación histórica en [TERCERA-ZONA.md](TERCERA-ZONA.md).

## Segunda zona renovada — entrega previa al respaldo completo

- Implementado el [plan de segunda zona](PLAN-SEGUNDA-ZONA.md): coberturas, circuitos, territorios para Greeny, encuentros de Enfadillo y dos secretos.
- Conservados enemigos, recompensas y conexiones; recordatorio breve de cobertura y fondo por capas.
- Migración del guardado anterior con conservación del progreso y reubicación de partidas de esa zona en la esmeralda.
- Alcance, pruebas y límites en [SEGUNDA-ZONA.md](SEGUNDA-ZONA.md). El plan se conserva como antecedente, aunque su encabezado aún describe la fase de planificación.

## Restauración de Enfadillo y menú marino — entregas previas

- Restaurado el disparo original de los Enfadillos de la segunda zona, con inmovilidad, alineación, línea de tiro y colisiones del proyectil. [ENFADILLO.md](ENFADILLO.md) describe esa etapa; la inmovilidad en la tercera se incorporó después.
- Integrado el GIF marino aprobado en el menú, con escalado sin suavizado, proporciones conservadas y textos con contraste. Detalles en [MENU-MARINO.md](MENU-MARINO.md), fechado el 31 de octubre de 2026.

## Primera zona renovada — 31 de octubre de 2026

- Nueva distribución del arrecife inicial: refugio, cuatro áreas de combate, un circuito occidental y un desvío al sur. Se conservan los seis Greeny, sus atributos y recompensas.
- Territorios locales para evitar que los enemigos se acumulen o invadan el refugio. Los demás mapas conservan su comportamiento anterior.
- Dos detalles opcionales: una marca de recuerdo y una piedra sonriente. Se examinan con Enter mirando hacia ellos; no interrumpen automáticamente la exploración.
- Recordatorios contextuales de movimiento, ataque, defensa y disparo que desaparecen tras probar cada acción. Shadow y la habitación extra original permanecen.
- Fondo marino pixelado: cinco cuadros de animación lenta, dos capas con desplazamiento relativo y pequeñas partículas fuera del terreno jugable. Caché fija de aproximadamente 3,75 MiB; sin nuevas lecturas de archivos por cuadro.
- Adaptación de partidas de la primera zona anterior, conservando estadísticas, enemigos derrotados, objetos y progreso de las demás zonas.
- Ningún PNG ni WAV original fue modificado. Cambia `maps/stage1.txt`; el fondo nuevo se dibuja con geometría Java2D.

Los apartados siguientes documentan la estabilización previa. Su sección antigua de audio se complementa y corrige con [CORRECCION-AUDIO.md](CORRECCION-AUDIO.md).

## Estabilización de la base — entrega anterior a las zonas renovadas

Los siguientes apartados describen esa entrega histórica, no el inventario final tras añadir la nueva cuarta zona.

### Partidas y progreso

- Se guardan destreza y defensa correctamente y se reconstruye el objeto de salida `Exit`.
- El guardado nuevo incluye mapa, posición, área, equipo y linterna, inventario, cofres, obstáculos destruidos, rocas y placas, estado de enemigos y progreso del jefe.
- Los objetos tienen identificadores estables; los nombres antiguos siguen reconocidos para importar partidas.
- La carga reconstruye y valida los datos antes de cambiar el mundo activo. Un archivo incompleto, una versión desconocida o un objeto inválido produce un error y conserva la partida en memoria.
- Se escribe primero un archivo temporal y se mantiene un respaldo del guardado válido anterior. Importar `save.dat` no modifica ese archivo.
- Reintentar conserva el progreso del mundo. Una partida nueva limpia objetos, proyectiles, equipo y estados anteriores.

Los enemigos comunes mantienen la regla original de reaparecer al cambiar de área. Guardar su estado en un punto de descanso no elimina esa regla. Las rocas de los rompecabezas ya no vuelven a su posición inicial al cruzar entre áreas.

### Combate, inventario y controles

- Soltar Espacio cancela la guardia incluso estando quieto. Se conserva la ventana de parry y la reducción del bloqueo.
- Un bloqueo con defensa alta ya no cura por producir daño negativo.
- Las burbujas hacen daño desde el nivel 1; se conserva la fórmula original desde el nivel 2.
- Una recompensa grande de experiencia concede todos los niveles cuyos umbrales se han alcanzado.
- Un objeto rechazado por inventario lleno permanece en el mundo; un cofre conserva su recompensa.
- Sin espacio para un proyectil no se consume agua. Cambiar de mapa libera el disparo pendiente y permite volver a disparar.
- P pausa como indica la ayuda y Y sigue funcionando. R recarga el mapa activo.
- Perder el foco libera movimiento, defensa y disparo. Enter no queda pendiente para saltarse un diálogo posterior o confirmar una opción al abrir el menú.

### Funcionamiento interno

- Se reconstruyó el proyecto y se añadió una compilación reproducible con pruebas, además de un JAR con punto de entrada ejecutable.
- El bucle conserva una cadencia de 60 actualizaciones por segundo y espera entre ellas. Se eliminó la espera activa continua.
- Las imágenes se comparten mediante una caché. Las vistas de mapa se generan cuando hacen falta, usando el mismo gestor de terreno.
- El audio utiliza un número limitado de voces y cierra los recursos; la ausencia de un dispositivo de audio no impide jugar.
- La búsqueda de caminos usa costes acumulados y evita el escaneo repetido de todos los obstáculos por cada casilla.
- Diálogos, compras, sueño, transiciones, cinemáticas, muerte y temporizadores avanzan con la simulación. Dibujar la pantalla no ejecuta esas acciones.
- Las preferencias se escriben al cambiarlas, en lugar de escribir el archivo en cada dibujo del menú.

### Contenido conservado y límites de aquella entrega

Se mantienen los 291 PNG, los 28 WAV y los siete mapas del JAR original. La población inicial comprobada es de **87 enemigos**, incluidos el jefe y los enemigos secretos. No se añadieron zonas, jefes, diálogos ni sprites.

Esta entrega estabiliza y reorganiza sistemas concretos. `Entity` todavía reúne muchas responsabilidades, existen capacidades fijas y gran parte del contenido se define en código. La futura expansión necesitará continuar esa separación. El selector de apariencias incompleto heredado no se convirtió en una función nueva. El peso del paquete sigue alrededor de 97,6 MB: conservar los WAV originales conserva también la mayor parte de ese peso.

## Corrección posterior del audio — 30 de octubre de 2026

Las operaciones del dispositivo se trasladaron a un hilo propio con solicitudes sin espera desde el juego, recursos reutilizados, límites de voces y descarte de solicitudes caducadas. Esta corrección complementa y reemplaza las conclusiones antiguas de audio donde corresponda. Los resultados y su alcance están en [CORRECCION-AUDIO.md](CORRECCION-AUDIO.md).


## 2026-11-01 — Preparación visual de quinta zona y Sharky

- Añadido design/quinta-zona con decisiones, tarea y criterios para Codex.
- Preparados 32 PNG nuevos, SVG editables y fuentes de píxeles: fondo abisal, partículas, criaturas, borde, coral luminoso y overlays de Sharky en cuatro direcciones, conservando cuerpos originales.
- Previews, diagramas y referencia generada con procedencia documentada.
- Estado compartido registra aprobación de la nueva cuarta y reparto ChatGPT visual/diseño/publicación – Codex código/pruebas.
- Juego ejecutable, Java y recursos activos sin cambios. Validación de arte/documentación, sin nuevas suites.
