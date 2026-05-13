package org.example.demo3.entity;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidade que representa a tabela {@code tema} no banco de dados.
 *
 * <p>Regras de negócio refletidas na modelagem:
 * <ul>
 *   <li>{@code qtd_min_aulas} deve ser menor ou igual a {@code qtd_max_aulas}.</li>
 *   <li>{@code prioridade} segue convenção: 1 = maior prioridade.</li>
 *   <li>{@code deleted_at} não nulo indica registro excluído (soft-delete).</li>
 * </ul>
 */
public class Tema {

    // -------------------------------------------------------------------------
    // Campos
    // -------------------------------------------------------------------------

    private Integer id_tema;

    /** FK → disciplina.id_disciplina */
    private int disciplina_id;

    /** FK → semestre_letivo.id_semestre_letivo */
    private int semestre_letivo_id;

    private String nome;

    /** Indica se o tema é uma avaliação (prova, trabalho etc.). */
    private boolean eh_avaliacao;

    /** Quantidade mínima de aulas necessárias para o tema. */
    private byte qtd_min_aulas;

    /** Quantidade máxima de aulas alocáveis ao tema. */
    private byte qtd_max_aulas;

    /**
     * Prioridade de alocação do tema no planejamento.
     * Convenção: 1 = maior prioridade.
     */
    private short prioridade;

    /** Indica se o tema é de presença/participação opcional. */
    private boolean eh_opcional;

    /** Preenchido em exclusões lógicas (soft-delete). Nulo = registro ativo. */
    private LocalDateTime deleted_at;

    // -------------------------------------------------------------------------
    // Construtores
    // -------------------------------------------------------------------------

    /** Construtor padrão exigido por frameworks e utilitários de reflexão. */
    public Tema() {}

    /**
     * Construtor completo para criação de novos temas (sem {@code id} ainda).
     *
     * @param disciplina_id     ID da disciplina vinculada
     * @param semestre_letivo_id ID do semestre letivo vinculado
     * @param nome               nome do tema (máx. 120 caracteres)
     * @param eh_avaliacao       {@code true} se o tema representa uma avaliação
     * @param qtd_min_aulas      quantidade mínima de aulas
     * @param qtd_max_aulas      quantidade máxima de aulas
     * @param prioridade         prioridade (1 = maior)
     * @param eh_opcional        {@code true} se o tema é opcional
     */
    public Tema(int disciplina_id, int semestre_letivo_id, String nome,
                boolean eh_avaliacao, byte qtd_min_aulas, byte qtd_max_aulas,
                short prioridade, boolean eh_opcional) {

        this.disciplina_id      = disciplina_id;
        this.semestre_letivo_id = semestre_letivo_id;
        this.nome               = nome;
        this.eh_avaliacao       = eh_avaliacao;
        this.qtd_min_aulas      = qtd_min_aulas;
        this.qtd_max_aulas      = qtd_max_aulas;
        this.prioridade         = prioridade;
        this.eh_opcional        = eh_opcional;
    }

    // -------------------------------------------------------------------------
    // Getters e Setters
    // -------------------------------------------------------------------------

    public Integer getId_tema() { return id_tema; }
    public void setId_tema(Integer id_tema) { this.id_tema = id_tema; }

    public int getDisciplina_id() { return disciplina_id; }
    public void setDisciplina_id(int disciplina_id) { this.disciplina_id = disciplina_id; }

    public int getSemestre_letivo_id() { return semestre_letivo_id; }
    public void setSemestre_letivo_id(int semestre_letivo_id) { this.semestre_letivo_id = semestre_letivo_id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public boolean isEh_avaliacao() { return eh_avaliacao; }
    public void setEh_avaliacao(boolean eh_avaliacao) { this.eh_avaliacao = eh_avaliacao; }

    public byte getQtd_min_aulas() { return qtd_min_aulas; }
    public void setQtd_min_aulas(byte qtd_min_aulas) { this.qtd_min_aulas = qtd_min_aulas; }

    public byte getQtd_max_aulas() { return qtd_max_aulas; }
    public void setQtd_max_aulas(byte qtd_max_aulas) { this.qtd_max_aulas = qtd_max_aulas; }

    public short getPrioridade() { return prioridade; }
    public void setPrioridade(short prioridade) { this.prioridade = prioridade; }

    public boolean isEh_opcional() { return eh_opcional; }
    public void setEh_opcional(boolean eh_opcional) { this.eh_opcional = eh_opcional; }

    public LocalDateTime getDeleted_at() { return deleted_at; }
    public void setDeleted_at(LocalDateTime deleted_at) { this.deleted_at = deleted_at; }

    // -------------------------------------------------------------------------
    // Utilitários
    // -------------------------------------------------------------------------

    /** @return {@code true} enquanto {@code deleted_at} for nulo. */
    public boolean isAtivo() { return deleted_at == null; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tema)) return false;
        Tema tema = (Tema) o;
        return Objects.equals(id_tema, tema.id_tema);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id_tema); }

    @Override
    public String toString() {
        return "Tema{" +
                "id_tema="              + id_tema              +
                ", disciplina_id="      + disciplina_id        +
                ", semestre_letivo_id=" + semestre_letivo_id   +
                ", nome='"             + nome                 + '\'' +
                ", eh_avaliacao="      + eh_avaliacao         +
                ", qtd_min_aulas="     + qtd_min_aulas        +
                ", qtd_max_aulas="     + qtd_max_aulas        +
                ", prioridade="        + prioridade           +
                ", eh_opcional="       + eh_opcional          +
                ", deleted_at="        + deleted_at           +
                '}';
    }
}