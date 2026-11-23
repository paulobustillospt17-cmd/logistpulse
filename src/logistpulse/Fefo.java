package logistpulse;

import java.time.LocalDate;

/**
 * Reserva FEFO (First Expired, First Out).
 * Elegible: vencimiento posterior a la fecha de referencia (un lote que vence hoy ya no se despacha) y disponible > 0.
 * Orden: (vencimiento ascendente, código de lote ascendente) -> desempate reproducible.
 * Ordenamiento: merge sort propio (estable), O(L log L) tiempo y O(L) espacio.
 * Asignación: un recorrido O(L). Total O(L log L) tiempo, O(L) espacio.
 * Política si no alcanza: PARCIAL reserva lo que hay; RECHAZAR no reserva nada (todo o nada).
 */
public class Fefo {

    public static class Lote {
        public final String codigo;
        public final LocalDate vencimiento;
        public final int disponible;
        public Lote(String codigo, LocalDate vencimiento, int disponible) {
            if (codigo == null || codigo.trim().isEmpty()) throw new IllegalArgumentException("Lote sin código");
            if (vencimiento == null) throw new IllegalArgumentException("Lote " + codigo + " sin fecha de vencimiento");
            if (disponible < 0) throw new IllegalArgumentException("Lote " + codigo + " con disponible negativo: " + disponible);
            this.codigo = codigo.trim(); this.vencimiento = vencimiento; this.disponible = disponible;
        }
    }

    public enum Politica { PARCIAL, RECHAZAR }
    public enum Estado { COMPLETO, PARCIAL, RECHAZADO }

    public static class Plan {
        public final Estado estado;
        public final String[] lotes;
        public final int[] cantidades;
        public final int pendiente;
        Plan(Estado estado, String[] lotes, int[] cantidades, int pendiente) {
            this.estado = estado; this.lotes = lotes; this.cantidades = cantidades; this.pendiente = pendiente;
        }
        public String texto() {
            StringBuilder sb = new StringBuilder(estado.toString()).append(" [");
            for (int i = 0; i < lotes.length; i++) { if (i > 0) sb.append(", "); sb.append(lotes[i]).append(':').append(cantidades[i]); }
            return sb.append("] pendiente=").append(pendiente).toString();
        }
    }

    public static Plan reservar(Lote[] lotes, int cantidadPedida, LocalDate hoy, Politica politica) {
        if (lotes == null) throw new IllegalArgumentException("Lista de lotes nula");
        if (hoy == null) throw new IllegalArgumentException("Fecha de referencia nula");
        if (cantidadPedida <= 0) throw new IllegalArgumentException("Cantidad pedida debe ser > 0: " + cantidadPedida);

        // 1) Filtrar candidatos: O(L)
        Lote[] cand = new Lote[lotes.length];
        int c = 0;
        for (Lote l : lotes) {
            if (l == null) throw new IllegalArgumentException("Lote nulo en la entrada");
            if (l.disponible > 0 && l.vencimiento.isAfter(hoy)) cand[c++] = l;
        }
        // 2) Ordenar: O(L log L)
        ordenar(cand, c);
        // 3) Asignar en orden: O(L)
        String[] nom = new String[c]; int[] cant = new int[c]; int k = 0;
        int resta = cantidadPedida;
        for (int i = 0; i < c && resta > 0; i++) {
            int tomar = Math.min(resta, cand[i].disponible);
            nom[k] = cand[i].codigo; cant[k] = tomar; k++;
            resta -= tomar;
        }
        if (resta > 0 && politica == Politica.RECHAZAR) return new Plan(Estado.RECHAZADO, new String[0], new int[0], cantidadPedida);
        String[] rn = new String[k]; int[] rc = new int[k];
        for (int i = 0; i < k; i++) { rn[i] = nom[i]; rc[i] = cant[i]; }
        return new Plan(resta == 0 ? Estado.COMPLETO : Estado.PARCIAL, rn, rc, resta);
    }

    static int comparar(Lote a, Lote b) {
        int r = a.vencimiento.compareTo(b.vencimiento);
        return r != 0 ? r : a.codigo.compareTo(b.codigo);
    }

    /** Merge sort sobre los primeros n elementos. */
    static void ordenar(Lote[] a, int n) {
        if (n < 2) return;
        Lote[] aux = new Lote[n];
        mergeSort(a, aux, 0, n - 1);
    }

    private static void mergeSort(Lote[] a, Lote[] aux, int lo, int hi) {
        if (lo >= hi) return;
        int mid = (lo + hi) >>> 1;
        mergeSort(a, aux, lo, mid);
        mergeSort(a, aux, mid + 1, hi);
        int i = lo, j = mid + 1, k = lo;
        while (i <= mid && j <= hi) aux[k++] = comparar(a[i], a[j]) <= 0 ? a[i++] : a[j++];
        while (i <= mid) aux[k++] = a[i++];
        while (j <= hi) aux[k++] = a[j++];
        for (k = lo; k <= hi; k++) a[k] = aux[k];
    }
}
