package br.pucminas.moedaestudantil.model;

import jakarta.persistence.*;

/** Professor envia moedas a um aluno. */
@Entity
@DiscriminatorValue("ENVIO")
public class TransacaoEnvio extends Transacao {

    @Column(length = 500)
    private String motivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id")
    private Professor professor;

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public Professor getProfessor() { return professor; }
    public void setProfessor(Professor professor) { this.professor = professor; }
}
