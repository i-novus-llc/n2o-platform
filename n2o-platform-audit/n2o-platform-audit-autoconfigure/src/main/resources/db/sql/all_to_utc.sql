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