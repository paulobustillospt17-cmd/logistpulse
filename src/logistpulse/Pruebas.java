package logistpulse;

import java.time.LocalDate;

/**
 * Pruebas reproducibles sin dependencias externas: casos normales, límite e inválidos.
 * Ejecutar desde la raíz del proyecto:  java -cp out logistpulse.Pruebas
 * Termina con código 0 si todas pasan y 1 si alguna falla.
 */
public class Pruebas {
    static int ok = 0, fallos = 0;

    static void check(String nombre, boolean cond, String detalle) {
        if (cond) { ok++; System.out.println("[OK]    " + nombre + "  -> " + detalle); }
        else { fallos++; System.out.println("[FALLA] " + nombre + "  -> " + detalle); }
    }

    /** Verifica que la acción lance IllegalArgumentException (entrada inválida). */
    static void esperaError(String nombre, Runnable r) {
        try { r.run(); check(nombre, false, "no lanzó error"); }
        catch (IllegalArgumentException e) { check(nombre, true, "rechazado: " + e.getMessage()); }
    }

    interface Accion { void run() throws Exception; }
    static Runnable envolver(Accion a) {
        return () -> { try { a.run(); } catch (IllegalArgumentException e) { throw e; } catch (Exception e) { throw new RuntimeException(e); } };
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Autómatas ===");
        String[][] casos = {
            {"pedido","crpedg","1"},{"pedido","x","1"},{"pedido","crx","1"},
            {"pedido","d","0"},{"pedido","cpedg","0"},{"pedido","crpedgx","0"},{"pedido","cxr","0"},{"pedido","crped","0"},
            {"reserva","vcp","1"},{"reserva","vcx","1"},{"reserva","vct","1"},{"reserva","n","1"},
            {"reserva","p","0"},{"reserva","vp","0"},{"reserva","vcpx","0"},{"reserva","vc","0"},
            {"compra","saek","1"},{"compra","saerf","1"},{"compra","saerrrf","1"},{"compra","sj","1"},
            {"compra","sje","0"},{"compra","se","0"},{"compra","saekr","0"},{"compra","saer","0"},
            {"pedido","","0"},{"pedido","cz","0"}};
        for (String[] c : casos) {
            Afd m = c[0].equals("pedido") ? Afd.pedido() : c[0].equals("reserva") ? Afd.reserva() : Afd.compra();
            boolean esperado = c[2].equals("1");
            check("AFD " + c[0] + " '" + c[1] + "'", m.acepta(c[1]) == esperado, m.ejecutar(c[1]));
        }

        System.out.println("\n=== Dijkstra ===");
        Grafo g = LectorDatos.leerGrafo("datos/planta.txt");
        Dijkstra.Resultado r = Dijkstra.buscar(g, "R", "D");
        check("R->D costo 11", r.existe && r.costo == 11, r.rutaTexto() + " costo " + r.costo);
        check("R->D empate: 2 rutas óptimas", r.rutasOptimas == 2, "rutas óptimas = " + r.rutasOptimas);
        check("R->D desempate por menos aristas", r.rutaTexto().equals("R -> B -> A -> E -> D"), r.rutaTexto());
        Dijkstra.Resultado r2 = Dijkstra.buscar(g, "R", "P");
        check("R->P costo 6", r2.costo == 6, r2.rutaTexto() + " costo " + r2.costo);
        Dijkstra.Resultado r3 = Dijkstra.buscar(g, "D", "D");
        check("Límite: origen = destino", r3.existe && r3.costo == 0 && r3.ruta.length == 1, r3.rutaTexto());
        Grafo gb = LectorDatos.leerGrafo("datos/planta_bloqueo_A.txt");
        Dijkstra.Resultado r4 = Dijkstra.buscar(gb, "R", "D");
        check("Bloqueo A: R->D costo 12", r4.existe && r4.costo == 12, r4.rutaTexto() + " costo " + r4.costo + ", óptimas " + r4.rutasOptimas);
        gb.bloquear("E");
        Dijkstra.Resultado r5 = Dijkstra.buscar(gb, "R", "D");
        check("Límite: E bloqueado aísla D", !r5.existe, "sin ruta (D solo conecta con E)");
        Grafo gd = new Grafo(true);
        gd.agregarArista("X", "Y", 1); gd.agregarArista("Y", "Z", 1);
        check("Dirigido: Z->X sin ruta", !Dijkstra.buscar(gd, "Z", "X").existe, "arcos de un solo sentido");
        Grafo g0 = new Grafo(false);
        g0.agregarArista("U", "V", 0); g0.agregarArista("V", "W", 2);
        check("Límite: peso 0 permitido", Dijkstra.buscar(g0, "U", "W").costo == 2, "costo 2");
        esperaError("Inválido: peso negativo en archivo", envolver(() -> LectorDatos.leerGrafo("datos/planta_invalida.txt")));
        esperaError("Inválido: destino inexistente", () -> Dijkstra.buscar(g, "R", "Z"));
        esperaError("Inválido: peso NaN", () -> new Grafo(false).agregarArista("A", "B", Double.NaN));

        System.out.println("\n=== FEFO ===");
        LocalDate hoy = LocalDate.parse("2026-11-17");
        Fefo.Lote[] lotes = LectorDatos.leerLotes("datos/lotes.txt");
        Fefo.Plan p1 = Fefo.reservar(lotes, 100, hoy, Fefo.Politica.PARCIAL);
        check("FEFO 100 u. completo", p1.estado == Fefo.Estado.COMPLETO && p1.texto().equals("COMPLETO [L-104:40, L-101:30, L-103:25, L-102:5] pendiente=0"), p1.texto());
        Fefo.Plan p2 = Fefo.reservar(lotes, 30, hoy, Fefo.Politica.PARCIAL);
        check("FEFO 30 u. desde un solo lote", p2.lotes.length == 1 && p2.cantidades[0] == 30, p2.texto());
        check("FEFO excluye vencido, vence-hoy y sin stock", p1.texto().indexOf("L-099") < 0 && p1.texto().indexOf("L-105") < 0 && p1.texto().indexOf("L-106") < 0, "L-099, L-105, L-106 excluidos");
        Fefo.Plan p3 = Fefo.reservar(lotes, 200, hoy, Fefo.Politica.PARCIAL);
        check("Límite: stock insuficiente PARCIAL", p3.estado == Fefo.Estado.PARCIAL && p3.pendiente == 45, p3.texto());
        Fefo.Plan p4 = Fefo.reservar(lotes, 200, hoy, Fefo.Politica.RECHAZAR);
        check("Límite: stock insuficiente RECHAZAR", p4.estado == Fefo.Estado.RECHAZADO && p4.lotes.length == 0, p4.texto());
        Fefo.Plan p5 = Fefo.reservar(lotes, 155, hoy, Fefo.Politica.RECHAZAR);
        check("Límite: pedido = stock elegible exacto", p5.estado == Fefo.Estado.COMPLETO && p5.pendiente == 0, p5.texto());
        Fefo.Plan p6 = Fefo.reservar(new Fefo.Lote[0], 5, hoy, Fefo.Politica.PARCIAL);
        check("Límite: sin lotes", p6.estado == Fefo.Estado.PARCIAL && p6.pendiente == 5 && p6.lotes.length == 0, p6.texto());
        esperaError("Inválido: cantidad 0", () -> Fefo.reservar(lotes, 0, hoy, Fefo.Politica.PARCIAL));
        esperaError("Inválido: disponible negativo", () -> new Fefo.Lote("L-X", hoy, -3));
        esperaError("Inválido: fecha mal formada en archivo", envolver(() -> LectorDatos.leerLotes("datos/lotes_invalidos.txt")));

        System.out.println("\n=== Reposición ===");
        Reposicion.Sku[] s = LectorDatos.leerSkus("datos/skus.txt");
        int[] sug = Reposicion.sugerencias(s);
        check("Ejemplo del enunciado: posición 23, sugerencia 27", s[0].posicion() == 23 && sug[0] == 27, "SKU-001 -> " + sug[0]);
        check("Sobre el mínimo: 0", sug[1] == 0, "SKU-002 posición " + s[1].posicion());
        check("Sin OC abierta: 70", sug[2] == 70, "SKU-003 -> " + sug[2]);
        check("OC abierta evita duplicar", sug[3] == 0, "SKU-004 posición " + s[3].posicion() + " > mínimo");
        check("Límite: posición negativa (sobre-reserva)", s[4].posicion() == -15 && sug[4] == 55, "SKU-005 posición -15 -> 55");
        check("Límite: posición = mínimo = objetivo", sug[5] == 0, "SKU-006 -> max(0, 30-30) = 0");
        Reposicion.Sku tras = new Reposicion.Sku("SKU-001", 18, 10 + 27, 5, 30, 50);
        check("Idempotencia: tras crear la OC sugerida ya no sugiere", Reposicion.sugerencia(tras) == 0, "posición " + tras.posicion());
        esperaError("Inválido: objetivo < mínimo", () -> new Reposicion.Sku("X", 1, 0, 0, 30, 10));
        esperaError("Inválido: reservado negativo", () -> new Reposicion.Sku("X", 1, 0, -1, 0, 10));

        System.out.println("\n=== Carga de flota (NP-completo): verificador, exacto y heurística ===");
        Object[] d = LectorDatos.leerCarga("datos/carga.txt");
        int[] w = (int[]) d[0]; int k = (Integer) d[1], cap = (Integer) d[2]; int[] cert = (int[]) d[3];
        check("Certificado válido aceptado", CargaFlota.verificar(w, k, cap, cert) == null, "carga por camión = 10,10,10");
        String m1 = CargaFlota.verificar(w, k, cap, new int[]{0,0,1,1,2,2,2});
        check("Certificado con sobrecarga rechazado", m1 != null, m1);
        String m2 = CargaFlota.verificar(w, k, cap, new int[]{0,1,2,3,0,1,2});
        check("Certificado con camión inexistente rechazado", m2 != null, m2);
        String m3 = CargaFlota.verificar(w, k, cap, new int[]{0,1});
        check("Certificado incompleto rechazado", m3 != null, m3);
        int[] ex = CargaFlota.exacto(w, k, cap);
        check("Exacto encuentra solución y el verificador la acepta", ex != null && CargaFlota.verificar(w, k, cap, ex) == null, Main.arr(ex));
        check("FFD encuentra solución", CargaFlota.ffd(w, k, cap) != null, Main.arr(CargaFlota.ffd(w, k, cap)));
        Object[] d2 = LectorDatos.leerCarga("datos/carga_ffd_falla.txt");
        int[] w2 = (int[]) d2[0];
        check("Exacto: SÍ existe solución", CargaFlota.exacto(w2, 2, 10) != null, Main.arr(CargaFlota.exacto(w2, 2, 10)));
        check("FFD falla en la misma instancia (heurística sin garantía)", CargaFlota.ffd(w2, 2, 10) == null, "FFD no empaca en 2 camiones");
        check("Exacto: NO existe con suma > k·C", CargaFlota.exacto(new int[]{6,6,6}, 1, 10) == null, "18 > 10");
        check("Límite: paquete más pesado que C", CargaFlota.exacto(new int[]{11}, 5, 10) == null, "11 > 10");
        check("Límite: sin paquetes", CargaFlota.exacto(new int[0], 1, 10) != null, "asignación vacía válida");
        esperaError("Inválido: peso 0", () -> CargaFlota.verificar(new int[]{0}, 1, 10, new int[]{0}));
        esperaError("Inválido: 0 camiones", () -> CargaFlota.exacto(new int[]{1}, 0, 10));

        System.out.println("\nResumen: " + ok + " pruebas OK, " + fallos + " fallas");
        System.exit(fallos == 0 ? 0 : 1);
    }
}
