package logistpulse;

/**
 * Simulador de AFD con tabla de transiciones en arrays (filas = estados, columnas = símbolos).
 * Una celda -1 representa el sumidero implícito (transición no definida => rechazo), igual que en JFLAP.
 * Tiempo O(m) para una cadena de longitud m (búsqueda del símbolo O(|Σ|), constante para cada autómata).
 */
public class Afd {
    private final String[] estados;
    private final char[] simbolos;
    private final int[][] delta;
    private final boolean[] finales;
    private final int inicial;

    public Afd(String[] estados, char[] simbolos, int inicial, String[] finalesNombres) {
        this.estados = estados; this.simbolos = simbolos; this.inicial = inicial;
        delta = new int[estados.length][simbolos.length];
        for (int i = 0; i < estados.length; i++) for (int j = 0; j < simbolos.length; j++) delta[i][j] = -1;
        finales = new boolean[estados.length];
        for (String f : finalesNombres) finales[idx(f)] = true;
    }

    private int idx(String e) {
        for (int i = 0; i < estados.length; i++) if (estados[i].equals(e)) return i;
        throw new IllegalArgumentException("Estado inexistente: " + e);
    }

    private int sym(char c) {
        for (int j = 0; j < simbolos.length; j++) if (simbolos[j] == c) return j;
        return -1;
    }

    public Afd t(String desde, char s, String hacia) {
        int j = sym(s);
        if (j < 0) throw new IllegalArgumentException("Símbolo fuera de Σ: " + s);
        if (delta[idx(desde)][j] != -1) throw new IllegalArgumentException("No determinista en " + desde + "," + s);
        delta[idx(desde)][j] = idx(hacia);
        return this;
    }

    /** Devuelve "ACEPTA en X", "RECHAZA: ..." describiendo el motivo. */
    public String ejecutar(String cadena) {
        int q = inicial;
        for (int i = 0; i < cadena.length(); i++) {
            int j = sym(cadena.charAt(i));
            if (j < 0) return "RECHAZA: símbolo '" + cadena.charAt(i) + "' fuera de Σ";
            int nq = delta[q][j];
            if (nq == -1) return "RECHAZA: δ(" + estados[q] + "," + cadena.charAt(i) + ") no definida";
            q = nq;
        }
        return finales[q] ? "ACEPTA en " + estados[q] : "RECHAZA: termina en " + estados[q] + " (no final)";
    }

    public boolean acepta(String cadena) { return ejecutar(cadena).startsWith("ACEPTA"); }

    // ---- Los tres autómatas de LOGISTPULSE ----
    public static Afd pedido() {
        return new Afd(new String[]{"Borrador","Confirmado","Reservado","Preparacion","Empacado","Despachado","Entregado","Cancelado"},
                "crpedgx".toCharArray(), 0, new String[]{"Entregado","Cancelado"})
            .t("Borrador",'c',"Confirmado").t("Borrador",'x',"Cancelado")
            .t("Confirmado",'r',"Reservado").t("Confirmado",'x',"Cancelado")
            .t("Reservado",'p',"Preparacion").t("Reservado",'x',"Cancelado")
            .t("Preparacion",'e',"Empacado").t("Empacado",'d',"Despachado").t("Despachado",'g',"Entregado");
    }

    public static Afd reserva() {
        return new Afd(new String[]{"Solicitada","Validada","Reservada","Consumida","Liberada","Expirada","Rechazada"},
                "vncpxt".toCharArray(), 0, new String[]{"Consumida","Liberada","Expirada","Rechazada"})
            .t("Solicitada",'v',"Validada").t("Solicitada",'n',"Rechazada").t("Validada",'c',"Reservada")
            .t("Reservada",'p',"Consumida").t("Reservada",'x',"Liberada").t("Reservada",'t',"Expirada");
    }

    public static Afd compra() {
        return new Afd(new String[]{"Borrador","EnAprobacion","Aprobada","Enviada","RecepcionParcial","Recibida","Rechazada"},
                "sajerfk".toCharArray(), 0, new String[]{"Recibida","Rechazada"})
            .t("Borrador",'s',"EnAprobacion").t("EnAprobacion",'a',"Aprobada").t("EnAprobacion",'j',"Rechazada")
            .t("Aprobada",'e',"Enviada").t("Enviada",'k',"Recibida").t("Enviada",'r',"RecepcionParcial")
            .t("RecepcionParcial",'r',"RecepcionParcial").t("RecepcionParcial",'f',"Recibida");
    }
}
