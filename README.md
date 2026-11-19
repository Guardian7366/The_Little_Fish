# The Little Fish

Juego de exploración y combate en Java Swing/Java2D. El recorrido actual incluye once mapas internos, ocho refugios, el mercado de TV, el encuentro de seis fases de God Fish, la playa jugable y los créditos de 120 segundos con el instrumental aprobado.

El estado de las entregas y sus autorizaciones está en [ESTADO_PROYECTO.md](ESTADO_PROYECTO.md). Las prioridades están en [HOJA_RUTA.md](HOJA_RUTA.md), los cambios en [CAMBIOS.md](CAMBIOS.md) y los contratos técnicos en [ARQUITECTURA.md](ARQUITECTURA.md). [AGENTS.md](AGENTS.md) contiene las reglas permanentes de trabajo.

## Compilar y probar

Se necesita un JDK 17 o posterior con `java`, `javac` y `jar` en el PATH. La compilación usa `--release 17` y las bibliotecas estándar de Java.

Desde la raíz del checkout, ejecutar en PowerShell.

~~~powershell
./build.ps1
./build.ps1 -Test
~~~

`Compilar.cmd` y `Compilar.cmd -Test` ofrecen las mismas opciones. Las 36 suites generan sus partidas, capturas y resultados bajo `build/`. Algunos casos históricos adicionales se activan cuando existen fixtures locales. Para generar los de quinta a partir del historial, ejecutar primero `./tools/verify_fifth_baseline.ps1`. La importación de un JAR original externo puede comprobarse con `./build.ps1 -Test -OriginalJar "C:\\ruta\\MyGame.jar"`.

## Jugar

Después de compilar, abrir `Jugar.cmd`. El lanzador utiliza `build/TheLittleFish.jar`. También puede ejecutarse `java -jar ./build/TheLittleFish.jar`.

El juego escribe partidas y configuración en el directorio desde el que se inicia. Para probar el JAR sin tocar una partida personal, iniciarlo desde una carpeta aislada con la ruta absoluta al ejecutable.

| Acción | Tecla |
|---|---|
| Moverse | WASD |
| Confirmar, interactuar o atacar | Enter |
| Disparar burbuja | F |
| Defender y hacer parry | Espacio |
| Inventario y equipo | C |
| Pausar o continuar | P o Y |
| Opciones | Esc |
| Mapa / minimapa | M / X |

La foto al final de la playa inicia el desenlace. **Play Again** vuelve al menú y conserva la partida. Los agradecimientos originales se consultan con C desde la despedida.

## Partidas y configuración

Un punto de descanso esmeralda guarda `save-v1.dat` y conserva la versión válida anterior en `save-v1.dat.bak`. Cargar valida el archivo antes de publicar su estado en memoria. Los guardados antiguos mantienen sus migraciones de mapas, equipo y progreso.

Para trasladar una partida, copiar su archivo a una carpeta de juego propia y seleccionar LOAD GAME. Para importar el formato original, colocar `save.dat` en una carpeta sin `save-v1.dat`. La importación conserva los datos disponibles y reanuda al inicio. [Persistencia y migraciones](ARQUITECTURA.md#contrato-de-guardado).

Las partidas, los respaldos y `config.txt` son archivos locales ignorados por Git. El historial conserva las versiones antiguas que habían sido incluidas anteriormente.

## Carpetas

| Ruta | Contenido |
|---|---|
| `src/main/java/` | Código del juego |
| `src/main/resources/` | Mapas, imágenes, sonidos, música y fuentes vigentes |
| `src/test/` | Pruebas y contratos de conservación de recursos |
| `design/` | Guías, manifiestos y recursos activos del cierre |
| `tools/` | Generadores de mapas y auxiliares de guardados históricos |
| `build/` | JAR, clases, partidas de prueba y evidencias generadas |

La compilación empaqueta directamente los dibujos, las fotos, el GIF y el instrumental desde `design/lore/creditos-finales/`, y genera la cronología desde `creditos.json`. Es necesario conservar esta carpeta al copiar las fuentes. [Dependencias de diseño](design/README.md).

Las maquetas, las copias de entregas y los ejecutables antiguos retirados del árbol actual siguen disponibles en el historial de Git. [ORIGEN.json](ORIGEN.json) documenta la recuperación histórica de las fuentes mediante CFR, y describe aquella entrega.

## Mantenimiento

Los documentos de cada zona describen las colisiones, los encuentros y la compatibilidad. [GOD-FISH.md](GOD-FISH.md) explica el combate final. Los contratos del cierre están en [el guion](design/lore/GUION-Y-ENCARGO.md), [la playa](design/lore/playa/TAREA-CODEX.md) y [los créditos](design/lore/creditos-finales/TAREA-CODEX.md).

Los informes históricos registran resultados de sus entregas. Los resultados de una comprobación nueva se generan en `build/`. La limpieza fue aprobada por el usuario el 18 de noviembre de 2026.
