# Guion de la presentación (≈ 9 min 50 s)

Versión editable en línea: https://claude.ai/artifact/EeC8T2xHnFDmCQBS8BqMwW

## Diapositiva 1 · cover

(30 s) Buenos días. Somos el equipo de LOGISTPULSE. En este deber fundamentamos formalmente la operación de la empresa: modelamos sus procesos como autómatas, buscamos rutas y reservas con algoritmos probados, y clasificamos diez problemas del simulador según lo que cuesta resolverlos. Cerramos con lo que no se puede resolver en absoluto.

## Diapositiva 2 · enfoque

(45 s) El trabajo responde tres preguntas. Primero, qué secuencias de eventos son legales en los procesos: eso es un autómata. Segundo, cuál es la mejor decisión operativa: rutas, reservas y reposición, con algoritmos cuyo costo y terminación demostramos. Tercero, cuánto cuesta resolver cada problema que llega por correo: ahí entran P, NP y NP-completo, y al final, la decidibilidad.

## Diapositiva 3 · automatas

(75 s) Modelamos los tres procesos como AFD con la quíntupla Q, Sigma, delta, q cero, F. Cada evento es una letra, para que JFLAP lo trate como símbolo. La función de transición es parcial: lo que no está definido va a un sumidero y se rechaza, que es justo como funciona JFLAP. Los estados finales son los cierres legítimos del proceso, exitosos o no. En la tabla, una secuencia válida y una inválida por proceso: no se puede cancelar un pedido entregado, consumir una reserva sin confirmar, ni enviar una orden rechazada. Agregamos un lazo de recepciones parciales en la compra. Las 26 secuencias dieron lo esperado en el simulador Java, y un validador que lee directamente los archivos .jff confirma las 24 cadenas de prueba. [Mostrar aquí la captura de JFLAP Multiple Run.]

## Diapositiva 4 · grafo

(70 s) La planta es un grafo no dirigido con siete zonas y once pasillos con su longitud en metros. Como los pesos son no negativos, Dijkstra es exacto. La ruta mínima de Recepción a Despacho mide 11 metros, y encontramos algo que la figura del enunciado no muestra: hay dos rutas óptimas empatadas. Por eso definimos un desempate reproducible, menos tramos primero. Notar que BFS no sirve aquí: minimiza tramos y daría una ruta de 12 metros. Con montículo binario, el costo es O de V más E por log V. También probamos bloqueos: con el pasillo A cerrado, la ruta sube a 12 metros.

## Diapositiva 5 · inventario

(70 s) Para FEFO, filtramos los lotes elegibles, los ordenamos con merge sort por vencimiento y desempatamos por código, para que el resultado sea reproducible. Un lote que vence hoy ya no se despacha. El costo es O de L log L. Si no alcanza el stock, la política es explícita: reservar parcial o rechazar todo. Para la reposición aplicamos la fórmula del enunciado: 18 más 10 menos 5 da 23, que está bajo el mínimo de 30, así que se sugieren 27. La clave para no duplicar sugerencias es que en orden incluye las órdenes de compra abiertas del autómata: al crear la orden, la posición sube. Es O de P.

## Diapositiva 6 · correos

(80 s) Clasificamos los diez correos del simulador según la evidencia que sostiene cada etiqueta. Tres están en P porque tenemos un algoritmo polinómico: alcanzabilidad, ruta mínima y flujo máximo. Cuatro solo afirmamos que están en NP, con certificado y verificador: isomorfismo, factor acotado, logaritmo discreto y simetría. No decimos que sean NP-completos; de hecho, se cree que no lo son. Y tres son NP-completos, con una reducción conocida: SAT por Cook-Levin, TSP desde ciclo hamiltoniano y vertex cover desde clique. Cada uno tiene su programa en Python con verificador y pruebas.

## Diapositiva 7 · verificar

(55 s) Esta es la idea que une los grupos B y C. En el correo del TSP, revisar un recorrido propuesto es lineal: sumar distancias y comparar con el presupuesto. Pero la búsqueda puede tener que revisar n menos 1 factorial recorridos: con solo 20 centros son más de cien mil billones. Nuestro programa lo muestra: la versión de decisión encontró un recorrido de 33 km dentro del presupuesto de 35, mientras que el óptimo, que es otra pregunta, es de 32. Si alguien encontrara un algoritmo polinómico para un NP-completo, probaría P igual a NP.

## Diapositiva 8 · parada

(60 s) Distinguimos tres conceptos. Decidibilidad: si existe un algoritmo que siempre se detiene con la respuesta correcta. Computabilidad: qué función se calcula y bajo qué modelo. Complejidad: cuántos recursos cuesta. Una instancia finita, como la ruta de un AMR por 25 ubicaciones, es decidible: basta revisar todas las rutas y el proceso termina, aunque tome millones de años. La parada es distinta: Turing demostró que ningún programa puede decidir si otro programa arbitrario termina. Por eso no existe un verificador general de terminación; lo que hacemos es demostrar la terminación de cada uno de nuestros algoritmos.

## Diapositiva 9 · implementacion

(60 s) Implementamos en Java el núcleo: Dijkstra con bloqueos, FEFO, reposición, los tres autómatas y un verificador de certificados para carga de flota. Todo con estructuras propias, sin colecciones de java.util, y separado en lectura, procesamiento y presentación. Las 70 pruebas cubren casos normales, límite e inválidos, y pasan todas. Cada correo tiene además un programa en Python con su verificador. El repositorio tiene una etiqueta por sprint y el release v1.0. [Mencionar aquí quién hizo qué en cada sprint.]

## Diapositiva 10 · cierre

(45 s) Para cerrar: los autómatas hacen explícitas las reglas de negocio y permiten probarlas. Los algoritmos necesitan reglas claras de desempate y de faltantes para ser reproducibles. Toda etiqueta de complejidad exige su evidencia: un algoritmo, un certificado o una reducción. Y decidible no significa practicable, mientras que la parada no se puede decidir en absoluto. Gracias, quedamos atentos a sus preguntas.

