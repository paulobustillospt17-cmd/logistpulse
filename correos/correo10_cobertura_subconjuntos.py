from itertools import combinations

def verificar(corredores, seleccion, k):
    """VERIFICADOR separado: ¿como máximo k zonas y cada corredor tiene un equipo en un extremo? O(n + m)."""
    S = set(seleccion)
    if len(S) > k:
        return False, "excede k"
    for u, v in corredores:
        if u not in S and v not in S:
            return False, f"corredor {u}-{v} sin cubrir"
    return True, None

def buscar_cobertura(zonas, corredores, k):
    """BÚSQUEDA por enumeración de subconjuntos de tamaño 0, 1, ..., k (del más pequeño al más grande).
    Devuelve la primera selección válida (de tamaño mínimo) o None si ninguna de tamaño <= k funciona."""
    if k < 0:
        raise ValueError("k debe ser >= 0")
    zonas = sorted(zonas)
    for tam in range(min(k, len(zonas)) + 1):          # conjunto finito de candidatos -> termina
        for candidato in combinations(zonas, tam):      # C(n, tam) subconjuntos de este tamaño
            if verificar(corredores, candidato, k)[0]:
                return list(candidato)
    return None                                         # se agotaron todos los subconjuntos de tamaño <= k

# Entrada: mapa del expediente (zonas y corredores de transferencia) y el límite k de equipos.
zonas = ["Durán", "Quito", "Guayaquil", "Norte", "Sur"]
corredores = [("Durán", "Quito"), ("Durán", "Guayaquil"), ("Quito", "Norte"), ("Guayaquil", "Sur")]
k = 2                                                   # REEMPLAZAR con el k del expediente

sel = buscar_cobertura(zonas, corredores, k)
if sel:
    print(f"SÍ bastan {k} equipos. Selección (certificado): {sel}")
    print("Verificador separado:", verificar(corredores, sel, k))
else:
    print(f"NO: ninguna selección de {k} zonas o menos cubre todos los corredores")

# Pruebas
assert sel == ["Guayaquil", "Quito"] and verificar(corredores, sel, 2) == (True, None)
assert buscar_cobertura(zonas, corredores, 1) is None                        # con 1 equipo no alcanza
assert verificar(corredores, ["Durán"], 2) == (False, "corredor Quito-Norte sin cubrir")
assert verificar(corredores, ["Durán", "Norte", "Sur"], 2)[1] == "excede k"  # cubre, pero usa 3 > k
assert verificar(corredores, ["Durán", "Norte", "Sur"], 3) == (True, None)   # cubrir no exige mínimo
assert buscar_cobertura(["A", "B"], [], 0) == []                             # sin corredores: 0 equipos
triangulo = [("A", "B"), ("B", "C"), ("A", "C")]
assert buscar_cobertura("ABC", triangulo, 1) is None and len(buscar_cobertura("ABC", triangulo, 2)) == 2
print("Pruebas OK")
