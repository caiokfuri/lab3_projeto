package br.pucminas.moedaestudantil.config;

import br.pucminas.moedaestudantil.service.ArmazenamentoFotoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AutenticacaoInterceptor autenticacaoInterceptor;
    private final ArmazenamentoFotoService fotos;
    private final String frontendDir;

    public WebConfig(AutenticacaoInterceptor autenticacaoInterceptor, ArmazenamentoFotoService fotos,
                     @Value("${app.frontend.dir}") String frontendDir) {
        this.autenticacaoInterceptor = autenticacaoInterceptor;
        this.fotos = fotos;
        this.frontendDir = frontendDir.endsWith("/") ? frontendDir : frontendDir + "/";
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(autenticacaoInterceptor).addPathPatterns("/api/**");
    }

    /** Permite rodar o front-end pelo servidor de desenvolvimento do Vite chamando a API na 8080. */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE");
    }

    /** O handler de recursos ignora o caminho vazio; a raiz vai direto para o index.html do React. */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(ArmazenamentoFotoService.PREFIXO_URL + "**")
                .addResourceLocations(fotos.getDiretorio().toUri().toString());

        // Build do React (SPA): rotas do front-end que não são arquivos devolvem o index.html,
        // para que o React Router trate a URL (ex.: recarregar a página em /aluno/extrato).
        registry.addResourceHandler("/**")
                .addResourceLocations("file:" + frontendDir)
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String caminho, Resource local) throws IOException {
                        Resource recurso = local.createRelative(caminho);
                        if (recurso.exists() && recurso.isReadable()) return recurso;
                        if (caminho.startsWith("api/") || caminho.contains(".")) return null;
                        Resource index = local.createRelative("index.html");
                        return index.exists() ? index : null;
                    }
                });
    }
}
