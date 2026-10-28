package br.pucminas.moedaestudantil.model;

import jakarta.persistence.*;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/** Comprovante do resgate; o código vai nos e-mails do aluno e do parceiro para a conferência presencial. */
@Entity
@Table(name = "cupom")
public class Cupom {

    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // sem 0/O e 1/I
    private static final SecureRandom RANDOM = new SecureRandom();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String codigo;

    @Column(name = "data_emissao", nullable = false)
    private LocalDateTime dataEmissao = LocalDateTime.now();

    @Column(nullable = false)
    private boolean utilizado = false;

    @OneToOne(optional = false)
    @JoinColumn(name = "transacao_id", nullable = false, unique = true)
    private TransacaoResgate transacao;

    /** Cria o cupom de um resgate com um código aleatório no formato XXXX-XXXX. */
    public static Cupom gerar(TransacaoResgate transacao) {
        Cupom cupom = new Cupom();
        cupom.codigo = novoCodigo();
        cupom.transacao = transacao;
        transacao.setCupom(cupom);
        return cupom;
    }

    private static String novoCodigo() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            if (i == 4) sb.append('-');
            sb.append(ALFABETO.charAt(RANDOM.nextInt(ALFABETO.length())));
        }
        return sb.toString();
    }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public LocalDateTime getDataEmissao() { return dataEmissao; }
    public boolean isUtilizado() { return utilizado; }
    public void setUtilizado(boolean utilizado) { this.utilizado = utilizado; }
    public TransacaoResgate getTransacao() { return transacao; }
    public void setTransacao(TransacaoResgate transacao) { this.transacao = transacao; }
}
