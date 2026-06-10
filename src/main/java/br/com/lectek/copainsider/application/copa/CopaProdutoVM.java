package br.com.lectek.copainsider.application.copa;

import br.com.lectek.copainsider.domain.enums.TipoCopaProduto;
import java.math.BigDecimal;

public record CopaProdutoVM(
        String slug,
        TipoCopaProduto tipo,
        BigDecimal preco,
        BigDecimal precoEur,
        String nome,
        String descricao,
        String hotmartUrl,
        String imagemUrl,
        String slugTime1,
        String slugTime2,
        boolean categoriaComMultiplos,
        long totalNaCategoria
) {
    public boolean temHotmartUrl() {
        return hotmartUrl != null && !hotmartUrl.isBlank() && !"#".equals(hotmartUrl);
    }
}
