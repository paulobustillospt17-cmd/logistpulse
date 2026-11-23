from collections import deque

def flujo_maximo(tramos, origen, sumidero, registro=True):
    """Edmonds-Karp: flujo máximo de origen a sumidero respetando capacidades. O(V·E²)."""
    cap, ady = {}, {}
    for u, v, c in tramos:
        if c < 0:
            raise ValueError(f"Capacidad negativa en {u}->{v}")
        cap[(u, v)] = cap.get((u, v), 0) + c
        cap.setdefault((v, u), 0)                     # arco residual de retorno
        ady.setdefault(u, set()).add(v)
        ady.setdefault(v, set()).add(u)
    if origen not in ady or sumidero not in ady:
        return 0, {}
    flujo = {k: 0 for k in cap}
    total, paso = 0, 0
    while True:
        padre = {origen: None}                        # BFS en la red residual
        cola = deque([origen])
        while cola and sumidero not in padre:
            u = cola.popleft()
            for v in sorted(ady[u]):                  # orden fijo -> registro reproducible
                if v not in padre and cap[(u, v)] - flujo[(u, v)] > 0:
                    padre[v] = u
                    cola.append(v)
        if sumidero not in padre:
            break                                     # no hay camino aumentante -> flujo máximo
        camino, v = [], sumidero
        while padre[v] is not None:
            camino.append((padre[v], v))
            v = padre[v]
        camino.reverse()
        delta = min(cap[e] - flujo[e] for e in camino)  # cuello de botella
        for u, v in camino:
            flujo[(u, v)] += delta
            flujo[(v, u)] -= delta                    # permite deshacer flujo después
        total += delta
        paso += 1
        if registro:
            ruta = " -> ".join([camino[0][0]] + [v for _, v in camino])
            print(f"Paso {paso}: {ruta} | +{delta} | total = {total}")
            for u, v in camino:
                c = cap[(u, v)]
                cs, ds = ("∞", "∞") if c >= 10**9 else (c, c - flujo[(u, v)])
                print(f"   {u}->{v}: usado {max(flujo[(u, v)], 0)}/{cs}, disponible {ds}")
    usados = {(u, v): flujo[(u, v)] for (u, v), c in cap.items() if c > 0}
    return total, usados

def verificar_flujo(tramos, origen, sumidero, f, valor):
    """Verificador de un certificado de flujo: capacidades, conservación y valor (O(V+E))."""
    cap = {(u, v): c for u, v, c in tramos}
    if any(not (0 <= f.get(e, 0) <= c) for e, c in cap.items()):
        return False
    balance = {}
    for (u, v), x in f.items():
        balance[u] = balance.get(u, 0) - x
        balance[v] = balance.get(v, 0) + x
    return (all(b == 0 for n, b in balance.items() if n not in (origen, sumidero))
            and balance.get(sumidero, 0) == valor)

# Entrada: tramos (origen, destino, camiones/día). REEMPLAZAR capacidades con las del portal.
# "Clientes" agrupa las zonas Norte y Sur (súper-sumidero con capacidad ilimitada).
INF = 10**9
red = [("Durán", "Quito", 10), ("Durán", "Guayaquil", 8),
       ("Quito", "Norte", 4), ("Guayaquil", "Sur", 6),
       ("Norte", "Clientes", INF), ("Sur", "Clientes", INF)]

maximo, usados = flujo_maximo(red, "Durán", "Clientes")
print("Máximo de camiones/día:", maximo)

# Pruebas
assert maximo == 10                                   # aunque salen 10 + 8 = 18 de Durán
assert verificar_flujo(red, "Durán", "Clientes", usados, maximo)
assert flujo_maximo([("A", "B", 5)], "B", "A", False)[0] == 0      # sin camino: flujo 0
# red donde el flujo debe "devolverse" por el arco residual: máximo 2
cruce = [("S", "A", 1), ("S", "B", 1), ("A", "B", 1), ("A", "T", 1), ("B", "T", 1)]
assert flujo_maximo(cruce, "S", "T", False)[0] == 2
print("Pruebas OK")
