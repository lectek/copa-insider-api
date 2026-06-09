package br.com.redemaisfarma.application.service.auth;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.UsuarioJpaRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ClienteRepository;
import br.com.redemaisfarma.application.core.exception.CpfDuplicadoException;
import br.com.redemaisfarma.application.core.exception.EmailDuplicadoException;
import br.com.redemaisfarma.application.dto.request.CadastroClienteRequestDTO;
import br.com.redemaisfarma.application.port.inbound.RegistrationAppService;
import br.com.redemaisfarma.domain.user.Role;
import br.com.redemaisfarma.domain.user.RoleRepository;
import java.util.Locale;
import java.util.Objects;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationAppServiceImpl implements RegistrationAppService {

    private static final String ROLE_CLIENTE = "ROLE_CLIENTE";
    private static final String CLIENTE = "CLIENTE";
    private static final String ROLE_USER = "ROLE_USER";
    private static final String USER = "USER";

    private final UsuarioJpaRepository usuarioRepo;
    private final ClienteRepository clienteRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder encoder;

    public RegistrationAppServiceImpl(UsuarioJpaRepository usuarioRepo,
                                      ClienteRepository clienteRepo,
                                      RoleRepository roleRepo,
                                      PasswordEncoder encoder) {
        this.usuarioRepo = usuarioRepo;
        this.clienteRepo = clienteRepo;
        this.roleRepo = roleRepo;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void cadastrarNovoCliente(CadastroClienteRequestDTO request) {
        final String email = normalizeEmail(request.getEmail());
        final String cpf = normalizeCpf(request.getCpf());
        final String nome = safe(request.getNome());
        final String raw = safe(request.getSenha());
        final String telefone = safe(request.getTelefone());

        // user uniqueness
        if (usuarioRepo.existsByEmail(email)) {
            throw new EmailDuplicadoException();
        }
        if (usuarioRepo.findByEmailOrCpf(cpf).isPresent()) {
            throw new CpfDuplicadoException();
        }

        UsuarioEntity u = new UsuarioEntity();
        u.setEmail(email);
        u.setCpf(cpf);
        u.setNome(nome);
        u.setTelefone(telefone);

        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Senha obrigatoria.");
        }
        String encodedPassword = encoder.encode(raw);
        u.setSenha(encodedPassword);

        u.addRole(resolveCustomerRole());
        usuarioRepo.save(u);

        syncClienteCadastro(request, email, cpf, nome, telefone, encodedPassword);
    }

    private static String safe(String v) {
        return v == null ? null : v.trim();
    }

    private static String normalizeEmail(String value) {
        String cleaned = safe(value);
        return cleaned == null ? null : cleaned.toLowerCase(Locale.ROOT);
    }

    private static String normalizeCpf(String value) {
        if (value == null) {
            return null;
        }
        String digits = value.replaceAll("\\D", "");
        return digits.isBlank() ? null : digits;
    }

    private void syncClienteCadastro(CadastroClienteRequestDTO request,
                                     String email,
                                     String cpf,
                                     String nome,
                                     String telefone,
                                     String encodedPassword) {
        var byEmail = clienteRepo.findByEmailIgnoreCase(email);
        var byCpf = clienteRepo.findByCpf(cpf);

        if (byEmail.isPresent() && byCpf.isPresent()
                && !Objects.equals(byEmail.get().getId(), byCpf.get().getId())) {
            throw new IllegalStateException("Cadastro inconsistente para email/cpf.");
        }

        ClienteEntity cliente = byEmail.or(() -> byCpf).orElseGet(ClienteEntity::new);
        cliente.setNome(nome);
        cliente.setEmail(email);
        cliente.setCpf(cpf);
        cliente.setTelefone(telefone);
        cliente.setDataDeNascimento(request.getDataDeNascimento());
        cliente.setAtivo(true);
        cliente.setSenha(encodedPassword);
        clienteRepo.save(cliente);
    }

    private Role resolveCustomerRole() {
        return roleRepo.findByNome(ROLE_CLIENTE)
                .or(() -> roleRepo.findByNome(CLIENTE))
                .or(() -> roleRepo.findByNome(ROLE_USER))
                .or(() -> roleRepo.findByNome(USER))
                .orElseGet(this::createCustomerRoleSafely);
    }

    private Role createCustomerRoleSafely() {
        try {
            return roleRepo.save(Role.of(ROLE_CLIENTE));
        } catch (DataIntegrityViolationException ex) {
            return roleRepo.findByNome(ROLE_CLIENTE)
                    .or(() -> roleRepo.findByNome(CLIENTE))
                    .or(() -> roleRepo.findByNome(ROLE_USER))
                    .or(() -> roleRepo.findByNome(USER))
                    .orElseThrow(() -> ex);
        }
    }
}
