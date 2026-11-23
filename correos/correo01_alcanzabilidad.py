"""
Correo 1 - LOGISTPULSE: ¿Puede llegar el envío a la Zona Norte?

Problema de decisión (PATH / alcanzabilidad en grafo dirigido):
  Entrada: grafo dirigido G = (V, E) dado como lista de conexiones "origen destino",
           un vértice origen s y un vértice destino t.
  Pregunta: ¿existe un camino dirigido de s a t?  (Sí / No)
Certificado de un "Sí": la secuencia de tramos s -> ... -> t (verificable en O(longitud)).
Algoritmo: BFS (búsqueda en anchura). Tiempo O(|V| + |E|), espacio O(|V| + |E|).
"""
from collections import deque
import sys


def construir_grafo(conexiones):
    """Lista de adyacencia a partir de pares (origen, destino). Rutas en un solo sentido."""
    grafo = {}
    for par in conexiones:
        if len(par) != 2 or not all(isinstance(x, str) and x.strip() for x in par):
            raise ValueError(f"Conexión inválida: {par!r}")
        u, v = par[0].strip(), par[1].strip()
        grafo.setdefault(u, []).append(v)
        grafo.setdefault(v, [])
    return grafo


def existe_ruta(conexiones, origen, destino):
    """Devuelve (True, camino) si hay ruta dirigida de origen a destino; (False, None) si no."""
    grafo = construir_grafo(conexiones)
    if origen not in grafo or destino not in grafo:
        return False, None              # un nodo sin conexiones no puede alcanzarse
    padre = {origen: None}              # también sirve de conjunto de visitados
    cola = deque([origen])
    while cola:                         # cada vértice entra a la cola una sola vez
        u = cola.popleft()
        if u == destino:
            camino = []
            while u is not None:
                camino.append(u)
                u = padre[u]
            return True, camino[::-1]
        for v in grafo[u]:              # cada arista se revisa una sola vez
            if v not in padre:
                padre[v] = u
                cola.append(v)
    return False, None


def verificar_certificado(conexiones, origen, destino, camino):
    """Verifica en tiempo polinómico que 'camino' es una ruta válida de origen a destino."""
    tramos = {(u.strip(), v.strip()) for u, v in conexiones}
    return (bool(camino) and camino[0] == origen and camino[-1] == destino
            and all((camino[i], camino[i + 1]) in tramos for i in range(len(camino) - 1)))


def leer_conexiones(texto):
    """Lee una conexión por línea: 'Origen Destino' (ignora líneas vacías y comentarios #)."""
    conexiones = []
    for n, linea in enumerate(texto.splitlines(), 1):
        linea = linea.split("#")[0].strip()
        if not linea:
            continue
        partes = linea.split()
        if len(partes) != 2:
            raise ValueError(f"Línea {n}: se esperaba 'Origen Destino', se leyó {linea!r}")
        conexiones.append((partes[0], partes[1]))
    return conexiones


# ---------------------------------------------------------------- pruebas
def pruebas():
    caso_p1 = [("Durán", "Quito"), ("Durán", "Guayaquil"), ("Quito", "Norte"), ("Guayaquil", "Sur")]

    ok, camino = existe_ruta(caso_p1, "Durán", "Norte")
    assert ok and camino == ["Durán", "Quito", "Norte"]
    assert verificar_certificado(caso_p1, "Durán", "Norte", camino)
    print("Prueba 1 (datos del caso, CON ruta):", "SÍ", " -> ".join(camino))

    sin_quito_norte = [c for c in caso_p1 if c != ("Quito", "Norte")]
    ok, camino = existe_ruta(sin_quito_norte, "Durán", "Norte")
    assert not ok and camino is None
    print("Prueba 2 (tramo Quito->Norte cerrado, SIN ruta): NO")

    ok, _ = existe_ruta(caso_p1, "Norte", "Durán")
    assert not ok
    print("Prueba 3 (sentido contrario Norte->Durán): NO, las rutas son dirigidas")

    ok, camino = existe_ruta(caso_p1, "Durán", "Durán")
    assert ok and camino == ["Durán"]
    print("Prueba 4 (origen = destino): SÍ, camino de longitud 0")

    con_ciclo = caso_p1 + [("Quito", "Durán"), ("Sur", "Guayaquil")]
    ok, _ = existe_ruta(con_ciclo, "Guayaquil", "Norte")
    assert not ok
    print("Prueba 5 (red con ciclos, Guayaquil->Norte): NO, y el BFS termina igual")

    ok, _ = existe_ruta(caso_p1, "Durán", "Loja")
    assert not ok
    print("Prueba 6 (destino que no existe en la red): NO")

    assert not verificar_certificado(caso_p1, "Durán", "Norte", ["Durán", "Sur", "Norte"])
    print("Prueba 7 (certificado falso Durán->Sur->Norte): rechazado por el verificador")

    try:
        leer_conexiones("Durán Quito\nQuito")
        raise AssertionError("debió fallar")
    except ValueError as e:
        print("Prueba 8 (entrada mal formada): error detectado ->", e)

    print("Todas las pruebas pasaron.")


if __name__ == "__main__":
    # Uso: python alcanzable.py conexiones.txt Durán Norte    (sin argumentos: corre las pruebas)
    if len(sys.argv) == 4:
        with open(sys.argv[1], encoding="utf-8") as f:
            ok, camino = existe_ruta(leer_conexiones(f.read()), sys.argv[2], sys.argv[3])
        print("SÍ:" if ok else "NO: no existe ruta", " -> ".join(camino) if ok else "")
    else:
        pruebas()
