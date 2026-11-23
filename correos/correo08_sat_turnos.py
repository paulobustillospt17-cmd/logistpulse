from itertools import product

# Formato CNF: cada cláusula es una lista de literales; el entero k significa "x_k" y -k significa "NO x_k".

def verificar(clausulas, asignacion):
    """Comprobación INDEPENDIENTE: ¿la asignación hace verdadera CADA cláusula? O(tamaño de la fórmula)."""
    for i, c in enumerate(clausulas):
        if not any(asignacion.get(abs(l)) == (l > 0) for l in c):
            return False, i                                   # índice de la cláusula incumplida
    return True, None

def dpll(clausulas, asignacion=None):
    """DPLL: retroceso con poda (propagación unitaria). Devuelve una asignación que satisface todo, o None."""
    asignacion = dict(asignacion or {})
    while True:                                               # propagación unitaria
        pendientes, unitaria = [], None
        for c in clausulas:
            if any(asignacion.get(abs(l)) == (l > 0) for l in c):
                continue                                      # cláusula ya satisfecha
            libres = [l for l in c if abs(l) not in asignacion]
            if not libres:
                return None                                   # conflicto: cláusula falsa -> podar rama
            if len(libres) == 1:
                unitaria = libres[0]
            pendientes.append(c)
        if not pendientes:
            return asignacion                                 # todas satisfechas
        if unitaria is None:
            break
        asignacion[abs(unitaria)] = unitaria > 0              # literal forzado
    var = next(abs(l) for c in pendientes for l in c if abs(l) not in asignacion)
    for valor in (True, False):                               # ramificación: a lo sumo 2 ramas por variable
        r = dpll(pendientes, {**asignacion, var: valor})
        if r is not None:
            return r
    return None                                               # ambas ramas fallan -> insatisfacible aquí

def completar(asignacion, n):
    return {v: asignacion.get(v, False) for v in range(1, n + 1)}   # variables libres: cualquier valor sirve

def fuerza_bruta(clausulas, n):
    """Enumeración de las 2^n asignaciones (para comparar en las pruebas)."""
    for valores in product([False, True], repeat=n):
        a = dict(zip(range(1, n + 1), valores))
        if verificar(clausulas, a)[0]:
            return a
    return None

# Entrada: REEMPLAZAR con las variables y cláusulas del caso.
# Ejemplo: x1 = Camión A en franja 1, x2 = Camión A en franja 2, x3 = Camión B en franja 1, x4 = Camión B en franja 2
nombres = {1: "A-franja1", 2: "A-franja2", 3: "B-franja1", 4: "B-franja2"}
clausulas = [[1, 2],      # A debe cargar en alguna franja
             [-1, -2],    # A no puede estar en dos franjas
             [3, 4],      # B debe cargar en alguna franja
             [-3, -4],    # B no puede estar en dos franjas
             [-1, -3],    # A y B no comparten la franja 1 (capacidad del muelle)
             [-2, -4],    # A y B no comparten la franja 2
             [-3]]        # B no puede ir en la franja 1 (horario / tipo de mercancía)
n = 4

sol = dpll(clausulas)
if sol is None:
    print("NO existe asignación: hay que renegociar la ventana de despacho")
else:
    sol = completar(sol, n)
    print("SÍ existe asignación:", [nombres[v] for v in sorted(sol) if sol[v]])
    print("Comprobación independiente (cumple cada cláusula):", verificar(clausulas, sol)[0])

# Pruebas
assert sol == {1: True, 2: False, 3: False, 4: True}                     # A en franja 1, B en franja 2
assert verificar(clausulas, sol) == (True, None)
assert verificar(clausulas, {1: True, 2: False, 3: True, 4: False}) == (False, 4)   # viola la cláusula 4
insat = clausulas + [[-4]]                                               # B tampoco puede ir en franja 2
assert dpll(insat) is None and fuerza_bruta(insat, n) is None            # NO: renegociar
assert dpll([[1], [-1]]) is None                                         # contradicción directa
assert dpll([]) == {}                                                    # sin reglas: trivialmente SÍ
# DPLL y fuerza bruta coinciden en una fórmula 3-CNF más grande
f3 = [[1, -2, 3], [-1, 2, 4], [2, -3, -4], [-1, -2, -3], [1, 3, 4], [-2, 3, -4]]
r = dpll(f3)
assert (r is None) == (fuerza_bruta(f3, 4) is None)
assert r is None or verificar(f3, completar(r, 4))[0]
print("Pruebas OK")
