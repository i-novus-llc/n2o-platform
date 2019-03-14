-------------------tables--------------------------
CREATE TABLE aud_excluded_schemas
(
  id integer NOT NULL,
  schema_name character varying,
  CONSTRAINT aud_excluded_schemas_pkey PRIMARY KEY (id)
);

CREATE SEQUENCE aud_excluded_schemas_seq;

insert into aud_excluded_schemas(id, schema_name) values(nextval('aud_excluded_schemas_seq'),'liquibase');
insert into aud_excluded_schemas(id, schema_name) values(nextval('aud_excluded_schemas_seq'),'schedule');
insert into aud_excluded_schemas(id, schema_name) values(nextval('aud_excluded_schemas_seq'),'pentaho');
insert into aud_excluded_schemas(id, schema_name) values(nextval('aud_excluded_schemas_seq'),'supp');
insert into aud_excluded_schemas(id, schema_name) values(nextval('aud_excluded_schemas_seq'),'jenkins');



CREATE TABLE aud_excluded_tables
(
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

CREATE SEQUENCE aud_excluded_tables_seq;

insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.aud_excluded_schemas');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.aud_excluded_table');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'amb.change_test');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'analytics.t039test1');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'messenger.message_log');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'messenger.message__user');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.a');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.aa');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.activemq_acks');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.activemq_lock');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.activemq_msgs');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.audit_log');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.aud_log');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.cas_user_tgt_counter');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.cmn_build_version');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.cmn_module');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.cmn_module_group');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.cmn_msg');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.cmn_msg_alert_date_time');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.cmn_msg_alert_type');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.cmn_msg_log');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.cmn_msg_recipient_type');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.cmn_msg_severity_level');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.cmn_msg_to_group');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.cmn_msg_to_role');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.cmn_msg_to_user');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.cmn_num_current_value');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.cmn_num_scope_current_value');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.com_audit_trail');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.ehr_protocol_query');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.ehr_protocol_query_result');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.email_log');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.fin_bill_spec_item');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.fin_bill_spec_item_error');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.fin_bill_spec_item_returned');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.fin_bill_spec_item_returned_error');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.fin_bill_spec_item_returned_error_lock');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.iehr_xds_registry_message');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.iehr_xds_repository_message');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.i_lis_referral');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_atc');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_authorization');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_authorization_status');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_authorization_type');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_char_property');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_commodity_group');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_commodity_group_req_type');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_contractor_type');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_descr');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_detail');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_drug_group');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inventory_provider_line');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_expiration_period');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_form');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_form_descr');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_form_form_param');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_form_pack');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_form_param');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_form_param_value');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_form_price');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_form_type');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_formulary_list');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_holding');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_holding2_v');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_holding_atc');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_holding_form_org');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_holding_formulary_list');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_holding_form_wo_org');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_holding_name');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_holding_pharm_action');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_holding_pharm_group');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_importance_category');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_inn');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_inn_pharm_action');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_inn_pharm_group');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_inn_pharm_group_diagnosis');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_list');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_md_holding');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_md_holding_pharm_prop');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_nosological_class');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_opr');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_opr_kind');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_opr_source');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_opr_ty_commodity_grp');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_opr_type');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_opr_type_cons');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_opr_type_dep');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_opr_type_org_role');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_pack_kind');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_pack_param');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_pharm_action');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_pharm_group');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_pharm_property');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_potent_poison_group');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_producer');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_record_state');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_reg_cert');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_reg_cert_form');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_reg_cert_status');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_reg_cert_status_data');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_reg_cert_status_data_v');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_reg_cert_type');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_reg_cert_v');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_remains');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_remains_export_v');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_remains_h');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_remains_h_export_v');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_remains_norm');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_response_level');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_response_person');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_restriction_desc');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_restriction_type');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_series');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_series_expire');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_series_restriction');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_spec');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_spec_remains_v');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_storage_condition');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_store');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_store_commodity_group');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_store_level');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_store_nomenclature');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_store_post');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_store_prop');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_supervisor_letter');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.inv_taking_method');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.i_prg_address');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.i_prg_address_ref');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.i_prg_clinic');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.i_prg_department');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.i_prgdep_dep');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.i_prg_edu_organization');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.i_prg_employee');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.i_prg_employee_to_position');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.i_prg_emp_session');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.i_prg_position_role');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.i_prg_region');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.i_prg_reg_type');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.i_prg_task');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.i_prg_task_operation');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.i_prg_task_status');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.i_prg_task_type');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.i_rls_id_map');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.i_rls_tab');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.i_sync_entity');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.i_sync_entity_dep');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.i_sync_entity_ver');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.i_sync_ext_entity');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.i_sync_map');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.i_sync_tab');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.i_sync_tab_dep');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.i_sync_ver');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.i_sync_ver_map');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.kaluga_loader_log');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.kaluga_loader_src');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.locks');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.log_trace');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.mc_inv_consumable');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.mc_inv_opr_srv');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.mc_inv_spec_consumable');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.mc_inv_srv_consumable');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.mc_inv_srv_standard_consumable');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.md_cda_log');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.md_cda_section');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.md_cda_section_data_type');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.md_cda_signed');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.md_ehr_class');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.sec_audit_entry');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.sec_audit_entry_entity_deprecated');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.sms_log');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.sr_org_shift_aud');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.sr_scgenerated');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.sviridov_tmp_resgroup_searchable');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.sviridov_tmp_searchable');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.sviridov_tmp_searchable_term');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.sviridov_tmp_term');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.sviridov_tmp_term2');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.temp_cmn_page');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.temp_doc_code');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.temp_indiv');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.temp_test_r');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.test2310');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.test2310_1');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.test_indiv');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.test_sec_class');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.test_sec_filter_class');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.tmp_f002');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.tmp_new_uid_for_org_v2');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.tql_message_log');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.ts_temp_predpri');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.union_individuals_temp');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.v_pim_indiv_doc_and_code');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.zz_id_map');
insert into aud_excluded_tables(id, table_name) values(nextval('aud_excluded_tables_seq'), 'public.zz_test_sync_entity');

-------------------functions--------------------------
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


CREATE OR REPLACE FUNCTION audit_trigger_fun()
  RETURNS trigger AS
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


CREATE OR REPLACE FUNCTION aud_disable_audit_trigger(value boolean)
  RETURNS void AS
$BODY$
  BEGIN
    PERFORM disable_trigger($1, 'audit_trigger');
  END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;


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




