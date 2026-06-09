/*
 * Decompiled with CFR 0.152.
 */
package br.com.lectek.copainsider.application.port.inbound;

import br.com.lectek.copainsider.adapters.inbound.web.dto.ImageGenRequestDTO;

public interface ImageStudioUseCase {
    GeneratedImageResult generateSync(ImageGenRequestDTO request);

    record GeneratedImageResult(
            String reference,
            String mimeType,
            String provider,
            String sourcePrompt,
            String revisedPrompt
    ) {
    }
}
