# Corrección de pausas de audio — 2026-10-30

Se reprodujeron pausas de unos dos segundos en la versión anterior y se capturó la pila: Sound.setFile → release → DirectClip.implClose → Thread.join. El cierre esperaba al hilo del sonido mientras bloqueaba al juego. La apertura inicial de un sonido también demoró531ms en una prueba.

Corrección: operaciones del dispositivo en un hilo propio; solicitudes sin espera desde el juego; una copia reutilizable por recurso de audio; cierres solamente al finalizar; como máximo ocho efectos simultáneos y una música. Las solicitudes repetidas se agrupan y los efectos pendientes de más de250ms se descartan para evitar una ráfaga tardía. Al detener/cerrar se cancelan solicitudes pendientes. No cambian reglas de combate, diálogos, niveles, recursos ni formato de guardado.

La caché puede conservar hasta28 clips por controlador (catálogo finito; solo abre los usados), en vez de cerrar continuamente al reemplazar las ocho voces. Esto puede usar más memoria de audio tras visitar varias zonas. Repetir el mismo efecto reutiliza/reinicia su clip en lugar de superponer copias del mismo archivo. Los WAV originales permanecen intactos.

Prueba real de600 solicitudes con reproducción a volumen cero: antes29 pausas superiores a100ms, máximo2026ms; después0 pausas, máximo6ms en la llamada del juego. No es una garantía de latencia de todos los dispositivos; mide que el juego ya no espera al dispositivo. Una prueba controlada bloquea el dispositivo más de2segundos y valida respuestas rápidas, descarte de solicitudes canceladas, reutilización, límite de reproducción y cierre.55 comprobaciones de audio pasan.

Regresión completa: combate37, ejecución16, historia42, guardado31; recursos14826 aserciones. Las pruebas anteriores de apertura silenciosa se sustituyeron por reproducción real con ganancia cero y cierre verificado. Se conserva la comprobación anterior del recorrido automatizado hasta créditos. Esta actualización no se ha probado mediante una nueva partida manual completa.

## Instalar conservando tu partida

Cierra el juego. Guarda una copia del TheLittleFish.jar anterior y sustitúyelo por el TheLittleFish.jar de esta actualización. Conserva save-v1.dat, save-v1.dat.bak y config.txt en tu carpeta habitual. También puedes extraer la actualización en otra carpeta y copiar allí tu save-v1.dat. El formato de partida no cambió. No copies las partidas de prueba de build/.
