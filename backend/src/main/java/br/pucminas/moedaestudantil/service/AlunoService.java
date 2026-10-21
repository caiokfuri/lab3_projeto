package br.pucminas.moedaestudantil.service;

import br.pucminas.moedaestudantil.dao.AlunoDAO;
import br.pucminas.moedaestudantil.dao.InstituicaoDAO;
import br.pucminas.moedaestudantil.dao.TransacaoDAO;
import br.pucminas.moedaestudantil.dto.AlunoRequest;
import br.pucminas.moedaestudantil.dto.AlunoResponse;
import br.pucminas.moedaestudantil.exception.RecursoNaoEncontradoException;
import br.pucminas.moedaestudantil.exception.RegraNegocioException;
import br.pucminas.moedaestudantil.model.Aluno;
import br.pucminas.moedaestudantil.model.InstituicaoEnsino;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AlunoService {

    private final AlunoDAO alunoDAO;
    private final InstituicaoDAO instituicaoDAO;
    private final TransacaoDAO transacaoDAO;
    private final UsuarioService usuarioService;

    public AlunoService(AlunoDAO alunoDAO, InstituicaoDAO instituicaoDAO, TransacaoDAO transacaoDAO,
                        UsuarioService usuarioService) {
        this.alunoDAO = alunoDAO;
        this.instituicaoDAO = instituicaoDAO;
        this.transacaoDAO = transacaoDAO;
        this.usuarioService = usuarioService;
    }

    @Transactional(readOnly = true)
    public List<AlunoResponse> listar() {
        return alunoDAO.findAllByOrderByNomeAsc().stream().map(AlunoResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public AlunoResponse buscar(Long id) {
        return AlunoResponse.from(buscarEntidade(id));
    }

    @Transactional
    public AlunoResponse cadastrar(AlunoRequest dados) {
        Aluno aluno = new Aluno();
        preencher(aluno, dados);
        return AlunoResponse.from(alunoDAO.save(aluno));
    }

    @Transactional
    public AlunoResponse atualizar(Long id, AlunoRequest dados) {
        Aluno aluno = buscarEntidade(id);
        preencher(aluno, dados);
        return AlunoResponse.from(alunoDAO.save(aluno));
    }

    @Transactional
    public void remover(Long id) {
        Aluno aluno = buscarEntidade(id);
        if (transacaoDAO.existsByAlunoId(id)) {
            throw new RegraNegocioException(HttpStatus.CONFLICT,
                    "Aluno possui transações registradas e não pode ser removido");
        }
        alunoDAO.delete(aluno);
    }

    private void preencher(Aluno aluno, AlunoRequest dados) {
        String cpf = UsuarioService.somenteDigitos(dados.cpf());
        boolean cpfEmUso = aluno.getId() == null
                ? alunoDAO.existsByCpf(cpf)
                : alunoDAO.existsByCpfAndIdNot(cpf, aluno.getId());
        if (cpfEmUso) {
            throw RegraNegocioException.conflito("cpf", "CPF já cadastrado");
        }

        InstituicaoEnsino instituicao = instituicaoDAO.findById(dados.instituicaoId())
                .orElseThrow(() -> UsuarioService.campoInvalido("instituicaoId", "Instituição não encontrada"));

        usuarioService.aplicarCredenciais(aluno, dados.login(), dados.email(), dados.senha());
        aluno.setNome(dados.nome().trim());
        aluno.setCpf(cpf);
        aluno.setRg(dados.rg().trim());
        aluno.setEndereco(dados.endereco().trim());
        aluno.setCurso(dados.curso().trim());
        aluno.setInstituicao(instituicao);
    }

    private Aluno buscarEntidade(Long id) {
        return alunoDAO.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Aluno", id));
    }
}
