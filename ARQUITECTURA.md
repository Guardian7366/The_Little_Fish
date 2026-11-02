# Organización del proyecto

El juego sigue utilizando Java Swing y Java2D. La reconstrucción inicial compilable quedó registrada como referencia en el commit local `bb5676a`; las correcciones se desarrollaron a partir de ella. CFR 0.152 recuperó los fuentes desde el JAR proporcionado. Los recursos gráficos, sonoros y los mapas se conservaron como archivos originales.

## Carpetas

| Ruta | Función |
|---|---|
| `src/main/java` | Código del juego |
| `src/main/resources` | Sprites, sonido, fuentes y mapas |
| `src/test/java` | Pruebas ejecutables sin dependencias externas |
| `build.ps1` / `Compilar.cmd` | Compilación, pruebas y generación del JAR |
| `build` | Clases, archivos de prueba y ejecutable generado; no es código fuente |

## Responsabilidades principales

| Componente | Responsabilidad actual |
|---|---|
| `main.Main` | Crea la ventana en el hilo de Swing y conecta el cierre con la liberación de recursos |
| `main.GamePanel` | Coordina el mundo y la cadencia de actualización; solicita el repintado |
| `main.KeyHandler` | Recibe entradas y las entrega a la simulación mediante una cola cuando el juego está en marcha |
| `main.GameFlowController` | Avanza diálogos, comercio, opciones, sueño y cambios de zona una vez por actualización |
| `main.UI` | Dibuja la interfaz sin ejecutar compras, transiciones ni avance de diálogos |
| `main.CutsceneManager` | Mantiene las secuencias originales y separa su avance de su dibujo |
| `entity.Player`, `Entity`, `Projectile` | Movimiento, combate, equipo y estados de entidades; los temporizadores visuales avanzan durante la actualización |
| `main.SpriteCache` | Comparte imágenes por ruta y dimensiones; sus imágenes deben tratarse como inmutables |
| `tile.TileManager` | Carga y valida los mapas, conserva el terreno y dibuja las casillas visibles |
| `tile.Map` | Genera y conserva vistas de mapa de 800 × 800 cuando se solicitan; invalida la vista si cambia el terreno |
| `ai.PathFinder` | Busca caminos con costes acumulados y una cola de prioridad |
| `main.Sound` | Posee y reutiliza las voces de audio, con límites y cierre explícito |
| `data.SaveLoad` | Captura, valida, escribe y aplica partidas; importa el formato anterior |
| `data.SaveSnapshot` | Datos del formato versionado, separados de imágenes y objetos vivos |
| `main.EntityGenerator`, `data.WorldEntityRegistry` | Reconstruyen objetos y entidades mediante identificadores conocidos |
| `main.AssetSetter`, `main.EventHandler` | Conservan las colocaciones, portales y eventos originales |

## Flujo de una actualización

Se procesan las entradas pendientes, se actualiza el mundo cuando corresponde y avanzan los estados de interfaz y cinemáticas. Después se dibuja el cuadro. El dibujo no debe cambiar progreso, inventario, vida ni fase de una escena. La pausa congela los temporizadores de juego.

Un cambio de mapa descarta los proyectiles y partículas de la zona anterior. Conserva el progreso de los rompecabezas y mantiene la reaparición original de enemigos comunes. Una partida nueva restablece el mundo; reintentar conserva su progreso y restaura al jugador en el inicio.

## Contrato de guardado

`save-v1.dat` tiene versión explícita y datos de jugador, inventario y mundo. Se reconstruyen todas las entidades y se validan posiciones, equipo, identificadores y vínculos antes de publicar la partida cargada. La escritura temporal y el respaldo protegen el último guardado válido. `save.dat` solo se lee durante la importación, con la identidad serializada original de `DataStorage`.

Agregar un tipo de objeto requiere registrarlo; agregar una nueva entidad persistente requiere actualizar el registro del mundo y sus pruebas. Los cambios incompatibles de formato necesitan una versión y una migración deliberadas. No deben añadirse imágenes, audio ni referencias vivas al archivo de partida.

## Límites que siguen presentes

`GamePanel` aún expone estado compartido y `Entity` sigue siendo una clase amplia. Las capacidades de mapas y entidades siguen siendo fijas. Colocaciones, diálogos y eventos continúan mayoritariamente en código; las cinemáticas siguen dedicadas al jefe y desenlace originales. Estas decisiones redujeron cambios simultáneos durante la estabilización, pero no son la arquitectura final para una expansión grande.

Antes de añadir más contenido conviene extraer definiciones de zonas y encuentros, dividir responsabilidades de entidad y ampliar el modelo de progreso. Cada paso debe conservar los contratos de las mecánicas existentes y añadir pruebas para el contenido nuevo.
