package br.pucminas.moedaestudantil.model;

import jakarta.persistence.*;

/**
 * Imagem de uma vantagem guardada no próprio banco. Na nuvem (Render) o disco do servidor é apagado a cada
 * reinício, então arquivos enviados não podem ficar no sistema de arquivos.
 */
@Entity
@Table(name = "foto")
public class Foto {

    public static final int TAMANHO_MAXIMO = 5 * 1024 * 1024;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_conteudo", nullable = false, length = 50)
    private String tipoConteudo;

    /** bytea no PostgreSQL, varbinary no H2. */
    @Column(nullable = false, length = TAMANHO_MAXIMO)
    private byte[] conteudo;

    protected Foto() {
    }

    public Foto(String tipoConteudo, byte[] conteudo) {
        this.tipoConteudo = tipoConteudo;
        this.conteudo = conteudo;
    }

    public Long getId() { return id; }
    public String getTipoConteudo() { return tipoConteudo; }
    public byte[] getConteudo() { return conteudo; }
}
