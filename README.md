# The Little Fish

Juego de exploración y combate en Java Swing/Java2D. La base actual incluye estabilización de sistemas y guardado, corrección de audio, menú marino animado, renovación de las tres primeras zonas y una nueva cuarta zona de profundidades. **La edición definitiva sigue en desarrollo.**

Repositorio: [Guardian7366/The_Little_Fish](https://github.com/Guardian7366/The_Little_Fish). Revisión documental del **1 de noviembre de 2026**, sobre la base `296fb57`, «Respaldo completo del juego hasta la nueva cuarta zona». El estado vigente se registra en [ESTADO_PROYECTO.md](ESTADO_PROYECTO.md).

## Sistema de trabajo

ChatGPT ayuda con planificación y decisiones creativas; el usuario aprueba el alcance; Codex implementa y verifica. Las decisiones y los avances se registran en el repositorio para continuar entre sesiones.

| Documento | Función |
|---|---|
| [AGENTS.md](AGENTS.md) | Reglas permanentes para el trabajo técnico |
| [ESTADO_PROYECTO.md](ESTADO_PROYECTO.md) | Base actual, evidencias, pendientes y siguiente paso |
| [HOJA_RUTA.md](HOJA_RUTA.md) | Prioridades y propuestas, con su estado de autorización |
| [CAMBIOS.md](CAMBIOS.md) | Historial de cambios realizados |
| [ARQUITECTURA.md](ARQUITECTURA.md) | Estructura y contratos técnicos |

ChatGPT puede consultar la versión publicada en GitHub. Los cambios locales estarán disponibles allí después de una publicación autorizada; no existe sincronización automática desde las conversaciones.

## Contenido actual

- [Primera zona](PRIMERA-ZONA.md): recorrido inicial renovado, territorios de enemigos, secretos, ayudas y fondo marino por capas.
- [Segunda zona](SEGUNDA-ZONA.md): coberturas, encuentros distribuidos y Enfadillos estacionarios con el disparo restaurado.
- [Tercera zona](TERCERA-ZONA.md): progresión con Rocky, territorios de Picarones, Enfadillos estacionarios y ambientación profunda con ajuste visual aprobado.
- [Nueva cuarta zona](CUARTA-ZONA.md): profundidades con ocho Picarones, pasos de Rocky, atajos y tres secretos. Conecta la tercera con la antigua cuarta, ahora quinta en el recorrido. Usa el mapa interno 7 y conserva los IDs anteriores 0–6.
- [Menú marino](MENU-MARINO.md): GIF aprobado, proporciones conservadas y textos con mayor contraste.
- [Audio](CORRECCION-AUDIO.md): solicitudes asíncronas para evitar que el juego espere al dispositivo.

Se conservan los sprites y sonidos originales según los informes de las entregas previas. Hay mapas renovados y uno nuevo; la base ya supera los siete mapas jugables originales. La procedencia histórica se registra en [ORIGEN.json](ORIGEN.json).

## Jugar

1. Extrae por completo el paquete del juego en una carpeta propia o utiliza esta copia del repositorio.
2. Abre `Jugar.cmd`. Necesitas Java 17 o posterior; Java no se incluye en la distribución.
3. Elige NEW GAME o LOAD GAME desde el menú.

También puedes ejecutar `java -jar TheLittleFish.jar` desde la raíz. Los archivos de partida y configuración se escriben en la carpeta desde la que se inicia el juego; `Jugar.cmd` utiliza su propia carpeta.

`Jugar.cmd` prefiere `TheLittleFish.jar` de la raíz y solo usa `build/TheLittleFish.jar` si el primero no existe. Compilar no sustituye el JAR de la raíz. Para abrir expresamente el resultado de una compilación desde la raíz del proyecto, usa `java -jar ./build/TheLittleFish.jar`. Para probar sin afectar tus partidas, ejecútalo desde una carpeta aislada indicando la ruta absoluta del JAR.

| Acción | Tecla |
|---|---|
| Moverse | WASD |
| Confirmar, interactuar o atacar | Enter |
| Disparar burbuja | F |
| Defender y hacer parry | Espacio |
| Inventario y equipo | C |
| Pausar o continuar | P o Y |
| Opciones | Esc |
| Mapa | M |
| Minimapa | X |

Los controles de desarrollo heredados siguen disponibles: T muestra información de depuración, G alterna el modo invulnerable y R recarga el mapa actual.

## Guardar y recuperar una partida

Interactúa con un punto de descanso esmeralda usando Enter. Se guarda en `save-v1.dat`; desde el segundo guardado válido, `save-v1.dat.bak` conserva el guardado válido anterior. La carga valida el archivo antes de sustituir la partida en memoria; si falla, aparece un error.

Para trasladar una partida de una versión corregida anterior, conserva su carpeta y **copia** `save-v1.dat` a la nueva carpeta; después elige LOAD GAME. Las revisiones de las zonas primera, segunda y tercera adaptan la distribución anterior conservando el progreso previsto por cada migración. La primera y segunda pueden reubicar al jugador en su esmeralda; consulta [SEGUNDA-ZONA.md](SEGUNDA-ZONA.md) y [TERCERA-ZONA.md](TERCERA-ZONA.md) para sus reglas. La nueva cuarta inicializa su mapa al cargar partidas anteriores sin reubicar al jugador. Cargar no reescribe el archivo: la adaptación se conserva al guardar de nuevo. No lleves el nuevo guardado de vuelta a una versión con mapas anteriores.

Para experimentar el nuevo inicio con sus encuentros completos, elige NEW GAME en una carpeta sin tu partida personal. Se mantienen las reglas originales de reaparición de enemigos al cambiar de área.

Para importar una partida antigua, **copia** su `save.dat` a una carpeta del juego que todavía no tenga `save-v1.dat` y selecciona cargar. El archivo antiguo permanece intacto. Al guardar en un punto de descanso se crea el formato nuevo. El formato antiguo no incluía posición ni todo el estado del mundo: se reanuda al inicio con atributos, inventario y objetos recuperables. No es posible recuperar información que nunca se guardó.

Para restaurar el respaldo, cierra el juego, conserva una copia del `save-v1.dat` actual y copia `save-v1.dat.bak` con el nombre `save-v1.dat`. No hay un botón de restauración automática. Si existe un archivo nuevo dañado, el juego muestra el error; no carga silenciosamente el formato antiguo.

## Compilar y comprobar

Se necesita un **JDK 17 o posterior**. `build.ps1` localiza `jar` mediante el PATH y usa `java.exe` y `javac.exe` de esa misma carpeta. No requiere bibliotecas Java externas ni descargar dependencias.

Desde PowerShell en la raíz del repositorio:

```powershell
./build.ps1
./build.ps1 -Test
```

También puedes usar `./Compilar.cmd` o `./Compilar.cmd -Test`. El script genera `build/TheLittleFish.jar`, con clases compatibles con Java 17 mediante `--release 17`. Con `-Test` compila las pruebas y ejecuta 12 suites de regresión.

Para incluir la comprobación de importación contra un ejecutable original disponible por separado:

```powershell
./build.ps1 -Test -OriginalJar "C:\ruta\MyGame.jar"
```

La ruta anterior es un ejemplo, no un archivo incluido en esta copia. Las salidas de compilación, pruebas y capturas se crean bajo `build/`.

La compilación histórica se documentó con JDK 25. El paquete recibido originalmente no contenía fuentes Java: su recuperación previa utilizó CFR 0.152. El repositorio actual ya contiene esas fuentes y su desarrollo posterior, pruebas y recursos; no es necesario descompilar el JAR para trabajar. No se presentan como los fuentes originales del autor.

## Estructura

| Ruta | Contenido |
|---|---|
| `src/main/java/` | Código del juego |
| `src/main/resources/` | Sprites, sonidos, música, fuentes, menú y mapas |
| `src/test/java/` | Suites de regresión y auxiliar de guardado legado |
| `build.ps1` / `Compilar.cmd` | Compilación y pruebas sin dependencias Java externas |
| `tools/` | Generadores y empaquetadores auxiliares en Python; no se necesitan para jugar o compilar Java |
| `build/` | Salida generada y archivos de comprobación; ignorada por Git |
| `TheLittleFish.jar` / `Jugar.cmd` | Ejecutable existente y lanzador |

Consulta [ARQUITECTURA.md](ARQUITECTURA.md) para responsabilidades y contratos de actualización, dibujo y persistencia.

## Evidencias y límites

Los informes [VALIDACION.md](VALIDACION.md), [VALIDACION-PRIMERA-ZONA.md](VALIDACION-PRIMERA-ZONA.md) y los documentos de cada zona registran pruebas de entregas anteriores. Sus cifras son históricas y no equivalen a pruebas ejecutadas nuevamente sobre cada revisión del repositorio.

La revisión documental actual comprobó archivos y scripts; no compiló ni ejecutó el juego o las suites. La nueva cuarta zona mantiene pendiente la prueba manual del usuario. Las capturas y pruebas automáticas no sustituyen una partida completa ni la valoración de ritmo, dificultad y sonido; tampoco acreditan FPS en el equipo del jugador.
