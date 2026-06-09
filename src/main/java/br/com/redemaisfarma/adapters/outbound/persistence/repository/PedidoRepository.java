// src/main/java/br/com/redemaisfarma/adapters/outbound/persistence/repository/PedidoRepository.java
package br.com.redemaisfarma.adapters.outbound.persistence.repository;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.redemaisfarma.domain.enums.ModoEntrega;
import br.com.redemaisfarma.domain.enums.StatusPedido;
import br.com.redemaisfarma.domain.enums.TipoPagamento;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;

public interface PedidoRepository extends JpaRepository<PedidoEntity, Long> {

    long countByStatus(StatusPedido status);

    // ====== Relatório • Clientes ======
    interface ClienteRelatorioRow {
        String getNome();
        Long getQtdPedidos();
        BigDecimal getValorTotal();
    }

    @Query("""
           select c.nome as nome,
                  count(p) as qtdPedidos,
                  coalesce(sum(coalesce(p.total, 0)), 0) as valorTotal
             from PedidoEntity p
             join p.cliente c
            group by c.id, c.nome
            order by coalesce(sum(coalesce(p.total, 0)), 0) desc
           """)
    List<ClienteRelatorioRow> listarResumoPorCliente();

    // ====== Relatório • Vendas por dia ======
    interface VendasRelatorioRowDia {
        LocalDate getData();
        Long getQtd();
        BigDecimal getTotal();
    }

    @Query("""
        select function('date', p.createdAt) as data,
               count(p) as qtd,
               coalesce(sum(coalesce(p.total, 0)), 0) as total
          from PedidoEntity p
         where (:de  is null or p.createdAt >= :de)
           and (:ate is null or p.createdAt <  :ate)
         group by function('date', p.createdAt)
         order by function('date', p.createdAt) asc
    """)
    List<VendasRelatorioRowDia> listarResumoVendasPorDia(LocalDateTime de, LocalDateTime ate);

    // ====== (Opcional) Relatório • Vendas por mês ======
    interface VendasRelatorioRowMes {
        Integer getAno();
        Integer getMes();
        Long getQtd();
        BigDecimal getTotal();
    }

    interface VendasPagamentoPorDiaRow {
        LocalDate getData();
        TipoPagamento getTipoPagamento();
        Long getQtd();
        BigDecimal getTotal();
    }

    interface PagamentoResumoRow {
        TipoPagamento getTipoPagamento();
        Long getQtd();
        BigDecimal getTotal();
    }

    @Query("""
        select year(p.createdAt) as ano,
               month(p.createdAt) as mes,
               count(p) as qtd,
               coalesce(sum(coalesce(p.total, 0)), 0) as total
          from PedidoEntity p
         where (:de  is null or p.createdAt >= :de)
           and (:ate is null or p.createdAt <  :ate)
         group by year(p.createdAt), month(p.createdAt)
         order by year(p.createdAt), month(p.createdAt)
    """)
    List<VendasRelatorioRowMes> listarResumoVendasPorMes(LocalDateTime de, LocalDateTime ate);

    @Query("""
        select function('date', p.data) as data,
               p.tipoPagamento as tipoPagamento,
               count(p) as qtd,
               coalesce(sum(coalesce(p.total, 0)), 0) as total
          from PedidoEntity p
         where (:de is null or p.data >= :de)
           and (:ate is null or p.data < :ate)
           and p.status in :statuses
         group by function('date', p.data), p.tipoPagamento
         order by function('date', p.data) asc, p.tipoPagamento asc
    """)
    List<VendasPagamentoPorDiaRow> listarResumoVendasPorDiaEFormaPagamento(
            @Param("de") LocalDateTime de,
            @Param("ate") LocalDateTime ate,
            @Param("statuses") List<StatusPedido> statuses
    );

    @Query("""
        select p.tipoPagamento as tipoPagamento,
               count(p) as qtd,
               coalesce(sum(coalesce(p.total, 0)), 0) as total
          from PedidoEntity p
         where p.data >= :de
           and p.data < :ate
           and p.status in :statuses
         group by p.tipoPagamento
    """)
    List<PagamentoResumoRow> listarResumoPagamentoPorPeriodo(@Param("de") LocalDateTime de,
                                                             @Param("ate") LocalDateTime ate,
                                                             @Param("statuses") List<StatusPedido> statuses);

    @Query("""
           select p
             from PedidoEntity p
             join fetch p.cliente c
            where ((:email is not null and lower(c.email) = lower(:email))
                or (:cpf is not null and c.cpf = :cpf))
            order by p.data desc
           """)
    List<PedidoEntity> listarPorCliente(@Param("email") String email, @Param("cpf") String cpf);

    @Query("""
           select distinct p
             from PedidoEntity p
             left join fetch p.cliente c
             left join fetch p.itens i
             left join fetch i.produto
            where p.id = :id
           """)
    Optional<PedidoEntity> buscarDetalheAdmin(@Param("id") Long id);
    @Query("""
           select p
             from PedidoEntity p
             left join fetch p.cliente c
            order by p.data desc
           """)
    List<PedidoEntity> listarRecentes(Pageable pageable);

    @Query("""
           select p
             from PedidoEntity p
             left join fetch p.cliente c
            where (:de is null or p.data >= :de)
            order by p.data desc
           """)
    List<PedidoEntity> listarRecentes(LocalDateTime de, Pageable pageable);

    interface PedidoItensCountRow {
        Long getId();
        Long getTotalItens();
    }

    @Query("""
           select p.id as id,
                  coalesce(sum(i.quantidade), 0) as totalItens
             from PedidoEntity p
             left join p.itens i
            where p.id in :ids
            group by p.id
           """)
    List<PedidoItensCountRow> contarItensPorPedidos(@Param("ids") List<Long> ids);

    @Query("""
           select distinct p
             from PedidoEntity p
             join fetch p.cliente c
             left join fetch p.itens i
             left join fetch i.produto
            where p.id = :id
              and ((:email is not null and lower(c.email) = lower(:email))
                or (:cpf is not null and c.cpf = :cpf))
           """)
    Optional<PedidoEntity> buscarDetalhePorCliente(@Param("id") Long id,
                                                   @Param("email") String email,
                                                   @Param("cpf") String cpf);

    @Query("""
           select distinct p
             from PedidoEntity p
             left join fetch p.cliente c
            where p.id in :ids
           """)
    List<PedidoEntity> buscarPorIdsComCliente(@Param("ids") List<Long> ids);

    @Query("""
           select p
             from PedidoEntity p
             left join fetch p.cliente c
            where p.modoEntrega = :modoEntrega
              and p.status in :statuses
            order by p.data asc
           """)
    List<PedidoEntity> listarPorModoEntregaEStatusComCliente(
            @Param("modoEntrega") ModoEntrega modoEntrega,
            @Param("statuses") List<StatusPedido> statuses,
            Pageable pageable
    );

    @Query("""
           select p
             from PedidoEntity p
             left join fetch p.cliente c
            where p.modoEntrega = :modoEntrega
              and p.status not in :excludedStatuses
            order by p.data asc
           """)
    List<PedidoEntity> listarPorModoEntregaExcluindoStatusComCliente(
            @Param("modoEntrega") ModoEntrega modoEntrega,
            @Param("excludedStatuses") List<StatusPedido> excludedStatuses,
            Pageable pageable
    );

    List<PedidoEntity> findByModoEntregaAndStatusInOrderByDataAsc(
            ModoEntrega modoEntrega,
            List<StatusPedido> statuses,
            Pageable pageable
    );

    Optional<PedidoEntity> findByGatewayExternalReference(
            String gatewayExternalReference
    );

    Optional<PedidoEntity> findByGatewayPaymentId(String gatewayPaymentId);
}
