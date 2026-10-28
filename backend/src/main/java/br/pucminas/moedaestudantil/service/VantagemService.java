package br.pucminas.moedaestudantil.service;

import br.pucminas.moedaestudantil.dao.EmpresaDAO;
import br.pucminas.moedaestudantil.dao.TransacaoDAO;
import br.pucminas.moedaestudantil.dao.VantagemDAO;
import br.pucminas.moedaestudantil.dto.VantagemRequest;
import br.pucminas.moedaestudantil.dto.VantagemResponse;
import br.pucminas.moedaestudantil.exception.RecursoNaoEncontradoException;
import br.pucminas.moedaestudantil.exception.RegraNegocioException;
import br.pucminas.moedaestudantil.model.EmpresaParceira;
import br.pucminas.moedaestudantil.model.Vantagem;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
public class VantagemService {

    private final VantagemDAO vantagemDAO;
    private final EmpresaDAO empresaDAO;
    private final TransacaoDAO transacaoDAO;
    private final ArmazenamentoFotoService fotos;

    public VantagemService(VantagemDAO vantagemDAO, EmpresaDAO empresaDAO, TransacaoDAO transacaoDAO,
                           ArmazenamentoFotoService fotos) {
        this.vantagemDAO = vantagemDAO;
        this.empresaDAO = empresaDAO;
        this.transacaoDAO = transacaoDAO;
        this.fotos = fotos;
    }

    @Transactional(readOnly = true)
    public List<VantagemResponse> listar(Long empresaId) {
        List<Vantagem> vantagens = empresaId == null
                ? vantagemDAO.findAllByOrderByCustoMoedasAsc()
                : vantagemDAO.findByEmpresaIdOrderByNomeAsc(empresaId);
        return vantagens.stream().map(VantagemResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public VantagemResponse buscar(Long id) {
        return VantagemResponse.from(buscarEntidade(id));
    }

    @Transactional
    public VantagemResponse cadastrar(Long empresaId, VantagemRequest dados, MultipartFile foto) {
        if (foto == null || foto.isEmpty()) {
            throw new RegraNegocioException(HttpStatus.BAD_REQUEST, "A foto do produto é obrigatória",
                    Map.of("foto", "A foto do produto é obrigatória"));
        }
        EmpresaParceira empresa = empresaDAO.findById(empresaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa parceira", empresaId));

        Vantagem vantagem = new Vantagem();
        preencher(vantagem, dados);
        vantagem.setFotoUrl(fotos.salvar(foto));
        empresa.cadastrarVantagem(vantagem);
        return VantagemResponse.from(vantagemDAO.save(vantagem));
    }

    /** A foto é opcional na edição: sem arquivo, mantém a atual. */
    @Transactional
    public VantagemResponse atualizar(Long empresaId, Long id, VantagemRequest dados, MultipartFile foto) {
        Vantagem vantagem = buscarDaEmpresa(empresaId, id);
        preencher(vantagem, dados);
        if (foto != null && !foto.isEmpty()) {
            String antiga = vantagem.getFotoUrl();
            vantagem.setFotoUrl(fotos.salvar(foto));
            fotos.remover(antiga);
        }
        return VantagemResponse.from(vantagem);
    }

    @Transactional
    public void remover(Long empresaId, Long id) {
        Vantagem vantagem = buscarDaEmpresa(empresaId, id);
        if (transacaoDAO.existsResgateDaVantagem(id)) {
            throw new RegraNegocioException(HttpStatus.CONFLICT,
                    "Esta vantagem já foi resgatada por alunos e não pode ser removida");
        }
        vantagem.getEmpresa().getVantagens().remove(vantagem);
        vantagemDAO.delete(vantagem);
        fotos.remover(vantagem.getFotoUrl());
    }

    private void preencher(Vantagem vantagem, VantagemRequest dados) {
        vantagem.setNome(dados.nome().trim());
        vantagem.setDescricao(dados.descricao().trim());
        vantagem.setCustoMoedas(dados.custoMoedas());
    }

    private Vantagem buscarDaEmpresa(Long empresaId, Long id) {
        Vantagem vantagem = buscarEntidade(id);
        if (!vantagem.getEmpresa().getId().equals(empresaId)) {
            throw RegraNegocioException.acessoNegado();
        }
        return vantagem;
    }

    private Vantagem buscarEntidade(Long id) {
        return vantagemDAO.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Vantagem", id));
    }
}
