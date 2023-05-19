create table if not exists patients.info (
	id serial4 NOT NULL,
	sex text NULL,
	birth_date date NULL,
	address text NULL,
	oms text NULL,
	created_at timestamptz NULL DEFAULT now(),
	first_name varchar(255) NULL,
	second_name varchar(255) NULL,
	middle_name varchar(255) NULL,
	deleted bool NOT NULL DEFAULT false,
	updated_at timestamptz NOT NULL DEFAULT now(),
	CONSTRAINT info_oms_key UNIQUE (oms),
	CONSTRAINT info_pkey PRIMARY KEY (id)
);
