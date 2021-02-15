DROP SCHEMA IF EXISTS audit CASCADE;

CREATE SCHEMA audit;

DROP TABLE IF EXISTS public.aud_excluded_columns;
DROP TABLE IF EXISTS public.aud_excluded_schemas;
DROP TABLE IF EXISTS public.aud_excluded_tables;

CREATE TABLE public.aud_excluded_columns (
    column_name varchar,
    table_name varchar,
    CONSTRAINT aud_excluded_columns_pk PRIMARY KEY(column_name, table_name)
);

CREATE TABLE aud_excluded_schemas (
  id integer NOT NULL,
  schema_name character varying,
  CONSTRAINT aud_excluded_schemas_pkey PRIMARY KEY (id)
);
CREATE SEQUENCE IF NOT EXISTS aud_excluded_schemas_seq;
ALTER TABLE aud_excluded_schemas ADD CONSTRAINT aud_excluded_schemas_schema_name_uq UNIQUE (schema_name);

INSERT INTO aud_excluded_schemas(id, schema_name) VALUES (nextval('aud_excluded_schemas_seq'), 'quartz') ON CONFLICT (schema_name) DO NOTHING;
INSERT INTO aud_excluded_schemas(id, schema_name) VALUES (nextval('aud_excluded_schemas_seq'), 'audit') ON CONFLICT (schema_name) DO NOTHING;
INSERT INTO aud_excluded_schemas(id, schema_name) VALUES (nextval('aud_excluded_schemas_seq'), 'liquibase') ON CONFLICT (schema_name) DO NOTHING;

CREATE TABLE aud_excluded_tables(
  id integer NOT NULL,
  table_name character varying,
  aud_who character varying,
  aud_who_create character varying,
  aud_when timestamp without time zone,
  aud_when_create timestamp without time zone,
  aud_source_create character varying,
  aud_source character varying,
  CONSTRAINT aud_excluded_tables_pkey PRIMARY KEY (id)
);
CREATE SEQUENCE IF NOT EXISTS aud_excluded_tables_seq;
ALTER TABLE aud_excluded_tables ADD CONSTRAINT aud_excluded_tables_table_name_uq UNIQUE (table_name);
ALTER TABLE aud_excluded_tables ADD CONSTRAINT aud_excluded_tables_check_schema CHECK (split_part(table_name, '.', 2) != '');

INSERT INTO aud_excluded_tables(id, table_name) VALUES (nextval('aud_excluded_tables_seq'), 'public.aud_excluded_schemas') ON CONFLICT (table_name) DO NOTHING;
INSERT INTO aud_excluded_tables(id, table_name) VALUES (nextval('aud_excluded_tables_seq'), 'public.aud_excluded_tables') ON CONFLICT (table_name) DO NOTHING;
INSERT INTO aud_excluded_tables(id, table_name) VALUES (nextval('aud_excluded_tables_seq'), 'public.aud_excluded_columns') ON CONFLICT (table_name) DO NOTHING;

CREATE OR REPLACE FUNCTION aud_add_audit(table_name text)
  RETURNS void AS
$BODY$
    BEGIN
      EXECUTE format('select aud_add_audit_columns(''%s'');
        select aud_add_audit_trigger(''%s'')', $1, $1);
    END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;


CREATE OR REPLACE FUNCTION aud_add_audit()
  RETURNS void AS
$BODY$
    BEGIN
	    perform aud_add_audit(ta.*) from aud_tables_to_audit() ta;
    END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;

CREATE OR REPLACE FUNCTION aud_add_audit_columns(table_name text)
  RETURNS void AS
$BODY$
BEGIN
  BEGIN
    EXECUTE format('ALTER TABLE %s ADD COLUMN aud_who varchar;', $1);
    EXCEPTION
    WHEN duplicate_column THEN RAISE NOTICE 'column aud_who already exists in %', $1;
  END;

  BEGIN
    EXECUTE format('ALTER TABLE %s ADD COLUMN aud_when timestamp;', $1);
    EXCEPTION
    WHEN duplicate_column THEN RAISE NOTICE 'column aud_when already exists in %', $1;
  END;

  BEGIN
    EXECUTE format('ALTER TABLE %s ADD COLUMN aud_source varchar;', $1);
    EXCEPTION
    WHEN duplicate_column THEN RAISE NOTICE 'column aud_source already exists in %', $1;
  END;

  BEGIN
    EXECUTE format('ALTER TABLE %s ADD COLUMN aud_who_create varchar;', $1);
    EXCEPTION
    WHEN duplicate_column THEN RAISE NOTICE 'column aud_who_create already exists in %', $1;
  END;

  BEGIN
    EXECUTE format('ALTER TABLE %s ADD COLUMN aud_when_create timestamp;', $1);
    EXCEPTION
    WHEN duplicate_column THEN RAISE NOTICE 'column aud_when_create already exists in %', $1;
  END;

  BEGIN
    EXECUTE format('ALTER TABLE %s ADD COLUMN aud_source_create varchar;', $1);
    EXCEPTION
    WHEN duplicate_column THEN RAISE NOTICE 'column aud_source_create already exists in %', $1;
  END;

END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;

CREATE OR REPLACE FUNCTION aud_add_audit_trigger(table_name text)
  RETURNS void AS
$BODY$
    BEGIN
        EXECUTE format('CREATE TRIGGER audit_trigger
			BEFORE INSERT OR UPDATE
			ON %s
			FOR EACH ROW
			EXECUTE PROCEDURE audit_trigger_fun();', $1);
    END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;

CREATE OR REPLACE FUNCTION aud_disable_audit_trigger(value BOOLEAN)
  RETURNS VOID AS
  $BODY$
  BEGIN
    PERFORM disable_trigger($1, 'audit_trigger');
  END;
  $BODY$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION aud_drop_audit(table_name text)
  RETURNS void AS
$BODY$
    BEGIN
	EXECUTE format('ALTER TABLE %s DROP COLUMN IF EXISTS aud_who;
			ALTER TABLE %s DROP COLUMN IF EXISTS aud_when;
			ALTER TABLE %s DROP COLUMN IF EXISTS aud_source;
			ALTER TABLE %s DROP COLUMN IF EXISTS aud_when_create;
			ALTER TABLE %s DROP COLUMN IF EXISTS aud_who_create;
			ALTER TABLE %s DROP COLUMN IF EXISTS aud_source_create;
			DROP TRIGGER IF EXISTS audit_trigger ON %s ', $1, $1, $1, $1, $1, $1, $1);
    END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;


CREATE OR REPLACE FUNCTION aud_drop_audit()
  RETURNS void AS
$BODY$
    DECLARE
	table_name varchar;
    BEGIN
	FOR table_name IN select event_object_schema ||'.'|| event_object_table from information_schema.triggers where trigger_name = 'audit_trigger' group by 1
	LOOP
            EXECUTE format('select aud_drop_audit(''%s'');', table_name);
	END LOOP;
    END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;

CREATE OR REPLACE FUNCTION aud_get_table_status(table_name TEXT)
  RETURNS CHARACTER VARYING AS
  $BODY$
  DECLARE
    enabled TEXT;
    aud_level TEXT;
  BEGIN
    SELECT pg_trigger.tgenabled INTO enabled
    FROM pg_trigger
    WHERE tgname = 'audit_trigger_full' AND EXISTS(SELECT 1
                                               FROM information_schema.triggers
                                               WHERE trigger_name = pg_trigger.tgname
                                                     AND
                                                     (event_object_schema || '."' || event_object_table || '"' = $1 OR
                                                      event_object_schema || '.' || event_object_table = $1 OR
                                                      event_object_table = $1));
    IF (enabled IS NOT NULL)
    THEN
      IF(enabled ='D') THEN RETURN 'FULL_AUDITED_OFF'; ELSE RETURN 'FULL_AUDITED_ON'; END IF;
    ELSE
      SELECT pg_trigger.tgenabled INTO enabled
      FROM pg_trigger
      WHERE tgname = 'audit_trigger' AND EXISTS(SELECT 1
                                                 FROM information_schema.triggers
                                                 WHERE trigger_name = pg_trigger.tgname
                                                       AND
                                                       (event_object_schema || '."' || event_object_table || '"' = $1 OR
                                                        event_object_schema || '.' || event_object_table = $1 OR
                                                        event_object_table = $1));
      IF(enabled is NULL ) THEN  RETURN 'NO'; END IF;
      IF(enabled ='D') THEN RETURN 'AUDITED_OFF'; ELSE RETURN 'AUDITED_ON'; END IF;
    END IF;
  END;
  $BODY$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION aud_tables_to_audit()
  RETURNS SETOF character varying AS
$BODY$
    DECLARE r varchar;
    BEGIN
	for r in select table_schema||'."'||table_name||'"'  from information_schema.tables
           where table_type = 'BASE TABLE' and not table_schema like 'pg_%' and table_schema <> 'information_schema'
           and table_name <> 'databasechangelog' and table_name <> 'databasechangeloglock'
           and not table_name like '%_aud'
           and not exists (select 1 from aud_excluded_schemas where schema_name = information_schema.tables.table_schema)
           and not exists(select 1 from information_schema.triggers where trigger_name = 'audit_trigger' and event_object_schema ||'.'|| event_object_table = information_schema.tables.table_schema||'.'||information_schema.tables.table_name)
           and not exists(select 1 from aud_excluded_tables where table_name = information_schema.tables.table_schema||'.'||information_schema.tables.table_name)
	LOOP
	   return next r;
	END LOOP;
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

CREATE OR REPLACE FUNCTION audit.add()
  RETURNS VOID AS
  $BODY$
  BEGIN
    PERFORM audit.add(t.*)
    FROM audit.tables_to_full_audit() t;
  END;
$BODY$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION audit.add(table_name TEXT)
  RETURNS VOID AS
  $BODY$
  DECLARE
    count_trigger INTEGER;
  BEGIN
    IF (0 = (SELECT count(*)
             FROM pg_index, pg_class, pg_attribute, pg_namespace
             WHERE
               pg_class.oid = table_name :: REGCLASS AND
               indrelid = pg_class.oid AND
               pg_class.relnamespace = pg_namespace.oid AND
               pg_attribute.attrelid = pg_class.oid AND
               pg_attribute.attnum = ANY (pg_index.indkey)
               AND indisprimary)
    )
    THEN
      RAISE NOTICE '% was skipped, because table has not primary key', table_name;
      RETURN;
    END IF;
    SELECT count(*)
    INTO count_trigger
    FROM information_schema.triggers
    WHERE trigger_name = 'audit_trigger'
          AND (event_object_schema || '."' || event_object_table || '"' = $1 OR
               event_object_schema || '.' || event_object_table = $1 OR event_object_table = $1);
    IF (count_trigger = 0)
    THEN
      PERFORM aud_add_audit($1);
    END IF;
    PERFORM audit.add_table($1);
    PERFORM audit.add_trigger($1);
  END;
  $BODY$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION audit.add_trigger(table_name text)
  RETURNS void AS
  $BODY$
  BEGIN
    EXECUTE format('CREATE TRIGGER audit_trigger_full
			AFTER INSERT OR UPDATE OR DELETE
      ON %s
      FOR EACH ROW
      EXECUTE PROCEDURE audit.trigger_fun();', $1);
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

CREATE OR REPLACE FUNCTION audit.create_current_parts(tbl_limit integer)
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

CREATE OR REPLACE FUNCTION audit.create_next_part(original_table_name TEXT)
  RETURNS VOID AS
  $BODY$
  BEGIN
    PERFORM set_config('app.source', 'create_next_part', TRUE);
    PERFORM audit.add_table(original_table_name);
  END;
  $BODY$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION audit.create_next_parts()
  RETURNS VOID AS
  $BODY$
  DECLARE
    r TEXT;
  BEGIN
    FOR r IN
    SELECT event_object_schema || '.' || event_object_table
    FROM information_schema.triggers
    WHERE trigger_name = 'audit_trigger_full'
    GROUP BY event_object_schema || '.' || event_object_table
    LOOP
      PERFORM audit.create_next_part(r);
    END LOOP;
  END;
  $BODY$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION audit.create_next_parts(tbl_limit INTEGER)
  RETURNS INTEGER AS
  $BODY$
  DECLARE
    r TEXT;
    i INTEGER;
  BEGIN
    PERFORM set_config('app.source', 'create_next_part', TRUE);
    i := 0;
    FOR r IN
    SELECT event_object_schema || '.' || event_object_table
    FROM information_schema.triggers
    WHERE trigger_name = 'audit_trigger_full'
          AND exists(SELECT audit.get_primary_keys(event_object_schema || '.' || event_object_table))
          AND NOT exists(SELECT 1
                         FROM information_schema.tables
                         WHERE
                           table_name = event_object_schema || '$' || event_object_table || audit.get_part_postfix() AND
                           table_schema = 'audit')
    GROUP BY event_object_schema || '.' || event_object_table
    LIMIT tbl_limit
    LOOP
      PERFORM audit.create_next_part(r);
      i := i + 1;
    END LOOP;
    RETURN i;
  END;
  $BODY$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION audit.disable(value BOOLEAN)
  RETURNS VOID AS
  $BODY$
  BEGIN
    PERFORM disable_trigger($1, 'audit_trigger_full');
  END;
  $BODY$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION audit.exclude_denorm_columns(
  data     JSONB,
  tbl_name TEXT)
  RETURNS JSONB AS
  $BODY$
  DECLARE _column_name TEXT;
          _result      JSONB;
  BEGIN
    _result := data;
    FOR _column_name IN SELECT column_name
                        FROM public.aud_excluded_columns
                        WHERE table_name = tbl_name
    LOOP
      _result := _result - _column_name;
    END LOOP;
    RETURN _result;
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

CREATE OR REPLACE FUNCTION audit.get_audited_tables()
  RETURNS SETOF CHARACTER VARYING AS
  $BODY$
  DECLARE r VARCHAR;
  BEGIN
    FOR r IN SELECT table_schema || '."' || table_name || '"'
             FROM information_schema.tables
             WHERE exists(SELECT 1
                          FROM information_schema.triggers
                          WHERE trigger_name = 'audit_trigger_full' AND
                                event_object_schema || '.' || event_object_table =
                                information_schema.tables.table_schema || '.' || information_schema.tables.table_name)
    LOOP
      RETURN NEXT r;
    END LOOP;
  END;
  $BODY$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION audit.get_part_postfix()
    RETURNS TEXT AS
$BODY$
DECLARE
    current_dt TIMESTAMP;
BEGIN
    /*
    ------- replicate mode ---------
    current_dt := now() at time zone 'utc;
    IF ('create_next_part' = (SELECT current_setting('app.source'))) THEN
      RETURN '_y' || date_part( 'year', current_dt + interval '1 month' )::text || '_m' || date_part( 'month', current_dt + interval '1 month')::text;
    ELSEIF ('drop_previous_part' = (SELECT current_setting('app.source'))) THEN
      RETURN '_y' || date_part( 'year', current_dt - interval '1 month' )::text || '_m' || date_part( 'month', current_dt - interval '1 month')::text;
    ELSE
      RETURN '_y' || date_part( 'year', current_dt )::text || '_m' || date_part( 'month', current_dt )::text;
    END IF;
     ------- end ---------
    */

    -------- simple mode -------
    IF ('create_current_part' = (SELECT current_setting('app.source')))
    THEN
        current_dt := now() at time zone 'utc';
        RETURN '_y' || date_part('year', current_dt) :: TEXT || '_m' || date_part('month', current_dt) :: TEXT;
    ELSE
        RETURN '';
    END IF;
    ------- end ---------
END;
$BODY$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION audit_trigger_fun()
    RETURNS trigger AS
$BODY$
DECLARE
    app_user   TEXT;
    app_source TEXT;
    cur_time TIMESTAMP;
BEGIN
    cur_time := now() at time zone 'utc';
    --uses coalesce because have problem to equal with null
    IF (TG_OP = 'UPDATE' AND (coalesce(NEW.aud_who_create, 'empty') != coalesce(OLD.aud_who_create, 'empty')
        OR coalesce(NEW.aud_when_create, cur_time) != coalesce(OLD.aud_when_create, cur_time)
        OR coalesce(NEW.aud_source_create, 'empty') != coalesce(OLD.aud_source_create, 'empty')
        OR coalesce(NEW.aud_who, 'empty') != coalesce(OLD.aud_who, 'empty')
        OR coalesce(NEW.aud_when, cur_time) != coalesce(OLD.aud_when, cur_time)
        OR coalesce(NEW.aud_source, 'empty') != coalesce(OLD.aud_source, 'empty'))
        OR TG_OP = 'INSERT' AND (NEW.aud_who_create IS NOT NULL OR NEW.aud_when_create IS NOT NULL OR NEW.aud_source_create IS NOT NULL
            OR NEW.aud_who IS NOT NULL OR NEW.aud_when IS NOT NULL  OR NEW.aud_source IS NOT NULL)
        )
    THEN
        RAISE EXCEPTION 'AUDIT COLUMNS NOT EDITABLE';
        RETURN NULL;
    END IF;
    -- set value for extended_aud_trigger
    PERFORM set_config('aud.when'::TEXT, to_char(cur_time, 'YYYY-MM-DD HH24:MI:SS:MS'), true);
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
    NEW.aud_when:= cur_time;
    NEW.aud_who:= app_user;
    NEW.aud_source:= app_source;
    IF (TG_OP = 'INSERT')
    THEN
        NEW.aud_when_create:=NEW.aud_when;
        NEW.aud_who_create:=NEW.aud_who;
        NEW.aud_source_create:=NEW.aud_source;
    END IF;
    RETURN NEW;
END;
$BODY$
    LANGUAGE plpgsql VOLATILE
                     COST 100;

CREATE OR REPLACE FUNCTION audit.get_primary_keys(IN table_name TEXT)
  RETURNS TABLE(column_name TEXT, type TEXT) AS
  $BODY$
  DECLARE
  BEGIN
    RETURN QUERY
    SELECT
      pg_attribute.attname :: TEXT                               AS column_name,
      format_type(pg_attribute.atttypid, pg_attribute.atttypmod) AS type
    FROM pg_index, pg_class, pg_attribute, pg_namespace
    WHERE
      pg_class.oid = $1 :: REGCLASS AND
      indrelid = pg_class.oid AND
      pg_class.relnamespace = pg_namespace.oid AND
      pg_attribute.attrelid = pg_class.oid AND
      pg_attribute.attnum = ANY (pg_index.indkey)
      AND indisprimary;
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

CREATE OR REPLACE FUNCTION audit.jsonb_diff_val(newVal JSONB,oldVal JSONB)
  RETURNS JSONB AS $$
DECLARE
  result JSONB;
  v RECORD;
BEGIN
   result = newVal;
   FOR v IN SELECT * FROM jsonb_each(oldVal) LOOP
     IF result @> jsonb_build_object(v.key,v.value)
        THEN result = result - v.key;
     ELSIF result ? v.key THEN CONTINUE;
     ELSE
        result = result || jsonb_build_object(v.key,'null');
     END IF;
   END LOOP;
   RETURN result;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION audit.tables_to_full_audit()
  RETURNS SETOF CHARACTER VARYING AS
  $BODY$
  DECLARE r VARCHAR;
  BEGIN
    FOR r IN SELECT table_schema || '."' || table_name || '"' AS table_name
             FROM information_schema.tables
             WHERE table_type = 'BASE TABLE' AND NOT table_schema LIKE 'pg_%' AND table_schema <> 'information_schema'
                   AND table_name <> 'databasechangelog' AND table_name <> 'databasechangeloglock'
                   AND NOT table_name LIKE '%_aud'
                   AND NOT exists(SELECT 1
                                  FROM public.aud_excluded_schemas
                                  WHERE schema_name = information_schema.tables.table_schema)
                   AND NOT exists(SELECT 1
                                  FROM information_schema.triggers
                                  WHERE trigger_name = 'audit_trigger_full' AND
                                        event_object_schema || '.' || event_object_table =
                                        information_schema.tables.table_schema || '.' ||
                                        information_schema.tables.table_name)
                   AND NOT exists(SELECT 1
                                  FROM public.aud_excluded_tables
                                  WHERE table_name = information_schema.tables.table_schema || '.' ||
                                                     information_schema.tables.table_name)
                   AND NOT table_name LIKE 'aud_excluded_%'
    LOOP
      RETURN NEXT r;
    END LOOP;
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
                USING 'D', data, now() at time zone 'utc', app_user, app_source;
        END;
        RETURN OLD;
    END IF;

    RETURN NEW;
END;
$BODY$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION audit.add_table(table_name TEXT)
  RETURNS VOID AS
  $BODY$
  DECLARE
    ddl                             TEXT;
    parent_table_name               TEXT;
    audit_table_name                TEXT;
    audit_table_name_without_scheme TEXT;
  BEGIN
    SELECT audit.gen_audit_table_ddl(table_name)
    INTO ddl;
    EXECUTE ddl;
  END;
  $BODY$
LANGUAGE plpgsql;