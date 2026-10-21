package br.pucminas.moedaestudantil.service;

import br.pucminas.moedaestudantil.dao.EmpresaDAO;
import br.pucminas.moedaestudantil.dao.TransacaoDAO;
import br.pucminas.moedaestudantil.dto.EmpresaRequest;
import br.pucminas.moedaestudantil.dto.EmpresaResponse;
import br.pucminas.moedaestudantil.exception.RecursoNaoEncontradoException;
import br.pucminas.moedaestudantil.exception.RegraNegocioException;
import br.pucminas.moedaestudantil.model.EmpresaParceira;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmpresaService {

    private final EmpresaDAO empresaDAO;
    private final TransacaoDAO transacaoDAO;
    private final UsuarioService usuarioService;

    public EmpresaService(EmpresaDAO empresaDAO, TransacaoDAO transacaoDAO, UsuarioService usuarioService) {
        this.empresaDAO = empresaDAO;
        this.transacaoDAO = transacaoDAO;
        this.usuarioService = usuarioService;
    }

    @Transactional(readOnly = true)
    public List<EmpresaResponse> listar() {
        return empresaDAO.findAllByOrderByNomeAsc().stream().map(EmpresaResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public EmpresaResponse buscar(Long id) {
        return EmpresaResponse.from(buscarEntidade(id));
    }

    @Transactional
    public EmpresaResponse cadastrar(EmpresaRequest dados) {
        EmpresaParceira empresa = new EmpresaParceira();
        preencher(empresa, dados);
        return EmpresaResponse.from(empresaDAO.save(empresa));
    }

    @Transactional
    public EmpresaResponse atualizar(Long id, EmpresaRequest dados) {
        EmpresaParceira empresa = buscarEntidade(id);
        preencher(empresa, dados);
        return EmpresaResponse.from(empresaDAO.save(empresa));
    }

    @Transactional
    public void remover(Long id) {
        EmpresaParceira empresa = buscarEntidade(id);
        if (transacaoDAO.existsResgateDaEmpresa(id)) {
            throw new RegraNegocioException(HttpStatus.CONFLICT,
                    "Empresa possui vantagens já resgatadas e não pode ser removida");
        }
        empresaDAO.delete(empresa);
    }

    private void preencher(EmpresaParceira empresa, EmpresaRequest dados) {
        String cnpj = UsuarioService.somenteDigitos(dados.cnpj());
        boolean cnpjEmUso = empresa.getId() == null
                ? empresaDAO.existsByCnpj(cnpj)
                : empresaDAO.existsByCnpjAndIdNot(cnpj, empresa.getId());
        if (cnpjEmUso) {
            throw RegraNegocioException.conflito("cnpj", "CNPJ já cadastrado");
        }

        usuarioService.aplicarCredenciais(empresa, dados.login(), dados.email(), dados.senha());
        empresa.setNome(dados.nome().trim());
        empresa.setCnpj(cnpj);
    }

    private EmpresaParceira buscarEntidade(Long id) {
        return empresaDAO.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Empresa parceira", id));
    }
}
