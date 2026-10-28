package br.pucminas.moedaestudantil.service;

import br.pucminas.moedaestudantil.model.Aluno;
import br.pucminas.moedaestudantil.model.Cupom;
import br.pucminas.moedaestudantil.model.EmpresaParceira;
import br.pucminas.moedaestudantil.model.TransacaoEnvio;
import br.pucminas.moedaestudantil.model.Vantagem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.format.DateTimeFormatter;

/**
 * Notificações por e-mail. As mensagens são montadas dentro da transação (com os dados já carregados)
 * e enviadas só depois do commit, para que uma operação desfeita não gere e-mail e uma falha de SMTP
 * não desfaça a operação. Sem {@code spring.mail.host} configurado, o envio é simulado no log.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ObjectProvider<JavaMailSender> mailSender;
    private final String remetente;

    public EmailService(ObjectProvider<JavaMailSender> mailSender, @Value("${app.email.remetente}") String remetente) {
        this.mailSender = mailSender;
        this.remetente = remetente;
    }

    public void notificarRecebimento(Aluno aluno, TransacaoEnvio transacao) {
        enviar(aluno.getEmail(), "Você recebeu " + transacao.getValor() + " moedas!",
                """
                Olá, %s!

                O(A) professor(a) %s enviou %d moedas para você em %s.

                Motivo: %s

                Seu saldo atual é de %d moedas. Acesse o sistema para trocá-las por vantagens.

                Sistema de Moeda Estudantil
                """.formatted(aluno.getNome(), transacao.getProfessor().getNome(), transacao.getValor(),
                        DATA.format(transacao.getData()), transacao.getMotivo(), aluno.getSaldoMoedas()));
    }

    public void enviarCupomAluno(Aluno aluno, Cupom cupom) {
        Vantagem vantagem = cupom.getTransacao().getVantagem();
        enviar(aluno.getEmail(), "Seu cupom: " + vantagem.getNome() + " (" + cupom.getCodigo() + ")",
                """
                Olá, %s!

                Você trocou %d moedas pela vantagem "%s", oferecida por %s.

                CÓDIGO DO CUPOM: %s

                Apresente este código ao parceiro na troca presencial.
                Emitido em %s. Seu saldo atual é de %d moedas.

                Sistema de Moeda Estudantil
                """.formatted(aluno.getNome(), vantagem.getCustoMoedas(), vantagem.getNome(),
                        vantagem.getEmpresa().getNome(), cupom.getCodigo(), DATA.format(cupom.getDataEmissao()),
                        aluno.getSaldoMoedas()));
    }

    public void enviarConferenciaParceiro(EmpresaParceira empresa, Cupom cupom) {
        Vantagem vantagem = cupom.getTransacao().getVantagem();
        Aluno aluno = cupom.getTransacao().getAluno();
        enviar(empresa.getEmail(), "Novo resgate: " + vantagem.getNome() + " (" + cupom.getCodigo() + ")",
                """
                Olá, %s!

                O(A) aluno(a) %s resgatou a vantagem "%s" em %s.

                CÓDIGO PARA CONFERÊNCIA: %s

                Quando o aluno apresentar o cupom, confira se o código é o mesmo e registre a utilização
                na tela "Resgates" do sistema.

                Sistema de Moeda Estudantil
                """.formatted(empresa.getNome(), aluno.getNome(), vantagem.getNome(),
                        DATA.format(cupom.getDataEmissao()), cupom.getCodigo()));
    }

    private void enviar(String para, String assunto, String corpo) {
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(para);
        mensagem.setSubject(assunto);
        mensagem.setText(corpo);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    despachar(mensagem);
                }
            });
        } else {
            despachar(mensagem);
        }
    }

    private void despachar(SimpleMailMessage mensagem) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            log.info("[e-mail simulado] Para: {} | Assunto: {}\n{}",
                    String.join(", ", mensagem.getTo()), mensagem.getSubject(), mensagem.getText());
            return;
        }
        try {
            sender.send(mensagem);
            log.info("E-mail enviado para {}: {}", String.join(", ", mensagem.getTo()), mensagem.getSubject());
        } catch (MailException e) {
            log.warn("Falha ao enviar e-mail para {}: {}", String.join(", ", mensagem.getTo()), e.getMessage());
        }
    }
}
