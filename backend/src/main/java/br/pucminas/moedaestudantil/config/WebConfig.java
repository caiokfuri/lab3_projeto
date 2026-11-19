package br.pucminas.moedaestudantil.config;

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
    private final String frontendDir;
    private final String[] origensPermitidas;

    public WebConfig(AutenticacaoInterceptor autenticacaoInterceptor,
                     @Value("${app.frontend.dir}") String frontendDir,
                     @Value("${app.cors.origens}") String[] origensPermitidas) {
        this.autenticacaoInterceptor = autenticacaoInterceptor;
        this.frontendDir = frontendDir.endsWith("/") ? frontendDir : frontendDir + "/";
        this.origensPermitidas = origensPermitidas;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(autenticacaoInterceptor).addPathPatterns("/api/**");
    }

    /**
     * Front-end em outra origem: o Vite em desenvolvimento (localhost:5173) e a Vercel em produção.
     * A autenticação é por token no cabeçalho (não por cookie), então liberar a origem não expõe a sessão.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(origensPermitidas)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE");
    }

    /** O handler de recursos ignora o caminho vazio; a raiz vai direto para o index.html do React. */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
    }

    /**
     * Execução local: o próprio Spring serve o build do React. Rotas do front-end que não são arquivos
     * devolvem o index.html, para que o React Router trate a URL (ex.: recarregar em /aluno/extrato).
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
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
