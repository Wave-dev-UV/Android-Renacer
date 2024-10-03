# Plantilla pull-request.
Plantilla sugerida cada que se haga un pull-request, no es necesario que siga este formato especifico pero debe incluir los campos que aquí se mencionan.
Recuerde como asignar como reviewer a las otras 2 personas del equipo,  coloque la etiqueta correspondiente al tipo de pull-request (solución a un bug, funcionalidad nueva, etc.) y documentar
los componentes (indicando que reciben como parámetro) en caso de que le sean útiles a otro miembro del equipo.
## H.U.
**H.U. - <núm H.U.>:** Describa qué implementó. 
  
***Ejemplo:***  
  
**H.U. - 5:** Se terminó la ventana correspondiente a editar un recordatorio, se crearon los métodos necesarios dentro del viewModel para crear, borrar y actualizar notificaciones de un recordatorio (es necesario que en algún punto de la app. se pidan permisos para enviar notificaciones).

## Detalles.
En esta sección describa cómo utilizar los componentes implementados **solo** en caso de que otro miembro del equipo requiera usarlo.

  
***Ejemplo:***  
  
### ¿Cómo usar las notificaciones?
- **Crear notificaciones:** Llame al método ```crearNotificacion``` del viewModel, pase como parámetro el ```contexto``` de la app. ,```fecha``` (de tipo ```Date```), ```titulo```, ```lugar``` e ```intervalo``` (texto del select - ```String```, **ejemplo:** 15 mins).
- **Borrar notificaciones:** Llame al método ```borrarNotificacion``` del viewModel, pase como parámetro el ```contexto``` de la app. y el ```id``` de la notificación (recordatorio).

El ```id``` de las notificaciones se crea como un número aleatorio entre 1 y 1.000.000, **se recomienda** que en lugar de guardar el recordatorio de cada usuario en una misma colección, se cree una colección por usuario (teniendo como nombre el correo) y ahí se guarden los recordatorios (automáticamente se cree un documento en una colección de Firebase que no exista esta se crea).  

**Nota:** Las notificaciones están configuradas para sonar 20 segs. después de crear/editar el recordatorio con el propósito de hacer pruebas.
## Pruebas implementadas.
Esta sección es solo si se implementaron pruebas, de lo contrario omitalo. De la misma forma, si el PR es solo para implementar las pruebas solo mencione la H.U. y esta sección.  
Mencione el porcentaje de cobertura que tiene el viewmodel y los métodos sobre las que están implementadas.

## Componentes.
En esta sección, describa todo lo relacionado con los componentes (tanto de la interfaz como la lógica).  
### Componentes añadidos:
Mencione los componentes creados.  
  
***Ejemplo:***  
  
- NotificationAlarms.
- Fragment editar recordatorio.
- Mensaje emergente genérico (Dialogo).
- Funciones auxiliares (modulo utils).

### Componentes modificados.
Si hizo cambios en un componente ya existente, mencionelos.
  
***Ejemplo:***  
  
- **Toolbar:** De la rama para detallar una cita, se le cambió el nombre al archivo por conflictos al usarlo.
- **Modelo Recordatorio:** Se añadió un atributo para guardar el ID de notificación.

## Elementos pendientes.
Si hay elementos pendientes (como pruebas unitarias), mencionelos en esta sección.
  
***Ejemplo:***  
  
- Descomentar las acciones de navegación cuando se termine la ventana para detallar un recordatorio.
- En algún punto de la app. se deben pedir permisos para crear notificaciones.

## Permisos.
Si se añadieron o modificaron los permisos de la app, mencionelos.
  
***Ejemplo:***  
  
Se requieren permisos para: **crear alarmas**, **crear notificaciones** y **ver alarmas**.

## Capturas de pantalla (opcional).
En esta sección coloque las capturas de pantalla de la interfaz, solo si hizo cambios en la misma.  
  
***Ejemplo:***  
  
![admiti2](https://github.com/Criser2013/Miniproyecto-2-Apps.-m-viles-/assets/84862634/0c1c5f4d-53ee-45f5-adaa-0fe865efa093)
