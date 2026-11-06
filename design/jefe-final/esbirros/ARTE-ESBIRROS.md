# Aguijón y Baluarte · esbirros de God Fish

Paquete de arte para la fase 4. Los dos son calamares cíclopes sin boca, con el ojo verde de God Fish. Aguijón tiene manto estrecho verde petróleo y tridente de bronce. Baluarte tiene manto violeta más ancho y escudo con el pez verde. La parte central del cuerpo se acerca a la escala de los enemigos del juego. La celda deja margen para el equipo y los tentáculos.

[Vista de poses y efectos](preview/esbirros.png) · [Manifiesto](manifest.json)

## Integración visual

- Cada personaje tiene 32 cuadros, ocho poses por dirección. Las filas son down, left, right y up. Escala ×3 con vecino más cercano. Anclar en [16,16] sobre el centro lógico de la entidad. Separar las cajas de cuerpo y de impacto del rectángulo del atlas.
- Usar idle al detenerse, step-a y step-b al nadar y windup al fijar el ataque. Mostrar impact durante el golpe y recuperar con recover-a/recover-b. Defeat cierra el ojo y baja el cuerpo. Recortar y escalar una vez al cargar.
- Aguijón usa tres marcas en el suelo. Dibujar la fila spike del atlas de efectos en los mismos tres centros mundiales que resuelve el ataque. Mostrar los avisos antes del daño y la punta activa de cada centro de forma sucesiva. Las puntas crecen desde el suelo y conservan su orientación vertical en pantalla; la dirección está en la distribución de las marcas. Mantener quieto al enemigo al clavar y al recuperar.
- Baluarte tiene el escudo alzado durante la guardia y lo apoya de canto durante recuperación. Resolver el bloqueo frontal con la dirección fijada por su pose. Dibujar el arco de impacto en el frente real del golpe. La fila shield del atlas de efectos está orientada hacia arriba; rotarla en pasos de 90 grados para las otras direcciones. Mantener el arco, la caja y el desplazamiento sincronizados.
- Orden aprobado: Aguijón, Baluarte, Aguijón, Baluarte. Un esbirro activo por vez. Mantener cuatro sellos y generar las entidades en suelo libre contiguo, con espacio para salir del sello. Los cambios se limitan a las invocaciones de la fase 4. La segunda aparición de cada tipo cambia de flanco y conserva sus señales.
- Referencias iniciales para el balance: Aguijón 45 HP, defensa baja y daño base 7. Baluarte 55 HP, defensa moderada y daño base 9. Dejar una recuperación suficiente para que el jugador pueda castigar a cada uno. Mantener el avance de la fase ligado a la derrota del esbirro activo.
