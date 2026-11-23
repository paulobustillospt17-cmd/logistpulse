def preparar(conexiones):
    """Grafo no dirigido como diccionario de vecinos."""
    g = {}
    for u, v in conexiones:
        if u == v:
            raise ValueError(f"Lazo no permitido en {u}")
        g.setdefault(u, set()).add(v)
        g.setdefault(v, set()).add(u)
    return g

def verificar(conex_a, conex_b, f):
    """Verificador del certificado f: ¿es biyección y conserva TODAS las conexiones? O(n + m)."""
    A, B = preparar(conex_a), preparar(conex_b)
    if set(f) != set(A) or set(f.values()) != set(B) or len(set(f.values())) != len(f):
        return False                                   # no es uno a uno o no cubre todos los nodos
    aristas_b = {frozenset(e) for e in conex_b}
    aristas_a = {frozenset(e) for e in conex_a}
    return (len(aristas_a) == len(aristas_b) and
            all(frozenset((f[u], f[v])) in aristas_b for u, v in aristas_a))

def isomorfismo(conex_a, conex_b):
    """Búsqueda exacta con backtracking y poda por grado. Devuelve la correspondencia o None."""
    A, B = preparar(conex_a), preparar(conex_b)
    if len(A) != len(B) or sum(map(len, A.values())) != sum(map(len, B.values())):
        return None                                    # distinto número de nodos o conexiones
    if sorted(map(len, A.values())) != sorted(map(len, B.values())):
        return None                                    # invariante: secuencia de grados distinta
    orden = sorted(A, key=lambda x: -len(A[x]))        # primero los nodos más conectados
    f, usados = {}, set()

    def extender(i):
        if i == len(orden):
            return True
        u = orden[i]
        for w in B:
            if w in usados or len(B[w]) != len(A[u]):  # poda: el grado debe coincidir
                continue
            # cada vecino ya asignado de u debe ser vecino de w (y no-vecinos siguen siendo no-vecinos)
            if all((f[x] in B[w]) == (x in A[u]) for x in f):
                f[u] = w; usados.add(w)
                if extender(i + 1):
                    return True
                del f[u]; usados.discard(w)            # deshacer y probar otro candidato
        return False

    return dict(f) if extender(0) else None

# Entrada: red actual (tabla del expediente) y red con los nombres nuevos del inventario
red_a = [("Durán", "Quito"), ("Durán", "Guayaquil"), ("Quito", "Norte"), ("Guayaquil", "Sur")]
red_b = [("N1", "N2"), ("N2", "N3"), ("N1", "N4"), ("N4", "N5")]   # REEMPLAZAR con la 2.ª red si el portal la trae

f = isomorfismo(red_a, red_b)
print("¿Misma estructura?", "SÍ" if f else "NO")
if f:
    for u, v in f.items():
        print(f"   {u} -> {v}")

# Pruebas
assert f is not None and verificar(red_a, red_b, f)                         # caso: SÍ, certificado válido
estrella = [("H", "X1"), ("H", "X2"), ("H", "X3"), ("H", "X4")]              # mismos 5 nodos y 4 conexiones
assert isomorfismo(red_a, estrella) is None                                 # pero otra estructura: NO
assert not verificar(red_a, red_b, {"Durán": "N1", "Quito": "N3", "Guayaquil": "N4",
                                    "Norte": "N2", "Sur": "N5"})            # correspondencia falsa: rechazada
assert isomorfismo(red_a, red_b[:3]) is None                                # distinto número de conexiones
c6 = [(i, (i + 1) % 6) for i in range(6)]                                   # mismo grado 2 en todos los nodos
dos_triangulos = [(0, 1), (1, 2), (2, 0), (3, 4), (4, 5), (5, 3)]
assert isomorfismo(c6, dos_triangulos) is None                              # grados iguales no bastan
print("Pruebas OK")
