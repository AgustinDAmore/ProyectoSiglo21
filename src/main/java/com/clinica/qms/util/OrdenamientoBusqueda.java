package com.clinica.qms.util;

import com.clinica.qms.model.Turno;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class OrdenamientoBusqueda {

    private OrdenamientoBusqueda() {
    }

    public static List<Turno> ordenarPorCodigo(List<Turno> turnos) {
        // Devuelve una copia ordenada por codigo alfabetico.
        List<Turno> copia = new ArrayList<>(turnos);
        copia.sort(Comparator.comparing(Turno::getCodigo));
        return copia;
    }

    public static List<Turno> ordenarPorEsperaDesc(List<Turno> turnos) {
        // Ordena de mayor a menor tiempo de espera.
        List<Turno> copia = new ArrayList<>(turnos);
        copia.sort(Comparator.comparingLong(Turno::getEsperaEnSegundos).reversed());
        return copia;
    }

    public static Optional<Turno> busquedaBinariaPorCodigo(List<Turno> turnosOrdenados, String codigo) {
        // Requiere lista ordenada previamente por codigo.
        int izquierda = 0;
        int derecha = turnosOrdenados.size() - 1;

        while (izquierda <= derecha) {
            int medio = izquierda + (derecha - izquierda) / 2;
            Turno actual = turnosOrdenados.get(medio);
            int comparacion = actual.getCodigo().compareToIgnoreCase(codigo);

            if (comparacion == 0) {
                return Optional.of(actual);
            }
            if (comparacion < 0) {
                izquierda = medio + 1;
            } else {
                derecha = medio - 1;
            }
        }

        return Optional.empty();
    }
}
