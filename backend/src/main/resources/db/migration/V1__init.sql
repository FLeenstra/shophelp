create table store (
    id        bigserial primary key,
    name      varchar(200)     not null,
    address   varchar(300),
    latitude  double precision not null,
    longitude double precision not null
);

create table product (
    id       bigserial primary key,
    name     varchar(200) not null,
    category varchar(100),
    unit     varchar(50)
);

create table store_price (
    id         bigserial primary key,
    store_id   bigint         not null references store (id),
    product_id bigint         not null references product (id),
    price      numeric(10, 2) not null,
    currency   varchar(3)     not null default 'EUR',
    updated_at timestamp      not null default now(),
    constraint uq_store_product unique (store_id, product_id)
);

create index idx_store_price_product on store_price (product_id);
create index idx_store_price_store on store_price (store_id);
