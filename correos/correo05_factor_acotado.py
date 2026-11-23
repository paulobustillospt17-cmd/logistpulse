def validar_entrada(N, B):
    if not (isinstance(N, int) and isinstance(B, int)) or N < 2 or B < 1:
        raise ValueError("Se requiere N >= 2 y B >= 1 enteros")

def verificar_divisor(N, B, d):
    """Rutina de VERIFICACIÓN del certificado d: una comparación y una división. O(k²) con k = bits de N."""
    validar_entrada(N, B)
    return isinstance(d, int) and 1 < d <= B and N % d == 0

def buscar_divisor(N, B):
    """Rutina de BÚSQUEDA por división exhaustiva: prueba d = 2, 3, ..., min(B, N).
    Devuelve el menor divisor en el intervalo o None. O(B · k²) -> exponencial en los bits de B."""
    validar_entrada(N, B)
    tope = min(B, N)
    d = 2
    while d <= tope:                 # a lo sumo B - 1 iteraciones -> siempre termina
        if d * d > N:                # si no hay divisor <= raíz de N, N es primo:
            return N if N <= tope else None   # su único divisor > 1 es N mismo
        if N % d == 0:
            return d
        d += 1
    return None

# Entrada: REEMPLAZAR con el N y B de la consola del caso
N, B = 9991, 120

d = buscar_divisor(N, B)
print(f"N = {N} ({N.bit_length()} bits), B = {B} ({B.bit_length()} bits)")
print("¿Tiene divisor en (1, B]?", f"SÍ, d = {d} (N = {d} × {N // d})" if d else "NO")
if d:
    print("Verificación independiente del certificado:", verificar_divisor(N, B, d))

# Pruebas
assert buscar_divisor(9991, 120) == 97 and verificar_divisor(9991, 120, 97)   # 9991 = 97 × 103
assert buscar_divisor(9991, 96) is None                                      # su menor factor es 97 > 96
assert buscar_divisor(101, 200) == 101 and buscar_divisor(101, 100) is None  # primo: solo él mismo
assert buscar_divisor(64, 1) is None                                         # intervalo vacío
assert not verificar_divisor(9991, 120, 7)        # 7 no divide a N
assert not verificar_divisor(9991, 96, 97)        # divide, pero está fuera del límite B
assert not verificar_divisor(9991, 120, 1)        # el 1 no cuenta
try:
    buscar_divisor(1, 10); assert False
except ValueError:
    pass                                          # entrada inválida rechazada
print("Pruebas OK")
