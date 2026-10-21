package br.pucminas.moedaestudantil.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "empresa_parceira")
@PrimaryKeyJoinColumn(name = "id")
public class EmpresaParceira extends Usuario {

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true, length = 14)
    private String cnpj;

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Vantagem> vantagens = new ArrayList<>();

    public void cadastrarVantagem(Vantagem vantagem) {
        vantagem.setEmpresa(this);
        vantagens.add(vantagem);
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }
    public List<Vantagem> getVantagens() { return vantagens; }
}
