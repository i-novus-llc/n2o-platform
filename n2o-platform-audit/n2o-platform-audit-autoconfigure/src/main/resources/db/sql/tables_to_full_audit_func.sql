CREATE OR REPLACE FUNCTION audit.tables_to_full_audit()
  RETURNS SETOF CHARACTER VARYING AS
  $BODY$
  DECLARE r VARCHAR;
  BEGIN
    FOR r IN SELECT table_schema || '."' || table_name || '"' as table_name
             FROM information_schema.tables
             WHERE table_type = 'BASE TABLE' AND NOT table_schema LIKE 'pg_%' AND table_schema <> 'information_schema'
                   AND table_name <> 'databasechangelog' AND table_name <> 'databasechangeloglock'
                   AND NOT table_name LIKE '%_aud'
                   AND NOT exists(SELECT 1
                                  FROM aud_excluded_schemas
                                  WHERE schema_name = information_schema.tables.table_schema)
                   AND NOT exists(SELECT 1
                                  FROM information_schema.triggers
                                  WHERE trigger_name = 'audit_trigger_full' AND
                                        event_object_schema || '.' || event_object_table =
                                        information_schema.tables.table_schema || '.' ||
                                        information_schema.tables.table_name)
                   AND NOT exists(SELECT 1
                                  FROM aud_excluded_tables
                                  WHERE table_name = information_schema.tables.table_schema || '.' ||
                                                     information_schema.tables.table_name)
    LOOP
      RETURN NEXT r;
    END LOOP;
  END;
  $BODY$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION audit.add(table_name text)
  RETURNS void AS
  $BODY$
  DECLARE
    count_trigger integer;
  BEGIN
    IF (0 = (SELECT  count (*) FROM pg_index, pg_class, pg_attribute, pg_namespace
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


CREATE OR REPLACE FUNCTION audit.add()
  RETURNS VOID AS
  $BODY$
  BEGIN
    PERFORM audit.add(t.*)
    FROM audit.tables_to_full_audit() t;
  END;
  $BODY$
LANGUAGE plpgsql;