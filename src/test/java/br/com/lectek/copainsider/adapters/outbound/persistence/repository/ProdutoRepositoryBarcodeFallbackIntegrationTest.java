package br.com.lectek.copainsider.adapters.outbound.persistence.repository;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.MetodoLeituraCodigoBarras;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoStatus;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.ProdutoJpaRepository;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = ProdutoRepositoryBarcodeFallbackIntegrationTest.TestApplication.class,
        properties = {
                "spring.datasource.url=jdbc:tc:mysql:8.0.43:///copainsider_test?useSSL=false&useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC",
                "spring.datasource.username=test",
                "spring.datasource.password=test",
                "spring.jpa.open-in-view=false",
                "spring.jpa.hibernate.ddl-auto=none",
                "spring.jpa.properties.hibernate.format_sql=false",
                "spring.jpa.properties.hibernate.show_sql=false",
                "spring.flyway.enabled=true",
                "spring.flyway.locations=classpath:db/migration,classpath:db/migration-mysql",
                "spring.flyway.validate-on-migrate=true",
                "spring.flyway.baseline-on-migrate=true",
                "spring.flyway.table=flyway_schema_history",
                "spring.task.scheduling.enabled=false",
                "spring.autoconfigure.exclude="
                        + "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration"
        }
)
@EnabledIfEnvironmentVariable(named = "RUN_DOCKER_TESTS", matches = "true")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class ProdutoRepositoryBarcodeFallbackIntegrationTest {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ProdutoJpaRepository produtoJpaRepository;

    @Test
    void codigoOriginalValidoContaComoBarcodePublicavelNasConsultas() {
        long baseline = this.produtoRepository.countPubliclySellable();
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        ProdutoEntity produto = new ProdutoEntity();
        produto.setNome("Produto fallback " + suffix);
        produto.setDescricao("Teste de barcode fallback " + suffix);
        produto.setCategoria("CAT-" + suffix);
        produto.setPrecoVenda(new BigDecimal("19.90"));
        produto.setPrecoCusto(new BigDecimal("11.50"));
        produto.setEstoque(5);
        produto.setDisponivel(Boolean.TRUE);
        produto.setStatus(ProdutoStatus.PUBLICADO);
        produto.setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.MANUAL);
        produto.setCodigoOriginal(7891234567895L);
        produto.setDataCadastro(LocalDate.now());

        ProdutoEntity persisted = this.produtoRepository.saveAndFlush(produto);

        assertThat(this.produtoRepository.countPubliclySellable()).isEqualTo(baseline + 1);
        assertThat(this.produtoRepository.findPublicById(persisted.getId())).isPresent();
        assertThat(this.produtoJpaRepository.findPublicById(persisted.getId())).isPresent();
        assertThat(this.produtoRepository.findStockSubscribableById(persisted.getId())).isPresent();
        assertThat(this.produtoRepository.searchPublicPage(persisted.getNome(), PageRequest.of(0, 10)).getContent())
                .extracting(ProdutoEntity::getId)
                .contains(persisted.getId());
        assertThat(this.produtoJpaRepository.searchPublicPage(persisted.getNome(), PageRequest.of(0, 10)).getContent())
                .extracting(ProdutoEntity::getId)
                .contains(persisted.getId());
        assertThat(this.produtoRepository.findDistinctCategoriasPublicas()).contains(persisted.getCategoria());
        assertThat(this.produtoJpaRepository.findDistinctCategoriasPublicas()).contains(persisted.getCategoria());
        assertThat(this.produtoRepository.searchNaoDisponiveis(persisted.getNome(), PageRequest.of(0, 10)).getContent())
                .extracting(ProdutoEntity::getId)
                .doesNotContain(persisted.getId());
    }

    @Test
    void codigoOriginalComOnzeDigitosContinuaValendoComoBarcodePublicavel() {
        long baseline = this.produtoRepository.countPubliclySellable();
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        ProdutoEntity produto = new ProdutoEntity();
        produto.setNome("Produto zero esquerda " + suffix);
        produto.setDescricao("Teste barcode 11 digitos " + suffix);
        produto.setCategoria("CATZ-" + suffix);
        produto.setPrecoVenda(new BigDecimal("21.90"));
        produto.setPrecoCusto(new BigDecimal("13.10"));
        produto.setEstoque(4);
        produto.setDisponivel(Boolean.TRUE);
        produto.setStatus(ProdutoStatus.PUBLICADO);
        produto.setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.MANUAL);
        produto.setCodigoOriginal(12345678901L);
        produto.setDataCadastro(LocalDate.now());

        ProdutoEntity persisted = this.produtoRepository.saveAndFlush(produto);

        assertThat(persisted.getCodigoBarras()).isEqualTo("012345678901");
        assertThat(this.produtoRepository.countPubliclySellable()).isEqualTo(baseline + 1);
        assertThat(this.produtoRepository.findPublicById(persisted.getId())).isPresent();
        assertThat(this.produtoJpaRepository.findPublicById(persisted.getId())).isPresent();
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan(basePackages = "br.com.lectek.copainsider")
    @EnableJpaRepositories(basePackages = {
            "br.com.lectek.copainsider.adapters.outbound.persistence",
            "br.com.lectek.copainsider.domain.financeiro.config",
            "br.com.lectek.copainsider.domain.financeiro.mercadopago",
            "br.com.lectek.copainsider.domain.user",
            "br.com.lectek.copainsider.user.audit"
    })
    static class TestApplication {
    }
}
