from itertools import permutations

def aristas(conexiones, dirigido):
    return {(u, v) if dirigido else frozenset((u, v)) for u, v in conexiones}

def verificar(nodos, conexiones, f, dirigido=False):
    """Verificador INDEPENDIENTE del certificado f: biyección, no identidad y conserva adyacencias. O(n + m)."""
    E = aristas(conexiones, dirigido)
    if set(f) != set(nodos) or set(f.values()) != set(nodos):
        return False                                       # no es una permutación de los nodos
    if all(f[v] == v for v in nodos):
        return False                                       # la identidad no cuenta como simetría
    imagen = {(f[u], f[v]) if dirigido else frozenset((f[u], f[v])) for u, v in conexiones}
    return imagen == E                                     # mismas adyacencias (y mismas no-adyacencias)

def buscar_simetria(nodos, conexiones, dirigido=False):
    """Recorre las n! permutaciones (con poda por grado) y verifica la adyacencia de cada una.
    Devuelve (primera simetría no trivial o None, número de permutaciones revisadas)."""
    nodos = list(nodos)
    E = aristas(conexiones, dirigido)
    grado = {v: sum(v in (e if not dirigido else e) for e in E) for v in nodos}
    revisadas = 0
    for imagen in permutations(nodos):                     # n! candidatos -> conjunto finito, termina
        f = dict(zip(nodos, imagen))
        if any(grado[v] != grado[f[v]] for v in nodos):
            continue                                       # poda: una simetría conserva el grado
        revisadas += 1
        if verificar(nodos, conexiones, f, dirigido):
            return f, revisadas
    return None, revisadas                                 # se agotaron TODAS las permutaciones

# Entrada: grafo cargado en el portal (zonas y adyacencias del plano)
zonas = ["Durán", "Quito", "Guayaquil", "Norte", "Sur"]
adyacencias = [("Durán", "Quito"), ("Durán", "Guayaquil"), ("Quito", "Norte"), ("Guayaquil", "Sur")]

f, revisadas = buscar_simetria(zonas, adyacencias)
print("¿El plano tiene una simetría no trivial?", "SÍ" if f else "NO")
if f:
    print("Certificado (permutación):", {u: v for u, v in f.items()})
    print("Verificador independiente:", verificar(zonas, adyacencias, f))
    print("Con sentido de las rutas (dirigido):", verificar(zonas, adyacencias, f, dirigido=True))

# Pruebas
assert f == {"Durán": "Durán", "Quito": "Guayaquil", "Guayaquil": "Quito", "Norte": "Sur", "Sur": "Norte"}
assert verificar(zonas, adyacencias, f) and verificar(zonas, adyacencias, f, dirigido=True)
assert not verificar(zonas, adyacencias, {z: z for z in zonas})                  # identidad: rechazada
assert not verificar(zonas, adyacencias, {"Durán": "Quito", "Quito": "Durán", "Guayaquil": "Guayaquil",
                                          "Norte": "Norte", "Sur": "Sur"})        # rompe adyacencias
# Grafo asimétrico de 6 nodos (el tamaño mínimo posible): no tiene simetría -> el programa agota las opciones
asim = [(1, 2), (1, 3), (1, 4), (2, 3), (2, 5), (4, 6)]
g, n_rev = buscar_simetria(range(1, 7), asim)
assert g is None
print(f"Grafo asimétrico: NO, tras revisar {n_rev} de 720 permutaciones (el resto descartado por grado)")
print("Pruebas OK")
