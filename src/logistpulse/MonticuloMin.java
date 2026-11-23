package logistpulse;

/**
 * Cola de prioridad mínima (montículo binario) sobre arrays propios.
 * Cada entrada es (prioridad, saltos, vértice). Se usa "inserción perezosa": en vez de decrease-key
 * se inserta una nueva entrada y las obsoletas se descartan al extraerlas.
 * insertar y extraerMin: O(log n). Con a lo sumo |E| inserciones, el tamaño es O(|E|).
 */
public class MonticuloMin {
    private double[] prioridad = new double[16];
    private int[] saltos = new int[16];
    private int[] vertice = new int[16];
    private int tam = 0;

    public boolean estaVacio() { return tam == 0; }
    public int tamano() { return tam; }

    /** Orden lexicográfico (distancia, saltos, índice de vértice) para que el resultado sea reproducible. */
    private boolean menor(int i, int j) {
        if (prioridad[i] != prioridad[j]) return prioridad[i] < prioridad[j];
        if (saltos[i] != saltos[j]) return saltos[i] < saltos[j];
        return vertice[i] < vertice[j];
    }

    private void intercambiar(int i, int j) {
        double p = prioridad[i]; prioridad[i] = prioridad[j]; prioridad[j] = p;
        int s = saltos[i]; saltos[i] = saltos[j]; saltos[j] = s;
        int v = vertice[i]; vertice[i] = vertice[j]; vertice[j] = v;
    }

    public void insertar(double p, int h, int v) {
        if (tam == prioridad.length) {
            int n = tam * 2;
            double[] np = new double[n]; int[] ns = new int[n]; int[] nv = new int[n];
            for (int i = 0; i < tam; i++) { np[i] = prioridad[i]; ns[i] = saltos[i]; nv[i] = vertice[i]; }
            prioridad = np; saltos = ns; vertice = nv;
        }
        prioridad[tam] = p; saltos[tam] = h; vertice[tam] = v;
        int i = tam++;
        while (i > 0 && menor(i, (i - 1) / 2)) { intercambiar(i, (i - 1) / 2); i = (i - 1) / 2; }
    }

    /** Devuelve {prioridad, saltos, vertice} de la mínima y la elimina. */
    public double[] extraerMin() {
        if (tam == 0) throw new IllegalStateException("Montículo vacío");
        double[] r = { prioridad[0], saltos[0], vertice[0] };
        tam--;
        if (tam > 0) {
            prioridad[0] = prioridad[tam]; saltos[0] = saltos[tam]; vertice[0] = vertice[tam];
            int i = 0;
            while (true) {
                int l = 2 * i + 1, d = l + 1, m = i;
                if (l < tam && menor(l, m)) m = l;
                if (d < tam && menor(d, m)) m = d;
                if (m == i) break;
                intercambiar(i, m); i = m;
            }
        }
        return r;
    }
}
