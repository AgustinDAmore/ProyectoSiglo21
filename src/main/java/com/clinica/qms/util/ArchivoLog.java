package com.clinica.qms.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Manejo de archivos para el log de auditoria del sistema.
 * Escribe en modo append y permite lectura de ultimas entradas.
 */
public class ArchivoLog {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String rutaArchivo;

    public ArchivoLog() {
        this("logs/qms_audit.log");
    }

    public ArchivoLog(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
        crearDirectorioSiNoExiste();
    }

    private void crearDirectorioSiNoExiste() {
        File archivo = new File(rutaArchivo);
        File directorio = archivo.getParentFile();
        if (directorio != null && !directorio.exists()) {
            directorio.mkdirs();
        }
    }

    /**
     * Registra una entrada en el log con timestamp.
     * Usa FileWriter en modo append y BufferedWriter para eficiencia.
     */
    public void registrar(String mensaje) {
        String entrada = "[" + LocalDateTime.now().format(FORMATO) + "] " + mensaje;
        try (FileWriter fw = new FileWriter(rutaArchivo, true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(entrada);
            bw.newLine();
        } catch (IOException e) {
            System.err.println("[LOG ERROR] No se pudo escribir en " + rutaArchivo + ": " + e.getMessage());
        }
    }

    /**
     * Lee todas las entradas del log.
     * Usa BufferedReader para lectura eficiente linea a linea.
     */
    public ArrayList<String> leerEntradas() {
        ArrayList<String> entradas = new ArrayList<String>();
        File archivo = new File(rutaArchivo);
        if (!archivo.exists()) return entradas;

        try (FileReader fr = new FileReader(archivo);
             BufferedReader br = new BufferedReader(fr)) {
            String linea;
            while ((linea = br.readLine()) != null) {
                entradas.add(linea);
            }
        } catch (IOException e) {
            System.err.println("[LOG ERROR] No se pudo leer " + rutaArchivo + ": " + e.getMessage());
        }
        return entradas;
    }

    /** Devuelve las ultimas N entradas del log. */
    public ArrayList<String> leerUltimas(int cantidad) {
        ArrayList<String> todas = leerEntradas();
        int inicio = Math.max(0, todas.size() - cantidad);
        ArrayList<String> ultimas = new ArrayList<String>();
        for (int i = inicio; i < todas.size(); i++) {
            ultimas.add(todas.get(i));
        }
        return ultimas;
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }
}
