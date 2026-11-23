package logistpulse;

/**
 * Grafo ponderado con lista de adyacencia implementada con arrays y nodos enlazados propios
 * (sin colecciones de java.util). Los vértices se identifican por nombre y se mapean a índices 0..V-1.
 * Espacio: O(|V| + |E|).
 */
public class Grafo {

    /** Nodo de la lista de adyacencia: arista saliente u -> destino con peso no negativo. */
    public static class Arista {
        public final int destino;
        public final double peso;
        public final Arista siguiente;
        Arista(int destino, double peso, Arista siguiente) {
            this.destino = destino; this.peso = peso; this.siguiente = siguiente;
        }
    }

    private String[] nombres;
    private Arista[] adyacencia;
    private boolean[] bloqueado;
    private int numVertices;
    private int numAristas;
    private final boolean dirigido;

    public Grafo(boolean dirigido) {
        this.dirigido = dirigido;
        nombres = new String[8];
        adyacencia = new Arista[8];
        bloqueado = new boolean[8];
    }

    /** Devuelve el índice del vértice; lo crea si no existe. Búsqueda lineal O(V): suficiente para plantas pequeñas. */
    public int agregarVertice(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) throw new IllegalArgumentException("Nombre de vértice vacío");
        int i = indice(nombre);
        if (i >= 0) return i;
        if (numVertices == nombres.length) crecer();
        nombres[numVertices] = nombre.trim();
        return numVertices++;
    }

    private void crecer() {
        int n = nombres.length * 2;
        String[] nn = new String[n]; Arista[] na = new Arista[n]; boolean[] nb = new boolean[n];
        for (int i = 0; i < numVertices; i++) { nn[i] = nombres[i]; na[i] = adyacencia[i]; nb[i] = bloqueado[i]; }
        nombres = nn; adyacencia = na; bloqueado = nb;
    }

    public int indice(String nombre) {
        if (nombre == null) return -1;
        for (int i = 0; i < numVertices; i++) if (nombres[i].equals(nombre.trim())) return i;
        return -1;
    }

    /** Agrega un pasillo. Rechaza pesos negativos o no finitos (precondición de Dijkstra). */
    public void agregarArista(String u, String v, double peso) {
        if (Double.isNaN(peso) || Double.isInfinite(peso) || peso < 0)
            throw new IllegalArgumentException("Peso inválido en arista " + u + "-" + v + ": " + peso + " (debe ser finito y >= 0)");
        if (u.trim().equals(v.trim())) throw new IllegalArgumentException("Lazo no permitido en " + u);
        int a = agregarVertice(u), b = agregarVertice(v);
        adyacencia[a] = new Arista(b, peso, adyacencia[a]);
        if (!dirigido) adyacencia[b] = new Arista(a, peso, adyacencia[b]);
        numAristas++;
    }

    public void bloquear(String nombre) {
        int i = indice(nombre);
        if (i < 0) throw new IllegalArgumentException("Zona a bloquear no existe: " + nombre);
        bloqueado[i] = true;
    }

    public void desbloquearTodo() { for (int i = 0; i < numVertices; i++) bloqueado[i] = false; }

    public boolean estaBloqueado(int i) { return bloqueado[i]; }
    public Arista adyacentes(int i) { return adyacencia[i]; }
    public String nombre(int i) { return nombres[i]; }
    public int numVertices() { return numVertices; }
    public int numAristas() { return numAristas; }
    public boolean esDirigido() { return dirigido; }
}
