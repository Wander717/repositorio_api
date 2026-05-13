package org.example.demo3.dao;

import org.example.demo3.entity.Tema;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) responsável pelas operações de persistência
 * da entidade {@link Tema} na tabela {@code tema}.
 *
 * <p><b>Convenções adotadas:</b>
 * <ul>
 *   <li>A conexão é injetada via construtor — sem acoplamento a fontes específicas.</li>
 *   <li>Todas as queries usam {@link PreparedStatement} para evitar SQL Injection.</li>
 *   <li>O soft-delete é preservado: buscas padrão filtram {@code deleted_at IS NULL}.</li>
 *   <li>Métodos de busca lançam {@link TemaNotFoundException} quando não encontram resultado.</li>
 *   <li>Erros de banco são relançados como {@link RuntimeException} para não poluir
 *       assinaturas com {@code SQLException} verificada.</li>
 * </ul>
 */
public class TemaDAO {

    // -------------------------------------------------------------------------
    // SQL
    // -------------------------------------------------------------------------

    private static final String SQL_INSERT =
            "INSERT INTO tema " +
                    "(disciplina_id, semestre_letivo_id, nome, eh_avaliacao, " +
                    " qtd_min_aulas, qtd_max_aulas, prioridade, eh_opcional) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_SELECT_BY_ID =
            "SELECT * FROM tema WHERE id_tema = ? AND deleted_at IS NULL";

    private static final String SQL_SELECT_ALL =
            "SELECT * FROM tema WHERE deleted_at IS NULL ORDER BY prioridade ASC";

    private static final String SQL_SELECT_BY_DISCIPLINA =
            "SELECT * FROM tema " +
                    "WHERE disciplina_id = ? AND semestre_letivo_id = ? AND deleted_at IS NULL " +
                    "ORDER BY prioridade ASC";

    private static final String SQL_UPDATE =
            "UPDATE tema SET " +
                    "disciplina_id = ?, semestre_letivo_id = ?, nome = ?, " +
                    "eh_avaliacao = ?, qtd_min_aulas = ?, qtd_max_aulas = ?, " +
                    "prioridade = ?, eh_opcional = ? " +
                    "WHERE id_tema = ? AND deleted_at IS NULL";

    /** Soft-delete: apenas marca o timestamp de exclusão. */
    private static final String SQL_SOFT_DELETE =
            "UPDATE tema SET deleted_at = NOW() WHERE id_tema = ? AND deleted_at IS NULL";

    /** Hard-delete: remoção física (use com cautela). */
    private static final String SQL_HARD_DELETE =
            "DELETE FROM tema WHERE id_tema = ?";

    // -------------------------------------------------------------------------
    // Estado
    // -------------------------------------------------------------------------

    private final Connection connection;

    // -------------------------------------------------------------------------
    // Construtor
    // -------------------------------------------------------------------------

    /**
     * @param connection conexão JDBC ativa; o gerenciamento do ciclo de vida
     *                   (abertura, commit, rollback, fechamento) é responsabilidade
     *                   do chamador.
     */
    public TemaDAO(Connection connection) {
        this.connection = connection;
    }

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------

    /**
     * Insere um novo tema e preenche o {@code id_tema} gerado automaticamente.
     *
     * @param tema objeto a ser persistido; {@code id_tema} é ignorado na entrada
     * @return o mesmo objeto com {@code id_tema} preenchido
     * @throws RuntimeException em caso de falha no banco
     */
    public Tema inserir(Tema tema) {
        try (PreparedStatement ps = connection.prepareStatement(
                SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            mapearParametros(ps, tema);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    tema.setId_tema(keys.getInt(1));
                }
            }
            return tema;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir Tema: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // READ
    // -------------------------------------------------------------------------

    /**
     * Busca um tema ativo pelo seu identificador primário.
     *
     * @param id_tema chave primária
     * @return tema encontrado
     * @throws TemaNotFoundException se não existir registro ativo com o id informado
     */
    public Tema buscarPorId(int id_tema) {
        try (PreparedStatement ps = connection.prepareStatement(SQL_SELECT_BY_ID)) {
            ps.setInt(1, id_tema);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearResultado(rs);
                }
                throw new TemaNotFoundException("Tema não encontrado para id_tema=" + id_tema);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar Tema por id: " + e.getMessage(), e);
        }
    }

    /**
     * Retorna todos os temas ativos, ordenados por prioridade crescente.
     *
     * @return lista (possivelmente vazia) de temas ativos
     */
    public List<Tema> listarTodos() {
        List<Tema> temas = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(SQL_SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                temas.add(mapearResultado(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar Temas: " + e.getMessage(), e);
        }
        return temas;
    }

    /**
     * Lista os temas ativos de uma disciplina em um semestre letivo específico,
     * ordenados por prioridade crescente.
     *
     * @param disciplina_id     ID da disciplina
     * @param semestre_letivo_id ID do semestre letivo
     * @return lista (possivelmente vazia) de temas
     */
    public List<Tema> listarPorDisciplinaESemestre(int disciplina_id, int semestre_letivo_id) {
        List<Tema> temas = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(SQL_SELECT_BY_DISCIPLINA)) {
            ps.setInt(1, disciplina_id);
            ps.setInt(2, semestre_letivo_id);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    temas.add(mapearResultado(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao listar Temas por disciplina/semestre: " + e.getMessage(), e);
        }
        return temas;
    }

    // -------------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------------

    /**
     * Atualiza todos os campos editáveis de um tema ativo.
     *
     * @param tema objeto com os novos valores; {@code id_tema} identifica o registro
     * @throws TemaNotFoundException se nenhum registro ativo for encontrado
     */
    public void atualizar(Tema tema) {
        try (PreparedStatement ps = connection.prepareStatement(SQL_UPDATE)) {

            mapearParametros(ps, tema);             // índices 1–8
            ps.setInt(9, tema.getId_tema());         // índice 9 = WHERE id_tema

            int linhasAfetadas = ps.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new TemaNotFoundException(
                        "Tema não encontrado ou já excluído para id_tema=" + tema.getId_tema());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar Tema: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    /**
     * Realiza o <b>soft-delete</b> do tema, preenchendo {@code deleted_at} com o
     * momento atual. O registro permanece no banco para fins de auditoria.
     *
     * @param id_tema chave primária do tema a ser excluído logicamente
     * @throws TemaNotFoundException se não existir registro ativo com o id informado
     */
    public void excluir(int id_tema) {
        try (PreparedStatement ps = connection.prepareStatement(SQL_SOFT_DELETE)) {
            ps.setInt(1, id_tema);

            int linhasAfetadas = ps.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new TemaNotFoundException(
                        "Tema não encontrado ou já excluído para id_tema=" + id_tema);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir (soft) Tema: " + e.getMessage(), e);
        }
    }

    /**
     * Realiza o <b>hard-delete</b> do tema, removendo o registro fisicamente.
     * Use apenas quando necessário (ex.: rotina de limpeza de dados).
     *
     * @param id_tema chave primária do tema a ser removido
     * @throws TemaNotFoundException se nenhum registro for encontrado
     */
    public void excluirFisicamente(int id_tema) {
        try (PreparedStatement ps = connection.prepareStatement(SQL_HARD_DELETE)) {
            ps.setInt(1, id_tema);

            int linhasAfetadas = ps.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new TemaNotFoundException(
                        "Tema não encontrado para id_tema=" + id_tema);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir (hard) Tema: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

    /**
     * Define os parâmetros do {@link PreparedStatement} a partir dos campos
     * editáveis de {@link Tema} (índices 1–8, na mesma ordem de INSERT e UPDATE).
     */
    private void mapearParametros(PreparedStatement ps, Tema tema) throws SQLException {
        ps.setInt    (1, tema.getDisciplina_id());
        ps.setInt    (2, tema.getSemestre_letivo_id());
        ps.setString (3, tema.getNome());
        ps.setBoolean(4, tema.isEh_avaliacao());
        ps.setByte   (5, tema.getQtd_min_aulas());
        ps.setByte   (6, tema.getQtd_max_aulas());
        ps.setShort  (7, tema.getPrioridade());
        ps.setBoolean(8, tema.isEh_opcional());
    }

    /**
     * Constrói um objeto {@link Tema} a partir da linha atual do {@link ResultSet}.
     */
    private Tema mapearResultado(ResultSet rs) throws SQLException {
        Tema tema = new Tema();

        tema.setId_tema            (rs.getInt      ("id_tema"));
        tema.setDisciplina_id      (rs.getInt      ("disciplina_id"));
        tema.setSemestre_letivo_id (rs.getInt      ("semestre_letivo_id"));
        tema.setNome               (rs.getString   ("nome"));
        tema.setEh_avaliacao       (rs.getBoolean  ("eh_avaliacao"));
        tema.setQtd_min_aulas      (rs.getByte     ("qtd_min_aulas"));
        tema.setQtd_max_aulas      (rs.getByte     ("qtd_max_aulas"));
        tema.setPrioridade         (rs.getShort    ("prioridade"));
        tema.setEh_opcional        (rs.getBoolean  ("eh_opcional"));

        Timestamp deleted_at = rs.getTimestamp("deleted_at");
        tema.setDeleted_at(deleted_at != null ? deleted_at.toLocalDateTime() : null);

        return tema;
    }

    // -------------------------------------------------------------------------
    // Exceção de domínio
    // -------------------------------------------------------------------------

    /**
     * Exceção lançada quando um {@link Tema} esperado não é encontrado no banco.
     * Estende {@link RuntimeException} para não forçar tratamento obrigatório
     * nos pontos de chamada, mas ainda permite captura explícita quando necessário.
     */
    public static class TemaNotFoundException extends RuntimeException {
        public TemaNotFoundException(String message) {
            super(message);
        }
    }
}