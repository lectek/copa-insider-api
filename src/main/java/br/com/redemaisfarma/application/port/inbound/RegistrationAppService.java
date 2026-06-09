package br.com.redemaisfarma.application.port.inbound;

import br.com.redemaisfarma.application.dto.request.CadastroClienteRequestDTO;

public interface RegistrationAppService {
    void cadastrarNovoCliente(CadastroClienteRequestDTO request);
}
