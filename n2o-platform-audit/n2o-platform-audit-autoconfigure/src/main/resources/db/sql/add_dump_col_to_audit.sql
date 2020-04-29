CREATE OR REPLACE FUNCTION audit.gen_audit_table_ddl(table_name TEXT)
  RETURNS TEXT AS
  $BODY$
  DECLARE
    pk_column_info               RECORD;
    pk_value_to_write_with_types TEXT;
    pk_value_to_write            TEXT;
    full_table_name              TEXT;
    audit_table_name             TEXT;
  BEGIN
    pk_value_to_write_with_types := '';
    pk_value_to_write := '';
    IF (strpos($1, '.') = 0)
    THEN full_table_name := 'public.' || $1;
    ELSE
      full_table_name := $1;
    END IF;
    SELECT replace(regexp_replace(audit.get_audit_table_name(full_table_name), 'audit\.', ''), '"', '')
    INTO audit_table_name;

    FOR pk_column_info IN SELECT *
                          FROM audit.get_primary_keys(full_table_name)
    LOOP
      pk_value_to_write_with_types :=
      pk_value_to_write_with_types || pk_column_info.column_name || ' ' || pk_column_info.type || ',';
      pk_value_to_write := pk_value_to_write || pk_column_info.column_name || ',';

    END LOOP;
    RETURN format('CREATE TABLE audit."%s"(
                                aud_rec bigserial,
				%s
				type TEXT NOT NULL CHECK (type IN (''I'',''D'',''U'',''T'')),
				delta jsonb,
				aud_when timestamp(6),
				aud_who varchar,
				aud_source varchar,
				data jsonb,
				dump jsonb,
				CONSTRAINT %s_pk PRIMARY KEY (aud_rec));
				CREATE INDEX ON audit.%s(%s);
				CREATE INDEX %s_aud_when_ix ON audit.%s(aud_when);
				CREATE INDEX %s_aud_who_ix ON audit.%s(aud_who);
				CREATE INDEX %s_aud_source_ix ON audit.%s(aud_source);'
    , audit_table_name, pk_value_to_write_with_types, audit_table_name, audit_table_name,
                  substring(pk_value_to_write, 1, length(pk_value_to_write) - 1)
    , audit_table_name, audit_table_name, audit_table_name, audit_table_name, audit_table_name, audit_table_name,
                  audit_table_name);
  END;
  $BODY$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION audit.trigger_fun()
  RETURNS TRIGGER AS
  $BODY$
  DECLARE
    app_user           TEXT;
    app_source         TEXT;
    new_row_json       JSONB;
    old_row_json       JSONB;
    data               JSONB;
    delta              JSONB;
    dump               JSONB;
    nullable_key       TEXT;
    insert_query       TEXT;
    pk_column_info     RECORD;
    pk_column_names    TEXT;
    pk_values_from_new TEXT;
    pk_values_from_old TEXT;
    audit_table_name   TEXT;
    seq_name           TEXT;

  BEGIN

    --     init app settings
    BEGIN
      SELECT current_setting('app.user')
      INTO app_user;
      IF (app_user = 'unknown')
      THEN
        app_user:= CURRENT_USER;
      END IF;
      EXCEPTION
      WHEN OTHERS THEN
        app_user:= CURRENT_USER;
    END;

    BEGIN
      SELECT current_setting('app.source')
      INTO app_source;
      EXCEPTION
      WHEN OTHERS THEN
        app_source := 'DB';
    END;

    IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE')
    THEN
      new_row_json := row_to_json(NEW.*);
    END IF;

    IF (TG_OP = 'DELETE' OR TG_OP = 'UPDATE')
    THEN
      old_row_json := row_to_json(OLD.*);
    END IF;

    pk_column_names := '';
    pk_values_from_new := '';
    pk_values_from_old := '';
    FOR pk_column_info IN SELECT
                            pg_attribute.attname :: TEXT                               AS column_name,
                            format_type(pg_attribute.atttypid, pg_attribute.atttypmod) AS type
                          FROM pg_index, pg_class, pg_attribute, pg_namespace
                          WHERE
                            pg_class.oid = (TG_TABLE_SCHEMA || '.' || TG_TABLE_NAME) :: REGCLASS AND
                            indrelid = pg_class.oid AND
                            pg_class.relnamespace = pg_namespace.oid AND
                            pg_attribute.attrelid = pg_class.oid AND
                            pg_attribute.attnum = ANY (pg_index.indkey)
                            AND indisprimary
    LOOP

      pk_column_names := pk_column_names || pk_column_info.column_name || ', ';

      IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE')
      THEN
        pk_values_from_new :=
        pk_values_from_new || '''' || replace((new_row_json :: JSONB -> pk_column_info.column_name)::varchar, '"', '') || '''' || '::' ||
        pk_column_info.type || ', ';
      END IF;

      IF (TG_OP = 'DELETE')
      THEN
        pk_values_from_old :=
        pk_values_from_old || '''' || replace((old_row_json :: JSONB -> pk_column_info.column_name)::varchar, '"', '') || '''' || '::' ||
        pk_column_info.type || ', ';
      END IF;

    END LOOP;

    SELECT audit.get_audit_table_name(TG_TABLE_SCHEMA || '.' || TG_TABLE_NAME)
    INTO audit_table_name;

    seq_name := replace(replace(regexp_replace(audit_table_name, '"$', '_aud_rec_seq"'), audit.get_part_postfix(), ''),
                        '"', '');
    IF (TG_OP = 'INSERT')
    THEN

      FOR nullable_key IN SELECT hs.key
                          FROM jsonb_each(new_row_json) hs
                          WHERE hs.value :: TEXT = 'null'
      LOOP
        -- exclude null values
        new_row_json := new_row_json - nullable_key :: TEXT;
      END LOOP;
      -- exclude audit and denorm columns
      data :=
      audit.exclude_denorm_columns(new_row_json, TG_TABLE_SCHEMA || '.' || TG_TABLE_NAME) - 'aud_when' - 'aud_who' -
      'aud_source' - 'aud_when_create' - 'aud_who_create' -
      'aud_source_create';
      IF (data = '{}' :: JSONB)
      THEN
        RETURN NULL;
      END IF;

      insert_query := format(
          'INSERT INTO %s (%s type, data, delta, aud_when, aud_who, aud_source, aud_rec) VALUES (%s $1, $2, $3, $4, $5, $6, nextval(''%s'') );'
          , audit_table_name, pk_column_names, pk_values_from_new, seq_name);

      BEGIN
        EXECUTE insert_query
        USING 'I', data, data, to_timestamp(current_setting('aud.when'),
                                            'YYYY-MM-DD HH24:MI:SS:MS'), app_user, app_source;
      END;

    ELSEIF (TG_OP = 'UPDATE')
      THEN
        FOR nullable_key IN SELECT hs.key
                            FROM jsonb_each(new_row_json) hs
                            WHERE hs.value :: TEXT = 'null'
        LOOP
          -- exclude null and denorm values
          new_row_json := audit.exclude_denorm_columns(new_row_json, TG_TABLE_SCHEMA || '.' || TG_TABLE_NAME) -
                          nullable_key :: TEXT;
          old_row_json := audit.exclude_denorm_columns(old_row_json, TG_TABLE_SCHEMA || '.' || TG_TABLE_NAME) -
                          nullable_key :: TEXT;
        END LOOP;

        IF (new_row_json = old_row_json)
        THEN
          RETURN NULL;
        END IF;
        -- exclude audit columns
        data := new_row_json - 'aud_when' - 'aud_who' - 'aud_source' - 'aud_when_create' - 'aud_who_create' -
                'aud_source_create';
        IF (data = '{}' :: JSONB)
        THEN
          RETURN NULL;
        END IF;

        delta := audit.jsonb_diff_val(data, (
          old_row_json - 'aud_when' - 'aud_who' - 'aud_source' - 'aud_when_create' - 'aud_who_create' -
          'aud_source_create'));
        IF (delta = '{}' :: JSONB)
        THEN
          delta:=NULL;
        END IF;

        dump := audit.jsonb_diff_val((old_row_json - 'aud_when' - 'aud_who' - 'aud_source' - 'aud_when_create' - 'aud_who_create' - 'aud_source_create'), data);

        insert_query := format(
            'INSERT INTO %s (%s type, data, delta, dump, aud_when, aud_who, aud_source, aud_rec) VALUES (%s $1, $2, $3, $4, $5, $6, $7, nextval(''%s'') );'
            , audit_table_name, pk_column_names, pk_values_from_new, seq_name);
        BEGIN
          EXECUTE insert_query
          USING 'U', data, delta, dump, to_timestamp(current_setting('aud.when'),
                                               'YYYY-MM-DD HH24:MI:SS:MS'), app_user, app_source;
        END;

    ELSEIF (TG_OP = 'DELETE')
      THEN

        FOR nullable_key IN SELECT hs.key
                            FROM jsonb_each(new_row_json) hs
                            WHERE hs.value IS NULL
        LOOP
          -- exclude null values
          old_row_json := old_row_json - nullable_key :: TEXT;
        END LOOP;
        -- exclude audit and denorm columns
        data :=
        audit.exclude_denorm_columns(old_row_json, TG_TABLE_SCHEMA || '.' || TG_TABLE_NAME) - 'aud_when' - 'aud_who' -
        'aud_source' - 'aud_when_create' - 'aud_who_create' -
        'aud_source_create';

        insert_query := format(
            'INSERT INTO %s (%s type, data, aud_when, aud_who, aud_source, aud_rec) VALUES (%s $1, $2, $3, $4, $5, nextval(''%s'') );'
            , audit_table_name, pk_column_names, pk_values_from_old, seq_name);
        BEGIN
          EXECUTE insert_query
          USING 'D', data, now(), app_user, app_source;
        END;
        RETURN OLD;
    END IF;

    RETURN NEW;
  END;
  $BODY$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION aud_auditable_tables()
    RETURNS SETOF character varying AS
$BODY$
DECLARE r varchar;
BEGIN
    for r in select table_schema||'.'||table_name  from information_schema.tables
             where table_type = 'BASE TABLE' and not table_schema like 'pg_%' and table_schema <> 'information_schema'
               and table_name <> 'databasechangelog' and table_name <> 'databasechangeloglock'
               and not table_name like '%_aud'
               and not exists (select 1 from aud_excluded_schemas where schema_name = information_schema.tables.table_schema)
               and exists(select 1 from information_schema.triggers where trigger_name = 'audit_trigger_full' and event_object_schema ||'.'|| event_object_table = information_schema.tables.table_schema||'.'||information_schema.tables.table_name)
               and not exists(select 1 from aud_excluded_tables where table_name = information_schema.tables.table_schema||'.'||information_schema.tables.table_name)
        LOOP
            if exists (select 1 from information_schema.tables where table_schema = 'audit' and table_name =  replace(r, '.','$')) then
                return next r;
            end if;
        END LOOP;
END;
$BODY$
    LANGUAGE plpgsql;

do $$
    declare
        t varchar;
begin
    for t in select aud_auditable_tables()
        loop
            BEGIN
                EXECUTE format('ALTER TABLE audit.%s ADD COLUMN dump jsonb;', replace(t, '.','$'));
            EXCEPTION
                WHEN duplicate_column THEN RAISE NOTICE 'column dump already exists in %.', t;
            END;
        end loop;
end;
    $$
    LANGUAGE plpgsql;

drop function aud_auditable_tables();