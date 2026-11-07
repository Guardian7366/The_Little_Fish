# Estado del proyecto

Última revisión: **6 de noviembre de 2026**. Objetivo: desarrollar la versión definitiva de The Little Fish con planificación y decisiones creativas en ChatGPT, implementación técnica en Codex y archivos del repositorio como información compartida.

## Base y situación actual

- Repositorio: [Guardian7366/The_Little_Fish](https://github.com/Guardian7366/The_Little_Fish), rama `main`.
- Base de la séptima zona: f160c37. El usuario probó y aprobó el templo, Planta y Secret el 4 de noviembre de 2026 y autorizó publicar la entrega. Pasaron 22 suites. [Informe del templo](SEPTIMA-ZONA.md)
- Base de la sexta zona: `c7ae5e8`, paquete de Fishtella y encargo de la expansión. La quinta aprobada se publicó en `6f2f887`.
- La edición definitiva todavía no está terminada. La base incluye estabilización, menú marino, renovación de las tres primeras zonas y una nueva cuarta zona de profundidades.
- El usuario probó y aprobó Sharky, el recorrido general y la ambientación de la quinta. Se continuó desde esos cambios locales, sin reiniciar ni modificar lo aprobado. La revisión 2 añade Iron obligatorio y corrige las dos puertas adicionales; el usuario aprobó estas correcciones y autorizó el commit y la publicación el 4 de noviembre de 2026. El ejecutable de prueba se genera en `build/TheLittleFish.jar`.

## Disponibilidad comprobada

| Elemento | Situación |
|---|---|
| Código del juego | 98 archivos Java en `src/main/java` |
| Pruebas | 31 archivos Java en `src/test/java`. El script ejecuta 30 suites y utiliza una clase auxiliar para el guardado legado |
| Recursos | 574 archivos en `src/main/resources`; 71 PNG corresponden al paquete del templo |
| Compilación | `build.ps1` y `Compilar.cmd`, con salida en `build/TheLittleFish.jar` |
| Herramientas auxiliares | Scripts Python en `tools/`; no son una dependencia del juego ni del script de compilación Java |
| Ejecutable existente | `TheLittleFish.jar` y lanzador `Jugar.cmd` en la raíz |

Los recuentos corresponden a esta revisión, no son requisitos permanentes. El código procede de una recuperación previa mediante CFR y de modificaciones posteriores; no se presenta como los fuentes originales del autor. [ORIGEN.json](ORIGEN.json) conserva la procedencia de la entrega anterior, no describe el JAR generado de quinta. Se verificaron los recursos contra `ca4b3ad`, sin comparar con un original externo.

## Avances documentados

| Área | Referencia y alcance |
|---|---|
| Estabilización y persistencia | [ARQUITECTURA.md](ARQUITECTURA.md), [CAMBIOS.md](CAMBIOS.md) y [VALIDACION.md](VALIDACION.md): contratos técnicos y resultados históricos |
| Audio y menú | [CORRECCION-AUDIO.md](CORRECCION-AUDIO.md) y [MENU-MARINO.md](MENU-MARINO.md) |
| Primera zona | [PRIMERA-ZONA.md](PRIMERA-ZONA.md) y [VALIDACION-PRIMERA-ZONA.md](VALIDACION-PRIMERA-ZONA.md) |
| Segunda zona | [SEGUNDA-ZONA.md](SEGUNDA-ZONA.md); [PLAN-SEGUNDA-ZONA.md](PLAN-SEGUNDA-ZONA.md) conserva el plan anterior a la implementación |
| Tercera zona | [TERCERA-ZONA.md](TERCERA-ZONA.md), incluido el ajuste visual aprobado |
| Nueva cuarta zona | [CUARTA-ZONA.md](CUARTA-ZONA.md): mapa interno 7, ocho Picarones, pasos de Rocky, secretos y adaptación del guardado |
| Quinta zona y Sharky | [QUINTA-ZONA.md](QUINTA-ZONA.md): implementación local, guardados, pruebas, capturas y coste medido |

La nueva cuarta conecta la tercera con la antigua cuarta, ahora quinta en el recorrido. Conserva los identificadores anteriores 0–6. [ENFADILLO.md](ENFADILLO.md) describe la restauración inicial en la segunda zona; el comportamiento posterior de la tercera se documenta en TERCERA-ZONA.

## Validación y pendientes

- La revisión 2 compiló con JDK 25 para Java 17 y pasó las 17 suites de `build.ps1 -Test`. Sharky: 1.261 aserciones; quinta: 8.867; guardados de quinta: 214; 32 PNG preparados conservan hashes/dimensiones/alpha exactos. Log: `build/fifth-correction-verification.txt`; detalle en QUINTA-ZONA.
- Los resultados de suites y capturas descritos en documentos de entregas anteriores son evidencia histórica, no ejecuciones nuevas de esta revisión. Sus rutas `build/` pueden no estar presentes en el repositorio.
- El usuario confirmó que la nueva cuarta cumple sus expectativas y funciona correctamente; da por terminada esa ampliación. Es valoración manual del usuario, no una nueva ejecución técnica de esta entrega.
- Se generaron seis capturas aisladas de Sharky, mapa y veinte vistas de quinta con/sin linterna. Luz y fondo mantienen una caché estimada de píxeles de 2.689.408 bytes; hay medición headless con 40 luces y 32 Sharkys. No son FPS del juego interactivo.
- Guardados antiguos generados compilando las clases originales de `ca4b3ad`: migración y roundtrip pasan, sin reescritura al cargar. La prueba contra `MyGame.jar` externo se omite explícitamente por no estar incluido.
- Se comprobaron también siete guardados reales de la primera implementación local y ocho casos sintéticos siempre activos: nueva barrera, antiguo borde eliminado, lado de salida, cofre abierto sin Iron y otros mapas. La revisión 2 conserva la barrera destruida al guardar/cargar y al cambiar de área, así como los usos opcionales anteriores.
- Cuatro capturas nuevas en `build/fifth-correction-preview/`: barrera intacta/abierta y ambas puertas con tile 086. Se verificó que las 23 transiciones reales no permiten evitar la barrera desde la llegada normal; obtener Iron, volver y equiparlo abre el paso mediante las colisiones existentes.
- Recursos originales y JAR raíz verificados contra la base; solo cambia `stage4.txt`. README, CAMBIOS, arquitectura y hoja de ruta se actualizan con esta entrega local.
- La carpeta local `promo/` estaba sin seguimiento al iniciar esta tarea; no forma parte del trabajo documental autorizado.
- Sharky y quinta están implementados dentro del encargo aprobado. La edición definitiva completa aún no tiene alcance final cerrado; no se autoriza desarrollar zonas posteriores a la quinta.

## Siguiente paso

La quinta, la sexta, la séptima y la octava zona están completadas y aprobadas. Los tres primeros refugios del mapa 5 también están completados y aprobados. Pump, Hongui y Ghost también están completados y aprobados. Handy e Ic completan los ocho refugios aprobados del mapa 5. El mercado de TV también está completado y aprobado. La fosa y el encuentro final de God Fish están completados, incluida la revisión de movilidad, ritmo y objetivos aprobada por el usuario. El recorrido principal ya llega del arrecife a los créditos. La integración narrativa está aprobada. La playa jugable y sus recursos están integrados y aprobados.


## Paquete visual para Codex — 2026-11-01

Base consultada: bc0c1c9. Preparado [design/quinta-zona/README.md](design/quinta-zona/README.md): tarea técnica, diseño, contrato de arte, 32 PNG nativos con SVG/rectángulos, tres previews y referencias de ambiente/recorrido/estados. El usuario autorizó crear y publicar esta entrega. Recursos candidatos: no jugables aprobados ni integrados.

Reparto solicitado: ChatGPT razona decisiones, prepara visuales y publica lo autorizado; Codex programa y verifica. Esta entrega NO modifica Java, recursos activos, mapas ni JAR. Validación: dimensiones/alpha y hashes de 32 PNG, inspección de tres previews y documentación. Sin compilación ni pruebas del juego. Git identificará la publicación efectiva.

El paquete se publicó en `ca4b3ad` y ya fue utilizado para la implementación local autorizada. Su estado «preparado, no integrado» describe la entrega histórica de arte; el resultado actual y las diferencias concretas están en [QUINTA-ZONA.md](QUINTA-ZONA.md). La autorización del 4 de noviembre de 2026 abarca la publicación de fuentes, recursos, pruebas y documentación.

## Sexta zona y Fishtella

El usuario aprobó la quinta zona y el ciclo de combate propuesto para Fishtella. Se prepararon doce sprites nativos y el encargo de la expansión. La entrega incorpora el mapa 8, Fishtella con sus doce sprites, un primer encuentro aislado, encuentros mixtos con Sharky, el paso obligatorio de Iron y la migración de partidas. Pasaron las 20 suites. El usuario probó la zona y el enemigo, confirmó que funcionaban como esperaba y autorizó su publicación el 4 de noviembre de 2026. [Informe de la sexta zona](SEXTA-ZONA.md)

## Séptima zona

El usuario aprobó las reimaginaciones de Planta y Secret y encargó un templo sumergido con cuatro rocas y placas, murales sobre D y dos Secret ocultos. El templo del mapa interno 4 incorpora los 71 PNG del paquete, un encuentro aislado con Planta antes de combinarla con Fishtella, dos Secret en nichos independientes con 100 EXP únicos por ejemplar, los murales de D y el fondo de tres capas. Las cuatro galerías tienen pasillos laterales y el portón solo abre con las cuatro parejas correctas. Pasaron las 24 órdenes de resolución, los guardados parciales y las pruebas de migración, combate y transiciones. [Informe y capturas](SEPTIMA-ZONA.md)

## Octava zona

La séptima está aprobada y publicada en b56661a. La base b9d1104 incorpora el encargo y las estatuas. La octava conecta la séptima con el jefe mediante el mapa 9. Incluye siete Plantas y cuatro Fishtellas distribuidas por cámaras, tres estatuas con inscripciones opcionales de D y tres parejas de roca y placa, una con giro. Pasaron 24 suites, incluidas las seis órdenes, la recarga en el codo, las transiciones y la migración de tres partidas históricas. El usuario confirmó tras jugar que la zona cumple los requisitos y autorizó su publicación el 4 de noviembre de 2026. [Informe y capturas](OCTAVA-ZONA.md)

## Primeras tres salas extras

El usuario aprobó tres retos independientes para Chicken con Linterna, Flower con poción y Spiky con cama portátil. La entrega sobre 6d1bd4c integra los 38 PNG, los dos faroles de Chicken, un solo Greeny en Flower y los dos tramos de Spiky. Las ocho salas tienen límites propios de cámara, dibujo, simulación y mapas. Se conservan los cofres y sus IDs. Pasaron las 26 suites, las 32 esquinas y seis guardados históricos. El usuario probó las tres salas, confirmó que funcionan como esperaba y aprobó la publicación el 4 de noviembre de 2026. [Informe de los refugios](SALAS-EXTRAS-1-3.md)

## Pump, Hongui y Ghost

Entrega sobre e982c07 con los 43 PNG del paquete. Pump conserva la poción de +5, Hongui el Super Shield y Ghost Rocky. Los cortes, la guardia o derrota y la secuencia de memoria se guardan por separado de los premios y de los tres retos anteriores. Pasaron 28 suites, seis guardados históricos nuevos y las 32 esquinas. El usuario probó las tres salas, confirmó que funcionan como esperaba y autorizó su publicación el 5 de noviembre de 2026. [Informe y capturas](SALAS-EXTRAS-4-6.md)

## Handy e Ic

Entrega sobre fe9f548 con los 37 PNG del paquete. Handy conserva Iron y usa dos cerrojos de Rocky. Ic conserva la segunda arma y usa dos marcas en una red de hielo. Se comprobaron 69 paradas, incluidas 44 contra paredes, y ocho guardados históricos. Pasaron las 30 suites. El usuario probó las dos salas y sus NPC, confirmó que funcionan como esperaba y autorizó su publicación el 5 de noviembre de 2026. [Informe y capturas](SALAS-EXTRAS-7-8.md)

## TV Supply · Mercado aprobado

Sobre `92fd83c`, el mapa interno 10 conecta la octava con la arena. TV atiende en el mostrador y la esmeralda permite descansar y guardar en el mercado. El CRT usa la mochila real para vender, confirma cada unidad y protege equipo puesto y recompensas únicas. Red Poti, Strong Potion y Water Tonic cuestan 4, 9 y 5 monedas respectivamente.

`./build.ps1 -Test` pasó las 32 suites. Se cargaron cuatro partidas generadas con las fuentes de `92fd83c` y aisladas bajo `build/`. Cubren octava resuelta y arena antes, durante y después del combate. La importación `save.dat` se probó con una muestra del formato original de diez filas. La comprobación adicional con el ejecutable histórico `MyGame.jar` quedó omitida por falta de ese archivo. Se comprobaron los 32 PNG contra los bytes, dimensiones y transparencia del manifiesto. El registro está en `build/mercado-verification.txt` y las seis capturas en `build/mercado-preview/`.

El usuario probó el mercado y a TV, confirmó que funcionan correctamente y autorizó publicar la implementación. El ejecutable de prueba se genera en `build/TheLittleFish.jar`. [Encargo y arte](design/mercado-tv/TAREA-CODEX.md)

## Arte de la fosa final

Base revisada c022bb7, mercado aprobado. El usuario aprobó seis fases para God Fish y pidió preparar primero los tiles y fondo. Se prepararon 19 PNG nativos y tres vistas. Formato y transparencia verificados. Estos recursos se integraron en el encuentro final. [Paquete de la fosa](design/jefe-final/fosa/ARTE-FOSA.md).

## Arte de combate de God Fish

Paquete visual del titán calamar sobre la base de la fosa de `ae8dd70`. Seis paletas, 24 atlas PNG con 291 celdas y cuatro vistas. El titán tiene un ojo sin boca, perfiles en tres cuartos y poses de puños diferenciadas para preparación, golpe y recuperación. Incluye armas, ataques, proyectiles, efectos y mecanismos. Los atlas revisados se integraron en el encuentro final. [Guía y atlas](design/jefe-final/god-fish/ARTE-GOD-FISH.md).

## God Fish y la fosa

Sobre `8ab5965`, el mapa 6 incorpora la fosa, la introducción con cámara independiente y las seis fases de God Fish. Se mantienen el regreso previo al mercado, la llave y los créditos. La migración adapta posiciones, reinicia los combates legados y conserva las victorias con su botín. Pasaron las 34 suites, el recorrido automatizado del mercado a los créditos y seis guardados generados con las fuentes de la versión anterior. El usuario autorizó publicar esta entrega el 6 de noviembre de 2026. [Guía de juego y balance](GOD-FISH.md)

### Revisión aprobada del combate

Sobre `e1d17bb`, se integraron los seis atlas de nado, la movilidad protegida, el retorno anunciado, la introducción con movimiento y los objetivos revisados de las seis fases. La revisión de arena conserva el regreso al mercado y las victorias con llave pendiente. Pasaron las 34 suites y el recorrido automatizado con el JAR empaquetado. El usuario aprobó la revisión y autorizó su publicación el 6 de noviembre de 2026. [Guía y balance](GOD-FISH.md)

### Esbirros de la fase 4 y contacto del manto

Sobre `d75386b`, la entrega aprobada incorpora Aguijón y Baluarte exclusivamente como las cuatro invocaciones de God Fish. Los sellos conservan un solo esbirro activo y un vínculo por derrota confirmada. Pasaron las 34 suites y se repitió el recorrido del mercado a los créditos con el JAR empaquetado. Se comprobaron el bloqueo de aparición, el reintento, la correspondencia de los avisos con el daño y la apertura tras el cuarto vínculo. El centro del manto inflige 1 HP mientras el jefe está protegido. Se comprobó el contacto entre ataques, la ausencia de daño y empuje durante la vulnerabilidad y el desplazamiento lateral seguro junto a una pared. El usuario aprobó esta entrega y autorizó su publicación. [Guía del encuentro](GOD-FISH.md)

## Lore y desenlace de D

Integración sobre `1a609a8`. El usuario revisó los diálogos y el nuevo final en juego, aprobó la entrega y autorizó publicarla. Las pistas de NPC, refugios, inscripciones y murales usan los puntos de interacción existentes. God Fish conserva cuatro páginas de introducción y el reintento usa la cuarta. La primera entrega del desenlace incluía el diálogo de D y Echo y una maqueta silenciosa de la costa. La playa jugable descrita abajo sustituyó esa maqueta. Después se muestran el título y los créditos con su música y atribuciones. [Guion y encargo](design/lore/GUION-Y-ENCARGO.md).

Pasaron las 35 suites de `build.ps1 -Test`. Se leyeron las pistas en sus interacciones normales, incluidos los grupos 3–17, y se visitaron los ocho refugios en un orden alternativo. El desenlace llegó a los créditos con y sin pistas opcionales. Una partida generada con las fuentes de `1a609a8` conservó sus datos y permitió iniciar las conversaciones con índices de diálogo guardados. Se verificaron el texto a 960 × 576, la costa a 1280 × 720 y el recorrido del mercado a los créditos con reintento. Registro en `build/lore-verification.txt`.

## La última orilla

Sobre `356bb1b` se integró el portal en la salida original de la fosa y el paseo horizontal de tres pantallas. La recogida de la foto abre el diálogo de D y Echo y da paso al retrato, al atardecer y a los créditos. El paseo se mantiene en memoria durante la escena. El oleaje usa el volumen de efectos y se desvanece antes del título. [Encargo y manifiesto](design/lore/playa/TAREA-CODEX.md)

Pasaron las 35 suites de `build.ps1 -Test`. Se repitió con el JAR empaquetado el recorrido del mercado a los créditos. Tres partidas generadas con `356bb1b` confirmaron la recuperación de la llave pendiente, el paso por la puerta abierta y la conservación del desenlace consumido. Se comprobaron la ida y vuelta con los controles normales, el bloqueo de guardado, la recogida única con Enter retenido, la proporción de pantalla ancha y el ciclo del audio con un dispositivo simulado. Registros en `build/beach-verification.txt` y `build/beach-packaged.txt`. La comprobación técnica del oleaje se hizo con un dispositivo simulado.

El JAR de prueba queda en `build/TheLittleFish.jar` y las capturas en `build/beach-preview/`. El usuario aprobó esta entrega y autorizó su publicación en `main`.


## Créditos finales

Integración sobre `0b18f2d`. Los créditos reproducen 14 tomas deterministas de los mapas vigentes, con las 71 líneas y los dibujos en el orden y los tiempos del manifiesto. Las tres fotos se usan en la recogida, el epílogo y la despedida. **Play Again** devuelve al menú conservando la partida. El reloj dura 120 segundos y corre en silencio hasta disponer de la pista instrumental. [Encargo y recursos](design/lore/creditos-finales/TAREA-CODEX.md).

Pasaron las 36 suites de `build.ps1 -Test` y la comprobación de los créditos con el JAR empaquetado. Se validaron los 14 cortes, el ajuste de letra en resolución lógica y el escalado panorámico, la independencia de los mapas jugables, el Enter nuevo, el clic y la conservación del archivo de guardado. Se revisaron fotogramas de las 14 tomas en su cronología y se verificó el MP4 continuo a 30 fps. Registros en `build/credits-verification.txt` y `build/credits-packaged.txt`.

El usuario probó los créditos y la despedida, aprobó la entrega y autorizó su publicación. El JAR está en `build/TheLittleFish.jar` y el video en `build/credits-preview/TheLittleFish-creditos.mp4`.
