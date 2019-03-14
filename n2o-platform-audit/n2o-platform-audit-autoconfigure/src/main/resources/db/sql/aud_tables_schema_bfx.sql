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