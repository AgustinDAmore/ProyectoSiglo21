package com.clinica.qms.dao;

import com.clinica.qms.db.ConexionDB;
import com.clinica.qms.exception.DataAccessException;
import com.clinica.qms.model.Paciente;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Optional;

/** Implementacion MySQL del PacienteDao. */
public class PacienteDaoMySqlImpl extends AbstractDao<Paciente, Integer> implements PacienteDao {

    public PacienteDaoMySqlImpl(ConexionDB conexionDB) {
        super(conexionDB);
    }

    @Override
    public Optional<Paciente> buscarPorDni(String dni) throws DataAccessException {
        String sql = "SELECT * FROM paciente WHERE dni = ?";
        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, dni);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapearResultSet(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error buscando paciente por DNI: " + e.getMessage(), e);
        }
    }

    @Override
    public Paciente guardar(Paciente paciente) throws DataAccessException {
        String sql = "INSERT INTO paciente(dni, nombre, apellido) VALUES(?, ?, ?)";
        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, paciente.getDni());
            ps.setString(2, paciente.getNombre());
            ps.setString(3, paciente.getApellido());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return new Paciente(keys.getInt(1), paciente.getNombre(),
                                       paciente.getApellido(), paciente.getDni());
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error guardando paciente: " + e.getMessage(), e);
        }
        return paciente;
    }

    @Override
    public void actualizar(Paciente paciente) throws DataAccessException {
        // Pacientes no se modifican en este sistema.
    }

    @Override
    public Optional<Paciente> buscarPorId(Integer id) throws DataAccessException {
        String sql = "SELECT * FROM paciente WHERE id_paciente = ?";
        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapearResultSet(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error buscando paciente por id: " + e.getMessage(), e);
        }
    }

    @Override
    public ArrayList<Paciente> buscarTodos() throws DataAccessException {
        String sql = "SELECT * FROM paciente ORDER BY apellido, nombre";
        ArrayList<Paciente> lista = new ArrayList<Paciente>();
        try (PreparedStatement ps = obtenerConexion().prepareStatement(sql);
             ResultSet rs         = ps.executeQuery()) {
            while (rs.next()) lista.add(mapearResultSet(rs));
        } catch (SQLException e) {
            throw new DataAccessException("Error buscando pacientes: " + e.getMessage(), e);
        }
        return lista;
    }

    @Override
    protected Paciente mapearResultSet(ResultSet rs) throws SQLException {
        return new Paciente(rs.getInt("id_paciente"), rs.getString("nombre"),
                            rs.getString("apellido"), rs.getString("dni"));
    }
}
