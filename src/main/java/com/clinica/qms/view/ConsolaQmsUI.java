package com.clinica.qms.view;

import com.clinica.qms.exception.BusinessException;
import com.clinica.qms.exception.DataAccessException;
import com.clinica.qms.model.Area;
import com.clinica.qms.model.Box;
import com.clinica.qms.model.Operador;
import com.clinica.qms.model.Turno;
import com.clinica.qms.observer.ConsolaSalaEsperaObserver;
import com.clinica.qms.service.GestorTurnos;
import com.clinica.qms.service.MetricasServicio;
import com.clinica.qms.util.ArchivoLog;
import com.clinica.qms.util.ConsolaUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Vista de consola del sistema QMS.
 */
public class ConsolaQmsUI {

    private static final String[] MENU_PRINCIPAL = {
        "1. Autogestion de paciente",
        "2. Login de operador",
        "3. Sala de espera (pantalla publica)",
        "4. Ver log de auditoria",
        "0. Salir"
    };

    private static final String[] MENU_OPERADOR = {
        "1. Ver cola de espera",
        "2. Llamar siguiente turno",
        "3. Derivar turno",
        "4. Cerrar atencion",
        "5. Ver metricas",
        "0. Cerrar sesion"
    };

    private final GestorTurnos   gestor;
    private final MetricasServicio metricas;
    private final ArchivoLog     log;
    private final ConsolaSalaEsperaObserver pantallaObserver;
    private final Scanner        sc;
    private final Map<String, Operador> operadores;

    public ConsolaQmsUI(GestorTurnos gestor,
                        ConsolaSalaEsperaObserver pantallaObserver,
                        ArchivoLog log) {
        this.gestor           = gestor;
        this.pantallaObserver = pantallaObserver;
        this.log              = log;
        this.metricas         = new MetricasServicio();
        this.sc               = new Scanner(System.in);
        this.operadores       = new LinkedHashMap<String, Operador>();
        configurarOperadores();
    }

    private void configurarOperadores() {
        ArrayList<Area> areas = gestor.obtenerAreas();
        if (areas.size() >= 1) {
            operadores.put("lperez",  new Operador(1, "Luis",   "Perez",  "lperez",  areas.get(0)));
        }
        if (areas.size() >= 2) {
            operadores.put("tacosta", new Operador(2, "Tamara", "Acosta", "tacosta", areas.get(1)));
        }
        if (areas.size() >= 3) {
            operadores.put("nruiz",   new Operador(3, "Nadia",  "Ruiz",   "nruiz",   areas.get(2)));
        }
    }

    public void iniciar() {
        ConsolaUtil.limpiarPantalla();
        ConsolaUtil.bannerPrincipal();

        boolean ejecutar = true;
        while (ejecutar) {
            ConsolaUtil.imprimirMenu(MENU_PRINCIPAL);
            String opcion = leerLinea("Opcion: ");
            switch (opcion.trim()) {
                case "1": menuAutogestion();  break;
                case "2": menuLoginOperador(); break;
                case "3": mostrarSalaEspera(); break;
                case "4": mostrarLog();        break;
                case "0": ejecutar = false;    break;
                default:
                    ConsolaUtil.limpiarPantalla();
                    ConsolaUtil.bannerPrincipal();
                    ConsolaUtil.aviso("Opcion invalida.");
                    pausa();
            }
            if (ejecutar && !opcion.trim().equals("2")) {
                ConsolaUtil.limpiarPantalla();
                ConsolaUtil.bannerPrincipal();
            }
        }
        ConsolaUtil.limpiarPantalla();
        ConsolaUtil.info("Sistema finalizado. Hasta pronto.");
        sc.close();
    }

    private void menuAutogestion() {
        ConsolaUtil.limpiarPantalla();
        ConsolaUtil.subtitulo("--- AUTOGESTION DE PACIENTE ---");
        String dni      = leerLinea("DNI: ").trim();
        String nombre   = leerLinea("Nombre: ").trim();
        String apellido = leerLinea("Apellido: ").trim();

        ArrayList<Area> areas = gestor.obtenerAreas();
        ConsolaUtil.titulo("Areas disponibles:");
        for (int i = 0; i < areas.size(); i++) {
            ConsolaUtil.detalle((i + 1) + ". " + areas.get(i).getNombre());
        }
        int idx = leerEnteroRango("Seleccione area: ", 1, areas.size()) - 1;
        Area area = areas.get(idx);

        try {
            Turno turno = gestor.solicitarTurno(dni, nombre, apellido, area);
            System.out.println();
            ConsolaUtil.ok("Turno generado: " + turno.getCodigo());
            ConsolaUtil.detalle("Area: " + area.getNombre());
            ConsolaUtil.detalle("Espere a ser llamado.");
        } catch (DataAccessException e) {
            ConsolaUtil.error(e.getMessage());
        }
        pausa();
    }

    private void menuLoginOperador() {
        ConsolaUtil.limpiarPantalla();
        ConsolaUtil.subtitulo("--- LOGIN DE OPERADOR ---");
        String usuario = leerLinea("Usuario: ").trim();
        Operador operador = operadores.get(usuario);
        if (operador == null) {
            ConsolaUtil.aviso("Usuario no encontrado.");
            pausa();
            ConsolaUtil.limpiarPantalla();
            ConsolaUtil.bannerPrincipal();
            return;
        }
        ConsolaUtil.ok("Bienvenido/a, " + operador.getNombre()
                + " - Area: " + operador.getAreaAsignada());

        List<Box> boxes = operador.getAreaAsignada().getBoxes();
        ConsolaUtil.titulo("Boxes disponibles:");
        for (int i = 0; i < boxes.size(); i++) {
            ConsolaUtil.detalle((i + 1) + ". " + boxes.get(i));
        }
        int idxBox = leerEnteroRango("Seleccione box: ", 1, boxes.size()) - 1;
        operador.seleccionarBox(boxes.get(idxBox));
        ConsolaUtil.info("Box asignado: " + operador.getBoxSeleccionado());
        pausa();

        menuOperador(operador);
        ConsolaUtil.limpiarPantalla();
        ConsolaUtil.bannerPrincipal();
    }

    private void menuOperador(Operador op) {
        boolean sesion = true;
        while (sesion) {
            ConsolaUtil.limpiarPantalla();
            ConsolaUtil.subtitulo("OPERADOR: " + op.getUsuario()
                + " | " + op.getAreaAsignada()
                + " | " + op.getBoxSeleccionado());
            ConsolaUtil.imprimirMenu(MENU_OPERADOR);
            String opc = leerLinea("Opcion: ").trim();
            switch (opc) {
                case "1": verCola(op);         break;
                case "2": llamarSiguiente(op); break;
                case "3": derivarTurno(op);    break;
                case "4": cerrarAtencion(op);  break;
                case "5": verMetricas();       break;
                case "0": sesion = false;      break;
                default:
                    ConsolaUtil.aviso("Opcion invalida.");
                    pausa();
            }
        }
    }

    private void verCola(Operador op) {
        ConsolaUtil.limpiarPantalla();
        ConsolaUtil.subtitulo("COLA DE ESPERA - " + op.getAreaAsignada().getNombre());
        try {
            ArrayList<Turno> cola = gestor.listarEsperaPorArea(op.getAreaAsignada());
            if (cola.isEmpty()) {
                ConsolaUtil.aviso("Cola vacia.");
            } else {
                for (Turno t : cola) {
                    String linea = "  " + t.getCodigo() + " - " + t.getPaciente().getNombre()
                            + " " + t.getPaciente().getApellido();
                    if (t.getPrioridad() > 0) {
                        ConsolaUtil.println(ConsolaUtil.AMARILLO, linea + " [DERIVADO]");
                    } else {
                        ConsolaUtil.println(ConsolaUtil.BLANCO, linea);
                    }
                }
            }
        } catch (DataAccessException e) {
            ConsolaUtil.error(e.getMessage());
        }
        pausa();
    }

    private void llamarSiguiente(Operador op) {
        ConsolaUtil.limpiarPantalla();
        ConsolaUtil.subtitulo("LLAMAR SIGUIENTE TURNO");
        try {
            Turno turno = gestor.llamarSiguiente(op.getAreaAsignada(), op.getBoxSeleccionado());
            System.out.println();
            ConsolaUtil.ok("Llamando: " + turno.getCodigo()
                    + " - " + turno.getPaciente().getNombre()
                    + " " + turno.getPaciente().getApellido());
        } catch (BusinessException e) {
            ConsolaUtil.aviso(e.getMessage());
        } catch (DataAccessException e) {
            ConsolaUtil.error(e.getMessage());
        }
        pausa();
    }

    private void derivarTurno(Operador op) {
        ConsolaUtil.limpiarPantalla();
        ConsolaUtil.subtitulo("DERIVAR TURNO");
        Turno activo = gestor.obtenerTurnoActivo(op.getBoxSeleccionado());
        if (activo == null) {
            ConsolaUtil.aviso("No hay turno activo en su box.");
            pausa();
            return;
        }
        ArrayList<Area> areas = gestor.obtenerAreas();
        ConsolaUtil.titulo("Derivar turno " + activo.getCodigo() + " a:");
        for (int i = 0; i < areas.size(); i++) {
            if (areas.get(i).getId() != op.getAreaAsignada().getId()) {
                ConsolaUtil.detalle((i + 1) + ". " + areas.get(i).getNombre());
            }
        }
        int idx = leerEnteroRango("Numero de area destino: ", 1, areas.size()) - 1;
        Area destino = areas.get(idx);
        try {
            gestor.derivarTurno(activo.getCodigo(), destino);
            ConsolaUtil.ok("Turno " + activo.getCodigo() + " derivado a " + destino.getNombre());
        } catch (BusinessException e) {
            ConsolaUtil.aviso(e.getMessage());
        } catch (DataAccessException e) {
            ConsolaUtil.error(e.getMessage());
        }
        pausa();
    }

    private void cerrarAtencion(Operador op) {
        ConsolaUtil.limpiarPantalla();
        ConsolaUtil.subtitulo("CERRAR ATENCION");
        try {
            Turno cerrado = gestor.cerrarAtencion(op.getBoxSeleccionado());
            ConsolaUtil.ok("Atencion cerrada: " + cerrado.getCodigo()
                    + " | Espera: " + cerrado.getEsperaEnSegundos() + "s"
                    + " | Atencion: " + cerrado.getAtencionEnSegundos() + "s");
        } catch (BusinessException e) {
            ConsolaUtil.aviso(e.getMessage());
        } catch (DataAccessException e) {
            ConsolaUtil.error(e.getMessage());
        }
        pausa();
    }

    private void verMetricas() {
        ConsolaUtil.limpiarPantalla();
        ConsolaUtil.subtitulo("--- METRICAS ---");
        try {
            ArrayList<Turno> historico = gestor.listarHistorico();
            String[] reporte = metricas.generarReporte(historico);
            for (String linea : reporte) {
                ConsolaUtil.println(ConsolaUtil.AZUL + ConsolaUtil.NEGRITA, "  " + linea);
            }
        } catch (DataAccessException e) {
            ConsolaUtil.error(e.getMessage());
        }
        pausa();
    }

    private void mostrarSalaEspera() {
        ConsolaUtil.limpiarPantalla();
        ConsolaUtil.subtitulo("========= SALA DE ESPERA =========");
        ArrayList<String> llamados = pantallaObserver.getUltimosLlamados();
        ConsolaUtil.titulo("Ultimos llamados:");
        if (llamados.isEmpty()) {
            ConsolaUtil.detalle("(Sin llamados aun)");
        } else {
            for (String linea : llamados) {
                ConsolaUtil.println(ConsolaUtil.VERDE, "  " + linea);
            }
        }
        ArrayList<Area> areas = gestor.obtenerAreas();
        for (Area area : areas) {
            try {
                ArrayList<Turno> espera = gestor.listarEsperaPorArea(area);
                System.out.println();
                ConsolaUtil.titulo(area.getNombre() + " (" + espera.size() + " en espera):");
                for (Turno t : espera) {
                    ConsolaUtil.detalle(t.getCodigo());
                }
            } catch (DataAccessException e) {
                ConsolaUtil.error(e.getMessage());
            }
        }
        pausa();
    }

    private void mostrarLog() {
        ConsolaUtil.limpiarPantalla();
        ConsolaUtil.subtitulo("--- ULTIMAS 10 ENTRADAS DEL LOG ---");
        ArrayList<String> entradas = log.leerUltimas(10);
        if (entradas.isEmpty()) {
            ConsolaUtil.detalle("(Log vacio)");
        } else {
            for (String e : entradas) {
                ConsolaUtil.println(ConsolaUtil.GRIS, "  " + e);
            }
        }
        pausa();
    }

    private void pausa() {
        leerLinea("\nPresione Enter para continuar...");
    }

    private String leerLinea(String prompt) {
        ConsolaUtil.print(ConsolaUtil.AMARILLO + ConsolaUtil.NEGRITA, prompt);
        return sc.nextLine();
    }

    private int leerEnteroRango(String prompt, int min, int max) {
        while (true) {
            try {
                int val = Integer.parseInt(leerLinea(prompt).trim());
                if (val >= min && val <= max) return val;
            } catch (NumberFormatException ignored) {}
            ConsolaUtil.aviso("Ingrese un numero entre " + min + " y " + max + ".");
        }
    }
}
