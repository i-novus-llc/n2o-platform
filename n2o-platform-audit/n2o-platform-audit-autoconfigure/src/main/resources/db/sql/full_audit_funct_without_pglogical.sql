DROP FUNCTION IF EXISTS audit.drop_replicate_subscriber();

DROP FUNCTION IF EXISTS audit.drop_replicate_provider();

DROP FUNCTION IF EXISTS audit.drop_previous_parts();

DROP FUNCTION IF EXISTS audit.add_replicate_provider( TEXT, TEXT, TEXT );

DROP FUNCTION IF EXISTS audit.add_replicate_subscriber( TEXT, TEXT, TEXT, TEXT, TEXT, TEXT );

DROP FUNCTION IF EXISTS audit.drop_previous_part( TEXT );

DROP FUNCTION IF EXISTS audit.do_inherit( TEXT, TEXT );

CREATE OR REPLACE FUNCTION audit.disable(value BOOLEAN)
  RETURNS VOID AS
  $BODY$
  BEGIN
    PERFORM disable_trigger($1, 'audit_trigger_full');
  END;
  $BODY$
LANGUAGE plpgsql;

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
    LOOP
      RETURN NEXT r;
    END LOOP;
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

CREATE OR REPLACE FUNCTION audit.get_part_postfix()
  RETURNS TEXT AS
  $BODY$
  DECLARE
    current_dt TIMESTAMP;
  BEGIN
    /*
    ------- replicate mode ---------
    current_dt := now();
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
      current_dt := now();
      RETURN '_y' || date_part('year', current_dt) :: TEXT || '_m' || date_part('month', current_dt) :: TEXT;
    ELSE
      RETURN '';
    END IF;
    ------- end ---------
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
				CONSTRAINT %s_pk PRIMARY KEY (aud_rec));
				CREATE INDEX ON audit.%s(%s);
				CREATE INDEX %s_aud_when_ix ON audit.%s(aud_when);
				CREATE INDEX %s_aud_who_ix ON audit.%s(aud_who);
				CREATE INDEX %s_aud_source_ix ON audit.%s(aud_source);

				COMMENT ON COLUMN audit."%s".delta IS ''deprecated'';'
    , audit_table_name, pk_value_to_write_with_types, audit_table_name, audit_table_name,
                  substring(pk_value_to_write, 1, length(pk_value_to_write) - 1)
    , audit_table_name, audit_table_name, audit_table_name, audit_table_name, audit_table_name, audit_table_name,
                  audit_table_name);
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

CREATE OR REPLACE FUNCTION audit.add()
  RETURNS VOID AS
  $BODY$
  BEGIN
    PERFORM audit.add(t.*)
    FROM audit.tables_to_full_audit() t;
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

