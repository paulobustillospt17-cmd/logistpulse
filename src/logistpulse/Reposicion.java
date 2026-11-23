package logistpulse;

/**
 * Regla de punto de pedido.
 * posicion = disponible + enOrden - reservado ; si posicion <= minimo: sugerir max(0, objetivo - posicion), si no 0.
 * Evitar duplicados: "enOrden" suma lo pendiente de recibir de toda OC abierta (estados EnAprobacion, Aprobada,
 * Enviada y RecepcionParcial del autómata de compra). Así, en cuanto se crea la OC sugerida la posición sube y una
 * segunda ejecución ya no vuelve a sugerir (la regla es idempotente).
 * Tiempo O(P) para P productos; O(1) espacio adicional por SKU, O(P) si se guardan todas las sugerencias.
 */
public class Reposicion {

    public static class Sku {
        public final String codigo;
        public final int disponible, enOrden, reservado, minimo, objetivo;
        public Sku(String codigo, int disponible, int enOrden, int reservado, int minimo, int objetivo) {
            if (codigo == null || codigo.trim().isEmpty()) throw new IllegalArgumentException("SKU sin código");
            if (disponible < 0 || enOrden < 0 || reservado < 0)
                throw new IllegalArgumentException("SKU " + codigo + ": cantidades no pueden ser negativas");
            if (minimo < 0 || objetivo < minimo)
                throw new IllegalArgumentException("SKU " + codigo + ": se requiere 0 <= minimo <= objetivo");
            this.codigo = codigo.trim(); this.disponible = disponible; this.enOrden = enOrden;
            this.reservado = reservado; this.minimo = minimo; this.objetivo = objetivo;
        }
        public int posicion() { return disponible + enOrden - reservado; }
    }

    public static int sugerencia(Sku s) {
        int pos = s.posicion();
        return pos <= s.minimo ? Math.max(0, s.objetivo - pos) : 0;
    }

    /** Procesa P productos; devuelve array de sugerencias (salida O(P)). */
    public static int[] sugerencias(Sku[] skus) {
        if (skus == null) throw new IllegalArgumentException("Lista de SKU nula");
        int[] r = new int[skus.length];
        for (int i = 0; i < skus.length; i++) {
            if (skus[i] == null) throw new IllegalArgumentException("SKU nulo en posición " + i);
            r[i] = sugerencia(skus[i]);
        }
        return r;
    }
}
