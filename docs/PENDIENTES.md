# Lo que falta antes de entregar (solo lo puede hacer el equipo)

1. **Capturas de JFLAP.** Abrir cada `jflap/<proceso>.jff` en JFLAP 7.1 → *Input → Multiple Run* → cargar las cadenas de
   `jflap/<proceso>_pruebas.txt` → guardar la captura como `diagramas/jflap_<proceso>.png`.
   Insertarlas en la sección 2 del informe (donde dice «Agregar aquí la captura») y en la diapositiva 3.
   Mientras tanto, `python jflap/validar_jff.py` demuestra que los archivos dan 24/24 (`pruebas/salida_jff.txt`).
2. **Datos del equipo** en el informe (portada, sección 8), el README y la diapositiva 1: nombres, roles,
   profesor, fecha y enlace al repositorio. Buscar «[Integrante», «[completar]» y «[COMPLETAR».
3. **Retrospectivas** de los tres sprints (tabla de la sección 8): qué funcionó, qué mejorar y la acción.
4. **Datos reales de los correos**, si el simulador los muestra: 3 (capacidades), 4 (segunda red), 5 (N y B),
   6 (p, g, h y B), 8 (cláusulas), 9 (matriz y L) y 10 (k). Cambiar la sección «Entrada» del script y recalcular.
5. **Subir el repositorio** y que cada integrante haga commits de su parte:
   `git remote add origin <url> && git push -u origin main --tags`.
   El historial actual lo generó un asistente en una sola sesión, a nombre de «Equipo LOGISTPULSE».
