# Hoja de ruta hacia la versión definitiva

Actualizada el **1 de noviembre de 2026**. Esta hoja ordena el trabajo; el estado comprobado y las evidencias están en [ESTADO_PROYECTO.md](ESTADO_PROYECTO.md). Una propuesta no autoriza cambios en código o recursos.

## Etapas

| Etapa | Estado | Resultado esperado y criterio de cierre |
|---|---|---|
| 1. Establecer el sistema compartido | Completada y aprobada por el usuario | AGENTS, ESTADO_PROYECTO y HOJA_RUTA creados; README y CAMBIOS actualizados, sin duplicar los detalles técnicos y de zonas |
| 2. Publicar la base documental | Autorizada por el usuario | Publicar únicamente los cinco archivos aprobados y comprobar que la referencia local coincide con GitHub; el commit documental identifica la base compartida |
| 3. Validar la base jugable actual | Propuesta | Acordar una tarea de comprobación técnica y una prueba manual de las zonas renovadas, especialmente la nueva cuarta; registrar incidencias y valoración del usuario |
| 4. Definir el alcance de la edición definitiva | Propuesta | Aprobar qué conservar, mejorar o ampliar en recorrido, combate, progresión, narrativa, presentación y final; fijar prioridades y criterios de aceptación |
| 5. Implementar por entregas pequeñas | Propuesta; depende de 3 y 4 | Para cada tarea aprobada: implementar, verificar, revisar con el usuario y registrar el resultado; resolver incidencias antes de ampliar alcance |
| 6. Preparar la entrega definitiva | Propuesta; depende del alcance aprobado | Validar recorrido completo, guardados y distribución; revisar experiencia manual, documentación y paquete final; publicar solo con autorización |

## Decisiones que faltan

- Qué significa «versión definitiva»: alcance de contenido y mejoras imprescindibles frente a opcionales.
- Qué valoración hace el usuario de la cuarta zona y de la continuidad de las tres primeras.
- Qué tarea concreta debe seguir y cómo se reconocerá que está terminada.

La separación de responsabilidades de entidades y la extracción de definiciones de zonas mencionadas en [ARQUITECTURA.md](ARQUITECTURA.md) son recomendaciones técnicas. Se evaluarán al definir una tarea que las necesite; no constituyen una refactorización ya autorizada. Tampoco se da por aprobado renovar zonas posteriores, añadir jefes o cambiar el desenlace.

## Ciclo de cada entrega

1. ChatGPT y el usuario concretan objetivo, límites y criterios. Registrar en el repositorio las decisiones aprobadas antes de depender de ellas en otra sesión.
2. Codex revisa la base y aplica la tarea autorizada siguiendo [AGENTS.md](AGENTS.md).
3. Codex registra cambios, verificaciones realizadas, límites y pendientes. El usuario valora los aspectos creativos y jugables que correspondan.
4. Actualizar estado e historial; cambiar esta hoja solo cuando cambien prioridades o etapas. Un fallo o una propuesta no se registran como entrega aprobada.
5. Cuando el usuario autorice la publicación, subir los cambios aprobados y comunicar la referencia Git para continuar la planificación desde GitHub.

ChatGPT consulta la información publicada. Una conversación, un archivo local o una propuesta de esta hoja no equivalen por sí solos a un avance aprobado y publicado.
