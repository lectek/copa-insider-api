/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.data.jpa.repository.JpaRepository
 *  org.springframework.data.jpa.repository.Modifying
 *  org.springframework.data.jpa.repository.Query
 *  org.springframework.data.repository.query.Param
 */
package br.com.lectek.copainsider.adapters.outbound.persistence.jpa;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.UsuarioEntity;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioJpaRepository
extends JpaRepository<UsuarioEntity, Long> {
    @Query(value="select u from UsuarioEntity u\nwhere lower(u.email) = lower(:id)\n   or u.cpf = :id\n")
    public Optional<UsuarioEntity> findByEmailOrCpf(@Param(value="id") String var1);

    @Query(value="select case when (u.tentativasFalhas >= 5) then true else false end\nfrom UsuarioEntity u\nwhere lower(u.email) = lower(:id) or u.cpf = :id\n")
    public Boolean isBlockedByEmailOrCpf(@Param(value="id") String var1);

    @Modifying
    @Query(value="update UsuarioEntity u set u.tentativasFalhas = u.tentativasFalhas + 1 where lower(u.email)=lower(:id) or u.cpf=:id")
    public void registrarTentativaFalha(@Param(value="id") String var1);

    @Modifying
    @Query(value="update UsuarioEntity u set u.tentativasFalhas = 0 where lower(u.email)=lower(:id) or u.cpf=:id")
    public void resetarTentativasFalhas(@Param(value="id") String var1);

    @Modifying
    @Query(value="update UsuarioEntity u set u.ultimoAcesso = :when where u.id = :id")
    public int updateUltimoAcesso(@Param(value="id") Long var1, @Param(value="when") LocalDateTime var2);

    @Query(value="select u.clienteVip from UsuarioEntity u\nwhere lower(u.email)=lower(:id) or u.cpf=:id\n")
    public Optional<Boolean> verificarClienteVip(@Param(value="id") String var1);

    @Query(value="select u from UsuarioEntity u where u.clienteVip = true")
    List<UsuarioEntity> findByClienteVipTrue();

    public boolean existsByEmail(String var1);

    @Modifying
    @Query(value="update UsuarioEntity u set u.emailVerificado = true where lower(u.email) = lower(:email)")
    int ativarEmailVerificado(@Param("email") String email);
}
