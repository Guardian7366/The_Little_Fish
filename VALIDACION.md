# Validación actual

La entrega de la primera zona renovada se documenta en [VALIDACION-PRIMERA-ZONA.md](VALIDACION-PRIMERA-ZONA.md). Los resultados siguientes son históricos.

# Actualización de audio

Consulta [CORRECCION-AUDIO.md](CORRECCION-AUDIO.md) para los resultados actuales y el cambio de caché. El informe siguiente corresponde a la primera entrega; las comprobaciones antiguas de audio se reemplazaron.

# Validación de la base reconstruida

Compilación y pruebas realizadas en Windows con JDK 25, generando clases compatibles con Java 17 mediante `--release 17`. Las pruebas no necesitan bibliotecas externas.

## Resultados

| Suite | Resultado | Cobertura principal |
|---|---:|---|
| `CombatRegression` | 37 pasan, 0 fallan | Guardia, parry, daño, proyectiles, agua, niveles, equipo, inventario y temporizadores |
| `main.RuntimeRegression` | 16 pasan, 0 fallan | Entrada de teclado, cambio de mapa, reinicio, pausa, carga fallida y cadencia del bucle |
| `main.StoryRegression` | 42 pasan, 0 fallan | 22 portales, 9 puntos de descanso, NPC, cofres, herramientas, rocas, comercio y secuencia del jefe hasta créditos |
| `data.SaveRegression` | 31 pasan | Guardado completo, respaldo, validación, rechazo de corrupción e importación desde el JAR original |
| `main.ResourceRegression` | 14.942 comprobaciones pasan | Píxeles, caché, terreno, rutas y límites de audio |

Las 14.942 comprobaciones de recursos son aserciones internas, **no 14.942 partidas ni escenarios independientes**. La suite de guardado incluye el caso legado cuando se proporciona `-OriginalJar` al script de compilación.

Antes de aplicar las correcciones, la reconstrucción de referencia y el JAR original dieron exactamente los mismos resultados en 32 contratos de combate, equipo, inventario y entrada: 24 pasaron y 8 reprodujeron defectos. Esta comparación respalda la recuperación de esos comportamientos; no demuestra equivalencia universal de todo el juego.

## Recursos y rendimiento

- Los 291 PNG escalados coinciden píxel a píxel con el procedimiento original.
- El terreno dibujado coincide con una referencia sin descarte de casillas en 42 posiciones de mapa y cámara.
- 280 consultas sobre los siete mapas coinciden con una búsqueda en anchura en alcance y longitud mínima del camino.
- Las vistas de mapa reservan cero bytes de píxeles al inicio, 2.560.000 bytes tras abrir un mapa y 17.920.000 bytes al abrir los siete: **17,09 MiB**. El original reservaba 230.400.000 bytes, **219,73 MiB**. Esto mide únicamente esas imágenes; no representa la memoria total del proceso.
- La prueba de audio abrió recursos sin reproducirlos: ocho voces de efectos como máximo y cero líneas pendientes después del cierre.
- La prueba del bucle con actualización y dibujo vacíos produjo 120 cuadros en unos 1.984 ms. El contador de CPU del hilo registró 0,00 ms en esa ejecución; su resolución no permite interpretar el resultado como consumo real nulo. La prueba confirma que el bucle espera entre cuadros. No es una medición de rendimiento del juego completo.

El audio WAV constituía aproximadamente el 99,7 % del contenido comprimido original. Optimizar memoria y CPU no reduce ese tamaño en disco; esta entrega conserva esos archivos.

## Alcance y límites

Los escenarios de historia utilizan posiciones y acciones preparadas y llaman a la lógica real del juego. Comprueban los enlaces entre sistemas y la secuencia hasta los créditos, pero no sustituyen una partida manual completa ni una evaluación de dificultad, ritmo, sonido o compatibilidad con todos los equipos.

Las pruebas de corrupción generan errores esperados en consola; el resultado correcto es que se rechace el archivo y se conserve el mundo activo. Los archivos de prueba se crean bajo `build/` y no forman parte de la distribución del juego.

Para repetir la comprobación completa:

```powershell
./build.ps1 -Test -OriginalJar "C:\ruta\MyGame.jar"
```

El original se usa como referencia y para generar una partida antigua de prueba. No se modifica.

## Comprobación visual del ejecutable entregado

El 29 de octubre de 2026 se inició `Jugar.cmd` desde la carpeta de entrega. Se comprobaron la pantalla de título, partida nueva, primera zona y HUD, apertura/cierre del mapa, inventario y estadísticas, pausa con P y diálogo de descanso. Se guardó en el primer punto esmeralda, se cerró el proceso y se volvió a abrir para cargar correctamente esa partida. Ambos cierres finalizaron sin errores en consola.

Esta revisión cubre el arranque y los flujos indicados. La secuencia completa hasta los créditos se verificó mediante escenarios automatizados; no se realizó una partida manual completa. No se evaluó subjetivamente toda la banda sonora.
