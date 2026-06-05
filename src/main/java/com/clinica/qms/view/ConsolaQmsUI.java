package com.clinica.qms.view;

import com.clinica.qms.exception.BusinessException;
import com.clinica.qms.model.Area;
import com.clinica.qms.model.Box;
import com.clinica.qms.model.Operador;
import com.clinica.qms.model.Paciente;
import com.clinica.qms.model.Turno;
import com.clinica.qms.repository.InMemoryTurnoRepository;
import com.clinica.qms.repository.MySqlTurnoRepository;
import com.clinica.qms.repository.TurnoRepository;
import com.clinica.qms.service.GestorTurnos;
import com.clinica.qms.service.MetricasServicio;
import com.clinica.qms.util.PasswordHashUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConsolaQmsUI {

    private final Scanner scanner;
    private final GestorTurnos gestorTurnos;
    private final MetricasServicio metricasServicio;
    private final Map<Integer, Area> areas;
    private final Map<String, Operador> operadoresPorUsuario;
    private final Map<String, String> passwordHashPorUsuario;
    private final List<String> ultimosLlamados;
    private final boolean persistenciaMySql;
    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;
    private int secuenciaPaciente;

    public ConsolaQmsUI() {
        this.scanner = new Scanner(System.in);
        this.persistenciaMySql = "mysql".equalsIgnoreCase(leerEnvConDefault("QMS_REPO", "memory"));
        this.dbUrl = leerEnvConDefault("QMS_DB_URL", "jdbc:mysql://localhost:3306/clinica_qms");
        this.dbUser = leerEnvConDefault("QMS_DB_USER", "root");
        this.dbPassword = leerEnvConDefault("QMS_DB_PASSWORD", "");
        this.gestorTurnos = new GestorTurnos(crearRepositorioTurnos());
        this.metricasServicio = new MetricasServicio();
        this.areas = new LinkedHashMap<Integer, Area>();
        this.operadoresPorUsuario = new LinkedHashMap<String, Operador>();
        this.passwordHashPorUsuario = new LinkedHashMap<String, String>();
        this.ultimosLlamados = new ArrayList<String>();
        this.secuenciaPaciente = 1;
        inicializarDatos();
    }

    public void iniciar() {
        // Loop principal de la app por consola.
        System.out.println("==============================================");
        System.out.println(" QMS CLINICA - INTERFAZ DE CONSOLA INTERACTIVA");
        System.out.println("==============================================");

        boolean continuar = true;
        while (continuar) {
            imprimirMenuPrincipal();
            int opcion = leerEntero("Seleccione una opcion: ");

            switch (opcion) {
                case 1:
                    flujoAutogestion();
                    break;
                case 2:
                    flujoLoginOperador();
                    break;
                case 3:
                    mostrarPantallaSalaEspera();
                    break;
                case 4:
                    menuAdministracionCatalogo();
                    break;
                case 0:
                    continuar = false;
                    break;
                default:
                    System.out.println("Opcion invalida.");
            }
        }

        System.out.println("Sistema finalizado.");
    }

    private void imprimirMenuPrincipal() {
        System.out.println("\n--- MENU PRINCIPAL ---");
        System.out.println("1. Terminal de autogestion (paciente)");
        System.out.println("2. Login de operador");
        System.out.println("3. Ver pantalla sala de espera");
        System.out.println("4. ABM de areas y boxes");
        System.out.println("0. Salir");
    }

    private void flujoAutogestion() {
        // Flujo simple para que un paciente saque turno.
        System.out.println("\n--- AUTOGESTION ---");
        String dni = leerTexto("DNI: ");
        String nombre = leerTexto("Nombre y apellido: ");
        Area area = seleccionarArea("Seleccione area para el turno");

        Paciente paciente = new Paciente(secuenciaPaciente++, nombre, dni);
        Turno turno = gestorTurnos.solicitarTurno(paciente, area);

        System.out.println("Turno generado correctamente: " + turno.getCodigo() + " (Area " + area.getNombre() + ")");
    }

    private void flujoLoginOperador() {
        // Login de operador con usuario y password.
        System.out.println("\n--- LOGIN OPERADOR ---");
        String usuario = leerTexto("Usuario: ");
        String password = leerTexto("Password: ");
        Operador operador = operadoresPorUsuario.get(usuario.toLowerCase());
        String hash = passwordHashPorUsuario.get(usuario.toLowerCase());

        if (operador == null || hash == null || !PasswordHashUtil.verify(password, hash)) {
            System.out.println("Credenciales invalidas.");
            return;
        }

        System.out.println("Bienvenido/a " + operador.getNombre() + " - Area: " + operador.getAreaAsignada().getNombre());
        Box box = seleccionarBoxOperador(operador);
        operador.seleccionarBox(box);
        System.out.println("Sesion iniciada en " + box + ".");

        menuOperador(operador);
    }

    private void menuOperador(Operador operador) {
        // Menu de acciones del operador.
        boolean continuar = true;

        while (continuar) {
            System.out.println("\n--- MENU OPERADOR (" + operador.getUsuario() + ") ---");
            System.out.println("1. Ver lista de espera de mi area");
            System.out.println("2. Llamar siguiente paciente");
            System.out.println("3. Derivar paciente a otra area");
            System.out.println("4. Cerrar atencion actual");
            System.out.println("5. Ver metricas");
            System.out.println("0. Cerrar sesion");

            int opcion = leerEntero("Seleccione una opcion: ");

            try {
                switch (opcion) {
                    case 1:
                        verColaArea(operador.getAreaAsignada());
                        break;
                    case 2:
                        llamarSiguiente(operador);
                        break;
                    case 3:
                        derivarPaciente();
                        break;
                    case 4:
                        cerrarAtencion(operador);
                        break;
                    case 5:
                        mostrarMetricas();
                        break;
                    case 0:
                        continuar = false;
                        break;
                    default:
                        System.out.println("Opcion invalida.");
                }
            } catch (BusinessException ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }

    private void verColaArea(Area area) {
        List<Turno> cola = gestorTurnos.listarEsperaPorArea(area);
        if (cola.isEmpty()) {
            System.out.println("No hay pacientes en espera en " + area.getNombre());
            return;
        }

        System.out.println("Lista de espera de " + area.getNombre() + ":");
        for (Turno turno : cola) {
            System.out.println("- " + turno.getCodigo() + " | Prioridad: " + turno.getPrioridad()
                + " | Estado: " + turno.getEstado());
        }
    }

    private void llamarSiguiente(Operador operador) {
        Optional<Turno> llamado = gestorTurnos.llamarSiguiente(operador.getAreaAsignada(), operador.getBoxSeleccionado());
        if (!llamado.isPresent()) {
            System.out.println("No hay pacientes en espera para llamar.");
            return;
        }

        Turno turno = llamado.get();
        // Guardamos los llamados recientes para la pantalla de sala de espera.
        String mensajePantalla = "Turno " + turno.getCodigo() + " dirigirse a " + operador.getBoxSeleccionado();
        ultimosLlamados.add(mensajePantalla);
        if (ultimosLlamados.size() > 10) {
            ultimosLlamados.remove(0);
        }

        System.out.println("Llamado realizado: " + mensajePantalla);
    }

    private void derivarPaciente() {
        String codigo = leerTexto("Ingrese codigo de turno a derivar (ej: A-001): ");
        Area destino = seleccionarArea("Seleccione area de destino");

        Turno derivado = gestorTurnos.derivarTurno(codigo, destino);
        System.out.println("Turno derivado: " + derivado.getCodigo() + " -> " + destino.getNombre()
            + " (prioridad alta)");
    }

    private void cerrarAtencion(Operador operador) {
        Turno cerrado = gestorTurnos.cerrarAtencion(operador.getBoxSeleccionado());
        System.out.println("Atencion cerrada para turno " + cerrado.getCodigo()
            + " en " + operador.getBoxSeleccionado());
    }

    private void mostrarMetricas() {
        List<Turno> historico = gestorTurnos.listarHistorico();
        if (historico.isEmpty()) {
            System.out.println("No hay datos para metricas.");
            return;
        }

        double esperaProm = metricasServicio.calcularEsperaPromedioSegundos(historico);
        double atencionProm = metricasServicio.calcularAtencionPromedioSegundos(historico);

        System.out.println("Metricas globales:");
        System.out.println("- Espera promedio (seg): " + String.format("%.2f", esperaProm));
        System.out.println("- Atencion promedio (seg): " + String.format("%.2f", atencionProm));
        System.out.println("- Pacientes registrados: " + historico.size());
    }

    private void mostrarPantallaSalaEspera() {
        System.out.println("\n=== PANTALLA SALA DE ESPERA ===");
        if (ultimosLlamados.isEmpty()) {
            System.out.println("Sin llamados recientes.");
            return;
        }

        for (String llamado : ultimosLlamados) {
            System.out.println("* " + llamado);
        }
    }

    private Area seleccionarArea(String titulo) {
        System.out.println(titulo + ":");
        for (Map.Entry<Integer, Area> entry : areas.entrySet()) {
            System.out.println(entry.getKey() + ". " + entry.getValue().getNombre());
        }

        while (true) {
            int opcion = leerEntero("Area: ");
            Area area = areas.get(opcion);
            if (area != null) {
                return area;
            }
            System.out.println("Area invalida, intente nuevamente.");
        }
    }

    private Box seleccionarBoxOperador(Operador operador) {
        List<Box> boxes = operador.getAreaAsignada().getBoxes();
        System.out.println("Boxes disponibles:");
        for (int i = 0; i < boxes.size(); i++) {
            System.out.println((i + 1) + ". " + boxes.get(i));
        }

        while (true) {
            int opcion = leerEntero("Seleccione box: ");
            if (opcion >= 1 && opcion <= boxes.size()) {
                return boxes.get(opcion - 1);
            }
            System.out.println("Box invalido, intente nuevamente.");
        }
    }

    private int leerEntero(String mensaje) {
        // Reintenta hasta que el usuario cargue un numero valido.
        while (true) {
            try {
                System.out.print(mensaje);
                String valor = scanner.nextLine();
                return Integer.parseInt(valor.trim());
            } catch (NumberFormatException ex) {
                System.out.println("Ingrese un numero valido.");
            }
        }
    }

    private String leerTexto(String mensaje) {
        // Reintenta hasta que se cargue texto no vacio.
        while (true) {
            System.out.print(mensaje);
            String valor = scanner.nextLine();
            if (!valor.trim().isEmpty()) {
                return valor.trim();
            }
            System.out.println("El campo es obligatorio.");
        }
    }

    private void inicializarDatos() {
        if (persistenciaMySql) {
            cargarAreasDesdeDb();
            cargarOperadoresDesdeDb();
            asegurarOperadoresSemillaSiNoHay();
            return;
        }

        // Datos de ejemplo para modo memoria.
        Area administracion = new Area(1, "Administracion", 'A');
        administracion.agregarBox(new Box(1, 1));
        administracion.agregarBox(new Box(2, 2));
        administracion.agregarBox(new Box(3, 3));
        administracion.agregarBox(new Box(4, 4));

        Area laboratorio = new Area(2, "Laboratorio", 'L');
        laboratorio.agregarBox(new Box(5, 1));
        laboratorio.agregarBox(new Box(6, 2));

        Area internacion = new Area(3, "Internacion", 'I');
        internacion.agregarBox(new Box(7, 1));

        areas.put(1, administracion);
        areas.put(2, laboratorio);
        areas.put(3, internacion);

        Operador opAdmin = new Operador(1, "Lucia Perez", "lperez", administracion);
        Operador opLab = new Operador(2, "Tomas Acosta", "tacosta", laboratorio);
        Operador opIntern = new Operador(3, "Nora Ruiz", "nruiz", internacion);

        operadoresPorUsuario.put(opAdmin.getUsuario().toLowerCase(), opAdmin);
        operadoresPorUsuario.put(opLab.getUsuario().toLowerCase(), opLab);
        operadoresPorUsuario.put(opIntern.getUsuario().toLowerCase(), opIntern);

        // Passwords de demo: admin123, lab123, inter123.
        passwordHashPorUsuario.put(opAdmin.getUsuario().toLowerCase(), PasswordHashUtil.hash("admin123"));
        passwordHashPorUsuario.put(opLab.getUsuario().toLowerCase(), PasswordHashUtil.hash("lab123"));
        passwordHashPorUsuario.put(opIntern.getUsuario().toLowerCase(), PasswordHashUtil.hash("inter123"));
    }

    private TurnoRepository crearRepositorioTurnos() {
        // Permite elegir persistencia por variable de entorno: QMS_REPO=mysql.
        if (persistenciaMySql) {
            return new MySqlTurnoRepository(dbUrl, dbUser, dbPassword);
        }
        return new InMemoryTurnoRepository();
    }

    private String leerEnvConDefault(String clave, String valorDefault) {
        String valor = System.getenv(clave);
        if (valor == null || valor.trim().isEmpty()) {
            return valorDefault;
        }
        return valor.trim();
    }

    private void menuAdministracionCatalogo() {
        if (!persistenciaMySql) {
            System.out.println("El ABM completo usa MySQL. Configura QMS_REPO=mysql para habilitarlo.");
            return;
        }

        boolean continuar = true;
        while (continuar) {
            System.out.println("\n--- ABM AREAS/BOXES ---");
            System.out.println("1. Listar areas y boxes");
            System.out.println("2. Alta area");
            System.out.println("3. Modificar area");
            System.out.println("4. Baja area");
            System.out.println("5. Alta box");
            System.out.println("6. Cambiar estado box");
            System.out.println("7. Baja box");
            System.out.println("0. Volver");

            int opcion = leerEntero("Seleccione una opcion: ");
            try {
                switch (opcion) {
                    case 1:
                        listarAreasYBoxes();
                        break;
                    case 2:
                        altaArea();
                        break;
                    case 3:
                        modificarArea();
                        break;
                    case 4:
                        bajaArea();
                        break;
                    case 5:
                        altaBox();
                        break;
                    case 6:
                        cambiarEstadoBox();
                        break;
                    case 7:
                        bajaBox();
                        break;
                    case 0:
                        continuar = false;
                        break;
                    default:
                        System.out.println("Opcion invalida.");
                }
            } catch (RuntimeException ex) {
                System.out.println("Error ABM: " + ex.getMessage());
            }
        }
    }

    private void listarAreasYBoxes() {
        cargarAreasDesdeDb();
        List<Area> listaAreas = new ArrayList<Area>(areas.values());
        listaAreas.sort(Comparator.comparingInt(Area::getId));

        if (listaAreas.isEmpty()) {
            System.out.println("No hay areas cargadas.");
            return;
        }

        for (Area area : listaAreas) {
            System.out.println("Area " + area.getId() + " - " + area.getNombre() + " (" + area.getLetraIdentificadora() + ")");
            if (area.getBoxes().isEmpty()) {
                System.out.println("  Sin boxes.");
            } else {
                for (Box box : area.getBoxes()) {
                    String estado = box.isDisponible() ? "DISPONIBLE" : "OCUPADO";
                    System.out.println("  Box id=" + box.getId() + " nro=" + box.getNumeroIdentificador() + " estado=" + estado);
                }
            }
        }
    }

    private void altaArea() {
        String nombre = leerTexto("Nombre de area: ");
        String letraTexto = leerTexto("Letra identificadora (1 caracter): ");
        char letra = Character.toUpperCase(letraTexto.trim().charAt(0));

        String sql = "INSERT INTO area(nombre, letra_identificadora, activa) VALUES(?, ?, 1)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, String.valueOf(letra));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo crear area", e);
        }

        cargarAreasDesdeDb();
        System.out.println("Area creada correctamente.");
    }

    private void modificarArea() {
        listarAreasYBoxes();
        int idArea = leerEntero("ID de area a modificar: ");
        String nombre = leerTexto("Nuevo nombre: ");
        String letraTexto = leerTexto("Nueva letra (1 caracter): ");
        char letra = Character.toUpperCase(letraTexto.trim().charAt(0));

        String sql = "UPDATE area SET nombre = ?, letra_identificadora = ? WHERE id_area = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, String.valueOf(letra));
            ps.setInt(3, idArea);
            int filas = ps.executeUpdate();
            if (filas == 0) {
                System.out.println("No existe area con ese ID.");
            } else {
                System.out.println("Area actualizada.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo modificar area", e);
        }

        cargarAreasDesdeDb();
    }

    private void bajaArea() {
        listarAreasYBoxes();
        int idArea = leerEntero("ID de area a eliminar: ");

        String sql = "DELETE FROM area WHERE id_area = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idArea);
            int filas = ps.executeUpdate();
            if (filas == 0) {
                System.out.println("No existe area con ese ID.");
            } else {
                System.out.println("Area eliminada.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo eliminar area (puede tener referencias)", e);
        }

        cargarAreasDesdeDb();
        cargarOperadoresDesdeDb();
    }

    private void altaBox() {
        listarAreasYBoxes();
        int idArea = leerEntero("ID de area para el box: ");
        int numero = leerEntero("Numero identificador de box: ");

        String sql = "INSERT INTO box_atencion(id_area, numero_identificador, estado) VALUES(?, ?, 'DISPONIBLE')";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idArea);
            ps.setInt(2, numero);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo crear box", e);
        }

        cargarAreasDesdeDb();
        System.out.println("Box creado correctamente.");
    }

    private void cambiarEstadoBox() {
        listarAreasYBoxes();
        int idBox = leerEntero("ID de box a actualizar: ");
        String estado = leerTexto("Nuevo estado (DISPONIBLE/OCUPADO): ").toUpperCase();

        if (!"DISPONIBLE".equals(estado) && !"OCUPADO".equals(estado)) {
            System.out.println("Estado invalido.");
            return;
        }

        String sql = "UPDATE box_atencion SET estado = ? WHERE id_box = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setInt(2, idBox);
            int filas = ps.executeUpdate();
            if (filas == 0) {
                System.out.println("No existe box con ese ID.");
            } else {
                System.out.println("Estado de box actualizado.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo cambiar estado del box", e);
        }

        cargarAreasDesdeDb();
    }

    private void bajaBox() {
        listarAreasYBoxes();
        int idBox = leerEntero("ID de box a eliminar: ");

        String sql = "DELETE FROM box_atencion WHERE id_box = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idBox);
            int filas = ps.executeUpdate();
            if (filas == 0) {
                System.out.println("No existe box con ese ID.");
            } else {
                System.out.println("Box eliminado.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo eliminar box (puede tener referencias)", e);
        }

        cargarAreasDesdeDb();
        cargarOperadoresDesdeDb();
    }

    private void cargarAreasDesdeDb() {
        areas.clear();

        String sql = "SELECT a.id_area, a.nombre, a.letra_identificadora, b.id_box, b.numero_identificador, b.estado "
            + "FROM area a LEFT JOIN box_atencion b ON b.id_area = a.id_area "
            + "ORDER BY a.id_area, b.numero_identificador";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int idArea = rs.getInt("id_area");
                Area area = areas.get(idArea);
                if (area == null) {
                    area = new Area(
                        idArea,
                        rs.getString("nombre"),
                        rs.getString("letra_identificadora").charAt(0)
                    );
                    areas.put(idArea, area);
                }

                int idBox = rs.getInt("id_box");
                if (!rs.wasNull()) {
                    Box box = new Box(idBox, rs.getInt("numero_identificador"));
                    box.setDisponible("DISPONIBLE".equalsIgnoreCase(rs.getString("estado")));
                    area.agregarBox(box);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudieron cargar areas/boxes desde DB", e);
        }
    }

    private void cargarOperadoresDesdeDb() {
        operadoresPorUsuario.clear();
        passwordHashPorUsuario.clear();

        String sql = "SELECT id_operador, usuario, password_hash, id_area FROM operador";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int idArea = rs.getInt("id_area");
                Area area = areas.get(idArea);
                if (area == null) {
                    continue;
                }

                int idOperador = rs.getInt("id_operador");
                String usuario = rs.getString("usuario");
                Operador operador = new Operador(idOperador, usuario, usuario, area);
                operadoresPorUsuario.put(usuario.toLowerCase(), operador);
                passwordHashPorUsuario.put(usuario.toLowerCase(), rs.getString("password_hash"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudieron cargar operadores desde DB", e);
        }
    }

    private void asegurarOperadoresSemillaSiNoHay() {
        if (!operadoresPorUsuario.isEmpty()) {
            return;
        }

        Integer idAdmin = buscarIdAreaPorNombre("Administracion");
        Integer idLab = buscarIdAreaPorNombre("Laboratorio");
        Integer idIntern = buscarIdAreaPorNombre("Internacion");

        if (idAdmin == null && idLab == null && idIntern == null) {
            return;
        }

        insertarOperadorSemilla("lperez", "admin123", "OPERADOR", idAdmin);
        insertarOperadorSemilla("tacosta", "lab123", "OPERADOR", idLab);
        insertarOperadorSemilla("nruiz", "inter123", "OPERADOR", idIntern);

        cargarOperadoresDesdeDb();
    }

    private Integer buscarIdAreaPorNombre(String nombreArea) {
        for (Area area : areas.values()) {
            if (area.getNombre().equalsIgnoreCase(nombreArea)) {
                return area.getId();
            }
        }
        return null;
    }

    private void insertarOperadorSemilla(String usuario, String passwordPlano, String perfil, Integer idArea) {
        if (idArea == null) {
            return;
        }

        String sql = "INSERT INTO operador(usuario, password_hash, perfil, id_area, id_box) VALUES(?, ?, ?, ?, NULL)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setString(2, PasswordHashUtil.hash(passwordPlano));
            ps.setString(3, perfil);
            ps.setInt(4, idArea);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("No se pudieron insertar operadores semilla", e);
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }
}
