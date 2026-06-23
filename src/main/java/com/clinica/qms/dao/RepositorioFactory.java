package com.clinica.qms.dao;

import com.clinica.qms.db.ConexionDB;
import com.clinica.qms.exception.DataAccessException;
import com.clinica.qms.model.Area;
import com.clinica.qms.service.GestorTurnos;
import com.clinica.qms.util.ConsolaUtil;

import java.util.ArrayList;

/**
 * Factory que intenta conectarse a MySQL.
 * Si falla, construye el GestorTurnos con implementaciones en memoria.
 * Principio abierto/cerrado: agregar nuevas fuentes sin modificar el servicio.
 */
public class RepositorioFactory {

    private RepositorioFactory() {}

    public static GestorTurnos crearGestorTurnos() {
        try {
            ConexionDB db = ConexionDB.getInstancia();

            AreaDaoMySqlImpl     areaDao     = new AreaDaoMySqlImpl(db);
            PacienteDaoMySqlImpl pacienteDao = new PacienteDaoMySqlImpl(db);
            ArrayList<Area>      areas       = areaDao.buscarTodasConBoxes();
            TurnoDaoMySqlImpl    turnoDao    = new TurnoDaoMySqlImpl(db, areas);

            ConsolaUtil.ok("Conexion a MySQL establecida. Base: clinica_qms_mod4.");
            return new GestorTurnos(turnoDao, pacienteDao, areas);

        } catch (DataAccessException e) {
            ConsolaUtil.aviso(e.getMessage());
            ConsolaUtil.info("Ejecutando en modo demo (datos en memoria, sin MySQL).");

            AreaDaoMemoriaImpl     areaDao     = new AreaDaoMemoriaImpl();
            PacienteDaoMemoriaImpl pacienteDao = new PacienteDaoMemoriaImpl();
            ArrayList<Area>        areas       = new ArrayList<Area>();
            try {
                areas = areaDao.buscarTodos();
            } catch (DataAccessException ignored) {}

            TurnoDaoMemoriaImpl turnoDao = new TurnoDaoMemoriaImpl();
            return new GestorTurnos(turnoDao, pacienteDao, areas);
        }
    }
}
