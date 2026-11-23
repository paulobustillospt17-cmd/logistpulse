package logistpulse;

import java.time.LocalDate;

/**
 * Capa de PRESENTACIÓN (línea de comandos). Lee con LectorDatos, procesa con las clases de dominio y muestra resultados.
 *
 * Uso:
 *   java -cp out logistpulse.Main ruta       <planta.txt> <origen> <destino> [zonaBloqueada]
 *   java -cp out logistpulse.Main fefo       <lotes.txt> <cantidad> <AAAA-MM-DD> [PARCIAL|RECHAZAR]
 *   java -cp out logistpulse.Main reposicion <skus.txt>
 *   java -cp out logistpulse.Main carga      <carga.txt>
 *   java -cp out logistpulse.Main afd        <pedido|reserva|compra> <cadena>
 */
public class Main {
    public static void main(String[] args) {
        if (args.length == 0) { uso(); System.exit(1); }
        try {
            switch (args[0]) {
                case "ruta": ruta(args); break;
                case "fefo": fefo(args); break;
                case "reposicion": reposicion(args); break;
                case "carga": carga(args); break;
                case "afd": afd(args); break;
                default: uso(); System.exit(1);
            }
        } catch (IllegalArgumentException e) {
            System.err.println("ERROR de entrada: " + e.getMessage()); System.exit(2);
        } catch (java.io.IOException e) {
            System.err.println("ERROR de lectura: " + e.getMessage()); System.exit(3);
        }
    }

    static void uso() {
        System.err.println("Uso: ruta <planta> <origen> <destino> [bloqueada] | fefo <lotes> <cant> <fecha> [PARCIAL|RECHAZAR]"
                + " | reposicion <skus> | carga <archivo> | afd <pedido|reserva|compra> <cadena>");
    }

    static void ruta(String[] a) throws java.io.IOException {
        if (a.length < 4) { uso(); return; }
        Grafo g = LectorDatos.leerGrafo(a[1]);
        if (a.length > 4) g.bloquear(a[4]);
        Dijkstra.Resultado r = Dijkstra.buscar(g, a[2], a[3]);
        System.out.println("Grafo " + (g.esDirigido() ? "dirigido" : "no dirigido") + ": |V|=" + g.numVertices() + ", |E|=" + g.numAristas());
        if (r.existe) System.out.println("Ruta: " + r.rutaTexto() + " | costo = " + fmt(r.costo) + " m | rutas óptimas = " + r.rutasOptimas);
        else System.out.println("No existe ruta de " + a[2] + " a " + a[3]);
    }

    static void fefo(String[] a) throws java.io.IOException {
        if (a.length < 4) { uso(); return; }
        Fefo.Lote[] lotes = LectorDatos.leerLotes(a[1]);
        int cant = LectorDatos.entero(a[2], "argumento", 0, "cantidad");
        Fefo.Politica pol = a.length > 4 ? Fefo.Politica.valueOf(a[4].toUpperCase()) : Fefo.Politica.PARCIAL;
        System.out.println("FEFO " + cant + " u. al " + a[3] + " (" + pol + "): " + Fefo.reservar(lotes, cant, LocalDate.parse(a[3]), pol).texto());
    }

    static void reposicion(String[] a) throws java.io.IOException {
        Reposicion.Sku[] s = LectorDatos.leerSkus(a[1]);
        int[] r = Reposicion.sugerencias(s);
        System.out.println("SKU        posicion  minimo  objetivo  sugerencia");
        for (int i = 0; i < s.length; i++)
            System.out.printf("%-10s %8d %7d %9d %11d%n", s[i].codigo, s[i].posicion(), s[i].minimo, s[i].objetivo, r[i]);
    }

    static void carga(String[] a) throws java.io.IOException {
        Object[] d = LectorDatos.leerCarga(a[1]);
        int[] w = (int[]) d[0]; int k = (Integer) d[1], cap = (Integer) d[2]; int[] cert = (int[]) d[3];
        if (cert != null) { String m = CargaFlota.verificar(w, k, cap, cert); System.out.println("Certificado: " + (m == null ? "VÁLIDO" : "INVÁLIDO - " + m)); }
        int[] ex = CargaFlota.exacto(w, k, cap), h = CargaFlota.ffd(w, k, cap);
        System.out.println("Exacto (backtracking): " + (ex == null ? "NO existe asignación" : "SÍ " + arr(ex)));
        System.out.println("Heurística FFD:        " + (h == null ? "no encontró asignación" : "SÍ " + arr(h)));
    }

    static void afd(String[] a) {
        Afd m = a[1].equals("pedido") ? Afd.pedido() : a[1].equals("reserva") ? Afd.reserva() : a[1].equals("compra") ? Afd.compra() : null;
        if (m == null) throw new IllegalArgumentException("Autómata desconocido: " + a[1]);
        System.out.println(a[1] + " '" + a[2] + "': " + m.ejecutar(a[2]));
    }

    static String arr(int[] x) { StringBuilder sb = new StringBuilder("["); for (int i = 0; i < x.length; i++) { if (i > 0) sb.append(','); sb.append(x[i]); } return sb.append(']').toString(); }
    static String fmt(double d) { return d == Math.floor(d) ? String.valueOf((long) d) : String.valueOf(d); }
}
