package br.pucminas.moedaestudantil.model;

import jakarta.persistence.*;

/** Aluno troca moedas por uma vantagem, gerando um cupom. */
@Entity
@DiscriminatorValue("RESGATE")
public class TransacaoResgate extends Transacao {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vantagem_id")
    private Vantagem vantagem;

    @OneToOne(mappedBy = "transacao", cascade = CascadeType.ALL)
    private Cupom cupom;

    public Vantagem getVantagem() { return vantagem; }
    public void setVantagem(Vantagem vantagem) { this.vantagem = vantagem; }
    public Cupom getCupom() { return cupom; }
    public void setCupom(Cupom cupom) { this.cupom = cupom; }
}
