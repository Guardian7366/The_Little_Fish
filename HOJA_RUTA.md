# Hoja de ruta hacia la versión definitiva

Actualizada el **4 de noviembre de 2026**. Esta hoja ordena el trabajo; el estado comprobado y las evidencias están en [ESTADO_PROYECTO.md](ESTADO_PROYECTO.md). Una propuesta no autoriza cambios en código o recursos.

## Etapas

| Etapa | Estado | Resultado esperado y criterio de cierre |
|---|---|---|
| 1. Establecer el sistema compartido | Completada y aprobada por el usuario | AGENTS, ESTADO_PROYECTO y HOJA_RUTA creados; README y CAMBIOS actualizados, sin duplicar los detalles técnicos y de zonas |
| 2. Publicar la base documental | Completada: bc0c1c9 | Publicar únicamente los cinco archivos aprobados y comprobar que la referencia local coincide con GitHub; el commit documental identifica la base compartida |
| 3. Validar la base jugable actual | Cuarta aprobada; Sharky, recorrido general y ambientación de quinta aprobados por el usuario | Correcciones de Iron y puertas aprobadas el 4 de noviembre de 2026. Pasaron las 17 suites |
| 4. Definir el alcance de la edición definitiva | Propuesta | Aprobar qué conservar, mejorar o ampliar en recorrido, combate, progresión, narrativa, presentación y final; fijar prioridades y criterios de aceptación |
| 5. Implementar por entregas pequeñas | Quinta a octava y tres primeros refugios completados y aprobados | Acordar el alcance de la próxima entrega |
| 6. Preparar la entrega definitiva | Propuesta; depende del alcance aprobado | Validar recorrido completo, guardados y distribución; revisar experiencia manual, documentación y paquete final; publicar solo con autorización |

## Decisiones que faltan

- Qué significa «versión definitiva»: alcance de contenido y mejoras imprescindibles frente a opcionales.
- La cuarta zona fue jugada y aprobada por el usuario; su ampliación queda cerrada.
- Acordar la siguiente tarea de contenido tras la aprobación de la quinta.

La separación de responsabilidades de entidades y la extracción de definiciones de zonas mencionadas en [ARQUITECTURA.md](ARQUITECTURA.md) son recomendaciones técnicas. Se evaluarán al definir una tarea que las necesite; no constituyen una refactorización ya autorizada. Tampoco se da por aprobado renovar zonas posteriores a la quinta, añadir jefes o cambiar el desenlace.

## Ciclo de cada entrega

1. ChatGPT y el usuario concretan objetivo, límites y criterios. Registrar en el repositorio las decisiones aprobadas antes de depender de ellas en otra sesión.
2. Codex revisa la base y aplica la tarea autorizada siguiendo [AGENTS.md](AGENTS.md).
3. Codex registra cambios, verificaciones realizadas, límites y pendientes. El usuario valora los aspectos creativos y jugables que correspondan.
4. Actualizar estado e historial; cambiar esta hoja solo cuando cambien prioridades o etapas. Un fallo o una propuesta no se registran como entrega aprobada.
5. Cuando el usuario autorice la publicación, subir los cambios aprobados y comunicar la referencia Git para continuar la planificación desde GitHub.

ChatGPT consulta la información publicada. Una conversación, un archivo local o una propuesta de esta hoja no equivalen por sí solos a un avance aprobado y publicado.


## Siguiente entrega concreta: Sharky y quinta zona

El usuario aprobó el comportamiento de Sharky, recorrido general y ambientación tras probar la primera implementación local. Desde esa misma base se corrigieron Iron obligatorio con dos bloques nuevos, las puertas originales 086 y la migración a revisión 2. Las 17 suites pasan; capturas y resultados reales en [QUINTA-ZONA.md](QUINTA-ZONA.md). El usuario aprobó las correcciones y autorizó el commit y la publicación el 4 de noviembre de 2026. El JAR de prueba se genera en `build/`.

## Próxima expansión

Nueva sexta zona de 50 × 50 con Fishtella, Sharky y un paso obligatorio de Iron. La implementación aislada y la integración pasaron las 20 suites. El usuario probó la zona y Fishtella, confirmó el comportamiento esperado y aprobó la entrega para publicación. [Informe de la sexta zona](SEXTA-ZONA.md)

## Séptima zona

El templo del mapa interno 4 ya incluye a Planta, Secret, Fishtella y cuatro galerías. Pasaron las pruebas de colisión, guardado y acceso al jefe. El usuario probó y aprobó la zona y los dos enemigos rediseñados el 4 de noviembre de 2026 y autorizó la publicación. [Informe de la séptima zona](SEPTIMA-ZONA.md)

## Extensión del templo antes del jefe

El mapa 9 conecta el templo aprobado con el jefe. El usuario probó las galerías, confirmó que cumplen los requisitos y autorizó la publicación el 4 de noviembre de 2026. [Informe de la octava zona](OCTAVA-ZONA.md)

## Primeros tres refugios de D

Chicken, Flower y Spiky están integrados en el mapa 5 con sus retos y recompensas originales. El usuario confirmó tras jugar que las tres salas funcionan como esperaba y autorizó su publicación el 4 de noviembre de 2026. El siguiente alcance queda por acordar. [Informe](SALAS-EXTRAS-1-3.md)
