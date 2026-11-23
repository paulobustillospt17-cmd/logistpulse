package logistpulse;

/**
 * Carga de flota como BIN PACKING (versión de decisión), problema NP-completo (Garey y Johnson, [SR1]).
 * Instancia: pesos w[0..n-1] > 0, k camiones de capacidad C. Pregunta: ¿existe una asignación de cada paquete
 * a un camión sin exceder C en ninguno?
 *
 *  - verificar(): verificador polinómico del certificado asignacion[i] in {0..k-1}. O(n + k) tiempo, O(k) espacio.
 *  - exacto(): backtracking con poda; peor caso O(k^n) -> solo instancias pequeñas. Siempre termina y es correcto.
 *  - ffd(): heurística First-Fit Decreasing, O(n log n + n·k). Si encuentra asignación, es válida; si no la
 *    encuentra NO prueba que no exista (puede responder "no" en instancias que sí tienen solución).
 */
public class CargaFlota {

    private static void validar(int[] w, int k, int cap) {
        if (w == null) throw new IllegalArgumentException("Pesos nulos");
        if (k <= 0) throw new IllegalArgumentException("Número de camiones debe ser > 0");
        if (cap <= 0) throw new IllegalArgumentException("Capacidad debe ser > 0");
        for (int i = 0; i < w.length; i++)
            if (w[i] <= 0) throw new IllegalArgumentException("Peso del paquete " + i + " debe ser > 0");
    }

    /** Verificador del certificado. Devuelve null si es válido o un mensaje con el motivo del rechazo. */
    public static String verificar(int[] w, int k, int cap, int[] asignacion) {
        validar(w, k, cap);
        if (asignacion == null || asignacion.length != w.length) return "El certificado debe asignar exactamente un camión por paquete";
        long[] carga = new long[k];
        for (int i = 0; i < w.length; i++) {
            int b = asignacion[i];
            if (b < 0 || b >= k) return "Paquete " + i + " asignado a camión inexistente " + b;
            carga[b] += w[i];
        }
        for (int b = 0; b < k; b++) if (carga[b] > cap) return "Camión " + b + " excede capacidad: " + carga[b] + " > " + cap;
        return null;
    }

    /** Búsqueda exacta. Devuelve asignación o null si no existe. */
    public static int[] exacto(int[] w, int k, int cap) {
        validar(w, k, cap);
        int n = w.length;
        for (int x : w) if (x > cap) return null;
        // Ordenar índices por peso descendente mejora la poda (inserción: n pequeño).
        int[] orden = new int[n];
        for (int i = 0; i < n; i++) orden[i] = i;
        for (int i = 1; i < n; i++) { int t = orden[i], j = i - 1; while (j >= 0 && w[orden[j]] < w[t]) { orden[j + 1] = orden[j]; j--; } orden[j + 1] = t; }
        int[] carga = new int[k], asig = new int[n];
        return bt(0, orden, w, k, cap, carga, asig) ? asig : null;
    }

    private static boolean bt(int p, int[] orden, int[] w, int k, int cap, int[] carga, int[] asig) {
        if (p == orden.length) return true;
        int i = orden[p];
        for (int b = 0; b < k; b++) {
            if (carga[b] + w[i] > cap) continue;
            // Simetría: no probar un camión vacío si uno anterior también está vacío.
            boolean vacioRepetido = false;
            if (carga[b] == 0) for (int c = 0; c < b; c++) if (carga[c] == 0) { vacioRepetido = true; break; }
            if (vacioRepetido) continue;
            carga[b] += w[i]; asig[i] = b;
            if (bt(p + 1, orden, w, k, cap, carga, asig)) return true;
            carga[b] -= w[i];
        }
        return false;
    }

    /** First-Fit Decreasing. Devuelve asignación o null si la heurística no logra empacar en k camiones. */
    public static int[] ffd(int[] w, int k, int cap) {
        validar(w, k, cap);
        int n = w.length;
        int[] orden = new int[n];
        for (int i = 0; i < n; i++) orden[i] = i;
        ordenarDesc(orden, w, 0, n - 1, new int[n]);
        int[] carga = new int[k], asig = new int[n];
        for (int p = 0; p < n; p++) {
            int i = orden[p], b = 0;
            while (b < k && carga[b] + w[i] > cap) b++;
            if (b == k) return null;
            carga[b] += w[i]; asig[i] = b;
        }
        return asig;
    }

    private static void ordenarDesc(int[] a, int[] w, int lo, int hi, int[] aux) {
        if (lo >= hi) return;
        int m = (lo + hi) >>> 1;
        ordenarDesc(a, w, lo, m, aux); ordenarDesc(a, w, m + 1, hi, aux);
        int i = lo, j = m + 1, t = lo;
        while (i <= m && j <= hi) aux[t++] = w[a[i]] >= w[a[j]] ? a[i++] : a[j++];
        while (i <= m) aux[t++] = a[i++];
        while (j <= hi) aux[t++] = a[j++];
        for (t = lo; t <= hi; t++) a[t] = aux[t];
    }
}
