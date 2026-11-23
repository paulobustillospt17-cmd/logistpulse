INF = float("inf")

def validar(D):
    n = len(D)
    if n < 2 or any(len(fila) != n for fila in D):
        raise ValueError("La matriz de distancias debe ser cuadrada y tener al menos 2 centros")
    if any(D[i][j] < 0 for i in range(n) for j in range(n)):
        raise ValueError("Distancias negativas no permitidas")

def verificar(D, recorrido, L):
    """VERIFICADOR del certificado: visita cada centro exactamente una vez, regresa al inicio,
    usa tramos existentes y cuesta <= L. O(n). Devuelve (válido, costo)."""
    validar(D)
    n = len(D)
    if sorted(recorrido) != list(range(n)):
        return False, None                                   # falta un centro o hay repetidos
    ciclo = list(recorrido) + [recorrido[0]]                 # regreso al punto de salida
    costo = sum(D[ciclo[i]][ciclo[i + 1]] for i in range(n))
    return costo <= L, costo                                 # costo INF = tramo inexistente

def existe_recorrido(D, L, salida=0):
    """DECISIÓN: backtracking que enumera permutaciones y poda en cuanto el costo parcial supera L.
    Se detiene en el PRIMER recorrido válido (no busca el mínimo). Devuelve (recorrido, costo) o (None, None)."""
    validar(D)
    n = len(D)
    camino, usado = [salida], [False] * n
    usado[salida] = True

    def extender(costo):
        if costo > L:
            return None                                      # poda: ya se pasó del presupuesto
        if len(camino) == n:
            total = costo + D[camino[-1]][salida]            # cerrar el ciclo
            return (list(camino), total) if total <= L else None
        for siguiente in range(n):
            if not usado[siguiente] and D[camino[-1]][siguiente] < INF:
                usado[siguiente] = True; camino.append(siguiente)
                r = extender(costo + D[camino[-2]][siguiente])
                if r:
                    return r
                camino.pop(); usado[siguiente] = False       # retroceder
        return None

    return extender(0) or (None, None)

def recorrido_minimo(D, salida=0):
    """OPTIMIZACIÓN (otra pregunta): Held-Karp, programación dinámica O(n²·2ⁿ) tiempo, O(n·2ⁿ) espacio."""
    validar(D)
    n = len(D)
    otros = [v for v in range(n) if v != salida]
    dp = {(1 << i, v): (D[salida][v], salida) for i, v in enumerate(otros)}
    for tam in range(2, n):
        for mask in range(1 << len(otros)):
            if bin(mask).count("1") != tam:
                continue
            for i, v in enumerate(otros):
                if not mask & (1 << i):
                    continue
                prev = mask ^ (1 << i)
                dp[(mask, v)] = min(((dp[(prev, u)][0] + D[u][v], u)
                                     for j, u in enumerate(otros) if prev & (1 << j)), default=(INF, None))
    full = (1 << len(otros)) - 1
    costo, ultimo = min(((dp[(full, v)][0] + D[v][salida], v) for v in otros), default=(INF, None))
    ruta, mask, v = [], full, ultimo
    while v != salida and v is not None:                     # reconstrucción con predecesores
        ruta.append(v)
        mask, v = mask ^ (1 << otros.index(v)), dp[(mask, v)][1]
    return [salida] + ruta[::-1], costo

# Entrada: REEMPLAZAR con la matriz de distancias (km) del expediente y el presupuesto L.
centros = ["Planta Durán", "CD Quito", "CD Guayaquil", "Zona Norte", "Zona Sur"]
D = [[0, 12, 10, 19, 8],
     [12, 0, 3, 7, 2],
     [10, 3, 0, 6, 20],
     [19, 7, 6, 0, 4],
     [8, 2, 20, 4, 0]]
L = 35

ruta, costo = existe_recorrido(D, L)
if ruta:
    print(f"SÍ existe recorrido con costo <= {L} km:", " -> ".join(centros[i] for i in ruta + [ruta[0]]), f"| {costo} km")
    print("Verificación ejecutable:", verificar(D, ruta, L))
else:
    print(f"NO existe ningún recorrido con costo <= {L} km")
mejor, cmin = recorrido_minimo(D)
print("(Pregunta distinta) Recorrido mínimo:", " -> ".join(centros[i] for i in mejor + [mejor[0]]), f"| {cmin} km")

# Pruebas
assert ruta is not None and verificar(D, ruta, L) == (True, costo) and costo <= L
assert cmin == 32 and verificar(D, mejor, cmin)[0]              # óptimo por Held-Karp
assert existe_recorrido(D, cmin - 1) == (None, None)            # por debajo del óptimo: NO
assert existe_recorrido(D, cmin)[1] == cmin                     # justo en el óptimo: SÍ
assert verificar(D, [0, 1, 2, 3], L) == (False, None)           # falta un centro: rechazado
assert verificar(D, [0, 1, 1, 3, 4], L) == (False, None)        # centro repetido: rechazado
sin_tramo = [[0, 1, INF], [1, 0, 1], [INF, 1, 0]]
assert existe_recorrido(sin_tramo, 100) == (None, None)         # no hay ciclo si falta el tramo 0-2
print("Pruebas OK")
