package br.com.redemaisfarma.domain.financeiro.config;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class GatewayConfigServiceImpl implements GatewayConfigService {
    private final GatewayConfigRepository repo;

    public GatewayConfigServiceImpl(GatewayConfigRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GatewayConfig> listar(Boolean onlyActive, String provider) {
        if (provider != null && !provider.isBlank()) {
            if (Boolean.TRUE.equals(onlyActive)) {
                return repo.findByAtivoAndProvedorIgnoreCaseOrderByAtualizadoEmDesc(true, provider);
            }
            if (Boolean.FALSE.equals(onlyActive)) {
                return repo.findByAtivoAndProvedorIgnoreCaseOrderByAtualizadoEmDesc(false, provider);
            }
            return repo.findByProvedorIgnoreCaseOrderByAtualizadoEmDesc(provider);
        }
        if (Boolean.TRUE.equals(onlyActive)) return repo.findByAtivoOrderByAtualizadoEmDesc(true);
        if (Boolean.FALSE.equals(onlyActive)) return repo.findByAtivoOrderByAtualizadoEmDesc(false);
        return repo.findAllByOrderByAtualizadoEmDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GatewayConfig> buscarPorId(Long id) {
        return repo.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GatewayConfig> buscarAtivaPorProvedor(String provider) {
        return repo.findFirstByProvedorIgnoreCaseAndAtivoTrue(provider);
    }

    @Override
    @Transactional
    public GatewayConfig criar(GatewayConfig nova) {
        nova.setId(null);
        validarObrigatorios(nova);
        validarUnicidade(nova.getProvedor(), nova.getNome(), null);
        if (nova.isAtivo()) {
            desativarAtivaAnterior(nova.getProvedor(), null);
        }
        return repo.save(nova);
    }

    @Override
    @Transactional
    public GatewayConfig atualizar(Long id, GatewayConfig alterada) {
        GatewayConfig atual = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Config não encontrada: id=" + id));

        validarObrigatorios(alterada);
        validarUnicidade(alterada.getProvedor(), alterada.getNome(), id);

        atual.setNome(alterada.getNome());
        atual.setProvedor(alterada.getProvedor());
        atual.setApiKey(alterada.getApiKey());
        atual.setApiSecret(alterada.getApiSecret());
        atual.setWebhookUrl(alterada.getWebhookUrl());
        atual.setTimeoutMs(alterada.getTimeoutMs());
        atual.setMaxRetries(alterada.getMaxRetries());
        atual.setMetadata(alterada.getMetadata());

        if (alterada.isAtivo() && !atual.isAtivo()) {
            desativarAtivaAnterior(atual.getProvedor(), id);
            atual.setAtivo(true);
        } else if (!alterada.isAtivo() && atual.isAtivo()) {
            atual.setAtivo(false);
        }
        return repo.save(atual);
    }

    @Override
    @Transactional
    public void remover(Long id) {
        GatewayConfig cfg = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Config não encontrada: id=" + id));
        if (cfg.isAtivo()) {
            throw new IllegalStateException("Não é permitido remover a configuração ATIVA do provedor " + cfg.getProvedor());
        }
        repo.deleteById(id);
    }

    @Override
    @Transactional
    public GatewayConfig ativar(Long id, boolean ativo) {
        GatewayConfig target = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Config não encontrada: id=" + id));

        if (ativo) {
            if (!target.isAtivo()) {
                desativarAtivaAnterior(target.getProvedor(), id);
                target.setAtivo(true);
                target = repo.save(target);
            }
        } else if (target.isAtivo()) {
            target.setAtivo(false);
            target = repo.save(target);
        }
        return target;
    }

    private void validarObrigatorios(GatewayConfig c) {
        if (c.getProvedor() == null || c.getProvedor().isBlank())
            throw new IllegalArgumentException("Provedor é obrigatório");
        if (c.getNome() == null || c.getNome().isBlank())
            throw new IllegalArgumentException("Nome é obrigatório");
        if (c.getApiKey() == null || c.getApiKey().isBlank())
            throw new IllegalArgumentException("API Key é obrigatória");
    }

    private void validarUnicidade(String provedor, String nome, Long currentId) {
        String p = provedor == null ? null : provedor.toLowerCase(Locale.ROOT);
        String n = nome == null ? null : nome.toLowerCase(Locale.ROOT);
        boolean exists = (currentId == null)
                ? repo.existsByProvedorIgnoreCaseAndNomeIgnoreCase(p, n)
                : repo.existsByProvedorIgnoreCaseAndNomeIgnoreCaseAndIdNot(p, n, currentId);
        if (exists) {
            throw new IllegalStateException("Já existe configuração com esse Nome para o provedor " + provedor);
        }
    }

    private void desativarAtivaAnterior(String provedor, Long exceptId) {
        repo.findFirstByProvedorIgnoreCaseAndAtivoTrue(provedor).ifPresent(old -> {
            if (exceptId == null || !old.getId().equals(exceptId)) {
                old.setAtivo(false);
                repo.save(old);
            }
        });
    }
}
