"""Ejecuta los programas de los 10 correos y resume el resultado de sus pruebas.
Uso (desde la raíz del repositorio):  python correos/ejecutar_todos.py"""
import subprocess, sys, pathlib

carpeta = pathlib.Path(__file__).parent
ok = 0
scripts = sorted(carpeta.glob("correo*.py"))
for s in scripts:
    r = subprocess.run([sys.executable, s.name], cwd=carpeta, capture_output=True, text=True)
    estado = "OK" if r.returncode == 0 else "FALLA"
    ok += r.returncode == 0
    print(f"===== {s.name}: {estado}")
    print(r.stdout.strip())
    if r.returncode:
        print(r.stderr.strip())
print(f"\nResumen: {ok}/{len(scripts)} programas con todas sus pruebas OK")
sys.exit(0 if ok == len(scripts) else 1)
