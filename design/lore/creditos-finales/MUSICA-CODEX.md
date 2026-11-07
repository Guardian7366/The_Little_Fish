# La luz que queda · instrumental de los créditos

El usuario escuchó y aprobó esta composición el 6 de noviembre de 2026 y autorizó publicarla. Archivo fuente aprobado: [tema-instrumental.wav](tema-instrumental.wav). Base de integración revisada: `253a54b`, créditos y despedida aprobados.

## Audio y recorrido musical

WAV PCM de 16 bits, estéreo, 44 100 Hz, duración exacta de 120 segundos. El archivo ocupa 21 168 044 bytes y su SHA-256 es `AAFB2585F5E3168F1FC0D41D85B27DFFEFD8933778A489AB1E41467BAFFDF403`.

Composición original instrumental. Empieza con teclado y frases espaciadas, añade bajo y arpegios, crece con una melodía de timbre retro y cuerdas, alcanza su mayor intensidad aproximadamente entre 84 y 96 segundos y se recoge hacia un cierre tranquilo. La última muestra audible cae aproximadamente en 119,79 s. Los últimos instantes permiten revelar la despedida después de que el sonido se extinga.

## Integración prevista por la base publicada

- `build.ps1` ya copia este archivo desde `design/lore/creditos-finales/tema-instrumental.wav` al recurso empaquetado `/credits/tema-instrumental.wav`. Mantener esta fuente única y comprobar su presencia en el JAR.
- `CutsceneManager.startCreditsMusic()` ya busca ese recurso, respeta el volumen de música y pide una reproducción sin bucle. Se llama junto con `credits.begin()`, después del fundido de la foto. Ese instante corresponde a The Little Fish y al cero del reloj de créditos.
- La letra, las 14 tomas y sus dibujos conservan la cronología publicada. La pista recorre los 120 s completos. Al pasar a `credits.farewell()`, `stopCreditsMusic()` cierra el clip. La canción ya tiene su cierre musical, así que el corte de seguridad debe ocurrir después de su caída natural.
- Al volver al menú o reiniciar, cerrar el clip y evitar música duplicada. El siguiente recorrido completo debe iniciar la pista desde cero. Probar el volumen silenciado y un volumen intermedio. La playa mantiene su oleaje y lo desvanece antes de los créditos según el flujo aprobado.
- Realizar solo el ajuste técnico que haga falta si la prueba descubre un problema. La ruta y los puntos de inicio y fin ya existen en esta base.

## Validación de la entrega jugable

Ejecutar `build.ps1 -Test` y comprobar que el JAR contiene la pista. Contrastar el audio empaquetado con el SHA-256 indicado y verificar que Java puede abrir su formato. Probar el recorrido real con sonido desde la foto, incluyendo el título, las 14 tomas, el fundido y la despedida. Escuchar que la pista arranca una vez y se apaga con su final natural antes de la última pantalla. Repetir la entrada al menú y el silencio de música. Los dispositivos simulados pueden comprobar el flujo; documentar aparte la escucha con salida de audio real. Entregar el JAR y un video con audio de los créditos completos para la revisión final del usuario.

En esta entrega de recursos se comprobó la copia exacta y la cabecera WAV. La sincronización audible dentro del juego queda para la integración.
