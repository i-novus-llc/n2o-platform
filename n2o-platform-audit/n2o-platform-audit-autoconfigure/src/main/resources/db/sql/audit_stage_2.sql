CREATE EXTENSION IF NOT EXISTS hstore;

CREATE SCHEMA audit;
insert into aud_excluded_schemas(id, schema_name) values(nextval('aud_excluded_schemas_seq'), 'dba');
insert into aud_excluded_schemas(id, schema_name) values(nextval('aud_excluded_schemas_seq'), 'audit');


CREATE OR REPLACE FUNCTION disable_trigger(value BOOLEAN, trigger_name TEXT)
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
                      WHERE trigger_name = $2
                      GROUP BY 1
    LOOP
      EXECUTE format('ALTER TABLE %s %s TRIGGER %s', table_name, action_var, $2);
    END LOOP;

  END;
  $BODY$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION audit.add_table(table_name TEXT)
  RETURNS VOID AS
  $BODY$
  DECLARE
    pk_column_info               RECORD;
    pk_value_to_write_with_types TEXT;
    pk_value_to_write TEXT;
    full_table_name TEXT;
    audit_table_name TEXT;
  BEGIN
    pk_value_to_write_with_types := '';
    pk_value_to_write := '';
    IF (strpos($1, '.') = 0)
    THEN full_table_name := 'public.' || $1;
    ELSE full_table_name := $1; END IF;
    audit_table_name := replace(replace(full_table_name, '.', '$'), '"', '');
    FOR pk_column_info IN SELECT
                            pg_attribute.attname :: TEXT                               AS column_name,
                            format_type(pg_attribute.atttypid, pg_attribute.atttypmod) AS type
                          FROM pg_index, pg_class, pg_attribute, pg_namespace
                          WHERE
                            pg_class.oid = full_table_name :: REGCLASS AND
                            indrelid = pg_class.oid AND
                            pg_class.relnamespace = pg_namespace.oid AND
                            pg_attribute.attrelid = pg_class.oid AND
                            pg_attribute.attnum = ANY (pg_index.indkey)
                            AND indisprimary
    LOOP
      pk_value_to_write_with_types := pk_value_to_write_with_types || pk_column_info.column_name || ' ' || pk_column_info.type || ',';
      pk_value_to_write := pk_value_to_write || pk_column_info.column_name || ',';

    END LOOP;
    EXECUTE format('CREATE TABLE audit."%s"(
        aud_rec bigserial,
				%s
				type TEXT NOT NULL CHECK (type IN (''I'',''D'',''U'',''T'')),
				delta hstore,
				aud_when timestamp(6),
				aud_who varchar,
				aud_source varchar,
				CONSTRAINT %s_pk PRIMARY KEY (aud_rec));
				CREATE INDEX ON audit.%s(%s) '
    , audit_table_name, pk_value_to_write_with_types, audit_table_name, audit_table_name, substring(pk_value_to_write, 1, length(pk_value_to_write) - 1));
  END;
  $BODY$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION audit.trigger_fun()
  RETURNS TRIGGER AS
  $BODY$
  DECLARE
    app_user           TEXT;
    app_source         TEXT;
    new_row_store      hstore;
    old_row_store      hstore;
    delta              hstore;
    nullable_key       TEXT;
    insert_query       TEXT;
    pk_column_info     RECORD;
    pk_column_names    TEXT;
    pk_values_from_new TEXT;
    pk_values_from_old TEXT;

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
      new_row_store := hstore(NEW.*);
    END IF;

    IF (TG_OP = 'DELETE' OR TG_OP = 'UPDATE')
    THEN
      old_row_store := hstore(OLD.*);
    END IF;

    pk_column_names := '';
    pk_values_from_new := '';
    pk_values_from_old := '';
    FOR pk_column_info IN SELECT
                            pg_attribute.attname :: TEXT                               AS column_name,
                            format_type(pg_attribute.atttypid, pg_attribute.atttypmod) AS type
                          FROM pg_index, pg_class, pg_attribute, pg_namespace
                          WHERE
                            pg_class.oid = TG_TABLE_NAME :: REGCLASS AND
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
        pk_values_from_new || '''' || (new_row_store :: hstore -> pk_column_info.column_name) || '''' || '::' ||
        pk_column_info.type || ', ';
      END IF;

      IF (TG_OP = 'DELETE')
      THEN
        pk_values_from_old :=
        pk_values_from_old || '''' || (old_row_store :: hstore -> pk_column_info.column_name) || '''' || '::' ||
        pk_column_info.type || ', ';
      END IF;

    END LOOP;

    IF (TG_OP = 'INSERT')
    THEN

      insert_query := format(
          'INSERT INTO %s (%s type, delta, aud_when, aud_who, aud_source) VALUES (%s $1, $2, $3, $4, $5);'
          , 'audit."'||TG_TABLE_SCHEMA||'$' || TG_TABLE_NAME :: TEXT ||'"', pk_column_names, pk_values_from_new);
      FOR nullable_key IN select hs.key
                          from each(new_row_store) hs
                          where hs.value IS NULL
      LOOP
        -- exclude null values
        new_row_store := new_row_store - ('{'||nullable_key||'}')::text[];
      END LOOP;
      delta := new_row_store - '{aud_when, aud_who, aud_source, aud_when_create, aud_who_create, aud_source_create}'::text[];
      BEGIN
        EXECUTE insert_query
        USING 'I', delta, to_timestamp(current_setting('aud.when'), 'YYYY-MM-DD HH24:MI:SS:MS'), app_user, app_source;
      END;

    ELSEIF (TG_OP = 'UPDATE')
      THEN
        delta := new_row_store - old_row_store - '{aud_when, aud_who, aud_source, aud_when_create, aud_who_create, aud_source_create}'::text[];
        IF (delta = hstore('')) THEN RETURN NULL; END IF;
        insert_query := format(
            'INSERT INTO %s (%s type, delta, aud_when, aud_who, aud_source) VALUES (%s $1, $2, $3, $4, $5);'
            , 'audit."'||TG_TABLE_SCHEMA||'$' || TG_TABLE_NAME :: TEXT ||'"', pk_column_names, pk_values_from_new);
        BEGIN
          EXECUTE insert_query
          USING 'U', delta, to_timestamp(current_setting('aud.when'), 'YYYY-MM-DD HH24:MI:SS:MS'), app_user, app_source;
        END;

    ELSEIF (TG_OP = 'DELETE')
      THEN

        insert_query := format(
            'INSERT INTO %s (%s type, delta, aud_when, aud_who, aud_source) VALUES (%s $1, $2, $3, $4, $5);'
            , 'audit."'||TG_TABLE_SCHEMA||'$' || TG_TABLE_NAME :: TEXT ||'"', pk_column_names, pk_values_from_old);
        BEGIN
          EXECUTE insert_query
          USING 'D', old_row_store, now(), app_user, app_source;
        END;
      RETURN OLD;
    END IF;

    RETURN NEW;
  END;
  $BODY$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION audit_trigger_fun()
  RETURNS TRIGGER AS
  $BODY$
  DECLARE
    app_user   TEXT;
    app_source TEXT;
    cur_time TIMESTAMP;
  BEGIN
    cur_time := now();
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
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION audit.add_trigger(table_name TEXT)
  RETURNS VOID AS
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

CREATE OR REPLACE FUNCTION audit.disable(value BOOLEAN)
  RETURNS VOID AS
  $BODY$
  BEGIN
    PERFORM disable_trigger($1, 'audit_trigger_full');
  END;
  $BODY$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION aud_disable_audit_trigger(value BOOLEAN)
  RETURNS VOID AS
  $BODY$
  BEGIN
    PERFORM disable_trigger($1, 'audit_trigger');
  END;
  $BODY$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION audit.add(table_name TEXT)
  RETURNS VOID AS
  $BODY$
  DECLARE
    count_trigger integer;
  BEGIN
    select count(*) into count_trigger from information_schema.triggers
    where trigger_name = 'audit_trigger'
          and (event_object_schema ||'."'|| event_object_table||'"' = $1 OR event_object_schema ||'.'|| event_object_table = $1 OR event_object_table = $1);
    IF(count_trigger = 0)
    THEN
      PERFORM aud_add_audit($1);
    END IF;
    PERFORM audit.add_table($1);
    PERFORM audit.add_trigger($1);
  END;
  $BODY$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION audit.get_audited_tables()
  RETURNS SETOF character varying AS
  $BODY$
    DECLARE r varchar;
    BEGIN
	for r in select table_schema||'."'||table_name||'"'  from information_schema.tables
           where  exists(select 1 from information_schema.triggers where trigger_name = 'audit_trigger_full' and event_object_schema ||'.'|| event_object_table = information_schema.tables.table_schema||'.'||information_schema.tables.table_name)
	LOOP
	   return next r;
	END LOOP;
END;
$BODY$
LANGUAGE plpgsql;

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

