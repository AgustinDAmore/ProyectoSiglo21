package com.clinica.qms.db;

import com.clinica.qms.exception.DataAccessException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Patron Singleton.
 * Garantiza una unica instancia de conexion JDBC durante toda la ejecucion.
 */
public class ConexionDB {

    // ---------- Configuracion de conexion MySQL/XAMPP ----------
    public static final String URL      = "jdbc:mysql://localhost:3306/clinica_qms_mod4?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    public static final String USUARIO  = "root";
    public static final String PASSWORD = "";
    // -----------------------------------------------------------

    private static ConexionDB instancia;
    private Connection conexion;

    /** Constructor privado: impide instanciacion directa (Singleton). */
    private ConexionDB() throws DataAccessException {
        conectar();
    }

    private void conectar() throws DataAccessException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.conexion = DriverManager.getConnection(URL, USUARIO, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new DataAccessException(
                "Driver MySQL no encontrado. Descargue mysql-connector-j.jar y agréguelo al classpath.", e);
        } catch (SQLException e) {
            String detalle = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            throw new DataAccessException(
                "No se pudo conectar a MySQL en " + URL + ". " + detalle
                + " (Verifique XAMPP/MySQL y que importo db/clinica_qms_mod4.sql).", e);
        }
    }

    /**
     * Devuelve la unica instancia (lazy initialization).
     * Si la conexion fue cerrada, la reabre.
     */
    public static ConexionDB getInstancia() throws DataAccessException {
        try {
            if (instancia == null || instancia.conexion.isClosed()) {
                instancia = new ConexionDB();
            }
        } catch (SQLException e) {
            instancia = new ConexionDB();
        }
        return instancia;
    }

    public Connection getConexion() {
        return conexion;
    }

    public void cerrar() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
            }
        } catch (SQLException ignored) {
            // cierre silencioso
        }
    }
}
