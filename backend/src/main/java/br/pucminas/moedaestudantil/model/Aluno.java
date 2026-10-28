package br.pucminas.moedaestudantil.model;

import br.pucminas.moedaestudantil.exception.RegraNegocioException;
import jakarta.persistence.*;
import org.springframework.http.HttpStatus;

@Entity
@Table(name = "aluno")
@PrimaryKeyJoinColumn(name = "id")
public class Aluno extends Usuario {

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(nullable = false, length = 20)
    private String rg;

    @Column(nullable = false)
    private String endereco;

    @Column(nullable = false)
    private String curso;

    @Column(name = "saldo_moedas", nullable = false)
    private int saldoMoedas = 0;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "instituicao_id", nullable = false)
    private InstituicaoEnsino instituicao;

    public void receberMoedas(int valor) {
        this.saldoMoedas += valor;
    }

    /** Troca moedas por uma vantagem: debita o custo e gera o resgate com seu cupom. */
    public Cupom resgatarVantagem(Vantagem vantagem) {
        int custo = vantagem.getCustoMoedas();
        if (custo > saldoMoedas) {
            throw new RegraNegocioException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Saldo insuficiente: a vantagem custa " + custo + " moedas e você possui " + saldoMoedas);
        }
        this.saldoMoedas -= custo;

        TransacaoResgate resgate = new TransacaoResgate();
        resgate.setAluno(this);
        resgate.setVantagem(vantagem);
        resgate.setValor(custo);
        return Cupom.gerar(resgate);
    }

    @Override
    public Perfil getPerfil() { return Perfil.ALUNO; }

    @Override
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getRg() { return rg; }
    public void setRg(String rg) { this.rg = rg; }
    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public String getCurso() { return curso; }
    public void setCurso(String curso) { this.curso = curso; }
    public int getSaldoMoedas() { return saldoMoedas; }
    public InstituicaoEnsino getInstituicao() { return instituicao; }
    public void setInstituicao(InstituicaoEnsino instituicao) { this.instituicao = instituicao; }
}
