# Menú marino animado — 31 de octubre de 2026

Se integra exactamente el GIF aprobado de la propuesta 02, sin regenerar ni modificar sus cuadros: 320 × 192, 64 cuadros y bucle de ocho segundos. El recurso se incluye dentro del juego; no requiere archivos externos ni conexión.

## Presentación

- Escalado a 960 × 576 mediante vecino más cercano: cada píxel del GIF ocupa exactamente 3 × 3 píxeles en la ventana normal.
- En pantallas de otra proporción, el menú se ajusta completo, centrado y con bandas negras si hacen falta. No se recorta ni se estira; el escalado final también evita suavizado. En tamaños no enteros el ancho físico de algunos píxeles puede variar un píxel.
- Se conservan el título, las fuentes, el pez naranja, NEW GAME, LOAD GAME, EXIT y el selector originales. Fondos oscuros translúcidos detrás del título y opciones, más contorno oscuro en estas, mantienen legibilidad durante la animación.
- Se conserva el comportamiento de los controles y del resto del juego, incluida la zona renovada y la corrección de audio.

## Funcionamiento

El GIF se decodifica una sola vez antes de entrar en el bucle del juego. Sus cuadros se comparten mediante una caché. La actualización respeta sus duraciones de 120 y 130 milisegundos y solo avanza mientras se muestra el menú. Dibujar no abre archivos, no decodifica imágenes y no cambia el cuadro activo.

## Comprobaciones

La suite TitleRegression compara todos los píxeles del fondo a escala 3× con el GIF, verifica que no pinte fuera de 960 × 576, que el dibujo conserve la configuración gráfica del llamante, la duración exacta del bucle, la pausa fuera del menú, el encuadre en 1280 × 720 y los controles para seleccionar e iniciar partida. También genera imágenes del menú en cuatro momentos para revisión visual.

Se ejecutan las suites anteriores de combate, recursos, audio, funcionamiento, historia, primera zona y guardado. El paquete se verifica contra el recurso aprobado y no incluye partidas de pruebas. Esta mejora no cambia el formato de guardado: se puede copiar save-v1.dat desde la entrega de la primera zona a la nueva carpeta.
