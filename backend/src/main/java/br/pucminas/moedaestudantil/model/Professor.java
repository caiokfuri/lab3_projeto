package br.pucminas.moedaestudantil.model;

import br.pucminas.moedaestudantil.exception.RegraNegocioException;
import jakarta.persistence.*;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.Objects;

/** Pré-cadastrado pela instituição parceira; distribui moedas aos alunos da sua instituição. */
@Entity
@Table(name = "professor")
@PrimaryKeyJoinColumn(name = "id")
public class Professor extends Usuario {

    public static final int CREDITO_SEMESTRAL = 1000;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(nullable = false)
    private String departamento;

    @Column(name = "saldo_moedas", nullable = false)
    private int saldoMoedas = 0;

    /** Último semestre (ex.: "2026/2") em que o crédito foi lançado; evita creditar duas vezes. */
    @Column(name = "ultimo_semestre_creditado", length = 6)
    private String ultimoSemestreCreditado;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "instituicao_id", nullable = false)
    private InstituicaoEnsino instituicao;

    /**
     * Acumula as 1.000 moedas do semestre ao saldo atual (o que não foi distribuído não se perde).
     * @return {@code false} se o semestre já havia sido creditado
     */
    public boolean creditarSemestre(String semestre) {
        if (semestre.equals(ultimoSemestreCreditado)) {
            return false;
        }
        this.saldoMoedas += CREDITO_SEMESTRAL;
        this.ultimoSemestreCreditado = semestre;
        return true;
    }

    /** Transfere moedas do saldo do professor para um aluno da mesma instituição. */
    public TransacaoEnvio enviarMoedas(Aluno aluno, int valor, String motivo) {
        if (valor <= 0) {
            throw invalido("valor", "Informe uma quantidade de moedas maior que zero");
        }
        if (motivo == null || motivo.isBlank()) {
            throw invalido("motivo", "O motivo do reconhecimento é obrigatório");
        }
        if (!Objects.equals(aluno.getInstituicao().getId(), instituicao.getId())) {
            throw invalido("alunoId", "O aluno não pertence à sua instituição");
        }
        if (valor > saldoMoedas) {
            throw new RegraNegocioException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Saldo insuficiente: você possui " + saldoMoedas + " moedas",
                    Map.of("valor", "Saldo disponível: " + saldoMoedas));
        }

        this.saldoMoedas -= valor;
        aluno.receberMoedas(valor);

        TransacaoEnvio envio = new TransacaoEnvio();
        envio.setProfessor(this);
        envio.setAluno(aluno);
        envio.setValor(valor);
        envio.setMotivo(motivo.trim());
        return envio;
    }

    private static RegraNegocioException invalido(String campo, String mensagem) {
        return new RegraNegocioException(HttpStatus.BAD_REQUEST, mensagem, Map.of(campo, mensagem));
    }

    @Override
    public Perfil getPerfil() { return Perfil.PROFESSOR; }

    @Override
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }
    public int getSaldoMoedas() { return saldoMoedas; }
    public String getUltimoSemestreCreditado() { return ultimoSemestreCreditado; }
    public InstituicaoEnsino getInstituicao() { return instituicao; }
    public void setInstituicao(InstituicaoEnsino instituicao) { this.instituicao = instituicao; }
}
