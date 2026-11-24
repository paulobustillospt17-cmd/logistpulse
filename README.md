# LOGISTPULSE · Deber 2 de Teoría de la Computación (USFQ)

Autómatas de procesos, rutas en la planta (Dijkstra), reserva FEFO, reposición por punto de pedido y un verificador de certificados para un problema NP-completo (carga de flota = BIN PACKING).

## Estructura

```
jflap/        pedido.jff, reserva.jff, compra.jff + *_pruebas.txt (cadenas para Multiple Run)
diagramas/    diagramas de estados (.png y .dot) y grafo de la planta
src/logistpulse/
  LectorDatos.java   capa de LECTURA (valida formato, errores con archivo:línea)
  Grafo.java, MonticuloMin.java, Dijkstra.java     rutas
  Fefo.java, Reposicion.java                        inventario
  CargaFlota.java    verificador + exacto (backtracking) + heurística FFD
  Afd.java           simulador de los tres autómatas
  Main.java          capa de PRESENTACIÓN (CLI)
  Pruebas.java       70 pruebas: normales, límite e inválidas
datos/        entradas de prueba (válidas e inválidas)
pruebas/      salidas observadas (salida_pruebas.txt, salida_cli.txt, salida_correos.txt)
correos/      10 programas Python (uno por correo del simulador) + ejecutar_todos.py
docs/         informe (PDF y Word), presentación (PPTX con notas y PDF), guion y PENDIENTES.md
```

Sin dependencias externas y sin colecciones de `java.util`: todas las estructuras (lista de adyacencia, montículo, merge sort) son propias.

## Ejecutar

Requiere JDK 11 o superior.

```bash
./ejecutar_pruebas.sh                       # compila y corre las 70 pruebas
javac -encoding UTF-8 -d out src/logistpulse/*.java
java -cp out logistpulse.Main ruta datos/planta.txt R D          # 11 m, 2 rutas óptimas
java -cp out logistpulse.Main ruta datos/planta.txt R D A        # con Pasillo A bloqueado
java -cp out logistpulse.Main fefo datos/lotes.txt 100 2026-11-17 PARCIAL
java -cp out logistpulse.Main reposicion datos/skus.txt
java -cp out logistpulse.Main carga datos/carga_ffd_falla.txt   # exacto SÍ, FFD falla
java -cp out logistpulse.Main afd compra sje                     # RECHAZA
```

En Windows: `javac -encoding UTF-8 -d out src\logistpulse\*.java` y luego `java -cp out logistpulse.Pruebas`.

## Correos del simulador (Python 3.8+)

```bash
python correos/ejecutar_todos.py        # 11/11 programas con sus pruebas
python correos/correo01_alcanzabilidad.py correos/conexiones_p1.txt Durán Norte
```

Cada script tiene una sección «Entrada»: reemplácela con los datos del expediente para recalcular.

| # | Problema | Grupo | Algoritmo |
|---|---|---|---|
| 1 | Alcanzabilidad | P | BFS |
| 2 | Ruta mínima | P | Dijkstra |
| 3 | Flujo máximo | P | Edmonds-Karp |
| 4 | Isomorfismo | NP | Enumeración de permutaciones |
| 5 | Factor acotado | NP | División exhaustiva |
| 6 | Logaritmo discreto | NP | Búsqueda con exponenciación modular |
| 7 | Automorfismo | NP | Permutaciones + verificación de adyacencia |
| 8 | SAT | NP-completo | DPLL |
| 9 | TSP de decisión | NP-completo | Backtracking con poda |
| 10 | Vertex cover | NP-completo | Subconjuntos / ramificación |

## JFLAP

Validar los archivos sin abrir JFLAP: `python jflap/validar_jff.py` (lee el XML de cada .jff y corre las cadenas; 24/24).

Abrir `jflap/pedido.jff` → *Input → Multiple Run* → pegar las cadenas de `jflap/pedido_pruebas.txt`. La segunda columna del archivo trae el resultado esperado (Accept/Reject). Leyenda de símbolos: ver la sección 2 del informe.

## Presentación

- `docs/Presentacion_LOGISTPULSE.pptx`: 10 diapositivas con notas del orador (≈ 9 min 50 s).
- `docs/Presentacion_LOGISTPULSE.pdf`: la misma, para proyectar sin PowerPoint.
- `docs/Guion_presentacion.md`: el guion por diapositiva.
- Versión editable en línea: https://claude.ai/artifact/EeC8T2xHnFDmCQBS8BqMwW

## Contribuciones

| Integrante | Rol | Historias / commits |
|---|---|---|
| [Integrante 1] | Coordinación y backlog | H10, H13 |
| [Integrante 2] | Formalización | H1–H4 |
| [Integrante 3] | Algoritmos y análisis | H5–H8 |
| [Integrante 4] | Implementación y validación | H9, H11 |

Etiquetas: `v0.1-sprint1` (autómatas y grafo), `v0.2-sprint2` (algoritmos y pruebas), `v1.0` (release final).
Para publicar: `git remote add origin <url> && git push -u origin main --tags`
