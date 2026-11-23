package logistpulse;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Capa de LECTURA: convierte archivos de texto en objetos del dominio y valida formato.
 * Líneas vacías y las que empiezan con '#' se ignoran. Los errores indican archivo y número de línea.
 */
public class LectorDatos {

    /** Lee todas las líneas útiles a un array propio (crece por duplicación). */
    static String[] lineas(String ruta) throws IOException {
        String[] buf = new String[16]; int n = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
            String s; int num = 0;
            while ((s = br.readLine()) != null) {
                num++;
                s = s.trim();
                if (s.isEmpty() || s.startsWith("#")) continue;
                if (n == buf.length) { String[] nb = new String[n * 2]; for (int i = 0; i < n; i++) nb[i] = buf[i]; buf = nb; }
                buf[n++] = num + "|" + s;   // guardamos el número de línea para mensajes de error
            }
        }
        String[] r = new String[n];
        for (int i = 0; i < n; i++) r[i] = buf[i];
        return r;
    }

    static int linea(String l) { return Integer.parseInt(l.substring(0, l.indexOf('|'))); }
    static String contenido(String l) { return l.substring(l.indexOf('|') + 1); }

    static int entero(String s, String ruta, int linea, String campo) {
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { throw new IllegalArgumentException(ruta + ":" + linea + ": " + campo + " no es entero: '" + s + "'"); }
    }

    /** Formato: "DIRIGIDO si|no" (primera directiva), "ARISTA u v peso", "BLOQUEADO zona". */
    public static Grafo leerGrafo(String ruta) throws IOException {
        String[] ls = lineas(ruta);
        boolean dirigido = false; int i = 0;
        if (ls.length > 0 && contenido(ls[0]).toUpperCase().startsWith("DIRIGIDO")) {
            String[] p = contenido(ls[0]).split("\\s+");
            dirigido = p.length > 1 && p[1].equalsIgnoreCase("si");
            i = 1;
        }
        Grafo g = new Grafo(dirigido);
        String[] bloqueos = new String[ls.length]; int nb = 0;
        for (; i < ls.length; i++) {
            String[] p = contenido(ls[i]).split("\\s+");
            int ln = linea(ls[i]);
            try {
                if (p[0].equalsIgnoreCase("ARISTA") && p.length == 4) g.agregarArista(p[1], p[2], Double.parseDouble(p[3]));
                else if (p[0].equalsIgnoreCase("BLOQUEADO") && p.length == 2) bloqueos[nb++] = p[1];
                else throw new IllegalArgumentException("directiva no reconocida");
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(ruta + ":" + ln + ": " + e.getMessage());
            }
        }
        for (int b = 0; b < nb; b++) g.bloquear(bloqueos[b]);
        return g;
    }

    /** Formato: codigo;AAAA-MM-DD;disponible */
    public static Fefo.Lote[] leerLotes(String ruta) throws IOException {
        String[] ls = lineas(ruta);
        Fefo.Lote[] r = new Fefo.Lote[ls.length];
        for (int i = 0; i < ls.length; i++) {
            String[] p = contenido(ls[i]).split(";");
            int ln = linea(ls[i]);
            if (p.length != 3) throw new IllegalArgumentException(ruta + ":" + ln + ": se esperaban 3 campos (codigo;fecha;disponible)");
            LocalDate f;
            try { f = LocalDate.parse(p[1].trim()); }
            catch (DateTimeParseException e) { throw new IllegalArgumentException(ruta + ":" + ln + ": fecha inválida '" + p[1] + "'"); }
            try { r[i] = new Fefo.Lote(p[0], f, entero(p[2], ruta, ln, "disponible")); }
            catch (IllegalArgumentException e) { throw new IllegalArgumentException(ruta + ":" + ln + ": " + e.getMessage()); }
        }
        return r;
    }

    /** Formato: codigo;disponible;enOrden;reservado;minimo;objetivo */
    public static Reposicion.Sku[] leerSkus(String ruta) throws IOException {
        String[] ls = lineas(ruta);
        Reposicion.Sku[] r = new Reposicion.Sku[ls.length];
        for (int i = 0; i < ls.length; i++) {
            String[] p = contenido(ls[i]).split(";");
            int ln = linea(ls[i]);
            if (p.length != 6) throw new IllegalArgumentException(ruta + ":" + ln + ": se esperaban 6 campos");
            try {
                r[i] = new Reposicion.Sku(p[0], entero(p[1], ruta, ln, "disponible"), entero(p[2], ruta, ln, "en_orden"),
                        entero(p[3], ruta, ln, "reservado"), entero(p[4], ruta, ln, "minimo"), entero(p[5], ruta, ln, "objetivo"));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(e.getMessage().startsWith(ruta) ? e.getMessage() : ruta + ":" + ln + ": " + e.getMessage());
            }
        }
        return r;
    }

    /** Lee una lista de enteros separados por comas. */
    public static int[] enteros(String csv, String ruta, int ln, String campo) {
        String[] p = csv.split(",");
        int[] r = new int[p.length];
        for (int i = 0; i < p.length; i++) r[i] = entero(p[i], ruta, ln, campo);
        return r;
    }

    /** Formato: CAPACIDAD c / CAMIONES k / PESOS w1,w2,... / CERTIFICADO b1,b2,... (opcional) */
    public static Object[] leerCarga(String ruta) throws IOException {
        String[] ls = lineas(ruta);
        int cap = -1, k = -1; int[] w = null, cert = null;
        for (String l : ls) {
            String[] p = contenido(l).split("\\s+", 2);
            int ln = linea(l);
            if (p.length < 2) throw new IllegalArgumentException(ruta + ":" + ln + ": falta valor");
            String clave = p[0].toUpperCase();
            if (clave.equals("CAPACIDAD")) cap = entero(p[1], ruta, ln, "capacidad");
            else if (clave.equals("CAMIONES")) k = entero(p[1], ruta, ln, "camiones");
            else if (clave.equals("PESOS")) w = enteros(p[1], ruta, ln, "peso");
            else if (clave.equals("CERTIFICADO")) cert = enteros(p[1], ruta, ln, "certificado");
            else throw new IllegalArgumentException(ruta + ":" + ln + ": clave desconocida " + p[0]);
        }
        if (cap < 0 || k < 0 || w == null) throw new IllegalArgumentException(ruta + ": faltan CAPACIDAD, CAMIONES o PESOS");
        return new Object[]{ w, k, cap, cert };
    }
}
