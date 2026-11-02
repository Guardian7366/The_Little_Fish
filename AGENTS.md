# Reglas de trabajo de The Little Fish

Estas instrucciones se aplican a todo el repositorio. Las instrucciones expresas del usuario para la tarea en curso prevalecen sobre este documento.

## Inicio de cada tarea

1. Leer [ESTADO_PROYECTO.md](ESTADO_PROYECTO.md) y consultar [HOJA_RUTA.md](HOJA_RUTA.md) para conocer la prioridad y su autorización.
2. Revisar `git status` y la referencia actual. Respetar los cambios existentes, incluidos los archivos sin seguimiento; no borrarlos ni incorporarlos a la tarea por defecto.
3. Consultar [ARQUITECTURA.md](ARQUITECTURA.md) y los documentos de la zona o sistema afectado, solo en lo necesario.
4. Trabajar dentro del alcance autorizado. Una propuesta de la hoja de ruta no autoriza su implementación.

## Reglas permanentes

- Mantener las mecánicas, identidad visual y recursos originales salvo cambios expresamente incluidos en la tarea aprobada. No sustituir sprites, sonidos, canciones o mapas por iniciativa propia.
- Proteger las partidas personales, sus respaldos y la configuración. Las comprobaciones deben usar archivos de prueba aislados bajo `build/`, nunca la partida del usuario.
- Trabajar con las fuentes disponibles en `src/`. No reconstruir ni descompilar el JAR.
- No instalar dependencias, crear commits, subir cambios ni usar subagentes sin autorización expresa.
- Evitar refactorizaciones y análisis exhaustivos ajenos a la tarea. Si aparece una decisión creativa o una ampliación de alcance, documentarla como propuesta y consultar al usuario antes de implementarla.
- Seguir los contratos de actualización, dibujo, recursos y guardado de [ARQUITECTURA.md](ARQUITECTURA.md). Los cambios de persistencia deben contemplar compatibilidad, migración y pruebas pertinentes.
- No reemplazar el JAR de la raíz automáticamente. La compilación genera `build/TheLittleFish.jar`; su sustitución o distribución requiere estar incluida en la tarea autorizada.

## Compilación y validación

El juego utiliza Java Swing/Java2D y un JDK 17 o posterior. `build.ps1` usa `jar` del PATH y los ejecutables `java.exe` y `javac.exe` de esa misma carpeta. No requiere bibliotecas externas.

- Compilar: `./build.ps1` o `./Compilar.cmd`.
- Compilar y ejecutar las suites: `./build.ps1 -Test` o `./Compilar.cmd -Test`.
- Incluir la importación legada contra un original disponible: `./build.ps1 -Test -OriginalJar "C:\ruta\MyGame.jar"`.

Ejecutar estos comandos solo cuando corresponda al alcance autorizado; una tarea de documentación no exige compilar. Para cambios técnicos, usar las comprobaciones pertinentes y la suite completa cuando afecten sistemas compartidos, guardado o transiciones. Los resultados y capturas se generan bajo `build/`. No afirmar que una prueba pasó si no se ejecutó; identificar los resultados heredados de informes anteriores. Las pruebas automáticas y capturas no sustituyen la valoración manual del jugador ni demuestran FPS en su equipo.

`Jugar.cmd` prefiere el JAR de la raíz si existe. Para comprobar una compilación nueva, ejecutar expresamente `java -jar <ruta-al-JAR-generado>` desde una carpeta de pruebas aislada, usando una ruta absoluta al JAR y sin copiar partidas personales.

## Información compartida y cierre

- ChatGPT ayuda a planificar y tomar decisiones creativas; el usuario aprueba alcance y prioridades; Codex implementa y verifica.
- Registrar decisiones aprobadas y resultados en archivos del repositorio. No depender del historial de una conversación para transmitir el estado a otra sesión.
- Actualizar [ESTADO_PROYECTO.md](ESTADO_PROYECTO.md) con el resultado, evidencias, pendientes y siguiente paso; [CAMBIOS.md](CAMBIOS.md) con cambios realizados; y [HOJA_RUTA.md](HOJA_RUTA.md) si cambia una prioridad o su estado. Evitar editar documentos sin relación con la tarea.
- Mantener README como entrada, ARQUITECTURA como referencia técnica y los documentos por zona como detalle. Enlazar esas fuentes en vez de copiarlas.
- Separar siempre: implementado, comprobado, aprobado por el usuario y propuesto. Citar referencia Git y fecha cuando ayuden a identificar la base; describir cambios locales como no publicados hasta su publicación real.
- Al terminar, resumir archivos modificados, validación realizada y límites pendientes. La publicación en GitHub se hará únicamente cuando el usuario la autorice. ChatGPT podrá consultar allí la versión publicada; los cambios locales no se sincronizan por sí solos.
