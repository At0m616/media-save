create table url_request_entity
(
	id bigserial,
	url varchar,
	path_to_save varchar,
	not_save_file_in_kb int,
	check_nested boolean,
	request_time timestamp
);

create unique index url_request_entity_id_uindex
	on url_request_entity (id);

alter table url_request_entity
	add constraint url_request_entity_pk
		primary key (id);

