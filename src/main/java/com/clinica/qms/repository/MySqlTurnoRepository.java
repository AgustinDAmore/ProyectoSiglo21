package com.clinica.qms.repository;

import com.clinica.qms.exception.DataAccessException;
import com.clinica.qms.model.Area;
import com.clinica.qms.model.Box;
import com.clinica.qms.model.EstadoTurno;
import com.clinica.qms.model.Paciente;
import com.clinica.qms.model.Turno;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MySqlTurnoRepository implements TurnoRepository {
    // Implementacion para persistir turnos en MySQL.
    private final String url;
    private final String user;
    private final String password;

    public MySqlTurnoRepository(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    @Override
    public Turno save(Turno turno) {
        // Inserta un turno nuevo en la tabla turno.
        String sql = "INSERT INTO turno(codigo, estado, prioridad, id_paciente, id_area, id_box, area_origen) VALUES(?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            int idPaciente = asegurarPaciente(conn, turno.getPaciente());
            int idArea = asegurarArea(conn, turno.getAreaActual());

            ps.setString(1, turno.getCodigo());
            ps.setString(2, turno.getEstado().name());
            ps.setInt(3, turno.getPrioridad());
            ps.setInt(4, idPaciente);
            ps.setInt(5, idArea);
            if (turno.getBoxAtencion() == null) {
                ps.setNull(6, java.sql.Types.INTEGER);
            } else {
                ps.setInt(6, turno.getBoxAtencion().getId());
            }
            ps.setString(7, turno.getAreaOrigen());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error al guardar turno en MySQL", e);
        }

        return turno;
    }

    @Override
    public void update(Turno turno) {
        // Actualiza los campos operativos del turno.
        String sql = "UPDATE turno SET estado = ?, prioridad = ?, id_area = ?, id_box = ?, area_origen = ?, timestamp_llamado = ? WHERE codigo = ?";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, turno.getEstado().name());
            ps.setInt(2, turno.getPrioridad());
            ps.setInt(3, turno.getAreaActual().getId());
            if (turno.getBoxAtencion() == null) {
                ps.setNull(4, java.sql.Types.INTEGER);
            } else {
                ps.setInt(4, turno.getBoxAtencion().getId());
            }
            ps.setString(5, turno.getAreaOrigen());
            if (turno.getTimestampLlamado() == null) {
                ps.setNull(6, java.sql.Types.TIMESTAMP);
            } else {
                ps.setTimestamp(6, java.sql.Timestamp.valueOf(turno.getTimestampLlamado()));
            }
            ps.setString(7, turno.getCodigo());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error al actualizar turno en MySQL", e);
        }
    }

    @Override
    public Optional<Turno> findByCodigo(String codigo) {
        String sql = "SELECT t.id_turno, t.codigo, t.estado, t.prioridad, t.timestamp_creacion, t.timestamp_llamado, "
            + "t.timestamp_inicio_atencion, t.timestamp_fin_atencion, t.area_origen, "
            + "p.id_paciente, p.dni, p.nombre, p.apellido, "
            + "a.id_area, a.nombre AS area_nombre, a.letra_identificadora, "
            + "b.id_box, b.numero_identificador "
            + "FROM turno t "
            + "JOIN paciente p ON p.id_paciente = t.id_paciente "
            + "JOIN area a ON a.id_area = t.id_area "
            + "LEFT JOIN box_atencion b ON b.id_box = t.id_box "
            + "WHERE t.codigo = ? LIMIT 1";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapTurno(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al buscar turno en MySQL", e);
        }

        return Optional.empty();
    }

    @Override
    public List<Turno> findAll() {
        String sql = "SELECT t.id_turno, t.codigo, t.estado, t.prioridad, t.timestamp_creacion, t.timestamp_llamado, "
            + "t.timestamp_inicio_atencion, t.timestamp_fin_atencion, t.area_origen, "
            + "p.id_paciente, p.dni, p.nombre, p.apellido, "
            + "a.id_area, a.nombre AS area_nombre, a.letra_identificadora, "
            + "b.id_box, b.numero_identificador "
            + "FROM turno t "
            + "JOIN paciente p ON p.id_paciente = t.id_paciente "
            + "JOIN area a ON a.id_area = t.id_area "
            + "LEFT JOIN box_atencion b ON b.id_box = t.id_box "
            + "ORDER BY t.timestamp_creacion ASC";

        List<Turno> turnos = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                turnos.add(mapTurno(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al listar turnos en MySQL", e);
        }
        return turnos;
    }

    private Turno mapTurno(ResultSet rs) throws SQLException {
        String nombrePaciente = rs.getString("nombre");
        String apellidoPaciente = rs.getString("apellido");
        String nombreCompleto = (nombrePaciente + " " + apellidoPaciente).trim();

        Paciente paciente = new Paciente(
            rs.getInt("id_paciente"),
            nombreCompleto,
            rs.getString("dni")
        );

        Area area = new Area(
            rs.getInt("id_area"),
            rs.getString("area_nombre"),
            rs.getString("letra_identificadora").charAt(0)
        );

        Box box = null;
        int idBox = rs.getInt("id_box");
        if (!rs.wasNull()) {
            box = new Box(idBox, rs.getInt("numero_identificador"));
        }

        Turno turno = new Turno(
            rs.getInt("id_turno"),
            rs.getString("codigo"),
            paciente,
            area
        );

        EstadoTurno estado = EstadoTurno.valueOf(rs.getString("estado"));
        LocalDateTime timestampCreacion = aLocalDateTime(rs.getTimestamp("timestamp_creacion"));
        LocalDateTime timestampLlamado = aLocalDateTime(rs.getTimestamp("timestamp_llamado"));
        LocalDateTime timestampInicioAtencion = aLocalDateTime(rs.getTimestamp("timestamp_inicio_atencion"));
        LocalDateTime timestampFinAtencion = aLocalDateTime(rs.getTimestamp("timestamp_fin_atencion"));

        turno.rehidratarDesdePersistencia(
            estado,
            rs.getInt("prioridad"),
            timestampCreacion,
            timestampLlamado,
            timestampInicioAtencion,
            timestampFinAtencion,
            rs.getString("area_origen"),
            box,
            area
        );

        return turno;
    }

    private LocalDateTime aLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private int asegurarPaciente(Connection conn, Paciente paciente) throws SQLException {
        String select = "SELECT id_paciente FROM paciente WHERE dni = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(select)) {
            ps.setString(1, paciente.getDni());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        String[] partes = paciente.getNombre().trim().split("\\s+", 2);
        String nombre = partes.length > 0 ? partes[0] : "Paciente";
        String apellido = partes.length > 1 ? partes[1] : "SinApellido";

        String insert = "INSERT INTO paciente(dni, nombre, apellido) VALUES(?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, paciente.getDni());
            ps.setString(2, nombre);
            ps.setString(3, apellido);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        throw new SQLException("No se pudo obtener id_paciente");
    }

    private int asegurarArea(Connection conn, Area area) throws SQLException {
        String selectById = "SELECT id_area FROM area WHERE id_area = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(selectById)) {
            ps.setInt(1, area.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        String selectByNombre = "SELECT id_area FROM area WHERE nombre = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(selectByNombre)) {
            ps.setString(1, area.getNombre());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        String insert = "INSERT INTO area(nombre, letra_identificadora, activa) VALUES(?, ?, 1)";
        try (PreparedStatement ps = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, area.getNombre());
            ps.setString(2, String.valueOf(area.getLetraIdentificadora()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        throw new SQLException("No se pudo obtener id_area");
    }

    private Connection getConnection() throws SQLException {
        // Conexion directa usando los datos del constructor.
        return DriverManager.getConnection(url, user, password);
    }
}
