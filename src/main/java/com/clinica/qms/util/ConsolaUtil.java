package com.clinica.qms.util;

/**
 * Utilidades de consola: limpieza de pantalla y colores ANSI.
 * Compatible con CMD y PowerShell en Windows 10+.
 */
public final class ConsolaUtil {

    public static final String RESET    = "\u001B[0m";
    public static final String NEGRITA  = "\u001B[1m";
    public static final String ROJO     = "\u001B[91m";
    public static final String VERDE    = "\u001B[92m";
    public static final String AMARILLO = "\u001B[93m";
    public static final String AZUL     = "\u001B[94m";
    public static final String MAGENTA  = "\u001B[95m";
    public static final String CYAN     = "\u001B[96m";
    public static final String GRIS     = "\u001B[90m";
    public static final String BLANCO   = "\u001B[37m";

    private ConsolaUtil() {}

    public static void limpiarPantalla() {
        String os = System.getProperty("os.name", "").toLowerCase();
        try {
            if (os.contains("win")) {
                Process p = Runtime.getRuntime().exec(new String[] {"cmd", "/c", "cls"});
                p.waitFor();
            } else {
                System.out.print("\033[H\033[2J\033[3J");
                System.out.flush();
            }
        } catch (Exception ignored) {
            for (int i = 0; i < 40; i++) {
                System.out.println();
            }
        }
    }

    public static void println(String color, String mensaje) {
        System.out.println(color + mensaje + RESET);
    }

    public static void print(String color, String mensaje) {
        System.out.print(color + mensaje + RESET);
    }

    public static void ok(String mensaje) {
        println(VERDE + NEGRITA, "[OK] " + mensaje);
    }

    public static void error(String mensaje) {
        println(ROJO + NEGRITA, "[ERROR] " + mensaje);
    }

    public static void aviso(String mensaje) {
        println(AMARILLO + NEGRITA, "[!] " + mensaje);
    }

    public static void info(String mensaje) {
        println(CYAN, "[INFO] " + mensaje);
    }

    public static void titulo(String mensaje) {
        println(CYAN + NEGRITA, mensaje);
    }

    public static void subtitulo(String mensaje) {
        println(MAGENTA + NEGRITA, mensaje);
    }

    public static void detalle(String mensaje) {
        println(GRIS, "  " + mensaje);
    }

    public static void bannerPrincipal() {
        println(CYAN + NEGRITA, "╔══════════════════════════════════════════╗");
        println(CYAN + NEGRITA, "║   CLINICA MEDICA - SISTEMA QMS - MOD4   ║");
        println(CYAN + NEGRITA, "╚══════════════════════════════════════════╝");
    }

    public static void imprimirMenu(String[] opciones) {
        System.out.println();
        for (String op : opciones) {
            println(BLANCO, "  " + op);
        }
    }
}
