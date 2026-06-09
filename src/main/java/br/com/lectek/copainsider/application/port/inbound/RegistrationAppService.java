package br.com.lectek.copainsider.application.port.inbound;

import br.com.lectek.copainsider.application.dto.request.CadastroClienteRequestDTO;

public interface RegistrationAppService {
    void cadastrarNovoCliente(CadastroClienteRequestDTO request);
    void ativarEmailVerificado(String email);
}
