// src/main/java/br/com/redemaisfarma/application/service/user/UserService.java
package br.com.redemaisfarma.application.service.user;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.redemaisfarma.application.dto.user.UsuarioFormDTO;
import br.com.redemaisfarma.application.dto.user.UsuarioListRow;
import br.com.redemaisfarma.application.dto.user.UsuarioPerfilDTO;
import br.com.redemaisfarma.domain.user.Role;
import br.com.redemaisfarma.domain.user.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class UserService {

    private final UsuarioRepository usuarioRepository;
    private final RoleRepository roleRepository;

    public UserService(UsuarioRepository usuarioRepository,
                       RoleRepository roleRepository) {
        this.usuarioRepository = usuarioRepository;
        this.roleRepository = roleRepository;
    }

    private boolean deriveAtivo(UsuarioEntity u) {
        // sua entidade não tem "ativo"; considere sempre true (ou mude a regra)
        return true;
    }

    private String pickPapel(UsuarioEntity u) {
        return u.getRoles() == null || u.getRoles().isEmpty()
                ? ""
                : u.getRoles().stream()
                    .map(Role::getNome)
                    .filter(n -> n != null && !n.isBlank())
                    .sorted(Comparator.naturalOrder())
                    .findFirst()
                    .orElse("");
    }

    public List<UsuarioListRow> listar(String q, String papel) {
        return usuarioRepository.search(q, papel).stream()
                .map(u -> new UsuarioListRow(
                        u.getId(), u.getNome(), u.getEmail(), pickPapel(u), deriveAtivo(u)
                ))
                .toList();
    }

    public UsuarioPerfilDTO buscarPerfil(Long id) {
        var u = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        return new UsuarioPerfilDTO(
                u.getId(), u.getNome(), u.getEmail(), u.getCpf(),
                pickPapel(u), deriveAtivo(u), u.getUltimoAcesso()
        );
    }

    public UsuarioFormDTO buscarForm(Long id) {
        var u = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        return new UsuarioFormDTO(
                u.getId(), u.getNome(), u.getEmail(), u.getCpf(),
                pickPapel(u), deriveAtivo(u)
        );
    }

    @Transactional
    public void salvar(UsuarioFormDTO dto) {
        UsuarioEntity u = (dto.id() != null)
                ? usuarioRepository.findById(dto.id()).orElse(new UsuarioEntity())
                : new UsuarioEntity();

        u.setNome(dto.nome());
        u.setEmail(dto.email());
        u.setCpf(dto.cpf());

        // Papel único selecionado no form
        if (dto.papel() != null && !dto.papel().isBlank()) {
            Role role = roleRepository.findByNome(dto.papel())
                    .orElseThrow(() -> new IllegalArgumentException("Papel não encontrado: " + dto.papel()));
            u.getRoles().clear();
            u.addRole(role);
        } else {
            u.getRoles().clear();
        }

        usuarioRepository.save(u);
    }
}
