"""Validador independiente de los archivos .jff: lee el XML de JFLAP (sin usar el código Java)
y ejecuta las cadenas de <proceso>_pruebas.txt sobre el autómata tal como está guardado en el archivo.
No reemplaza la ejecución en JFLAP; confirma que los .jff contienen exactamente los autómatas del informe.
Uso: python jflap/validar_jff.py"""
import pathlib, sys, xml.etree.ElementTree as ET

def cargar(ruta):
    a = ET.parse(ruta).getroot().find("automaton")
    nombres, inicial, finales, delta = {}, None, set(), {}
    for s in a.findall("state"):
        nombres[s.get("id")] = s.get("name")
        if s.find("initial") is not None: inicial = s.get("id")
        if s.find("final") is not None: finales.add(s.get("id"))
    for t in a.findall("transition"):
        clave = (t.findtext("from"), t.findtext("read"))
        if clave in delta: raise ValueError(f"{ruta}: no determinista en {clave}")
        delta[clave] = t.findtext("to")
    return nombres, inicial, finales, delta

def correr(aut, cadena):
    nombres, q, finales, delta = aut
    for c in cadena:
        if (q, c) not in delta: return "Reject", f"sin transición desde {nombres[q]} con '{c}'"
        q = delta[(q, c)]
    return ("Accept", f"termina en {nombres[q]}") if q in finales else ("Reject", f"termina en {nombres[q]} (no final)")

carpeta = pathlib.Path(__file__).parent
fallas = total = 0
for proc in ("pedido", "reserva", "compra"):
    aut = cargar(carpeta / f"{proc}.jff")
    print(f"== {proc}.jff: {len(aut[0])} estados, {len(aut[3])} transiciones, inicial {aut[0][aut[1]]}, "
          f"finales {sorted(aut[0][f] for f in aut[2])}")
    for linea in (carpeta / f"{proc}_pruebas.txt").read_text(encoding="utf-8").splitlines():
        if not linea or linea.startswith("#"): continue
        cadena, esperado, _ = linea.split("\t", 2)
        obtenido, motivo = correr(aut, cadena)
        total += 1; fallas += obtenido != esperado
        print(f"  {cadena:<8} esperado {esperado:<6} obtenido {obtenido:<6} {'OK' if obtenido == esperado else 'FALLA'}  ({motivo})")
print(f"\nResumen: {total - fallas}/{total} cadenas coinciden con lo esperado leyendo los .jff")
sys.exit(1 if fallas else 0)
