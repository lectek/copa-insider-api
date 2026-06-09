create table if not exists entrega_rota (
    id bigint not null auto_increment,
    data_operacao date not null,
    origem varchar(255) not null,
    distancia_total_km decimal(10,2) not null default 0.00,
    custo_total decimal(10,2) not null default 0.00,
    mapa_url varchar(1024),
    status varchar(30) not null,
    entregador_usuario_id bigint null,
    criada_por_usuario_id bigint null,
    despachada_em datetime null,
    iniciada_em datetime null,
    finalizada_em datetime null,
    created_at datetime not null default current_timestamp,
    updated_at datetime not null default current_timestamp on update current_timestamp,
    version bigint not null default 0,
    constraint pk_entrega_rota primary key (id),
    constraint fk_entrega_rota_entregador
        foreign key (entregador_usuario_id) references usuario (id),
    constraint fk_entrega_rota_criada_por
        foreign key (criada_por_usuario_id) references usuario (id)
);

create index idx_entrega_rota_data_operacao
    on entrega_rota (data_operacao);

create index idx_entrega_rota_status
    on entrega_rota (status);

create index idx_entrega_rota_created_at
    on entrega_rota (created_at);

create table if not exists entrega_parada (
    id bigint not null auto_increment,
    rota_id bigint not null,
    pedido_id bigint not null,
    ordem_rota int not null,
    cliente_nome_snapshot varchar(120) not null,
    endereco_snapshot varchar(255) not null,
    codigo_entrega_snapshot varchar(6),
    status varchar(30) not null,
    distancia_anterior_km decimal(10,2) not null default 0.00,
    distancia_acumulada_km decimal(10,2) not null default 0.00,
    duracao_anterior_segundos bigint null,
    duracao_acumulada_segundos bigint null,
    latitude decimal(10,6) null,
    longitude decimal(10,6) null,
    confirmado_em datetime null,
    motivo_falha varchar(120) null,
    observacao varchar(500) null,
    created_at datetime not null default current_timestamp,
    updated_at datetime not null default current_timestamp on update current_timestamp,
    version bigint not null default 0,
    constraint pk_entrega_parada primary key (id),
    constraint fk_entrega_parada_rota
        foreign key (rota_id) references entrega_rota (id),
    constraint fk_entrega_parada_pedido
        foreign key (pedido_id) references pedido (id),
    constraint uk_entrega_parada_rota_ordem
        unique (rota_id, ordem_rota)
);

create index idx_entrega_parada_rota
    on entrega_parada (rota_id);

create index idx_entrega_parada_pedido
    on entrega_parada (pedido_id);

create index idx_entrega_parada_status
    on entrega_parada (status);
