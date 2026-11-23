package logistpulse;

/**
 * Dijkstra con montículo binario y registro de predecesores.
 * Precondición: pesos >= 0 (lo garantiza Grafo.agregarArista).
 * Zonas bloqueadas: no se visitan ni se usan como paso intermedio. Si origen o destino están bloqueados, no hay ruta.
 * Desempate reproducible: entre rutas de igual costo se prefiere la de menos aristas (saltos);
 * además se cuenta cuántas rutas óptimas existen para informar empates.
 * Tiempo: O((|V| + |E|) log |V|). Espacio: O(|V| + |E|) (arrays O(V) + montículo con hasta O(E) entradas).
 * Terminación: cada vértice se cierra una sola vez y cada arista se relaja a lo sumo una vez por cierre,
 * por lo que el número de inserciones está acotado por |E| + 1 y el bucle termina.
 */
public class Dijkstra {

    public static class Resultado {
        public final boolean existe;
        public final double costo;
        public final String[] ruta;      // nombres en orden origen..destino
        public final long rutasOptimas;  // número de caminos de costo mínimo (empates)
        Resultado(boolean existe, double costo, String[] ruta, long rutasOptimas) {
            this.existe = existe; this.costo = costo; this.ruta = ruta; this.rutasOptimas = rutasOptimas;
        }
        public String rutaTexto() {
            if (!existe) return "(sin ruta)";
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < ruta.length; i++) { if (i > 0) sb.append(" -> "); sb.append(ruta[i]); }
            return sb.toString();
        }
    }

    private static final double EPS = 1e-9;

    public static Resultado buscar(Grafo g, String origen, String destino) {
        int s = g.indice(origen), t = g.indice(destino);
        if (s < 0) throw new IllegalArgumentException("Origen inexistente: " + origen);
        if (t < 0) throw new IllegalArgumentException("Destino inexistente: " + destino);
        if (g.estaBloqueado(s) || g.estaBloqueado(t)) return new Resultado(false, Double.POSITIVE_INFINITY, new String[0], 0);

        int n = g.numVertices();
        double[] dist = new double[n];
        int[] saltos = new int[n];
        int[] pred = new int[n];
        long[] cuenta = new long[n];
        boolean[] cerrado = new boolean[n];
        for (int i = 0; i < n; i++) { dist[i] = Double.POSITIVE_INFINITY; pred[i] = -1; saltos[i] = Integer.MAX_VALUE; }
        dist[s] = 0; saltos[s] = 0; cuenta[s] = 1;

        MonticuloMin cola = new MonticuloMin();
        cola.insertar(0, 0, s);
        while (!cola.estaVacio()) {
            double[] e = cola.extraerMin();
            int u = (int) e[2];
            if (cerrado[u]) continue;           // entrada obsoleta
            cerrado[u] = true;
            if (u == t) break;                   // la distancia al destino ya es definitiva
            for (Grafo.Arista a = g.adyacentes(u); a != null; a = a.siguiente) {
                int v = a.destino;
                if (cerrado[v] || g.estaBloqueado(v)) continue;
                double nd = dist[u] + a.peso;
                int nh = saltos[u] + 1;
                if (nd < dist[v] - EPS) {                       // mejora estricta
                    dist[v] = nd; saltos[v] = nh; pred[v] = u; cuenta[v] = cuenta[u];
                    cola.insertar(nd, nh, v);
                } else if (Math.abs(nd - dist[v]) <= EPS) {      // empate de costo
                    cuenta[v] += cuenta[u];
                    if (nh < saltos[v]) { saltos[v] = nh; pred[v] = u; cola.insertar(nd, nh, v); }
                }
            }
        }
        if (dist[t] == Double.POSITIVE_INFINITY) return new Resultado(false, dist[t], new String[0], 0);

        int largo = 0;
        for (int v = t; v != -1; v = pred[v]) largo++;
        String[] ruta = new String[largo];
        int k = largo - 1;
        for (int v = t; v != -1; v = pred[v]) ruta[k--] = g.nombre(v);
        return new Resultado(true, dist[t], ruta, cuenta[t]);
    }
}
