package com.clinica.qms;

import com.clinica.qms.dao.RepositorioFactory;
import com.clinica.qms.observer.ArchivoLogObserver;
import com.clinica.qms.observer.ConsolaSalaEsperaObserver;
import com.clinica.qms.service.GestorTurnos;
import com.clinica.qms.util.ArchivoLog;
import com.clinica.qms.util.ConsolaUtil;
import com.clinica.qms.view.ConsolaQmsUI;

/**
 * Punto de entrada del sistema QMS.
 * Configura el contexto de dependencias y lanza la interfaz de consola.
 */
public class App {

    public static void main(String[] args) {
        ConsolaUtil.info("Iniciando Sistema QMS...");

        // 1. Construir el GestorTurnos via factory (MySQL o memoria)
        GestorTurnos gestor = RepositorioFactory.crearGestorTurnos();

        // 2. Crear observers
        ConsolaSalaEsperaObserver pantallaObs = new ConsolaSalaEsperaObserver();
        ArchivoLog log = new ArchivoLog();
        ArchivoLogObserver logObs = new ArchivoLogObserver(log);

        // 3. Registrar observers en el gestor
        gestor.agregarObserver(pantallaObs);
        gestor.agregarObserver(logObs);

        // 4. Lanzar la UI
        ConsolaQmsUI ui = new ConsolaQmsUI(gestor, pantallaObs, log);
        ui.iniciar();
    }
}
