package com.clinica.qms.dao;

import com.clinica.qms.exception.DataAccessException;
import com.clinica.qms.model.Area;
import com.clinica.qms.model.Box;
import com.clinica.qms.model.EstadoTurno;
import com.clinica.qms.model.Paciente;
import com.clinica.qms.model.Turno;
import com.clinica.qms.db.ConexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Implementacion MySQL del TurnoDao.
 * Usa transacciones para generacion atomica de codigos.
 */
public class TurnoDaoMySqlImpl extends AbstractDao<Turno, Integer> implements TurnoDao {

    private final ArrayList<Area> areasDisponibles;

    public TurnoDaoMySqlImpl(ConexionDB conexionDB, ArrayList<Area> areas) {
        super(conexionDB);
        this.areasDisponibles = areas;
    }

    // ---------------------------------------------------------------
    // generarSiguienteCodigo: transaccion atomica sobre consecutivo_turno
    // ---------------------------------------------------------------
    @Override
    public String generarSiguienteCodigo(Area area) throws DataAccessException {
        String sqlUp  = "UPDATE consecutivo_turno SET ultimo_numero = ultimo_numero + 1 WHERE id_area = ?";
        String sqlSel = "SELECT ultimo_numero FROM consecutivo_turno WHERE id_area = ?";
        Connection conn = obtenerConexion();
        boolean autoCommitOriginal;
        try {
            autoCommitOriginal = conn.getAutoCommit();
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            throw new DataAccessException("Error al configurar transaccion", e);
        }
        try (PreparedStatement psUp  = conn.prepareStatement(sqlUp);
             PreparedStatement psSel = conn.prepareStatement(sqlSel)) {

            psUp.setInt(1, area.getId());
            psUp.executeUpdate();

            psSel.setInt(1, area.getId());
            try (ResultSet rs = psSel.executeQuery()) {
                if (rs.next()) {
                    int numero = rs.getInt("ultimo_numero");
                    conn.commit();
                    conn.setAutoCommit(autoCommitOriginal);
                    return String.format("%c-%03d", area.getLetraIdentificadora(), numero);
                }
            }
            conn.rollback();
            conn.setAutoCommit(autoCommitOriginal);
            throw new DataAccessException("Consecutivo no encontrado para area: " + area.getNombre(), null);

        } catch (SQLException e) {
            try { conn.rollback(); conn.setAutoCommit(true); } catch (SQLException ignored) {}
            throw new DataAccessException("Error al generar consecutivo: " + e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------------
    // guardar
    // ---------------------------------------------------------------
    @Override
    public Turno guardar(Turno turno) throws DataAccessException {
        String sql = "INSERT INTO turno(codigo, estado, prioridad, timestamp_creacion, id_paciente, id_area, area_origen) "
                   + "VALUES(?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, turno.getCodigo());
            ps.setString(2, turno.getEstado().name());
            ps.setInt(3, turno.getPrioridad());
            ps.setTimestamp(4, Timestamp.valueOf(turno.getTimestampCreacion()));
            ps.setInt(5, turno.getPaciente().getId());
            ps.setInt(6, turno.getAreaActual().getId());
            ps.setString(7, turno.getAreaOrigen());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    turno.setId(keys.getInt(1));
                }
            }
            return turno;
        } catch (SQLException e) {
            throw new DataAccessException("Error al guardar turno: " + e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------------
    // actualizar
    // ---------------------------------------------------------------
    @Override
    public void actualizar(Turno turno) throws DataAccessException {
        String sql = "UPDATE turno SET estado=?, prioridad=?, id_area=?, id_box=?, area_origen=?, "
                   + "timestamp_llamado=?, timestamp_inicio_aten=?, timestamp_fin_aten=? WHERE id_turno=?";
        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, turno.getEstado().name());
            ps.setInt(2, turno.getPrioridad());
            ps.setInt(3, turno.getAreaActual().getId());
            if (turno.getBoxAtencion() == null) {
                ps.setNull(4, java.sql.Types.INTEGER);
            } else {
                ps.setInt(4, turno.getBoxAtencion().getId());
            }
            ps.setString(5, turno.getAreaOrigen());
            ps.setTimestamp(6, turno.getTimestampLlamado()     == null ? null : Timestamp.valueOf(turno.getTimestampLlamado()));
            ps.setTimestamp(7, turno.getTimestampInicioAten()    == null ? null : Timestamp.valueOf(turno.getTimestampInicioAten()));
            ps.setTimestamp(8, turno.getTimestampFinAten()       == null ? null : Timestamp.valueOf(turno.getTimestampFinAten()));
            ps.setInt(9, turno.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error al actualizar turno: " + e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------------
    // buscarPorCodigo
    // ---------------------------------------------------------------
    @Override
    public Optional<Turno> buscarPorCodigo(String codigo) throws DataAccessException {
        String sql = "SELECT t.*, p.dni, p.nombre AS pac_nombre, p.apellido AS pac_apellido "
                   + "FROM turno t JOIN paciente p ON p.id_paciente = t.id_paciente "
                   + "WHERE t.codigo = ?";
        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearResultSet(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error buscando turno por codigo: " + e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------------
    // buscarEnEsperaPorArea
    // ---------------------------------------------------------------
    @Override
    public ArrayList<Turno> buscarEnEsperaPorArea(int idArea) throws DataAccessException {
        String sql = "SELECT t.*, p.dni, p.nombre AS pac_nombre, p.apellido AS pac_apellido "
                   + "FROM turno t JOIN paciente p ON p.id_paciente = t.id_paciente "
                   + "WHERE t.id_area = ? AND t.estado IN ('EN_ESPERA','DERIVADO') "
                   + "ORDER BY t.prioridad DESC, t.timestamp_creacion ASC";
        ArrayList<Turno> lista = new ArrayList<Turno>();
        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql)) {
            ps.setInt(1, idArea);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error buscando turnos en espera: " + e.getMessage(), e);
        }
        return lista;
    }

    // ---------------------------------------------------------------
    // buscarTodos
    // ---------------------------------------------------------------
    @Override
    public ArrayList<Turno> buscarTodos() throws DataAccessException {
        String sql = "SELECT t.*, p.dni, p.nombre AS pac_nombre, p.apellido AS pac_apellido "
                   + "FROM turno t JOIN paciente p ON p.id_paciente = t.id_paciente "
                   + "ORDER BY t.timestamp_creacion DESC";
        ArrayList<Turno> lista = new ArrayList<Turno>();
        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error buscando todos los turnos: " + e.getMessage(), e);
        }
        return lista;
    }

    // ---------------------------------------------------------------
    // buscarPorId
    // ---------------------------------------------------------------
    @Override
    public Optional<Turno> buscarPorId(Integer id) throws DataAccessException {
        String sql = "SELECT t.*, p.dni, p.nombre AS pac_nombre, p.apellido AS pac_apellido "
                   + "FROM turno t JOIN paciente p ON p.id_paciente = t.id_paciente "
                   + "WHERE t.id_turno = ?";
        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapearResultSet(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error buscando turno por id: " + e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------------
    // mapearResultSet (Template Method)
    // ---------------------------------------------------------------
    @Override
    protected Turno mapearResultSet(ResultSet rs) throws SQLException {
        int idArea = rs.getInt("id_area");
        Area area  = buscarAreaEnLista(idArea);

        int  idBox = rs.getInt("id_box");
        Box  box   = null;
        if (!rs.wasNull() && area != null) {
            box = buscarBoxEnArea(area, idBox);
        }

        Paciente paciente = new Paciente(
            rs.getInt("id_paciente"),
            rs.getString("pac_nombre"),
            rs.getString("pac_apellido"),
            rs.getString("dni")
        );

        Turno turno = new Turno(rs.getString("codigo"), paciente, area);
        turno.setId(rs.getInt("id_turno"));
        turno.setEstado(EstadoTurno.valueOf(rs.getString("estado")));
        turno.setPrioridad(rs.getInt("prioridad"));
        turno.setAreaOrigen(rs.getString("area_origen"));
        turno.setBoxAtencion(box);

        Timestamp tc = rs.getTimestamp("timestamp_creacion");
        if (tc != null) turno.setTimestampCreacion(tc.toLocalDateTime());

        Timestamp tl = rs.getTimestamp("timestamp_llamado");
        if (tl != null) turno.setTimestampLlamado(tl.toLocalDateTime());

        Timestamp ti = rs.getTimestamp("timestamp_inicio_aten");
        if (ti != null) turno.setTimestampInicioAten(ti.toLocalDateTime());

        Timestamp tf = rs.getTimestamp("timestamp_fin_aten");
        if (tf != null) turno.setTimestampFinAten(tf.toLocalDateTime());

        return turno;
    }

    private Area buscarAreaEnLista(int idArea) {
        for (Area a : areasDisponibles) {
            if (a.getId() == idArea) return a;
        }
        return null;
    }

    private Box buscarBoxEnArea(Area area, int idBox) {
        for (Box b : area.getBoxes()) {
            if (b.getId() == idBox) return b;
        }
        return null;
    }
}
