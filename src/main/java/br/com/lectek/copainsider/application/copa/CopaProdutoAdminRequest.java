package br.com.lectek.copainsider.application.copa;

import br.com.lectek.copainsider.domain.enums.TipoCopaProduto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CopaProdutoAdminRequest(

        @NotBlank
        String slug,

        @NotNull
        TipoCopaProduto tipo,

        @NotNull @DecimalMin("0.01")
        BigDecimal preco,

        @NotBlank
        String nomePtBr,

        String nomePtPt,
        String nomeEn,

        @NotBlank
        String descPtBr,

        String descPtPt,
        String descEn,

        /** URL de checkout no Hotmart — pode ser null ou "#" enquanto não configurado */
        String hotmartUrl,

        String imagemUrl,
        String slugTime1,
        String slugTime2,

        boolean ativo,
        int ordem
) {}
