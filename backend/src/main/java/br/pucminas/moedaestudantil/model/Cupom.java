package br.pucminas.moedaestudantil.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cupom")
public class Cupom {

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

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public LocalDateTime getDataEmissao() { return dataEmissao; }
    public boolean isUtilizado() { return utilizado; }
    public void setUtilizado(boolean utilizado) { this.utilizado = utilizado; }
    public TransacaoResgate getTransacao() { return transacao; }
    public void setTransacao(TransacaoResgate transacao) { this.transacao = transacao; }
}
