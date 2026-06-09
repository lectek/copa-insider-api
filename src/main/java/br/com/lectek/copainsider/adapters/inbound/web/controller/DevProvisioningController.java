package br.com.lectek.copainsider.adapters.inbound.web.controller;

import br.com.lectek.copainsider.application.service.DevProvisioningService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dev/provision")
public class DevProvisioningController {

    /**
     * Service responsible for dev provisioning flow.
     */
    private final DevProvisioningService service;

    /**
     * Creates controller with provisioning service dependency.
     *
     * @param provisioningService dev provisioning service
     */
    public DevProvisioningController(
            final DevProvisioningService provisioningService
    ) {
        this.service = provisioningService;
    }

    /**
     * Starts provisioning by sending a token to destination email.
     *
     * @param email destination email
     * @return provisioning start payload
     */
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> start(
            @RequestParam("email") @Email final String email
    ) {
        final String deliveryId = service.start(email);
        return ResponseEntity.ok(Map.of(
                "status",
                "sent",
                "deliveryId",
                deliveryId,
                "destination",
                email
        ));
    }

    /**
     * Verifies provisioning token and enables dev access.
     *
     * @param token verification token
     * @param password optional password for new account
     * @param cpfIfNew optional CPF for new account
     * @param nomeIfNew optional name for new account
     * @return provisioning completion payload
     */
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verify(
            @RequestParam("token") @NotBlank final String token,
            @RequestParam(value = "password", required = false)
            final String password,
            @RequestParam(value = "cpfIfNew", required = false)
            final String cpfIfNew,
            @RequestParam(value = "nomeIfNew", required = false)
            final String nomeIfNew
    ) {
        service.verifyByToken(token, password, cpfIfNew, nomeIfNew);
        return ResponseEntity.ok(Map.of("status", "dev-enabled"));
    }
}
