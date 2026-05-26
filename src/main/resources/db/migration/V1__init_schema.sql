create table users (
    id bigserial primary key,
    email varchar(255) not null unique,
    password_hash varchar(255) not null,
    full_name varchar(255) not null,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table transactions (
    id bigserial primary key,
    external_id varchar(255) not null unique,
    client_id varchar(255) not null,
    client_full_name varchar(255) not null,
    client_phone varchar(64),
    amount numeric(19,2) not null,
    currency varchar(16) not null,
    type varchar(64) not null,
    merchant varchar(255),
    recipient varchar(255),
    country varchar(128),
    city varchar(128),
    status varchar(64) not null,
    risk_score integer,
    risk_level varchar(32),
    created_at timestamp not null,
    processed_at timestamp
);

create table fraud_cases (
    id bigserial primary key,
    transaction_id bigint not null unique,
    risk_score integer not null,
    risk_level varchar(32) not null,
    status varchar(32) not null,
    assigned_analyst_id bigint,
    decision varchar(64),
    decision_comment text,
    created_at timestamp not null,
    assigned_at timestamp,
    closed_at timestamp,
    version bigint not null,
    constraint fk_fraud_cases_transaction foreign key (transaction_id) references transactions (id),
    constraint fk_fraud_cases_user foreign key (assigned_analyst_id) references users (id)
);

create table risk_rule_results (
    id bigserial primary key,
    transaction_id bigint not null,
    rule_code varchar(128) not null,
    points integer not null,
    description text not null,
    created_at timestamp not null,
    constraint fk_risk_rule_results_transaction foreign key (transaction_id) references transactions (id)
);

create table audit_logs (
    id bigserial primary key,
    actor_user_id bigint,
    action varchar(64) not null,
    entity_type varchar(64) not null,
    entity_id bigint not null,
    details text,
    created_at timestamp not null,
    constraint fk_audit_logs_user foreign key (actor_user_id) references users (id)
);

create index idx_transactions_client_id on transactions(client_id);
create index idx_transactions_status on transactions(status);
create index idx_transactions_created_at on transactions(created_at);

create index idx_fraud_cases_status on fraud_cases(status);
create index idx_fraud_cases_risk_level on fraud_cases(risk_level);
create index idx_fraud_cases_assigned_analyst_id on fraud_cases(assigned_analyst_id);

create index idx_audit_logs_entity on audit_logs(entity_type, entity_id);
