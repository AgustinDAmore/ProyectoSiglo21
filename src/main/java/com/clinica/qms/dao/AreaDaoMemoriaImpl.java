package com.clinica.qms.dao;

import com.clinica.qms.exception.DataAccessException;
import com.clinica.qms.model.Area;
import com.clinica.qms.model.Box;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Implementacion en memoria del AreaDao.
 * Configura areas y boxes predeterminados cuando MySQL no esta disponible.
 * Implementacion en memoria del AreaDao.
 */
public class AreaDaoMemoriaImpl implements AreaDao {

    // Datos iniciales de areas
    private static final String[] NOMBRES_AREAS  = {"Administracion", "Laboratorio", "Internacion"};
    private static final char[]   LETRAS_AREAS   = {'A', 'L', 'I'};
    private static final int[]    BOXES_POR_AREA = {4, 2, 1};

    private final ArrayList<Area> areas;

    public AreaDaoMemoriaImpl() {
        this.areas = new ArrayList<Area>();
        inicializarAreas();
    }

    private void inicializarAreas() {
        int idBox = 1;
        for (int i = 0; i < NOMBRES_AREAS.length; i++) {
            Area area = new Area(i + 1, NOMBRES_AREAS[i], LETRAS_AREAS[i]);
            for (int j = 1; j <= BOXES_POR_AREA[i]; j++) {
                area.agregarBox(new Box(idBox++, j, i + 1));
            }
            areas.add(area);
        }
    }

    @Override
    public ArrayList<Area> buscarTodasConBoxes() throws DataAccessException {
        return new ArrayList<Area>(areas);
    }

    @Override
    public ArrayList<Area> buscarTodos() throws DataAccessException {
        return buscarTodasConBoxes();
    }

    @Override
    public Optional<Area> buscarPorLetra(char letra) throws DataAccessException {
        for (Area a : areas) {
            if (a.getLetraIdentificadora() == letra) return Optional.of(a);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Area> buscarPorId(Integer id) throws DataAccessException {
        for (Area a : areas) {
            if (a.getId() == id) return Optional.of(a);
        }
        return Optional.empty();
    }

    @Override
    public Area guardar(Area entidad)    { throw new UnsupportedOperationException(); }

    @Override
    public void actualizar(Area entidad) { throw new UnsupportedOperationException(); }
}
