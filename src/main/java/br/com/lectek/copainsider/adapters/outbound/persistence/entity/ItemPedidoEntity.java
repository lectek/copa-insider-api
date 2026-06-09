package br.com.lectek.copainsider.adapters.outbound.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "item_pedido")
@EntityListeners(AuditingEntityListener.class)
public class ItemPedidoEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private ProdutoEntity produto;

    @Column(name = "produto_nome_snapshot", length = 255)
    private String produtoNomeSnapshot;

    @Column(name = "produto_codigo_barras_snapshot", length = 64)
    private String produtoCodigoBarrasSnapshot;

    @Column(name = "produto_categoria_snapshot", length = 255)
    private String produtoCategoriaSnapshot;

    @Column(name = "produto_fabricante_snapshot", length = 128)
    private String produtoFabricanteSnapshot;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private PedidoEntity pedido;

    @NotNull
    @Min(value = 1, message = "Quantidade deve ser no minimo 1")
    @Column(nullable = false)
    private Integer quantidade;

    @NotNull
    @DecimalMin(value = "0.00", inclusive = true, message = "Preco unitario nao pode ser negativo")
    @Column(name = "preco_unitario", precision = 19, scale = 2, nullable = false)
    private BigDecimal precoUnitario;

    @NotNull
    @DecimalMin(value = "0.00", inclusive = true, message = "Subtotal nao pode ser negativo")
    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal subtotal;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    @PrePersist
    public void prePersist() {
        captureProdutoSnapshot();
        if (this.precoUnitario == null) {
            if (this.subtotal != null && this.quantidade != null && this.quantidade > 0) {
                this.precoUnitario = this.subtotal.divide(
                        BigDecimal.valueOf(this.quantidade.longValue()),
                        2,
                        RoundingMode.HALF_UP
                );
            } else {
                this.precoUnitario = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            }
        }
        if (this.subtotal == null) {
            this.subtotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
    }

    @PreUpdate
    public void preUpdate() {
        captureProdutoSnapshot();
    }

    private void captureProdutoSnapshot() {
        if (this.produto == null) {
            return;
        }
        this.produtoNomeSnapshot = truncate(this.produto.getNome(), 255);
        this.produtoCodigoBarrasSnapshot = truncate(this.produto.getCodigoBarras(), 64);
        this.produtoCategoriaSnapshot = truncate(this.produto.getCategoria(), 255);
        this.produtoFabricanteSnapshot = truncate(this.produto.getFabricante(), 128);
    }

    private static String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, maxLength);
    }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public ProdutoEntity getProduto() { return produto; }

    public void setProduto(ProdutoEntity produto) { this.produto = produto; }

    public String getProdutoNomeSnapshot() {
        return produtoNomeSnapshot;
    }

    public void setProdutoNomeSnapshot(String produtoNomeSnapshot) {
        this.produtoNomeSnapshot = produtoNomeSnapshot;
    }

    public String getProdutoCodigoBarrasSnapshot() {
        return produtoCodigoBarrasSnapshot;
    }

    public void setProdutoCodigoBarrasSnapshot(String produtoCodigoBarrasSnapshot) {
        this.produtoCodigoBarrasSnapshot = produtoCodigoBarrasSnapshot;
    }

    public String getProdutoCategoriaSnapshot() {
        return produtoCategoriaSnapshot;
    }

    public void setProdutoCategoriaSnapshot(String produtoCategoriaSnapshot) {
        this.produtoCategoriaSnapshot = produtoCategoriaSnapshot;
    }

    public String getProdutoFabricanteSnapshot() {
        return produtoFabricanteSnapshot;
    }

    public void setProdutoFabricanteSnapshot(String produtoFabricanteSnapshot) {
        this.produtoFabricanteSnapshot = produtoFabricanteSnapshot;
    }

    public PedidoEntity getPedido() { return pedido; }

    public void setPedido(PedidoEntity pedido) { this.pedido = pedido; }

    public Integer getQuantidade() { return quantidade; }

    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }

    public BigDecimal getPrecoUnitario() { return precoUnitario; }

    public void setPrecoUnitario(BigDecimal precoUnitario) { this.precoUnitario = precoUnitario; }

    public BigDecimal getSubtotal() { return subtotal; }

    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getVersion() { return version; }

    public void setVersion(Long version) { this.version = version; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemPedidoEntity that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ItemPedidoEntity{" +
                "id=" + id +
                ", produtoId=" + (produto != null ? produto.getId() : null) +
                ", pedidoId=" + (pedido != null ? pedido.getId() : null) +
                ", quantidade=" + quantidade +
                ", precoUnitario=" + precoUnitario +
                ", subtotal=" + subtotal +
                ", version=" + version +
                '}';
    }
}
