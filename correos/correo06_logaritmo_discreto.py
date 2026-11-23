def validar(p, g, h, B):
    if not all(isinstance(v, int) for v in (p, g, h, B)) or p < 2 or B < 0:
        raise ValueError("Se requiere p >= 2, B >= 0 y parámetros enteros")
    if not (0 <= g < p and 0 <= h < p):
        raise ValueError("g y h deben estar en el rango 0..p-1")

def exp_modular(base, exp, mod):
    """Exponenciación modular por cuadrados sucesivos: O(log exp) multiplicaciones mod p."""
    resultado, base = 1 % mod, base % mod
    while exp > 0:
        if exp & 1:
            resultado = resultado * base % mod
        base = base * base % mod
        exp >>= 1
    return resultado

def verificar(p, g, h, B, x):
    """VERIFICADOR de una respuesta candidata x: rango + una exponenciación modular. O(b·k²)."""
    validar(p, g, h, B)
    return isinstance(x, int) and 0 <= x <= B and exp_modular(g, x, p) == h

def buscar_exponente(p, g, h, B):
    """BÚSQUEDA exhaustiva: recorre x = 0, 1, ..., B manteniendo g^x mod p (una multiplicación por paso).
    Devuelve el menor x válido o None. O(B·k²) -> exponencial en los bits de B."""
    validar(p, g, h, B)
    actual = 1 % p                        # g^0
    for x in range(B + 1):                # B + 1 iteraciones como máximo -> siempre termina
        if actual == h:
            return x
        actual = actual * g % p           # g^(x+1) = g^x · g  (mod p)
    return None

# Entrada: parámetros FICTICIOS de la estación. REEMPLAZAR con p, g, h y B de la consola.
p, g, h, B = 101, 2, 55, 50

x = buscar_exponente(p, g, h, B)
print(f"p = {p}, g = {g}, h = {h}, B = {B}")
print("¿Existe x en [0, B] con g^x ≡ h (mod p)?", f"SÍ, x = {x}" if x is not None else "NO")
if x is not None:
    print(f"Verificación: {g}^{x} mod {p} = {exp_modular(g, x, p)} ->", verificar(p, g, h, B, x))

# Pruebas reproducibles
assert exp_modular(2, 37, 101) == pow(2, 37, 101) == 55        # exponenciación modular correcta
assert buscar_exponente(101, 2, 55, 50) == 37 and verificar(101, 2, 55, 50, 37)
assert buscar_exponente(101, 2, 55, 30) is None                # existe x = 37, pero fuera del límite B
assert not verificar(101, 2, 55, 30, 37)                       # candidato fuera de rango: rechazado
assert not verificar(101, 2, 55, 50, 36)                       # candidato incorrecto: rechazado
assert buscar_exponente(101, 2, 1, 10) == 0                    # h = 1 -> x = 0
assert buscar_exponente(7, 2, 3, 100) is None                  # 2 solo genera {1, 2, 4} mod 7: nunca da 3
try:
    buscar_exponente(101, 2, 500, 10); assert False
except ValueError:
    pass                                                       # h fuera de rango: entrada inválida
print("Pruebas OK")
