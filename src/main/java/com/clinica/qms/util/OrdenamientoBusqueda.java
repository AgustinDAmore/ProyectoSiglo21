package com.clinica.qms.util;

import com.clinica.qms.model.Turno;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;

/** Algoritmos de ordenamiento y busqueda para turnos. */
public class OrdenamientoBusqueda {

    private OrdenamientoBusqueda() {}

    /** Ordena por codigo alfanumerico ascendente. */
    public static void ordenarPorCodigo(ArrayList<Turno> lista) {
        Collections.sort(lista, new Comparator<Turno>() {
            @Override
            public int compare(Turno a, Turno b) {
                return a.getCodigo().compareTo(b.getCodigo());
            }
        });
    }

    /** Ordena por tiempo de espera descendente (mayor espera primero). */
    public static void ordenarPorEsperaDesc(ArrayList<Turno> lista) {
        Collections.sort(lista, new Comparator<Turno>() {
            @Override
            public int compare(Turno a, Turno b) {
                return Long.compare(b.getEsperaEnSegundos(), a.getEsperaEnSegundos());
            }
        });
    }

    /**
     * Busqueda binaria por codigo (requiere lista ordenada por codigo primero).
     * Retorna Optional.empty() si no se encuentra.
     */
    public static Optional<Turno> busquedaBinariaPorCodigo(ArrayList<Turno> lista, String codigo) {
        ordenarPorCodigo(lista);
        int inicio = 0;
        int fin    = lista.size() - 1;
        while (inicio <= fin) {
            int medio = (inicio + fin) / 2;
            int cmp   = lista.get(medio).getCodigo().compareTo(codigo);
            if (cmp == 0) {
                return Optional.of(lista.get(medio));
            } else if (cmp < 0) {
                inicio = medio + 1;
            } else {
                fin = medio - 1;
            }
        }
        return Optional.empty();
    }
}
