package br.ufjf.planejamento.modelo;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Catálogo em memória de turmas ofertadas.
 */
public class CatalogoTurmas {
    private static final Map<String, Turma> turmas = Collections.synchronizedMap(new LinkedHashMap<>());

    public static void adicionarTurma(Turma t) {
        turmas.put(t.getId(), t);
    }

    public static Turma getTurma(String id) {
        return turmas.get(id);
    }

    public static Collection<Turma> getTodasTurmas() {
        return Collections.unmodifiableCollection(turmas.values());
    }

    public static boolean removerTurma(String id) {
        return turmas.remove(id) != null;
    }
}
