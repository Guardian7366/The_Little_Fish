# Estado del proyecto

Última revisión: **1 de noviembre de 2026**. Objetivo: desarrollar la versión definitiva de The Little Fish con planificación y decisiones creativas en ChatGPT, implementación técnica en Codex y archivos del repositorio como información compartida.

## Base y situación actual

- Repositorio: [Guardian7366/The_Little_Fish](https://github.com/Guardian7366/The_Little_Fish), rama `main`.
- Base de esta revisión: `296fb578b4b19116408e32adb0c44435caa7e73b`, «Respaldo completo del juego hasta la nueva cuarta zona». Local y GitHub coincidían al comprobar esta base.
- La edición definitiva todavía no está terminada. La base incluye estabilización, menú marino, renovación de las tres primeras zonas y una nueva cuarta zona de profundidades.
- El usuario aprobó los cinco documentos y autorizó crear el commit y publicarlo en GitHub. El commit que incorpora este sistema documental identifica la nueva base compartida; su publicación se confirma comprobando la referencia remota.

## Disponibilidad comprobada

| Elemento | Situación |
|---|---|
| Código del juego | 89 archivos Java en `src/main/java` |
| Pruebas | 13 archivos Java en `src/test/java`; el script ejecuta 12 suites y utiliza una clase auxiliar para el guardado legado |
| Recursos | 332 archivos en `src/main/resources`: 291 PNG, 28 WAV, 2 TTF, 1 GIF y 10 TXT |
| Compilación | `build.ps1` y `Compilar.cmd`, con salida en `build/TheLittleFish.jar` |
| Herramientas auxiliares | Scripts Python en `tools/`; no son una dependencia del juego ni del script de compilación Java |
| Ejecutable existente | `TheLittleFish.jar` y lanzador `Jugar.cmd` en la raíz |

Los recuentos corresponden a esta revisión, no son requisitos permanentes. El código disponible procede de una recuperación previa mediante CFR y de modificaciones posteriores; no se presenta como los fuentes originales del autor. La procedencia histórica está en [ORIGEN.json](ORIGEN.json). No se ha comparado aquí el JAR con las fuentes ni los recursos con un original externo.

## Avances documentados

| Área | Referencia y alcance |
|---|---|
| Estabilización y persistencia | [ARQUITECTURA.md](ARQUITECTURA.md), [CAMBIOS.md](CAMBIOS.md) y [VALIDACION.md](VALIDACION.md): contratos técnicos y resultados históricos |
| Audio y menú | [CORRECCION-AUDIO.md](CORRECCION-AUDIO.md) y [MENU-MARINO.md](MENU-MARINO.md) |
| Primera zona | [PRIMERA-ZONA.md](PRIMERA-ZONA.md) y [VALIDACION-PRIMERA-ZONA.md](VALIDACION-PRIMERA-ZONA.md) |
| Segunda zona | [SEGUNDA-ZONA.md](SEGUNDA-ZONA.md); [PLAN-SEGUNDA-ZONA.md](PLAN-SEGUNDA-ZONA.md) conserva el plan anterior a la implementación |
| Tercera zona | [TERCERA-ZONA.md](TERCERA-ZONA.md), incluido el ajuste visual aprobado |
| Nueva cuarta zona | [CUARTA-ZONA.md](CUARTA-ZONA.md): mapa interno 7, ocho Picarones, pasos de Rocky, secretos y adaptación del guardado |

La nueva cuarta conecta la tercera con la antigua cuarta, ahora quinta en el recorrido. Conserva los identificadores anteriores 0–6. [ENFADILLO.md](ENFADILLO.md) describe la restauración inicial en la segunda zona; el comportamiento posterior de la tercera se documenta en TERCERA-ZONA.

## Validación y pendientes

- Esta revisión comprobó estructura, inventario, scripts y documentación. No ejecutó el juego, compilación, suites ni una descompilación.
- Los resultados de suites y capturas descritos en documentos de entregas anteriores son evidencia histórica, no ejecuciones nuevas de esta revisión. Sus rutas `build/` pueden no estar presentes en el repositorio.
- CUARTA-ZONA declara pendiente la prueba manual del usuario. Ritmo, dificultad y experiencia completa hasta el final aún requieren valoración; no se afirma una partida manual completa ni una medición de FPS.
- README y CAMBIOS se actualizan para incorporar las entregas posteriores a la primera zona. Los informes originales permanecen como referencia histórica.
- La carpeta local `promo/` estaba sin seguimiento al iniciar esta tarea; no forma parte del trabajo documental autorizado.
- No hay una definición final aprobada del alcance de la edición definitiva ni autorización para implementar una nueva etapa de contenido.

## Siguiente paso

Publicar los cinco documentos aprobados y comprobar que `main` local y remoto coinciden. Después, acordar la siguiente tarea de desarrollo y sus criterios usando [HOJA_RUTA.md](HOJA_RUTA.md); sus propuestas no son autorización de implementación. ChatGPT podrá usar los documentos publicados como base de planificación.
