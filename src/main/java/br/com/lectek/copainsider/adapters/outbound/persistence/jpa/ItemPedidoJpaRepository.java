// src/main/java/br/com/lectek/copainsider/adapters/outbound/persistence/jpa/ItemPedidoJpaRepository.java
package br.com.lectek.copainsider.adapters.outbound.persistence.jpa;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ItemPedidoEntity;
import br.com.lectek.copainsider.domain.enums.StatusPedido;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ItemPedidoJpaRepository extends JpaRepository<ItemPedidoEntity, Long> {

    List<ItemPedidoEntity> findByPedidoStatusAndPedidoDataBetweenAndProdutoDisponivelTrueOrderByPedidoDataDesc(
            StatusPedido status,
            LocalDateTime de,
            LocalDateTime ate,
            Pageable pageable
    );

    // === Projection para Relatórios • Produtos ===
    interface ProdutoRelatorioRow {
        String getNome();
        Long getQtd();
        BigDecimal getTotal();
    }

    // Top produtos (sem filtro de período) — usa ip.subtotal
    @Query("""
        select ip.produto.nome as nome,
               sum(ip.quantidade) as qtd,
               coalesce(sum(ip.subtotal), 0) as total
          from ItemPedidoEntity ip
         group by ip.produto.id, ip.produto.nome
         order by sum(ip.quantidade) desc
    """)
    List<ProdutoRelatorioRow> listarResumoProdutos();

    // Versão com filtro por período (ajuste p.createdAt se seu Pedido tiver outro campo)
    @Query("""
        select ip.produto.nome as nome,
               sum(ip.quantidade) as qtd,
               coalesce(sum(ip.subtotal), 0) as total
          from ItemPedidoEntity ip
          join ip.pedido p
         where (:de  is null or p.createdAt >= :de)
           and (:ate is null or p.createdAt <  :ate)
         group by ip.produto.id, ip.produto.nome
         order by sum(ip.quantidade) desc
    """)
    List<ProdutoRelatorioRow> listarResumoProdutosPorPeriodo(LocalDateTime de, LocalDateTime ate);
}
