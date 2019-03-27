
CREATE OR REPLACE FUNCTION audit.get_audit_table_name(origin_table_name TEXT)
  RETURNS TEXT AS
  $BODY$
  BEGIN
    IF ((SELECT position('.' IN origin_table_name)) <> 0)
    THEN
      RETURN 'audit."' || regexp_replace(origin_table_name, '\.', '$') || audit.get_part_postfix() || '"';
    ELSE
      RETURN 'audit."' || 'public$' || origin_table_name || audit.get_part_postfix() || '"';
    END IF;
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
    nullable_key       TEXT;
    insert_query       TEXT;
    pk_column_info     RECORD;
    pk_column_names    TEXT;
    pk_values_from_new TEXT;
    pk_values_from_old TEXT;
    audit_table_name   TEXT;
    seq_name   TEXT;

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
        pk_values_from_new || '''' || (new_row_json :: JSONB -> pk_column_info.column_name) || '''' || '::' ||
        pk_column_info.type || ', ';
      END IF;

      IF (TG_OP = 'DELETE')
      THEN
        pk_values_from_old :=
        pk_values_from_old || '''' || (old_row_json :: JSONB -> pk_column_info.column_name) || '''' || '::' ||
        pk_column_info.type || ', ';
      END IF;

    END LOOP;

    SELECT audit.get_audit_table_name(TG_TABLE_SCHEMA || '.' || TG_TABLE_NAME)
    INTO audit_table_name;

    seq_name := replace(replace(regexp_replace(audit_table_name, '"$', '_aud_rec_seq"'), audit.get_part_postfix(), ''), '"', '');
    IF (TG_OP = 'INSERT')
    THEN

      FOR nullable_key IN SELECT hs.key
                          FROM jsonb_each(new_row_json) hs
                          WHERE hs.value :: TEXT = 'null'
      LOOP
        -- exclude null values
        new_row_json := new_row_json - nullable_key :: TEXT;
      END LOOP;
      -- exclude audit columns
      data := new_row_json - 'aud_when' - 'aud_who' - 'aud_source' - 'aud_when_create' - 'aud_who_create' -
              'aud_source_create';
      IF (data = '{}' :: JSONB)
      THEN
        RETURN NULL;
      END IF;

      insert_query := format(
          'INSERT INTO %s (%s type, data, aud_when, aud_who, aud_source, aud_rec) VALUES (%s $1, $2, $3, $4, $5, nextval(''%s'') );'
          , audit_table_name, pk_column_names, pk_values_from_new,  seq_name);

      BEGIN
        EXECUTE insert_query
        USING 'I', data, to_timestamp(current_setting('aud.when'), 'YYYY-MM-DD HH24:MI:SS:MS'), app_user, app_source;
      END;

    ELSEIF (TG_OP = 'UPDATE')
      THEN
        FOR nullable_key IN SELECT hs.key
                            FROM jsonb_each(new_row_json) hs
                            WHERE hs.value :: TEXT = 'null'
        LOOP
          -- exclude null values
          new_row_json := new_row_json - nullable_key :: TEXT;
          old_row_json := old_row_json - nullable_key :: TEXT;
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

        insert_query := format(
            'INSERT INTO %s (%s type, data, aud_when, aud_who, aud_source, aud_rec) VALUES (%s $1, $2, $3, $4, $5, nextval(''%s'') );'
            , audit_table_name, pk_column_names, pk_values_from_new, seq_name);
        BEGIN
          EXECUTE insert_query
          USING 'U', data, to_timestamp(current_setting('aud.when'), 'YYYY-MM-DD HH24:MI:SS:MS'), app_user, app_source;
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
        -- exclude audit columns
        data := old_row_json - 'aud_when' - 'aud_who' - 'aud_source' - 'aud_when_create' - 'aud_who_create' -
                'aud_source_create';

        insert_query := format(
            'INSERT INTO %s (%s type, data, aud_when, aud_who, aud_source, aud_rec) VALUES (%s $1, $2, $3, $4, $5, nextval(''%s'') );'
            , audit_table_name, pk_column_names, pk_values_from_old, seq_name);
        BEGIN
          EXECUTE insert_query
          USING 'D', old_row_json, now(), app_user, app_source;
        END;
        RETURN OLD;
    END IF;

    RETURN NEW;
  END;
  $BODY$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION audit.gen_audit_table_ddl(table_name TEXT)
  RETURNS TEXT AS
  $BODY$
  DECLARE
    pk_column_info               RECORD;
    pk_value_to_write_with_types TEXT;
    pk_value_to_write            TEXT;
    full_table_name              TEXT;
    audit_table_name             TEXT;
    index_name_prefix            TEXT;
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
    index_name_prefix :=  replace(audit_table_name, audit.get_part_postfix(), '');
    RETURN format('CREATE TABLE audit."%s"(
                                aud_rec bigserial,
				%s
				type TEXT NOT NULL ,
				delta jsonb,
				aud_when timestamp(6),
				aud_who varchar,
				aud_source varchar,
				data jsonb,
				CONSTRAINT %s_pk PRIMARY KEY (aud_rec));
				CREATE INDEX ON audit.%s(%s);
				CREATE INDEX ON audit.%s(aud_when);
				CREATE INDEX ON audit.%s(aud_who);
				CREATE INDEX ON audit.%s(aud_source);

				COMMENT ON COLUMN audit."%s".delta IS ''deprecated'';
        ALTER TABLE audit."%s" ADD CONSTRAINT "%s" CHECK (type IN (''I'',''D'',''U'',''T''));'
    , audit_table_name, pk_value_to_write_with_types, audit_table_name, audit_table_name,
                  substring(pk_value_to_write, 1, length(pk_value_to_write) - 1)
    , audit_table_name, audit_table_name, audit_table_name,
                  audit_table_name, audit_table_name, replace(audit_table_name, audit.get_part_postfix(), '_type_check'));
  END;
  $BODY$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION audit.add_table(table_name TEXT)
  RETURNS VOID AS
  $BODY$
  DECLARE
    ddl               TEXT;
    parent_table_name TEXT;
    audit_table_name  TEXT;
    audit_table_name_without_scheme  TEXT;
  BEGIN
    SELECT audit.gen_audit_table_ddl(table_name)
    INTO ddl;

    IF (
      (SELECT EXISTS(SELECT 1
                     FROM information_schema.schemata
                     WHERE schema_name = 'pglogical'))

    )
    THEN
      IF (SELECT EXISTS(SELECT 1
                        FROM pglogical.replication_set
                        WHERE set_name = 'audit_set'))
      THEN
        IF ((SELECT position('.' IN table_name)) <> 0)
        THEN
          audit_table_name_without_scheme := regexp_replace(replace(table_name, '"', ''), '\.', '$');
          parent_table_name := 'audit."' || audit_table_name_without_scheme || '"';
        ELSE
          audit_table_name_without_scheme := 'public$' || replace(table_name, '"', '');
          parent_table_name := 'audit."' || audit_table_name_without_scheme || '"';
        END IF;
        IF NOT EXISTS (SELECT 0 FROM pg_class where relname = audit_table_name_without_scheme ||'_aud_rec_seq' )
        THEN
          EXECUTE 'CREATE SEQUENCE '||audit_table_name_without_scheme||'_aud_rec_seq ;';
        END IF;
        audit_table_name := replace(replace(replace(audit.get_audit_table_name(table_name), '$"', '$'), '""', '"'), '"_y', '_y');
        EXECUTE ddl;
        PERFORM dblink_connect(audit.get_replica_conn());
        PERFORM dblink_exec(ddl);
        PERFORM * from dblink(format('SELECT audit.do_inherit(''%s'', ''%s''); select 1;', parent_table_name, audit_table_name)) as x(x text);
        PERFORM dblink_disconnect();
      ELSE
        EXECUTE ddl;
      END IF;
    ELSE
      EXECUTE ddl;
    END IF;
  END;
  $BODY$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION audit.do_inherit(parent_table_name text, audit_table_name text)
  RETURNS VOID AS
  $BODY$
  DECLARE
    table_name VARCHAR;
    action_var VARCHAR;
    check_conname VARCHAR;
  BEGIN
    IF (SELECT EXISTS(SELECT 1 FROM pglogical.subscription where sub_name = 'audit_replicate_subscription'))
    THEN
      EXECUTE  format(' CREATE TABLE IF NOT EXISTS %s( LIKE %s INCLUDING ALL ); ALTER TABLE %s INHERIT %s;',
                      parent_table_name, audit_table_name, audit_table_name, parent_table_name);
    END IF;
  END;
  $BODY$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION public.disable_trigger(
  value        BOOLEAN,
  trigger_name TEXT)
  RETURNS VOID AS
  $BODY$
  DECLARE
    table_name VARCHAR;
    action_var VARCHAR;
  BEGIN
    IF (value)
    THEN
      action_var := 'DISABLE';
    ELSE
      action_var := 'ENABLE';
    END IF;
    FOR table_name IN SELECT event_object_schema || '.' || event_object_table
                      FROM information_schema.triggers
                      WHERE information_schema.triggers.trigger_name = $2
                      GROUP BY 1
    LOOP
      EXECUTE format('ALTER TABLE %s %s TRIGGER %s', table_name, action_var, $2);
    END LOOP;

  END;
  $BODY$
LANGUAGE plpgsql;


-- runs on provider db
CREATE OR REPLACE FUNCTION audit.add_replicate_provider(db_host TEXT, db_port TEXT, db_name TEXT)
  RETURNS VOID AS
  $BODY$
  BEGIN
    CREATE EXTENSION IF NOT EXISTS pglogical;

    PERFORM pglogical.create_node(
        node_name := 'audit_replicate_provider',
        dsn := 'host=' || db_host || ' port=' || db_port || ' dbname=' || db_name
    );
    PERFORM pglogical.create_replication_set('audit_set', TRUE, TRUE, TRUE, FALSE);
    PERFORM pglogical.replication_set_add_all_tables('audit_set', ARRAY ['audit'], TRUE);
  END;
  $BODY$
LANGUAGE plpgsql;

-- runs on provider db
CREATE OR REPLACE FUNCTION audit.drop_replicate_provider()
  RETURNS VOID AS
  $BODY$
  BEGIN
    PERFORM pglogical.drop_replication_set('audit_set');
    PERFORM pglogical.drop_node('audit_replicate_provider');
  END;
  $BODY$
LANGUAGE plpgsql;


-- runs on subscriber db
CREATE OR REPLACE FUNCTION audit.add_replicate_subscriber(provider_host TEXT, provider_port TEXT, provider_db_name TEXT,
                                                          db_host       TEXT, db_port TEXT, db_name TEXT)
  RETURNS VOID AS
  $BODY$
  BEGIN
    CREATE EXTENSION IF NOT EXISTS pglogical;
    PERFORM pglogical.create_node(
        node_name := 'audit_replicate_subscriber',
        dsn := 'host=' || db_host || ' port=' || db_port || ' dbname=' || db_name
    );

    PERFORM pglogical.create_subscription(
        subscription_name := 'audit_replicate_subscription',
        provider_dsn := 'host=' || provider_host || ' port=' || provider_port || ' dbname=' || provider_db_name,
        replication_sets := ARRAY['audit_set']
    );

  END;
  $BODY$
LANGUAGE plpgsql;

-- runs on subscriber db
CREATE OR REPLACE FUNCTION audit.drop_replicate_subscriber()
  RETURNS VOID AS
  $BODY$
  BEGIN
    PERFORM pglogical.drop_subscription('audit_replicate_subscription');
    PERFORM pglogical.drop_node('audit_replicate_subscriber');
  END;
  $BODY$
LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION audit.create_next_parts(tbl_limit int)
  RETURNS integer AS
  $BODY$
   DECLARE
    r text;
    i integer;
   BEGIN
    PERFORM set_config('app.source', 'create_next_part', true);
    i := 0;
    FOR r IN
      SELECT event_object_schema || '.' || event_object_table
      FROM information_schema.triggers
      WHERE trigger_name = 'audit_trigger_full'
            and exists(SELECT audit.get_primary_keys(event_object_schema || '.' || event_object_table))
            and not exists(select 1 from information_schema.tables where table_name = event_object_schema || '$' || event_object_table||audit.get_part_postfix() and table_schema = 'audit')
      group by  event_object_schema || '.' || event_object_table limit tbl_limit
    LOOP
     PERFORM audit.create_next_part(r);
     i := i + 1;
    END LOOP;
    RETURN i;
   END;
  $BODY$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION audit.create_current_part(original_table_name text)
  RETURNS void AS
  $BODY$
   BEGIN
    PERFORM set_config('app.source', 'create_current_part', true);
    PERFORM audit.add_table(original_table_name);
   END;
  $BODY$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION audit.create_next_part(original_table_name text)
  RETURNS void AS
  $BODY$
   BEGIN
    PERFORM set_config('app.source', 'create_next_part', true);
    PERFORM audit.add_table(original_table_name);
   END;
  $BODY$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION audit.create_current_parts(tbl_limit int)
  RETURNS integer AS
  $BODY$
   DECLARE
    r text;
    i integer;
   BEGIN
    PERFORM set_config('app.source', 'create_current_part', true);
    i := 0;
    FOR r IN
      SELECT event_object_schema || '.' || event_object_table
      FROM information_schema.triggers
      WHERE trigger_name = 'audit_trigger_full'
            and exists(SELECT audit.get_primary_keys(event_object_schema || '.' || event_object_table))
            and not exists(select 1 from information_schema.tables where table_name = event_object_schema || '$' || event_object_table||audit.get_part_postfix() and table_schema = 'audit')
      group by  event_object_schema || '.' || event_object_table limit tbl_limit
    LOOP
     PERFORM audit.create_current_part(r);
     i := i + 1;
    END LOOP;
    RETURN i;
   END;
  $BODY$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION audit.get_replica_conn()
  RETURNS TEXT AS
  $BODY$
  BEGIN
    RETURN '';
  END;
  $BODY$
LANGUAGE plpgsql;






