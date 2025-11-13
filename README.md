# EmpresaFotos - App de Galería de Eventos

Esta es una aplicación móvil para Android diseñada para la gestión y visualización de galerías de fotos de eventos. Permite a los organizadores crear eventos, subir fotos y generar códigos QR únicos. Los asistentes (clientes) pueden escanear estos códigos QR para acceder y ver las fotos del evento correspondiente después de iniciar sesión.

## ✨ Características

La aplicación cuenta con dos roles de usuario principales con flujos de trabajo diferenciados:

### Flujo del Cliente

- **Autenticación de Usuario**: Registro e inicio de sesión son **requeridos** para acceder a cualquier galería.
- **Escáner de Código QR**: Utiliza la cámara del dispositivo para escanear códigos QR de eventos.
- **Visualización de Galería**: Muestra todas las fotos asociadas al evento escaneado en una cuadrícula.
- **Vista de Foto en Detalle**: Permite ver una foto en pantalla completa.
- **Enviar Feedback**: Opción para que los usuarios envíen sus comentarios sobre un evento.

### Flujo del Organizador

- **Autenticación de Usuario**: Registro e inicio de sesión con un rol específico de "organizer".
- **Dashboard de Eventos**: Pantalla principal para ver y gestionar los eventos creados **por el propio organizador**.
- **Creación de Eventos**: Permite crear un nuevo evento, que queda asociado al organizador y genera un ID único.
- **Gestión de Galería por Evento**: Para cada evento propio, el organizador puede subir múltiples fotos.
- **Generación de Código QR**: Genera y muestra un código QR único para cada evento, listo para ser compartido.
- **Revisión de Feedback**: Pantalla para visualizar los comentarios enviados por los clientes **a los eventos del organizador**.

## 💡 Casos de Uso

### Ejemplo: Fotógrafo en un Evento Deportivo

Este es un caso de uso práctico que ilustra cómo un fotógrafo puede gestionar la entrega de fotos a múltiples familias durante un evento, como una competición deportiva.

**El objetivo es garantizar la privacidad**, asegurando que cada familia solo pueda acceder a las fotos de sus propios hijos.

**Flujo de Trabajo Recomendado:**

El enfoque más seguro es tratar a **cada familia como un "evento" independiente** dentro de la aplicación.

1.  **Transferencia de Fotos**: El fotógrafo transfiere las fotos desde su cámara profesional a su teléfono móvil. Para un flujo más ágil, se recomienda organizar las fotos en carpetas separadas por familia antes de subirlas.
2.  **Crear un Evento por Familia**: En el Dashboard de la app, el fotógrafo crea un nuevo evento nombrandolo de forma clara (ej: "Torneo de Judo - Familia Pérez"). La app asigna automáticamente el `organizerId` al evento.
3.  **Subir las Fotos**: Inmediatamente después de crear el evento, la app navega a la pantalla de gestión de galería, desde donde el fotógrafo sube las fotos correspondientes a esa familia.
4.  **Compartir el Código QR**: La aplicación genera un código QR único para el evento recién creado. El fotógrafo puede mostrarlo directamente en la pantalla de su teléfono a la familia para que lo escanee, o enviarles una captura de pantalla.
5.  **Repetir el Proceso**: El fotógrafo repite estos pasos para cada familia.

**Dependencias a Considerar:**

La viabilidad de este proceso en tiempo real depende de dos factores clave:

-   **Velocidad de Transferencia**: La rapidez con la que las imágenes pueden ser transferidas de la cámara al teléfono (usando cables, Wi-Fi, o lectores de tarjetas).
-   **Capacidad de Almacenamiento**: El espacio disponible en el teléfono para almacenar temporalmente las fotos antes de subirlas a Firebase. Una vez subidas, las fotos se pueden eliminar del dispositivo para liberar espacio.

Este método, aunque requiere un paso por cada familia, es la forma más segura de proteger la privacidad de los clientes y aprovechar al máximo la funcionalidad de la aplicación.

## 📈 Estrategia de Negocio y Monetización

Para un fotógrafo que utiliza la aplicación para vender fotos a los clientes (como en el caso del evento deportivo), la elección del método de pago es una decisión estratégica clave. A continuación se presenta un análisis de las opciones y una recomendación.

### Opción 1: Pagos Offline (Efectivo o Transferencia Bancaria)

Es la solución más sencilla de implementar, ya que no requiere cambios en la app.

-   **Ventajas**: Cero comisiones por transacción. El fotógrafo recibe el 100% del pago.
-   **Desventajas**: Mayor fricción para el cliente, gestión de pagos completamente manual para el fotógrafo y posibles problemas de confianza (pagar antes de ver el producto final).

### Opción 2: Pagos Integrados en la App (Stripe, Google Pay)

Ofrece la mejor experiencia de usuario, pero tiene un coste significativo.

-   **Ventajas**: Flujo de pago profesional y sin fricciones, gestión automatizada y mayor probabilidad de conversión de venta.
-   **Desventajas**: Alta complejidad técnica y, lo más importante, las **altas comisiones de Google Play (15-30%)** por la venta de bienes digitales, además de las comisiones del propio proveedor de pago (ej. Stripe).

### Recomendación: Modelo Híbrido (Futura Funcionalidad)

Considerando que las comisiones de Google Play pueden reducir drásticamente los márgenes de beneficio, la estrategia más recomendable a largo plazo es implementar un **modelo híbrido** que combine la comodidad de la app con los pagos offline.

**Flujo del Modelo Híbrido:**

1.  **Vista Previa con Marca de Agua**: El cliente escanea el QR y accede a una galería de sus fotos con una marca de agua.
2.  **Instrucciones de Pago en la App**: Junto a las fotos, se muestran el precio y las instrucciones para realizar el pago por transferencia bancaria.
3.  **Botón de "He Pagado"**: El cliente realiza el pago y pulsa un botón en la app para notificar al fotógrafo.
4.  **Verificación Manual**: El fotógrafo recibe una notificación en su dashboard, verifica la recepción del pago en su cuenta bancaria, y pulsa un botón de "Confirmar Pago" en la app.
5.  **Liberación Automática**: Tras la confirmación, la app elimina automáticamente la marca de agua de las fotos para ese cliente.

Este enfoque **elimina las comisiones**, **automatiza la gestión** de la entrega de las fotos y mantiene un **flujo de trabajo profesional** dentro de la aplicación.

## 📝 Revisión de Flujo y Mejoras de Seguridad (Noviembre 2023)

Recientemente, se ha realizado una revisión completa del flujo de la aplicación para mejorar la seguridad y la privacidad de los datos. Los cambios más importantes son:

1.  **Eliminación del Acceso Anónimo**: Se ha eliminado la opción de "Acceso sin inicio de sesión". Ahora es **obligatorio** que todos los usuarios (tanto clientes como organizadores) inicien sesión para poder acceder a las galerías de eventos. Esto previene el acceso no autorizado a fotos privadas.

2.  **Aislamiento de Datos para Organizadores**: Se ha implementado una arquitectura de datos más robusta para asegurar que cada organizador solo tenga acceso a su propia información:
    *   Al crear un evento, este se asocia automáticamente con el `organizerId` del usuario que lo crea.
    *   El **Dashboard** y la pantalla de **Revisión de Feedback** han sido actualizados para filtrar los datos y mostrar únicamente los eventos y comentarios que pertenecen al organizador que ha iniciado sesión.

### Nota Importante: Migración de Eventos Antiguos

Los eventos que fueron creados **antes** de esta actualización no tienen un `organizerId` asociado, por lo que no serán visibles para ningún organizador.

Para solucionar esto, debes actualizar manualmente tus eventos existentes en la base de datos de Firestore:

1.  **Obtén el UID de tu usuario organizador** desde la sección **Authentication** en la Consola de Firebase.
2.  Ve a tu **Cloud Firestore Database** y navega a la colección `events`.
3.  Para cada documento de evento que desees asociar a tu cuenta, **añade un nuevo campo**:
    *   **Field name**: `organizerId`
    *   **Type**: `string`
    *   **Value**: El UID de tu usuario organizador.

Todos los eventos nuevos creados desde la app ya incluirán este campo automáticamente.

## 🛠️ Stack Tecnológico

- **Lenguaje**: Kotlin
- **Interfaz de Usuario**: Jetpack Compose
- **Navegación**: Navigation Compose
- **Backend y Base de Datos**: Firebase
  - **Firebase Authentication**: Para la gestión de usuarios.
  - **Cloud Firestore**: Para almacenar la información de los eventos, los roles de usuario y las URLs de las fotos.
  - **Cloud Storage for Firebase**: Para el almacenamiento de los archivos de imagen.
- **Carga de Imágenes**: Coil
- **Generación de QR**: ZXing (via `com.google.zxing`)

## 🚀 Configuración del Proyecto

Para poder ejecutar la aplicación correctamente en tu entorno local, es crucial configurar el backend de Firebase.

### 1. Configurar un Proyecto en Firebase

1.  Ve a la [Consola de Firebase](https://console.firebase.google.com/) y crea un nuevo proyecto.
2.  Añade una nueva aplicación de Android a tu proyecto de Firebase. Sigue los pasos para registrar el nombre del paquete (`com.example.empresafotos`).
3.  Descarga el fichero `google-services.json` y colócalo en el directorio `app/` de tu proyecto en Android Studio.

### 2. Activar los Servicios de Firebase

Dentro de tu proyecto en la Consola de Firebase, debes activar y configurar los siguientes servicios:

- **Authentication**:
  - Ve a la sección "Authentication" -> "Sign-in method".
  - Habilita el proveedor de **"Correo electrónico/Contraseña"**.

- **Cloud Firestore**:
  - Ve a la sección "Firestore Database".
  - Haz clic en "Crear base de datos".
  - Inicia en **modo de prueba** para el desarrollo. Esto facilitará las operaciones de lectura/escritura sin configurar reglas de seguridad complejas al principio.

- **Cloud Storage**:
  - Ve a la sección "Storage".
  - Haz clic en "Comenzar".
  - Inicia en **modo de prueba**. Esto es fundamental para permitir que la app pueda subir las fotos.

### 3. Crear un Usuario Organizador

Por defecto, cualquier usuario que se registre a través de la app tendrá el rol de "client". Para acceder a las funcionalidades de organizador, sigue estos pasos:

1.  Regístrate en la app con un correo y contraseña nuevos.
2.  Ve a tu base de datos de **Cloud Firestore** en la Consola de Firebase.
3.  Busca la colección `users`.
4.  Encuentra el documento que corresponde al `uid` del usuario que acabas de registrar.
5.  Edita el campo `role` y cambia su valor de `"client"` a `"organizer"`.

Una vez completados estos pasos, al iniciar sesión con esa cuenta, la aplicación te redirigirá automáticamente al dashboard de organizador.
