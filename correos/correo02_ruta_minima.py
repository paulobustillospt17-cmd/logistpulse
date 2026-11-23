import heapq

def dijkstra(tramos, origen, destino):
    """Ruta de menor tiempo con pesos >= 0. Devuelve (tiempo, ruta) o (inf, None) si no hay ruta."""
    grafo = {}
    for u, v, t in tramos:                       # lista de adyacencia
        if t < 0:
            raise ValueError(f"Tiempo negativo en {u}->{v}: Dijkstra no aplica")
        grafo.setdefault(u, []).append((v, t))
        grafo.setdefault(v, [])
    if origen not in grafo or destino not in grafo:
        return float("inf"), None
    dist = {n: float("inf") for n in grafo}
    pred = {n: None for n in grafo}              # lo que el sistema debe guardar para reconstruir
    dist[origen] = 0
    cola = [(0, origen)]
    cerrados = set()
    while cola:
        d, u = heapq.heappop(cola)
        if u in cerrados:
            continue                             # entrada vieja de la cola
        cerrados.add(u)                          # cada nodo se cierra una vez -> termina
        if u == destino:
            break
        for v, t in grafo[u]:
            if d + t < dist[v]:                  # relajación
                dist[v] = d + t
                pred[v] = u
                heapq.heappush(cola, (dist[v], v))
    if dist[destino] == float("inf"):
        return float("inf"), None
    ruta, n = [], destino
    while n is not None:                         # reconstrucción siguiendo predecesores
        ruta.append(n)
        n = pred[n]
    return dist[destino], ruta[::-1]

def verificar(tramos, ruta, K):
    """Verificador del certificado (versión de decisión): ¿la ruta existe y cuesta <= K?"""
    peso = {(u, v): t for u, v, t in tramos}
    if not ruta or any((ruta[i], ruta[i+1]) not in peso for i in range(len(ruta) - 1)):
        return False
    return sum(peso[(ruta[i], ruta[i+1])] for i in range(len(ruta) - 1)) <= K

# Entrada del caso: tramos reportados en la consola (tabla datos_caso). No traen tiempos.
caso = [("Durán", "Quito"), ("Durán", "Guayaquil"), ("Quito", "Norte"), ("Guayaquil", "Sur")]

# 1) Respetando el sentido de los tramos (como en el mapa): ¿hay ruta Quito -> Guayaquil?
dirigido = [(u, v, 0) for u, v in caso]          # sin tiempos: solo importa si hay camino
print("Caso, dirigido:", dijkstra(dirigido, "Quito", "Guayaquil"))          # (inf, None) -> no hay ruta

# 2) Si Transporte confirma que los tramos son de doble sentido, la ruta posible es:
doble = [(u, v, 0) for u, v in caso] + [(v, u, 0) for u, v in caso]
print("Caso, doble sentido:", dijkstra(doble, "Quito", "Guayaquil")[1])     # Quito -> Durán -> Guayaquil

# Prueba pequeña con tiempos (en horas), para mostrar que elige la ruta indirecta más rápida
tramos = [("CD Quito", "Santo Domingo", 3), ("CD Quito", "Ambato", 2),
          ("Santo Domingo", "CD Guayaquil", 5), ("Ambato", "Riobamba", 1),
          ("Riobamba", "CD Guayaquil", 4), ("Ambato", "CD Guayaquil", 7)]
tiempo, ruta = dijkstra(tramos, "CD Quito", "CD Guayaquil")
print("Prueba:", " -> ".join(ruta), "| tiempo:", tiempo)

assert dijkstra(dirigido, "Quito", "Guayaquil") == (float("inf"), None)          # caso real: sin ruta
assert dijkstra(doble, "Quito", "Guayaquil")[1] == ["Quito", "Durán", "Guayaquil"]
assert (tiempo, ruta) == (7, ["CD Quito", "Ambato", "Riobamba", "CD Guayaquil"])  # gana la indirecta
assert verificar(tramos, ruta, 7) and not verificar(tramos, ruta, 6)              # certificado
assert dijkstra(tramos, "CD Quito", "CD Quito") == (0, ["CD Quito"])              # origen = destino
try:
    dijkstra([("A", "B", -1)], "A", "B"); assert False
except ValueError:
    pass                                                                          # peso negativo rechazado
print("Pruebas OK")
