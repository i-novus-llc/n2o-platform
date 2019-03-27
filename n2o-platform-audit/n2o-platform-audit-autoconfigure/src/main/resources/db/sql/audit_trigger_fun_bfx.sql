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
                            pg_class.oid = (TG_TABLE_SCHEMA||'.'||TG_TABLE_NAME) :: REGCLASS AND
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
