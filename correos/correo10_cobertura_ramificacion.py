def verificar(corredores, seleccion, k):
    """VERIFICADOR separado: ¿como máximo k zonas y cada corredor tiene un equipo en un extremo? O(n + m)."""
    S = set(seleccion)
    if len(S) > k:
        return False, "excede k"
    for u, v in corredores:
        if u not in S and v not in S:
            return False, f"corredor {u}-{v} sin cubrir"
    return True, None

def buscar_cobertura(corredores, k, seleccion=frozenset()):
    """BÚSQUEDA por backtracking / ramificación: toma un corredor sin cubrir (u, v); toda cobertura
    debe contener u o v, así que se ramifica en esas 2 opciones con un equipo menos. Profundidad <= k."""
    if k < 0:
        raise ValueError("k debe ser >= 0")
    pendiente = next(((u, v) for u, v in corredores if u not in seleccion and v not in seleccion), None)
    if pendiente is None:
        return sorted(seleccion)                     # todos los corredores cubiertos
    if k == 0:
        return None                                  # poda: queda un corredor y no hay equipos
    u, v = pendiente
    for zona in (u, v):                              # a lo sumo 2 ramas por nivel
        r = buscar_cobertura(corredores, k - 1, seleccion | {zona})
        if r is not None:
            return r
    return None                                      # ambas ramas fallan -> no existe con k equipos

# Entrada: mapa del expediente (corredores de transferencia) y el límite k de equipos.
corredores = [("Durán", "Quito"), ("Durán", "Guayaquil"), ("Quito", "Norte"), ("Guayaquil", "Sur")]
k = 2                                                # REEMPLAZAR con el k del expediente

sel = buscar_cobertura(corredores, k)
if sel is not None:
    print(f"SÍ bastan {k} equipos. Selección (certificado): {sel}")
    print("Verificador separado:", verificar(corredores, sel, k))
else:
    print(f"NO: ninguna selección de {k} zonas o menos cubre todos los corredores")

# Pruebas
assert sel == ["Guayaquil", "Quito"] and verificar(corredores, sel, 2) == (True, None)
assert buscar_cobertura(corredores, 1) is None                               # con 1 equipo no alcanza
assert verificar(corredores, ["Durán"], 2) == (False, "corredor Quito-Norte sin cubrir")
assert verificar(corredores, ["Durán", "Norte", "Sur"], 2)[1] == "excede k"  # cubre, pero usa 3 > k
assert buscar_cobertura([], 0) == []                                         # sin corredores: 0 equipos
triangulo = [("A", "B"), ("B", "C"), ("A", "C")]
assert buscar_cobertura(triangulo, 1) is None and verificar(triangulo, buscar_cobertura(triangulo, 2), 2)[0]
print("Pruebas OK")
