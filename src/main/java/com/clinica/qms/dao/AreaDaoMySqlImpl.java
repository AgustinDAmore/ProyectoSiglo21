package com.clinica.qms.dao;

import com.clinica.qms.db.ConexionDB;
import com.clinica.qms.exception.DataAccessException;
import com.clinica.qms.model.Area;
import com.clinica.qms.model.Box;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Implementacion MySQL del AreaDao. */
public class AreaDaoMySqlImpl extends AbstractDao<Area, Integer> implements AreaDao {

    public AreaDaoMySqlImpl(ConexionDB conexionDB) {
        super(conexionDB);
    }

    @Override
    public ArrayList<Area> buscarTodasConBoxes() throws DataAccessException {
        String sql = "SELECT a.id_area, a.nombre, a.letra_identificadora, "
                   + "       b.id_box, b.numero_identificador "
                   + "FROM area a "
                   + "LEFT JOIN box_atencion b ON b.id_area = a.id_area "
                   + "WHERE a.activa = 1 "
                   + "ORDER BY a.id_area, b.numero_identificador";

        Map<Integer, Area> mapaAreas = new LinkedHashMap<Integer, Area>();

        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql);
             ResultSet rs         = ps.executeQuery()) {

            while (rs.next()) {
                int    idArea = rs.getInt("id_area");
                String nombre = rs.getString("nombre");
                char   letra  = rs.getString("letra_identificadora").charAt(0);

                Area area = mapaAreas.get(idArea);
                if (area == null) {
                    area = new Area(idArea, nombre, letra);
                    mapaAreas.put(idArea, area);
                }

                int idBox = rs.getInt("id_box");
                if (!rs.wasNull()) {
                    area.agregarBox(new Box(idBox, rs.getInt("numero_identificador"), idArea));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error cargando areas y boxes: " + e.getMessage(), e);
        }

        return new ArrayList<Area>(mapaAreas.values());
    }

    @Override
    public ArrayList<Area> buscarTodos() throws DataAccessException {
        return buscarTodasConBoxes();
    }

    @Override
    public Optional<Area> buscarPorLetra(char letra) throws DataAccessException {
        ArrayList<Area> todas = buscarTodasConBoxes();
        for (Area a : todas) {
            if (a.getLetraIdentificadora() == letra) return Optional.of(a);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Area> buscarPorId(Integer id) throws DataAccessException {
        ArrayList<Area> todas = buscarTodasConBoxes();
        for (Area a : todas) {
            if (a.getId() == id) return Optional.of(a);
        }
        return Optional.empty();
    }

    @Override
    public Area guardar(Area entidad)   { throw new UnsupportedOperationException(); }

    @Override
    public void actualizar(Area entidad){ throw new UnsupportedOperationException(); }

    @Override
    protected Area mapearResultSet(ResultSet rs) throws SQLException {
        return new Area(rs.getInt("id_area"), rs.getString("nombre"),
                        rs.getString("letra_identificadora").charAt(0));
    }
}
