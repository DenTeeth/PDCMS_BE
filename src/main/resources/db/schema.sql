--
-- PostgreSQL database dump
--

-- Dumped from database version 13.4 (Debian 13.4-1.pgdg100+1)
-- Dumped by pg_dump version 13.4 (Debian 13.4-1.pgdg100+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: account_status; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.account_status AS ENUM (
    'ACTIVE',
    'INACTIVE',
    'SUSPENDED',
    'LOCKED',
    'PENDING_VERIFICATION'
);


--
-- Name: appointment_action_type; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.appointment_action_type AS ENUM (
    'CREATE',
    'DELAY',
    'RESCHEDULE_SOURCE',
    'RESCHEDULE_TARGET',
    'CANCEL',
    'STATUS_CHANGE'
);


--
-- Name: appointment_participant_role_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.appointment_participant_role_enum AS ENUM (
    'ASSISTANT',
    'SECONDARY_DOCTOR',
    'OBSERVER'
);


--
-- Name: appointment_reason_code; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.appointment_reason_code AS ENUM (
    'PREVIOUS_CASE_OVERRUN',
    'DOCTOR_UNAVAILABLE',
    'EQUIPMENT_FAILURE',
    'PATIENT_REQUEST',
    'OPERATIONAL_REDIRECT',
    'OTHER'
);


--
-- Name: appointment_status_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.appointment_status_enum AS ENUM (
    'SCHEDULED',
    'CHECKED_IN',
    'IN_PROGRESS',
    'COMPLETED',
    'CANCELLED',
    'CANCELLED_LATE',
    'NO_SHOW'
);


--
-- Name: appointmentactiontype; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.appointmentactiontype AS ENUM (
    'CANCEL',
    'CREATE',
    'DELAY',
    'RESCHEDULE_SOURCE',
    'RESCHEDULE_TARGET',
    'STATUS_CHANGE'
);


--
-- Name: appointmentreasoncode; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.appointmentreasoncode AS ENUM (
    'DOCTOR_UNAVAILABLE',
    'EQUIPMENT_FAILURE',
    'OPERATIONAL_REDIRECT',
    'OTHER',
    'PATIENT_REQUEST',
    'PREVIOUS_CASE_OVERRUN'
);


--
-- Name: appointmentstatus; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.appointmentstatus AS ENUM (
    'CANCELLED',
    'CANCELLED_LATE',
    'CHECKED_IN',
    'COMPLETED',
    'IN_PROGRESS',
    'NO_SHOW',
    'SCHEDULED'
);


--
-- Name: approval_status; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.approval_status AS ENUM (
    'DRAFT',
    'PENDING_REVIEW',
    'APPROVED',
    'REJECTED'
);


--
-- Name: attachment_type_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.attachment_type_enum AS ENUM (
    'XRAY',
    'PHOTO_BEFORE',
    'PHOTO_AFTER',
    'LAB_RESULT',
    'CONSENT_FORM',
    'OTHER'
);


--
-- Name: attachmenttypeenum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.attachmenttypeenum AS ENUM (
    'CONSENT_FORM',
    'LAB_RESULT',
    'OTHER',
    'PHOTO_AFTER',
    'PHOTO_BEFORE',
    'XRAY'
);


--
-- Name: balance_change_reason; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.balance_change_reason AS ENUM (
    'ANNUAL_RESET',
    'APPROVED_REQUEST',
    'REJECTED_REQUEST',
    'CANCELLED_REQUEST',
    'MANUAL_ADJUSTMENT'
);


--
-- Name: batchstatus; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.batchstatus AS ENUM (
    'ACTIVE',
    'EXPIRED',
    'DEPLETED'
);


--
-- Name: contact_history_action; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.contact_history_action AS ENUM (
    'CALL',
    'MESSAGE',
    'NOTE'
);


--
-- Name: customer_contact_source; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.customer_contact_source AS ENUM (
    'WEBSITE',
    'FACEBOOK',
    'ZALO',
    'WALK_IN',
    'REFERRAL'
);


--
-- Name: customer_contact_status; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.customer_contact_status AS ENUM (
    'NEW',
    'CONTACTED',
    'APPOINTMENT_SET',
    'NOT_INTERESTED',
    'CONVERTED'
);


--
-- Name: day_of_week; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.day_of_week AS ENUM (
    'MONDAY',
    'TUESDAY',
    'WEDNESDAY',
    'THURSDAY',
    'FRIDAY',
    'SATURDAY',
    'SUNDAY'
);


--
-- Name: employee_shifts_source; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.employee_shifts_source AS ENUM (
    'BATCH_JOB',
    'REGISTRATION_JOB',
    'OT_APPROVAL',
    'MANUAL_ENTRY'
);


--
-- Name: employment_type; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.employment_type AS ENUM (
    'FULL_TIME',
    'PART_TIME_FIXED',
    'PART_TIME_FLEX'
);


--
-- Name: exporttype; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.exporttype AS ENUM (
    'SERVICE',
    'SALE',
    'WASTAGE',
    'TRANSFER'
);


--
-- Name: gender; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.gender AS ENUM (
    'MALE',
    'FEMALE',
    'OTHER'
);


--
-- Name: holiday_type; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.holiday_type AS ENUM (
    'NATIONAL',
    'COMPANY'
);


--
-- Name: image_type; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.image_type AS ENUM (
    'XRAY',
    'PHOTO',
    'BEFORE_TREATMENT',
    'AFTER_TREATMENT',
    'SCAN',
    'OTHER'
);


--
-- Name: invoice_payment_status; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.invoice_payment_status AS ENUM (
    'PENDING_PAYMENT',
    'PARTIAL_PAID',
    'PAID',
    'CANCELLED'
);


--
-- Name: invoice_type; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.invoice_type AS ENUM (
    'APPOINTMENT',
    'TREATMENT_PLAN',
    'SUPPLEMENTAL'
);


--
-- Name: notification_entity_type; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.notification_entity_type AS ENUM (
    'APPOINTMENT',
    'TREATMENT_PLAN',
    'PAYMENT',
    'SYSTEM',
    'TIME_OFF_REQUEST',
    'OVERTIME_REQUEST',
    'PART_TIME_REGISTRATION'
);


--
-- Name: notification_type; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.notification_type AS ENUM (
    'APPOINTMENT_CREATED',
    'APPOINTMENT_UPDATED',
    'APPOINTMENT_CANCELLED',
    'APPOINTMENT_REMINDER',
    'APPOINTMENT_COMPLETED',
    'TREATMENT_PLAN_APPROVED',
    'TREATMENT_PLAN_UPDATED',
    'PAYMENT_RECEIVED',
    'SYSTEM_ANNOUNCEMENT',
    'REQUEST_TIME_OFF_PENDING',
    'REQUEST_OVERTIME_PENDING',
    'REQUEST_PART_TIME_PENDING'
);


--
-- Name: payment_method; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.payment_method AS ENUM (
    'SEPAY'
);


--
-- Name: payment_transaction_status; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.payment_transaction_status AS ENUM (
    'PENDING',
    'SUCCESS',
    'FAILED',
    'CANCELLED'
);


--
-- Name: paymentstatus; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.paymentstatus AS ENUM (
    'UNPAID',
    'PARTIAL',
    'PAID'
);


--
-- Name: paymenttype; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.paymenttype AS ENUM (
    'FULL',
    'PHASED',
    'INSTALLMENT'
);


--
-- Name: phasestatus; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.phasestatus AS ENUM (
    'PENDING',
    'IN_PROGRESS',
    'COMPLETED'
);


--
-- Name: plan_item_status; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.plan_item_status AS ENUM (
    'READY_FOR_BOOKING',
    'SCHEDULED',
    'PENDING',
    'IN_PROGRESS',
    'COMPLETED',
    'WAITING_FOR_PREREQUISITE',
    'SKIPPED'
);


--
-- Name: planactiontype; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.planactiontype AS ENUM (
    'STATUS_CHANGE',
    'PRICE_UPDATE',
    'PHASE_UPDATE',
    'APPROVAL'
);


--
-- Name: registrationstatus; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.registrationstatus AS ENUM (
    'PENDING',
    'APPROVED',
    'REJECTED'
);


--
-- Name: renewal_status; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.renewal_status AS ENUM (
    'PENDING_ACTION',
    'CONFIRMED',
    'FINALIZED',
    'DECLINED',
    'EXPIRED'
);


--
-- Name: request_status; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.request_status AS ENUM (
    'PENDING',
    'APPROVED',
    'REJECTED',
    'CANCELLED'
);


--
-- Name: shift_source; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.shift_source AS ENUM (
    'BATCH_JOB',
    'REGISTRATION_JOB',
    'OT_APPROVAL',
    'MANUAL_ENTRY'
);


--
-- Name: shift_status; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.shift_status AS ENUM (
    'SCHEDULED',
    'ON_LEAVE',
    'COMPLETED',
    'ABSENT',
    'CANCELLED'
);


--
-- Name: stockstatus; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.stockstatus AS ENUM (
    'IN_STOCK',
    'LOW_STOCK',
    'OUT_OF_STOCK'
);


--
-- Name: suppliertier; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.suppliertier AS ENUM (
    'PLATINUM',
    'GOLD',
    'SILVER',
    'BRONZE'
);


--
-- Name: time_off_status; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.time_off_status AS ENUM (
    'PENDING',
    'APPROVED',
    'REJECTED',
    'CANCELLED'
);


--
-- Name: tooth_condition_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.tooth_condition_enum AS ENUM (
    'HEALTHY',
    'CARIES_MILD',
    'CARIES_MODERATE',
    'CARIES_SEVERE',
    'FILLED',
    'CROWN',
    'MISSING',
    'IMPLANT',
    'ROOT_CANAL',
    'FRACTURED',
    'IMPACTED'
);


--
-- Name: transactionstatus; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.transactionstatus AS ENUM (
    'PENDING',
    'COMPLETED',
    'CANCELLED'
);


--
-- Name: transactiontype; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.transactiontype AS ENUM (
    'PURCHASE',
    'SALE',
    'SERVICE',
    'TRANSFER_IN',
    'TRANSFER_OUT',
    'ADJUSTMENT'
);


--
-- Name: treatmentplanstatus; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.treatmentplanstatus AS ENUM (
    'PENDING',
    'IN_PROGRESS',
    'COMPLETED',
    'CANCELLED'
);


--
-- Name: warehouseactiontype; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.warehouseactiontype AS ENUM (
    'IMPORT',
    'EXPORT',
    'TRANSFER',
    'ADJUSTMENT'
);


--
-- Name: warehousetype; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.warehousetype AS ENUM (
    'MAIN',
    'BRANCH'
);


--
-- Name: work_shift_category; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.work_shift_category AS ENUM (
    'NORMAL',
    'NIGHT'
);


--
-- Name: CAST (public.appointmentactiontype AS character varying); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (public.appointmentactiontype AS character varying) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (public.appointmentreasoncode AS character varying); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (public.appointmentreasoncode AS character varying) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (public.appointmentstatus AS character varying); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (public.appointmentstatus AS character varying) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (public.attachmenttypeenum AS character varying); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (public.attachmenttypeenum AS character varying) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (character varying AS public.appointmentactiontype); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (character varying AS public.appointmentactiontype) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (character varying AS public.appointmentreasoncode); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (character varying AS public.appointmentreasoncode) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (character varying AS public.appointmentstatus); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (character varying AS public.appointmentstatus) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (character varying AS public.attachmenttypeenum); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (character varying AS public.attachmenttypeenum) WITH INOUT AS IMPLICIT;


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: account_verification_tokens; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.account_verification_tokens (
    account_id integer NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    expires_at timestamp(6) without time zone NOT NULL,
    verified_at timestamp(6) without time zone,
    token_id character varying(50) NOT NULL,
    token character varying(100) NOT NULL
);


--
-- Name: accounts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.accounts (
    account_id integer NOT NULL,
    is_email_verified boolean,
    must_change_password boolean,
    created_at timestamp(6) without time zone,
    password_changed_at timestamp(6) without time zone,
    account_code character varying(20),
    role_id character varying(50) NOT NULL,
    username character varying(50) NOT NULL,
    email character varying(100) NOT NULL,
    password character varying(255) NOT NULL,
    status character varying(255),
    CONSTRAINT accounts_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'INACTIVE'::character varying, 'SUSPENDED'::character varying, 'LOCKED'::character varying, 'PENDING_VERIFICATION'::character varying])::text[])))
);


--
-- Name: accounts_account_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.accounts_account_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: accounts_account_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.accounts_account_id_seq OWNED BY public.accounts.account_id;


--
-- Name: appointment_audit_logs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.appointment_audit_logs (
    appointment_id integer NOT NULL,
    changed_by_employee_id integer,
    log_id integer NOT NULL,
    action_timestamp timestamp(6) without time zone,
    created_at timestamp(6) without time zone,
    new_start_time timestamp(6) without time zone,
    old_start_time timestamp(6) without time zone,
    new_value text,
    notes text,
    old_value text,
    action_type public.appointment_action_type NOT NULL,
    new_status public.appointment_status_enum,
    old_status public.appointment_status_enum,
    reason_code public.appointment_reason_code
);


--
-- Name: appointment_audit_logs_log_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.appointment_audit_logs_log_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: appointment_audit_logs_log_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.appointment_audit_logs_log_id_seq OWNED BY public.appointment_audit_logs.log_id;


--
-- Name: appointment_participants; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.appointment_participants (
    appointment_id integer NOT NULL,
    employee_id integer NOT NULL,
    participant_role character varying(255) NOT NULL,
    CONSTRAINT appointment_participants_participant_role_check CHECK (((participant_role)::text = ANY ((ARRAY['ASSISTANT'::character varying, 'SECONDARY_DOCTOR'::character varying, 'OBSERVER'::character varying])::text[])))
);


--
-- Name: appointment_plan_items; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.appointment_plan_items (
    appointment_id bigint NOT NULL,
    item_id bigint NOT NULL
);


--
-- Name: appointment_services; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.appointment_services (
    appointment_id integer NOT NULL,
    service_id integer NOT NULL
);


--
-- Name: appointments; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.appointments (
    appointment_id integer NOT NULL,
    created_by integer,
    employee_id integer NOT NULL,
    expected_duration_minutes integer NOT NULL,
    patient_id integer NOT NULL,
    reschedule_count integer NOT NULL,
    rescheduled_to_appointment_id integer,
    actual_end_time timestamp(6) without time zone,
    actual_start_time timestamp(6) without time zone,
    appointment_end_time timestamp(6) without time zone NOT NULL,
    appointment_start_time timestamp(6) without time zone NOT NULL,
    created_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    appointment_code character varying(20) NOT NULL,
    room_id character varying(50) NOT NULL,
    notes text,
    status character varying(255) NOT NULL
);


--
-- Name: appointments_appointment_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.appointments_appointment_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: appointments_appointment_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.appointments_appointment_id_seq OWNED BY public.appointments.appointment_id;


--
-- Name: base_roles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.base_roles (
    base_role_id integer NOT NULL,
    is_active boolean,
    created_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    base_role_name character varying(50) NOT NULL,
    description text
);


--
-- Name: base_roles_base_role_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.base_roles_base_role_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: base_roles_base_role_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.base_roles_base_role_id_seq OWNED BY public.base_roles.base_role_id;


--
-- Name: blacklisted_tokens; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.blacklisted_tokens (
    account_id integer,
    blacklisted_at timestamp(6) without time zone NOT NULL,
    expires_at timestamp(6) without time zone NOT NULL,
    reason character varying(50),
    token_hash character varying(512) NOT NULL
);


--
-- Name: chatbot_knowledge; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.chatbot_knowledge (
    is_active boolean,
    knowledge_id character varying(50) NOT NULL,
    keywords text NOT NULL,
    response text NOT NULL
);


--
-- Name: clinical_prescription_items; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.clinical_prescription_items (
    prescription_id integer NOT NULL,
    prescription_item_id integer NOT NULL,
    quantity integer NOT NULL,
    created_at timestamp(6) without time zone,
    item_master_id bigint,
    dosage_instructions text,
    item_name character varying(255) NOT NULL
);


--
-- Name: clinical_prescription_items_prescription_item_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.clinical_prescription_items_prescription_item_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: clinical_prescription_items_prescription_item_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.clinical_prescription_items_prescription_item_id_seq OWNED BY public.clinical_prescription_items.prescription_item_id;


--
-- Name: clinical_prescriptions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.clinical_prescriptions (
    clinical_record_id integer NOT NULL,
    prescription_id integer NOT NULL,
    created_at timestamp(6) without time zone,
    prescription_notes text
);


--
-- Name: clinical_prescriptions_prescription_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.clinical_prescriptions_prescription_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: clinical_prescriptions_prescription_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.clinical_prescriptions_prescription_id_seq OWNED BY public.clinical_prescriptions.prescription_id;


--
-- Name: clinical_record_attachments; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.clinical_record_attachments (
    attachment_id integer NOT NULL,
    clinical_record_id integer NOT NULL,
    uploaded_by integer,
    file_size bigint NOT NULL,
    uploaded_at timestamp(6) without time zone,
    mime_type character varying(100) NOT NULL,
    file_path character varying(500) NOT NULL,
    description text,
    file_name character varying(255) NOT NULL,
    attachment_type public.attachment_type_enum NOT NULL
);


--
-- Name: clinical_record_attachments_attachment_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.clinical_record_attachments_attachment_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: clinical_record_attachments_attachment_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.clinical_record_attachments_attachment_id_seq OWNED BY public.clinical_record_attachments.attachment_id;


--
-- Name: clinical_record_procedures; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.clinical_record_procedures (
    clinical_record_id integer NOT NULL,
    procedure_id integer NOT NULL,
    storage_transaction_id integer,
    created_at timestamp(6) without time zone,
    materials_deducted_at timestamp(6) without time zone,
    patient_plan_item_id bigint,
    service_id bigint,
    updated_at timestamp(6) without time zone,
    tooth_number character varying(10),
    materials_deducted_by character varying(100),
    notes text,
    procedure_description text NOT NULL
);


--
-- Name: clinical_record_procedures_procedure_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.clinical_record_procedures_procedure_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: clinical_record_procedures_procedure_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.clinical_record_procedures_procedure_id_seq OWNED BY public.clinical_record_procedures.procedure_id;


--
-- Name: clinical_records; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.clinical_records (
    appointment_id integer NOT NULL,
    clinical_record_id integer NOT NULL,
    follow_up_date date,
    created_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    chief_complaint text,
    diagnosis text,
    examination_findings text,
    treatment_notes text,
    vital_signs jsonb
);


--
-- Name: clinical_records_clinical_record_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.clinical_records_clinical_record_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: clinical_records_clinical_record_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.clinical_records_clinical_record_id_seq OWNED BY public.clinical_records.clinical_record_id;


--
-- Name: contact_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.contact_history (
    employee_id integer,
    created_at timestamp(6) without time zone,
    action character varying(20) NOT NULL,
    contact_id character varying(20) NOT NULL,
    history_id character varying(20) NOT NULL,
    content text NOT NULL,
    CONSTRAINT contact_history_action_check CHECK (((action)::text = ANY ((ARRAY['CALL'::character varying, 'MESSAGE'::character varying, 'NOTE'::character varying])::text[])))
);


--
-- Name: customer_contacts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.customer_contacts (
    assigned_to integer,
    converted_patient_id integer,
    created_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    phone character varying(15) NOT NULL,
    contact_id character varying(20) NOT NULL,
    source character varying(20),
    status character varying(20),
    email character varying(100),
    full_name character varying(100) NOT NULL,
    service_interested character varying(100),
    message text,
    notes text,
    CONSTRAINT customer_contacts_source_check CHECK (((source)::text = ANY ((ARRAY['WEBSITE'::character varying, 'FACEBOOK'::character varying, 'ZALO'::character varying, 'WALK_IN'::character varying, 'REFERRAL'::character varying])::text[]))),
    CONSTRAINT customer_contacts_status_check CHECK (((status)::text = ANY ((ARRAY['NEW'::character varying, 'CONTACTED'::character varying, 'APPOINTMENT_SET'::character varying, 'NOT_INTERESTED'::character varying, 'CONVERTED'::character varying])::text[])))
);


--
-- Name: dashboard_preferences; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.dashboard_preferences (
    auto_refresh boolean,
    id integer NOT NULL,
    refresh_interval integer,
    user_id integer NOT NULL,
    created_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    chart_type_preference character varying(255),
    default_date_range character varying(255),
    layout text,
    visible_widgets text
);


--
-- Name: dashboard_preferences_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.dashboard_preferences_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: dashboard_preferences_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.dashboard_preferences_id_seq OWNED BY public.dashboard_preferences.id;


--
-- Name: dashboard_saved_views; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.dashboard_saved_views (
    id integer NOT NULL,
    is_default boolean,
    is_public boolean,
    user_id integer NOT NULL,
    created_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    description character varying(255),
    filters text,
    view_name character varying(255) NOT NULL
);


--
-- Name: dashboard_saved_views_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.dashboard_saved_views_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: dashboard_saved_views_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.dashboard_saved_views_id_seq OWNED BY public.dashboard_saved_views.id;


--
-- Name: employee_leave_balances; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.employee_leave_balances (
    cycle_year integer NOT NULL,
    days_taken double precision NOT NULL,
    employee_id integer NOT NULL,
    total_days_allowed double precision NOT NULL,
    balance_id bigint NOT NULL,
    updated_at timestamp(6) without time zone,
    time_off_type_id character varying(50) NOT NULL,
    notes text
);


--
-- Name: employee_leave_balances_balance_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.employee_leave_balances_balance_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: employee_leave_balances_balance_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.employee_leave_balances_balance_id_seq OWNED BY public.employee_leave_balances.balance_id;


--
-- Name: employee_shift_registrations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.employee_shift_registrations (
    effective_from date NOT NULL,
    effective_to date,
    employee_id integer NOT NULL,
    is_active boolean DEFAULT true,
    part_time_slot_id bigint NOT NULL,
    registration_id character varying(20) NOT NULL
);


--
-- Name: employee_shifts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.employee_shifts (
    created_by integer,
    employee_id integer NOT NULL,
    is_overtime boolean NOT NULL,
    work_date date NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    source_registration_id bigint,
    updated_at timestamp(6) without time zone,
    employee_shift_id character varying(20) NOT NULL,
    source character varying(20) NOT NULL,
    source_off_request_id character varying(20),
    source_ot_request_id character varying(20),
    status character varying(20) NOT NULL,
    work_shift_id character varying(30) NOT NULL,
    notes text,
    CONSTRAINT employee_shifts_source_check CHECK (((source)::text = ANY ((ARRAY['BATCH_JOB'::character varying, 'REGISTRATION_JOB'::character varying, 'OT_APPROVAL'::character varying, 'MANUAL_ENTRY'::character varying])::text[]))),
    CONSTRAINT employee_shifts_source_check1 CHECK (((source)::text = ANY ((ARRAY['BATCH_JOB'::character varying, 'REGISTRATION_JOB'::character varying, 'OT_APPROVAL'::character varying, 'MANUAL_ENTRY'::character varying])::text[]))),
    CONSTRAINT employee_shifts_status_check CHECK (((status)::text = ANY ((ARRAY['SCHEDULED'::character varying, 'ON_LEAVE'::character varying, 'COMPLETED'::character varying, 'ABSENT'::character varying, 'CANCELLED'::character varying])::text[])))
);


--
-- Name: employee_specializations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.employee_specializations (
    employee_id integer NOT NULL,
    specialization_id integer NOT NULL
);


--
-- Name: employees; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.employees (
    account_id integer NOT NULL,
    date_of_birth date,
    employee_id integer NOT NULL,
    is_active boolean,
    created_at timestamp(6) without time zone,
    phone character varying(15),
    employee_code character varying(20),
    first_name character varying(50) NOT NULL,
    last_name character varying(50) NOT NULL,
    address text,
    employment_type character varying(255),
    CONSTRAINT employees_employment_type_check CHECK (((employment_type)::text = ANY ((ARRAY['FULL_TIME'::character varying, 'PART_TIME_FIXED'::character varying, 'PART_TIME_FLEX'::character varying, 'PROBATION'::character varying])::text[])))
);


--
-- Name: employees_employee_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.employees_employee_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: employees_employee_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.employees_employee_id_seq OWNED BY public.employees.employee_id;


--
-- Name: fixed_registration_days; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.fixed_registration_days (
    registration_id integer NOT NULL,
    day_of_week character varying(10) NOT NULL
);


--
-- Name: fixed_shift_registrations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.fixed_shift_registrations (
    effective_from date NOT NULL,
    effective_to date,
    employee_id integer NOT NULL,
    is_active boolean NOT NULL,
    registration_id integer NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone,
    work_shift_id character varying(30) NOT NULL
);


--
-- Name: fixed_shift_registrations_registration_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.fixed_shift_registrations_registration_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: fixed_shift_registrations_registration_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.fixed_shift_registrations_registration_id_seq OWNED BY public.fixed_shift_registrations.registration_id;


--
-- Name: holiday_dates; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.holiday_dates (
    holiday_date date NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone,
    definition_id character varying(20) NOT NULL,
    description character varying(500)
);


--
-- Name: holiday_definitions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.holiday_definitions (
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone,
    definition_id character varying(20) NOT NULL,
    holiday_type character varying(20) NOT NULL,
    holiday_name character varying(100) NOT NULL,
    description character varying(500),
    CONSTRAINT holiday_definitions_holiday_type_check CHECK (((holiday_type)::text = ANY ((ARRAY['NATIONAL'::character varying, 'COMPANY'::character varying])::text[])))
);


--
-- Name: invoice_items; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.invoice_items (
    invoice_id integer NOT NULL,
    item_id integer NOT NULL,
    quantity integer NOT NULL,
    service_id integer NOT NULL,
    subtotal numeric(15,2) NOT NULL,
    unit_price numeric(15,2) NOT NULL,
    service_code character varying(50),
    notes text,
    service_name character varying(255) NOT NULL
);


--
-- Name: invoice_items_item_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.invoice_items_item_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: invoice_items_item_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.invoice_items_item_id_seq OWNED BY public.invoice_items.item_id;


--
-- Name: invoices; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.invoices (
    appointment_id integer,
    created_by integer NOT NULL,
    installment_number integer,
    invoice_id integer NOT NULL,
    paid_amount numeric(15,2) NOT NULL,
    patient_id integer NOT NULL,
    phase_number integer,
    remaining_debt numeric(15,2) NOT NULL,
    total_amount numeric(15,2) NOT NULL,
    treatment_plan_id integer,
    created_at timestamp(6) without time zone NOT NULL,
    due_date timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    invoice_type character varying(20) NOT NULL,
    payment_status character varying(20) NOT NULL,
    invoice_code character varying(30) NOT NULL,
    notes text,
    CONSTRAINT invoices_invoice_type_check CHECK (((invoice_type)::text = ANY ((ARRAY['APPOINTMENT'::character varying, 'TREATMENT_PLAN'::character varying, 'SUPPLEMENTAL'::character varying])::text[]))),
    CONSTRAINT invoices_payment_status_check CHECK (((payment_status)::text = ANY ((ARRAY['PENDING_PAYMENT'::character varying, 'PARTIAL_PAID'::character varying, 'PAID'::character varying, 'CANCELLED'::character varying])::text[])))
);


--
-- Name: invoices_invoice_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.invoices_invoice_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: invoices_invoice_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.invoices_invoice_id_seq OWNED BY public.invoices.invoice_id;


--
-- Name: item_batches; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.item_batches (
    expiry_date date NOT NULL,
    initial_quantity integer,
    is_unpacked boolean,
    quantity_on_hand integer NOT NULL,
    batch_id bigint NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    imported_at timestamp(6) without time zone NOT NULL,
    item_master_id bigint NOT NULL,
    parent_batch_id bigint,
    supplier_id bigint,
    unpacked_at timestamp(6) without time zone,
    unpacked_by_transaction_id bigint,
    updated_at timestamp(6) without time zone,
    bin_location character varying(50),
    lot_number character varying(100) NOT NULL
);


--
-- Name: item_batches_batch_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.item_batches_batch_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: item_batches_batch_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.item_batches_batch_id_seq OWNED BY public.item_batches.batch_id;


--
-- Name: item_categories; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.item_categories (
    is_active boolean NOT NULL,
    category_id bigint NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    parent_category_id bigint,
    updated_at timestamp(6) without time zone,
    category_code character varying(50) NOT NULL,
    category_name character varying(255) NOT NULL,
    description text
);


--
-- Name: item_categories_category_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.item_categories_category_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: item_categories_category_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.item_categories_category_id_seq OWNED BY public.item_categories.category_id;


--
-- Name: item_masters; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.item_masters (
    cached_total_quantity integer,
    current_market_price numeric(15,2),
    default_shelf_life_days integer,
    is_active boolean NOT NULL,
    is_prescription_required boolean NOT NULL,
    is_tool boolean NOT NULL,
    max_stock_level integer NOT NULL,
    min_stock_level integer NOT NULL,
    cached_last_import_date timestamp(6) without time zone,
    cached_last_updated timestamp(6) without time zone,
    category_id bigint,
    created_at timestamp(6) without time zone NOT NULL,
    item_master_id bigint NOT NULL,
    updated_at timestamp(6) without time zone,
    item_code character varying(50) NOT NULL,
    unit_of_measure character varying(50),
    description text,
    item_name character varying(255) NOT NULL,
    warehouse_type character varying(255) NOT NULL,
    CONSTRAINT item_masters_warehouse_type_check CHECK (((warehouse_type)::text = ANY ((ARRAY['COLD'::character varying, 'NORMAL'::character varying])::text[])))
);


--
-- Name: item_masters_item_master_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.item_masters_item_master_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: item_masters_item_master_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.item_masters_item_master_id_seq OWNED BY public.item_masters.item_master_id;


--
-- Name: item_price_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.item_price_history (
    effective_date date NOT NULL,
    new_import_price numeric(15,2),
    old_import_price numeric(15,2),
    history_id bigint NOT NULL,
    item_master_id bigint NOT NULL,
    supplier_id bigint NOT NULL,
    notes text
);


--
-- Name: item_price_history_history_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.item_price_history_history_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: item_price_history_history_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.item_price_history_history_id_seq OWNED BY public.item_price_history.history_id;


--
-- Name: item_units; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.item_units (
    conversion_rate integer NOT NULL,
    display_order integer,
    is_active boolean NOT NULL,
    is_base_unit boolean NOT NULL,
    is_default_export_unit boolean NOT NULL,
    is_default_import_unit boolean NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    item_master_id bigint NOT NULL,
    unit_id bigint NOT NULL,
    updated_at timestamp(6) without time zone,
    unit_name character varying(50) NOT NULL
);


--
-- Name: item_units_unit_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.item_units_unit_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: item_units_unit_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.item_units_unit_id_seq OWNED BY public.item_units.unit_id;


--
-- Name: leave_balance_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.leave_balance_history (
    change_amount double precision NOT NULL,
    changed_by integer NOT NULL,
    balance_id bigint NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    history_id bigint NOT NULL,
    reason character varying(50) NOT NULL,
    notes text,
    CONSTRAINT leave_balance_history_reason_check CHECK (((reason)::text = ANY ((ARRAY['ANNUAL_RESET'::character varying, 'APPROVED_REQUEST'::character varying, 'REJECTED_REQUEST'::character varying, 'CANCELLED_REQUEST'::character varying, 'MANUAL_ADJUSTMENT'::character varying])::text[])))
);


--
-- Name: leave_balance_history_history_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.leave_balance_history_history_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: leave_balance_history_history_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.leave_balance_history_history_id_seq OWNED BY public.leave_balance_history.history_id;


--
-- Name: notifications; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.notifications (
    is_read boolean NOT NULL,
    user_id integer NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    notification_id bigint NOT NULL,
    read_at timestamp(6) without time zone,
    related_entity_type character varying(50),
    type character varying(50) NOT NULL,
    message text,
    related_entity_id character varying(255),
    title character varying(255) NOT NULL,
    CONSTRAINT notifications_related_entity_type_check CHECK (((related_entity_type)::text = ANY ((ARRAY['APPOINTMENT'::character varying, 'TREATMENT_PLAN'::character varying, 'PAYMENT'::character varying, 'SYSTEM'::character varying, 'TIME_OFF_REQUEST'::character varying, 'OVERTIME_REQUEST'::character varying, 'PART_TIME_REGISTRATION'::character varying])::text[]))),
    CONSTRAINT notifications_type_check CHECK (((type)::text = ANY ((ARRAY['APPOINTMENT_CREATED'::character varying, 'APPOINTMENT_UPDATED'::character varying, 'APPOINTMENT_CANCELLED'::character varying, 'APPOINTMENT_REMINDER'::character varying, 'APPOINTMENT_COMPLETED'::character varying, 'TREATMENT_PLAN_APPROVED'::character varying, 'TREATMENT_PLAN_UPDATED'::character varying, 'PAYMENT_RECEIVED'::character varying, 'SYSTEM_ANNOUNCEMENT'::character varying, 'REQUEST_TIME_OFF_PENDING'::character varying, 'REQUEST_OVERTIME_PENDING'::character varying, 'REQUEST_PART_TIME_PENDING'::character varying])::text[])))
);


--
-- Name: notifications_notification_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.notifications_notification_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: notifications_notification_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.notifications_notification_id_seq OWNED BY public.notifications.notification_id;


--
-- Name: overtime_requests; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.overtime_requests (
    approved_by integer,
    employee_id integer NOT NULL,
    requested_by integer NOT NULL,
    work_date date NOT NULL,
    approved_at timestamp(6) without time zone,
    created_at timestamp(6) without time zone NOT NULL,
    request_id character varying(12) NOT NULL,
    status character varying(20) NOT NULL,
    work_shift_id character varying(30) NOT NULL,
    cancellation_reason text,
    reason text NOT NULL,
    rejected_reason text,
    CONSTRAINT overtime_requests_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying, 'CANCELLED'::character varying])::text[])))
);


--
-- Name: part_time_registration_dates; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.part_time_registration_dates (
    registered_date date,
    registration_id integer NOT NULL
);


--
-- Name: part_time_registrations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.part_time_registrations (
    effective_from date NOT NULL,
    effective_to date NOT NULL,
    employee_id integer NOT NULL,
    is_active boolean NOT NULL,
    processed_by integer,
    registration_id integer NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    part_time_slot_id bigint NOT NULL,
    processed_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    version bigint NOT NULL,
    status character varying(20) NOT NULL,
    reason text,
    CONSTRAINT part_time_registrations_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying, 'CANCELLED'::character varying])::text[])))
);


--
-- Name: part_time_registrations_registration_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.part_time_registrations_registration_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: part_time_registrations_registration_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.part_time_registrations_registration_id_seq OWNED BY public.part_time_registrations.registration_id;


--
-- Name: part_time_slots; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.part_time_slots (
    effective_from date NOT NULL,
    effective_to date NOT NULL,
    is_active boolean NOT NULL,
    quota integer NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    slot_id bigint NOT NULL,
    updated_at timestamp(6) without time zone,
    work_shift_id character varying(30) NOT NULL,
    day_of_week character varying(255) NOT NULL
);


--
-- Name: part_time_slots_slot_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.part_time_slots_slot_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: part_time_slots_slot_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.part_time_slots_slot_id_seq OWNED BY public.part_time_slots.slot_id;


--
-- Name: password_reset_tokens; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.password_reset_tokens (
    account_id integer NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    expires_at timestamp(6) without time zone NOT NULL,
    used_at timestamp(6) without time zone,
    token_id character varying(50) NOT NULL,
    token character varying(100) NOT NULL
);


--
-- Name: patient_image_comments; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.patient_image_comments (
    created_by integer NOT NULL,
    is_deleted boolean NOT NULL,
    comment_id bigint NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    image_id bigint NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    comment_text text NOT NULL
);


--
-- Name: patient_image_comments_comment_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.patient_image_comments_comment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: patient_image_comments_comment_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.patient_image_comments_comment_id_seq OWNED BY public.patient_image_comments.comment_id;


--
-- Name: patient_images; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.patient_images (
    captured_date date,
    clinical_record_id integer,
    patient_id integer NOT NULL,
    uploaded_by integer,
    created_at timestamp(6) without time zone NOT NULL,
    image_id bigint NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    image_type character varying(50) NOT NULL,
    cloudinary_public_id character varying(255) NOT NULL,
    description text,
    image_url text NOT NULL,
    CONSTRAINT patient_images_image_type_check CHECK (((image_type)::text = ANY ((ARRAY['XRAY'::character varying, 'PHOTO'::character varying, 'BEFORE_TREATMENT'::character varying, 'AFTER_TREATMENT'::character varying, 'SCAN'::character varying, 'OTHER'::character varying])::text[])))
);


--
-- Name: patient_images_image_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.patient_images_image_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: patient_images_image_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.patient_images_image_id_seq OWNED BY public.patient_images.image_id;


--
-- Name: patient_plan_items; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.patient_plan_items (
    assigned_doctor_id integer,
    estimated_time_minutes integer,
    price numeric(10,2),
    price_updated_by integer,
    sequence_number integer NOT NULL,
    service_id integer NOT NULL,
    completed_at timestamp(6) without time zone,
    created_at timestamp(6) without time zone NOT NULL,
    item_id bigint NOT NULL,
    phase_id bigint NOT NULL,
    price_updated_at timestamp(6) without time zone,
    status character varying(30) NOT NULL,
    price_update_reason character varying(500),
    item_name character varying(255) NOT NULL
);


--
-- Name: patient_plan_items_item_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.patient_plan_items_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: patient_plan_items_item_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.patient_plan_items_item_id_seq OWNED BY public.patient_plan_items.item_id;


--
-- Name: patient_plan_phases; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.patient_plan_phases (
    completion_date date,
    estimated_duration_days integer,
    phase_number integer NOT NULL,
    start_date date,
    created_at timestamp(6) without time zone NOT NULL,
    patient_phase_id bigint NOT NULL,
    plan_id bigint NOT NULL,
    status character varying(20),
    phase_name character varying(255) NOT NULL,
    CONSTRAINT patient_plan_phases_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'IN_PROGRESS'::character varying, 'COMPLETED'::character varying])::text[])))
);


--
-- Name: patient_plan_phases_patient_phase_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.patient_plan_phases_patient_phase_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: patient_plan_phases_patient_phase_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.patient_plan_phases_patient_phase_id_seq OWNED BY public.patient_plan_phases.patient_phase_id;


--
-- Name: patient_tooth_status; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.patient_tooth_status (
    patient_id integer NOT NULL,
    tooth_status_id integer NOT NULL,
    recorded_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    tooth_number character varying(10) NOT NULL,
    notes text,
    status character varying(255) NOT NULL,
    CONSTRAINT patient_tooth_status_status_check CHECK (((status)::text = ANY ((ARRAY['HEALTHY'::character varying, 'CARIES_MILD'::character varying, 'CARIES_MODERATE'::character varying, 'CARIES_SEVERE'::character varying, 'FILLED'::character varying, 'CROWN'::character varying, 'MISSING'::character varying, 'IMPLANT'::character varying, 'ROOT_CANAL'::character varying, 'FRACTURED'::character varying, 'IMPACTED'::character varying])::text[])))
);


--
-- Name: patient_tooth_status_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.patient_tooth_status_history (
    changed_by integer NOT NULL,
    history_id integer NOT NULL,
    patient_id integer NOT NULL,
    changed_at timestamp(6) without time zone,
    tooth_number character varying(10) NOT NULL,
    new_status character varying(255) NOT NULL,
    old_status character varying(255),
    reason text,
    CONSTRAINT patient_tooth_status_history_new_status_check CHECK (((new_status)::text = ANY ((ARRAY['HEALTHY'::character varying, 'CARIES_MILD'::character varying, 'CARIES_MODERATE'::character varying, 'CARIES_SEVERE'::character varying, 'FILLED'::character varying, 'CROWN'::character varying, 'MISSING'::character varying, 'IMPLANT'::character varying, 'ROOT_CANAL'::character varying, 'FRACTURED'::character varying, 'IMPACTED'::character varying])::text[]))),
    CONSTRAINT patient_tooth_status_history_old_status_check CHECK (((old_status)::text = ANY ((ARRAY['HEALTHY'::character varying, 'CARIES_MILD'::character varying, 'CARIES_MODERATE'::character varying, 'CARIES_SEVERE'::character varying, 'FILLED'::character varying, 'CROWN'::character varying, 'MISSING'::character varying, 'IMPLANT'::character varying, 'ROOT_CANAL'::character varying, 'FRACTURED'::character varying, 'IMPACTED'::character varying])::text[])))
);


--
-- Name: patient_tooth_status_history_history_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.patient_tooth_status_history_history_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: patient_tooth_status_history_history_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.patient_tooth_status_history_history_id_seq OWNED BY public.patient_tooth_status_history.history_id;


--
-- Name: patient_tooth_status_tooth_status_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.patient_tooth_status_tooth_status_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: patient_tooth_status_tooth_status_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.patient_tooth_status_tooth_status_id_seq OWNED BY public.patient_tooth_status.tooth_status_id;


--
-- Name: patient_treatment_plans; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.patient_treatment_plans (
    approved_by integer,
    created_by integer,
    discount_amount numeric(12,2),
    expected_end_date date,
    final_cost numeric(12,2),
    installment_count integer,
    installment_interval_days integer,
    patient_id integer NOT NULL,
    specialization_id integer,
    start_date date,
    total_price numeric(12,2),
    approved_at timestamp(6) without time zone,
    created_at timestamp(6) without time zone NOT NULL,
    patient_consent_date timestamp(6) without time zone,
    plan_id bigint NOT NULL,
    template_id bigint,
    approval_status character varying(20) NOT NULL,
    payment_type character varying(20),
    status character varying(20),
    plan_code character varying(50) NOT NULL,
    plan_name character varying(255) NOT NULL,
    rejection_reason text,
    CONSTRAINT patient_treatment_plans_approval_status_check CHECK (((approval_status)::text = ANY ((ARRAY['DRAFT'::character varying, 'PENDING_REVIEW'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying])::text[]))),
    CONSTRAINT patient_treatment_plans_payment_type_check CHECK (((payment_type)::text = ANY ((ARRAY['FULL'::character varying, 'PHASED'::character varying, 'INSTALLMENT'::character varying])::text[]))),
    CONSTRAINT patient_treatment_plans_status_check CHECK (((status)::text = ANY ((ARRAY['IN_PROGRESS'::character varying, 'COMPLETED'::character varying, 'CANCELLED'::character varying])::text[])))
);


--
-- Name: patient_treatment_plans_plan_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.patient_treatment_plans_plan_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: patient_treatment_plans_plan_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.patient_treatment_plans_plan_id_seq OWNED BY public.patient_treatment_plans.plan_id;


--
-- Name: patient_unban_audit_logs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.patient_unban_audit_logs (
    patient_id integer NOT NULL,
    previous_no_show_count integer NOT NULL,
    audit_id bigint NOT NULL,
    "timestamp" timestamp(6) without time zone NOT NULL,
    performed_by_role character varying(50) NOT NULL,
    performed_by character varying(100) NOT NULL,
    reason text NOT NULL
);


--
-- Name: patient_unban_audit_logs_audit_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.patient_unban_audit_logs_audit_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: patient_unban_audit_logs_audit_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.patient_unban_audit_logs_audit_id_seq OWNED BY public.patient_unban_audit_logs.audit_id;


--
-- Name: patients; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.patients (
    account_id integer,
    consecutive_no_shows integer NOT NULL,
    date_of_birth date,
    is_active boolean,
    is_booking_blocked boolean NOT NULL,
    patient_id integer NOT NULL,
    blocked_at timestamp(6) without time zone,
    created_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    gender character varying(10),
    emergency_contact_phone character varying(15),
    guardian_phone character varying(15),
    phone character varying(15),
    guardian_citizen_id character varying(20),
    patient_code character varying(20),
    booking_block_reason character varying(50),
    first_name character varying(50) NOT NULL,
    guardian_relationship character varying(50),
    last_name character varying(50) NOT NULL,
    blocked_by character varying(100),
    email character varying(100),
    emergency_contact_name character varying(100),
    emergency_contact_relationship character varying(100),
    guardian_name character varying(100),
    address text,
    allergies text,
    booking_block_notes text,
    medical_history text,
    CONSTRAINT patients_booking_block_reason_check CHECK (((booking_block_reason)::text = ANY ((ARRAY['EXCESSIVE_NO_SHOWS'::character varying, 'PAYMENT_ISSUES'::character varying, 'STAFF_ABUSE'::character varying, 'POLICY_VIOLATION'::character varying, 'OTHER_SERIOUS'::character varying])::text[]))),
    CONSTRAINT patients_gender_check CHECK (((gender)::text = ANY ((ARRAY['MALE'::character varying, 'FEMALE'::character varying, 'OTHER'::character varying])::text[])))
);


--
-- Name: patients_patient_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.patients_patient_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: patients_patient_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.patients_patient_id_seq OWNED BY public.patients.patient_id;


--
-- Name: payment_transactions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.payment_transactions (
    amount numeric(15,2) NOT NULL,
    payment_id integer NOT NULL,
    transaction_id integer NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone,
    status character varying(20) NOT NULL,
    payment_link_id character varying(100),
    callback_data text,
    error_message text,
    CONSTRAINT payment_transactions_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'SUCCESS'::character varying, 'FAILED'::character varying, 'CANCELLED'::character varying])::text[])))
);


--
-- Name: payment_transactions_transaction_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.payment_transactions_transaction_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: payment_transactions_transaction_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.payment_transactions_transaction_id_seq OWNED BY public.payment_transactions.transaction_id;


--
-- Name: payments; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.payments (
    amount numeric(15,2) NOT NULL,
    created_by integer NOT NULL,
    invoice_id integer NOT NULL,
    payment_id integer NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    payment_date timestamp(6) without time zone NOT NULL,
    payment_method character varying(20) NOT NULL,
    payment_code character varying(30) NOT NULL,
    reference_number character varying(100),
    notes text,
    CONSTRAINT payments_payment_method_check CHECK (((payment_method)::text = 'SEPAY'::text))
);


--
-- Name: payments_payment_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.payments_payment_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: payments_payment_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.payments_payment_id_seq OWNED BY public.payments.payment_id;


--
-- Name: permissions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.permissions (
    display_order integer,
    is_active boolean,
    created_at timestamp(6) without time zone,
    module character varying(20) NOT NULL,
    parent_permission_id character varying(50),
    permission_id character varying(50) NOT NULL,
    permission_name character varying(100) NOT NULL,
    description text
);


--
-- Name: plan_audit_logs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.plan_audit_logs (
    performed_by integer NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    log_id bigint NOT NULL,
    plan_id bigint NOT NULL,
    new_approval_status character varying(20),
    old_approval_status character varying(20),
    action_type character varying(50) NOT NULL,
    notes text,
    CONSTRAINT plan_audit_logs_new_approval_status_check CHECK (((new_approval_status)::text = ANY ((ARRAY['DRAFT'::character varying, 'PENDING_REVIEW'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying])::text[]))),
    CONSTRAINT plan_audit_logs_old_approval_status_check CHECK (((old_approval_status)::text = ANY ((ARRAY['DRAFT'::character varying, 'PENDING_REVIEW'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying])::text[])))
);


--
-- Name: plan_audit_logs_log_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.plan_audit_logs_log_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: plan_audit_logs_log_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.plan_audit_logs_log_id_seq OWNED BY public.plan_audit_logs.log_id;


--
-- Name: procedure_material_usage; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.procedure_material_usage (
    actual_quantity numeric(10,2) NOT NULL,
    planned_quantity numeric(10,2) NOT NULL,
    procedure_id integer NOT NULL,
    quantity numeric(10,2) NOT NULL,
    item_master_id bigint NOT NULL,
    recorded_at timestamp(6) without time zone,
    unit_id bigint NOT NULL,
    usage_id bigint NOT NULL,
    recorded_by character varying(100),
    variance_reason character varying(500),
    notes text,
    variance_quantity numeric(10,2) GENERATED ALWAYS AS ((actual_quantity - quantity)) STORED
);


--
-- Name: procedure_material_usage_usage_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.procedure_material_usage_usage_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: procedure_material_usage_usage_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.procedure_material_usage_usage_id_seq OWNED BY public.procedure_material_usage.usage_id;


--
-- Name: refresh_tokens; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.refresh_tokens (
    account_id integer NOT NULL,
    is_active boolean NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    expires_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone,
    id character varying(36) NOT NULL,
    token_hash character varying(512) NOT NULL
);


--
-- Name: role_permissions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.role_permissions (
    permission_id character varying(50) NOT NULL,
    role_id character varying(50) NOT NULL
);


--
-- Name: roles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.roles (
    base_role_id integer NOT NULL,
    is_active boolean,
    requires_specialization boolean,
    created_at timestamp(6) without time zone,
    role_id character varying(50) NOT NULL,
    role_name character varying(50) NOT NULL,
    description text
);


--
-- Name: room_services; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.room_services (
    service_id integer NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    room_id character varying(255) NOT NULL
);


--
-- Name: rooms; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.rooms (
    is_active boolean,
    created_at timestamp(6) without time zone,
    room_code character varying(20) NOT NULL,
    room_id character varying(50) NOT NULL,
    room_type character varying(50),
    room_name character varying(100) NOT NULL
);


--
-- Name: service_categories; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.service_categories (
    display_order integer NOT NULL,
    is_active boolean NOT NULL,
    category_id bigint NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone,
    category_code character varying(50) NOT NULL,
    category_name character varying(255) NOT NULL,
    description text
);


--
-- Name: service_categories_category_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.service_categories_category_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: service_categories_category_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.service_categories_category_id_seq OWNED BY public.service_categories.category_id;


--
-- Name: service_consumables; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.service_consumables (
    quantity_per_service numeric(10,2) NOT NULL,
    item_master_id bigint NOT NULL,
    link_id bigint NOT NULL,
    service_id bigint NOT NULL,
    unit_id bigint NOT NULL,
    notes text
);


--
-- Name: service_consumables_link_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.service_consumables_link_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: service_consumables_link_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.service_consumables_link_id_seq OWNED BY public.service_consumables.link_id;


--
-- Name: service_dependencies; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.service_dependencies (
    min_days_apart integer,
    created_at timestamp(6) without time zone NOT NULL,
    dependency_id bigint NOT NULL,
    dependent_service_id bigint NOT NULL,
    service_id bigint NOT NULL,
    rule_type character varying(30) NOT NULL,
    receptionist_note text,
    CONSTRAINT service_dependencies_rule_type_check CHECK (((rule_type)::text = ANY ((ARRAY['REQUIRES_PREREQUISITE'::character varying, 'REQUIRES_MIN_DAYS'::character varying, 'EXCLUDES_SAME_DAY'::character varying, 'BUNDLES_WITH'::character varying])::text[])))
);


--
-- Name: service_dependencies_dependency_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.service_dependencies_dependency_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: service_dependencies_dependency_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.service_dependencies_dependency_id_seq OWNED BY public.service_dependencies.dependency_id;


--
-- Name: services; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.services (
    default_buffer_minutes integer NOT NULL,
    default_duration_minutes integer NOT NULL,
    display_order integer NOT NULL,
    is_active boolean NOT NULL,
    max_appointments_per_day integer,
    minimum_preparation_days integer,
    price numeric(15,2) NOT NULL,
    recovery_days integer,
    service_id integer NOT NULL,
    spacing_days integer,
    specialization_id integer,
    category_id bigint,
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone,
    service_code character varying(50) NOT NULL,
    description text,
    service_name character varying(255) NOT NULL,
    CONSTRAINT services_default_buffer_minutes_check CHECK ((default_buffer_minutes >= 0)),
    CONSTRAINT services_default_duration_minutes_check CHECK ((default_duration_minutes >= 1)),
    CONSTRAINT services_price_check CHECK ((price >= (0)::numeric))
);


--
-- Name: services_service_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.services_service_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: services_service_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.services_service_id_seq OWNED BY public.services.service_id;


--
-- Name: shift_renewal_requests; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.shift_renewal_requests (
    employee_id integer NOT NULL,
    expiring_registration_id integer NOT NULL,
    confirmed_at timestamp(6) without time zone,
    created_at timestamp(6) without time zone NOT NULL,
    expires_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone,
    renewal_id character varying(20) NOT NULL,
    status character varying(20) NOT NULL,
    decline_reason text,
    CONSTRAINT shift_renewal_requests_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING_ACTION'::character varying, 'CONFIRMED'::character varying, 'FINALIZED'::character varying, 'DECLINED'::character varying, 'EXPIRED'::character varying])::text[])))
);


--
-- Name: specializations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.specializations (
    is_active boolean,
    specialization_id integer NOT NULL,
    created_at timestamp(6) without time zone,
    specialization_code character varying(20) NOT NULL,
    specialization_name character varying(100) NOT NULL,
    description text
);


--
-- Name: storage_transaction_items; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.storage_transaction_items (
    price numeric(15,2),
    quantity_change integer NOT NULL,
    total_line_value numeric(15,2),
    batch_id bigint NOT NULL,
    transaction_id bigint NOT NULL,
    transaction_item_id bigint NOT NULL,
    unit_id bigint,
    item_code character varying(50),
    notes text
);


--
-- Name: storage_transaction_items_transaction_item_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.storage_transaction_items_transaction_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: storage_transaction_items_transaction_item_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.storage_transaction_items_transaction_item_id_seq OWNED BY public.storage_transaction_items.transaction_item_id;


--
-- Name: storage_transactions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.storage_transactions (
    approved_by_id integer,
    cancelled_by_id integer,
    created_by integer,
    due_date date,
    expected_delivery_date date,
    paid_amount numeric(15,2),
    rejected_by_id integer,
    related_appointment_id integer,
    remaining_debt numeric(15,2),
    total_value numeric(15,2),
    approved_at timestamp(6) without time zone,
    cancelled_at timestamp(6) without time zone,
    created_at timestamp(6) without time zone NOT NULL,
    rejected_at timestamp(6) without time zone,
    supplier_id bigint,
    transaction_date timestamp(6) without time zone NOT NULL,
    transaction_id bigint NOT NULL,
    approval_status character varying(20),
    export_type character varying(20),
    payment_status character varying(20),
    status character varying(20),
    transaction_code character varying(50) NOT NULL,
    invoice_number character varying(100),
    reference_code character varying(100),
    department_name character varying(200),
    requested_by character varying(200),
    cancellation_reason text,
    notes text,
    rejection_reason text,
    transaction_type character varying(255) NOT NULL,
    CONSTRAINT storage_transactions_approval_status_check CHECK (((approval_status)::text = ANY ((ARRAY['DRAFT'::character varying, 'PENDING_APPROVAL'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying, 'COMPLETED'::character varying, 'CANCELLED'::character varying])::text[]))),
    CONSTRAINT storage_transactions_payment_status_check CHECK (((payment_status)::text = ANY ((ARRAY['UNPAID'::character varying, 'PARTIAL'::character varying, 'PAID'::character varying])::text[]))),
    CONSTRAINT storage_transactions_transaction_type_check CHECK (((transaction_type)::text = ANY ((ARRAY['IMPORT'::character varying, 'EXPORT'::character varying])::text[])))
);


--
-- Name: storage_transactions_transaction_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.storage_transactions_transaction_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: storage_transactions_transaction_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.storage_transactions_transaction_id_seq OWNED BY public.storage_transactions.transaction_id;


--
-- Name: supplier_items; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.supplier_items (
    is_preferred boolean NOT NULL,
    item_master_id bigint NOT NULL,
    last_purchase_date timestamp(6) without time zone,
    supplier_id bigint NOT NULL,
    supplier_item_id bigint NOT NULL,
    supplier_item_code character varying(100)
);


--
-- Name: supplier_items_supplier_item_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.supplier_items_supplier_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: supplier_items_supplier_item_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.supplier_items_supplier_item_id_seq OWNED BY public.supplier_items.supplier_item_id;


--
-- Name: suppliers; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.suppliers (
    is_active boolean NOT NULL,
    is_blacklisted boolean,
    last_order_date date,
    rating_score numeric(3,1),
    total_orders integer,
    created_at timestamp(6) without time zone NOT NULL,
    supplier_id bigint NOT NULL,
    updated_at timestamp(6) without time zone,
    phone_number character varying(20),
    supplier_code character varying(50) NOT NULL,
    email character varying(100),
    address text,
    contact_person character varying(255),
    notes text,
    supplier_name character varying(255) NOT NULL,
    tier_level character varying(255) NOT NULL,
    CONSTRAINT suppliers_tier_level_check CHECK (((tier_level)::text = ANY ((ARRAY['TIER_1'::character varying, 'TIER_2'::character varying, 'TIER_3'::character varying])::text[])))
);


--
-- Name: suppliers_supplier_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.suppliers_supplier_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: suppliers_supplier_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.suppliers_supplier_id_seq OWNED BY public.suppliers.supplier_id;


--
-- Name: template_phase_services; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.template_phase_services (
    estimated_time_minutes integer,
    quantity integer NOT NULL,
    sequence_number integer NOT NULL,
    created_at timestamp(6) without time zone,
    phase_id bigint NOT NULL,
    phase_service_id bigint NOT NULL,
    service_id bigint NOT NULL,
    updated_at timestamp(6) without time zone
);


--
-- Name: template_phase_services_phase_service_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.template_phase_services_phase_service_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: template_phase_services_phase_service_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.template_phase_services_phase_service_id_seq OWNED BY public.template_phase_services.phase_service_id;


--
-- Name: template_phases; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.template_phases (
    estimated_duration_days integer,
    phase_number integer NOT NULL,
    created_at timestamp(6) without time zone,
    phase_id bigint NOT NULL,
    template_id bigint NOT NULL,
    updated_at timestamp(6) without time zone,
    phase_name character varying(255) NOT NULL
);


--
-- Name: template_phases_phase_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.template_phases_phase_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: template_phases_phase_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.template_phases_phase_id_seq OWNED BY public.template_phases.phase_id;


--
-- Name: time_off_requests; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.time_off_requests (
    approved_by integer,
    employee_id integer NOT NULL,
    end_date date NOT NULL,
    requested_by integer NOT NULL,
    start_date date NOT NULL,
    approved_at timestamp(6) without time zone,
    requested_at timestamp(6) without time zone NOT NULL,
    status character varying(20) NOT NULL,
    request_id character varying(50) NOT NULL,
    time_off_type_id character varying(50) NOT NULL,
    work_shift_id character varying(50),
    cancellation_reason text,
    reason text,
    rejected_reason text,
    CONSTRAINT time_off_requests_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying, 'CANCELLED'::character varying])::text[])))
);


--
-- Name: time_off_types; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.time_off_types (
    default_days_per_year double precision,
    is_active boolean NOT NULL,
    is_paid boolean NOT NULL,
    requires_approval boolean NOT NULL,
    requires_balance boolean NOT NULL,
    type_code character varying(50) NOT NULL,
    type_id character varying(50) NOT NULL,
    type_name character varying(100) NOT NULL,
    description text
);


--
-- Name: treatment_plan_templates; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.treatment_plan_templates (
    estimated_duration_days integer,
    is_active boolean,
    specialization_id integer,
    total_price numeric(12,2),
    created_at timestamp(6) without time zone,
    template_id bigint NOT NULL,
    updated_at timestamp(6) without time zone,
    template_code character varying(50) NOT NULL,
    description text,
    template_name character varying(255) NOT NULL
);


--
-- Name: treatment_plan_templates_template_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.treatment_plan_templates_template_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: treatment_plan_templates_template_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.treatment_plan_templates_template_id_seq OWNED BY public.treatment_plan_templates.template_id;


--
-- Name: vital_signs_reference; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.vital_signs_reference (
    age_max integer,
    age_min integer NOT NULL,
    effective_date date NOT NULL,
    high_threshold numeric(10,2),
    is_active boolean NOT NULL,
    low_threshold numeric(10,2),
    normal_max numeric(10,2),
    normal_min numeric(10,2),
    reference_id integer NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone,
    unit character varying(20) NOT NULL,
    vital_type character varying(50) NOT NULL,
    description text
);


--
-- Name: vital_signs_reference_reference_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.vital_signs_reference_reference_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: vital_signs_reference_reference_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.vital_signs_reference_reference_id_seq OWNED BY public.vital_signs_reference.reference_id;


--
-- Name: warehouse_audit_logs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.warehouse_audit_logs (
    performed_by integer,
    batch_id bigint,
    created_at timestamp(6) without time zone NOT NULL,
    item_master_id bigint,
    log_id bigint NOT NULL,
    transaction_id bigint,
    action_type character varying(255) NOT NULL,
    new_value text,
    old_value text,
    reason text,
    CONSTRAINT warehouse_audit_logs_action_type_check CHECK (((action_type)::text = ANY ((ARRAY['CREATE'::character varying, 'UPDATE'::character varying, 'DELETE'::character varying, 'ADJUST'::character varying, 'EXPIRE_ALERT'::character varying, 'TRANSFER'::character varying, 'DISCARD'::character varying])::text[])))
);


--
-- Name: warehouse_audit_logs_log_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.warehouse_audit_logs_log_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: warehouse_audit_logs_log_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.warehouse_audit_logs_log_id_seq OWNED BY public.warehouse_audit_logs.log_id;


--
-- Name: work_shifts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.work_shifts (
    end_time time(6) without time zone NOT NULL,
    is_active boolean NOT NULL,
    start_time time(6) without time zone NOT NULL,
    category character varying(20) NOT NULL,
    work_shift_id character varying(30) NOT NULL,
    shift_name character varying(100) NOT NULL,
    CONSTRAINT work_shifts_category_check CHECK (((category)::text = ANY ((ARRAY['NORMAL'::character varying, 'NIGHT'::character varying])::text[])))
);


--
-- Name: accounts account_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.accounts ALTER COLUMN account_id SET DEFAULT nextval('public.accounts_account_id_seq'::regclass);


--
-- Name: appointment_audit_logs log_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.appointment_audit_logs ALTER COLUMN log_id SET DEFAULT nextval('public.appointment_audit_logs_log_id_seq'::regclass);


--
-- Name: appointments appointment_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.appointments ALTER COLUMN appointment_id SET DEFAULT nextval('public.appointments_appointment_id_seq'::regclass);


--
-- Name: base_roles base_role_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.base_roles ALTER COLUMN base_role_id SET DEFAULT nextval('public.base_roles_base_role_id_seq'::regclass);


--
-- Name: clinical_prescription_items prescription_item_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.clinical_prescription_items ALTER COLUMN prescription_item_id SET DEFAULT nextval('public.clinical_prescription_items_prescription_item_id_seq'::regclass);


--
-- Name: clinical_prescriptions prescription_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.clinical_prescriptions ALTER COLUMN prescription_id SET DEFAULT nextval('public.clinical_prescriptions_prescription_id_seq'::regclass);


--
-- Name: clinical_record_attachments attachment_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.clinical_record_attachments ALTER COLUMN attachment_id SET DEFAULT nextval('public.clinical_record_attachments_attachment_id_seq'::regclass);


--
-- Name: clinical_record_procedures procedure_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.clinical_record_procedures ALTER COLUMN procedure_id SET DEFAULT nextval('public.clinical_record_procedures_procedure_id_seq'::regclass);


--
-- Name: clinical_records clinical_record_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.clinical_records ALTER COLUMN clinical_record_id SET DEFAULT nextval('public.clinical_records_clinical_record_id_seq'::regclass);


--
-- Name: dashboard_preferences id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dashboard_preferences ALTER COLUMN id SET DEFAULT nextval('public.dashboard_preferences_id_seq'::regclass);


--
-- Name: dashboard_saved_views id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dashboard_saved_views ALTER COLUMN id SET DEFAULT nextval('public.dashboard_saved_views_id_seq'::regclass);


--
-- Name: employee_leave_balances balance_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_leave_balances ALTER COLUMN balance_id SET DEFAULT nextval('public.employee_leave_balances_balance_id_seq'::regclass);


--
-- Name: employees employee_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employees ALTER COLUMN employee_id SET DEFAULT nextval('public.employees_employee_id_seq'::regclass);


--
-- Name: fixed_shift_registrations registration_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fixed_shift_registrations ALTER COLUMN registration_id SET DEFAULT nextval('public.fixed_shift_registrations_registration_id_seq'::regclass);


--
-- Name: invoice_items item_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.invoice_items ALTER COLUMN item_id SET DEFAULT nextval('public.invoice_items_item_id_seq'::regclass);


--
-- Name: invoices invoice_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.invoices ALTER COLUMN invoice_id SET DEFAULT nextval('public.invoices_invoice_id_seq'::regclass);


--
-- Name: item_batches batch_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.item_batches ALTER COLUMN batch_id SET DEFAULT nextval('public.item_batches_batch_id_seq'::regclass);


--
-- Name: item_categories category_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.item_categories ALTER COLUMN category_id SET DEFAULT nextval('public.item_categories_category_id_seq'::regclass);


--
-- Name: item_masters item_master_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.item_masters ALTER COLUMN item_master_id SET DEFAULT nextval('public.item_masters_item_master_id_seq'::regclass);


--
-- Name: item_price_history history_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.item_price_history ALTER COLUMN history_id SET DEFAULT nextval('public.item_price_history_history_id_seq'::regclass);


--
-- Name: item_units unit_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.item_units ALTER COLUMN unit_id SET DEFAULT nextval('public.item_units_unit_id_seq'::regclass);


--
-- Name: leave_balance_history history_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.leave_balance_history ALTER COLUMN history_id SET DEFAULT nextval('public.leave_balance_history_history_id_seq'::regclass);


--
-- Name: notifications notification_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notifications ALTER COLUMN notification_id SET DEFAULT nextval('public.notifications_notification_id_seq'::regclass);


--
-- Name: part_time_registrations registration_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.part_time_registrations ALTER COLUMN registration_id SET DEFAULT nextval('public.part_time_registrations_registration_id_seq'::regclass);


--
-- Name: part_time_slots slot_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.part_time_slots ALTER COLUMN slot_id SET DEFAULT nextval('public.part_time_slots_slot_id_seq'::regclass);


--
-- Name: patient_image_comments comment_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patient_image_comments ALTER COLUMN comment_id SET DEFAULT nextval('public.patient_image_comments_comment_id_seq'::regclass);


--
-- Name: patient_images image_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patient_images ALTER COLUMN image_id SET DEFAULT nextval('public.patient_images_image_id_seq'::regclass);


--
-- Name: patient_plan_items item_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patient_plan_items ALTER COLUMN item_id SET DEFAULT nextval('public.patient_plan_items_item_id_seq'::regclass);


--
-- Name: patient_plan_phases patient_phase_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patient_plan_phases ALTER COLUMN patient_phase_id SET DEFAULT nextval('public.patient_plan_phases_patient_phase_id_seq'::regclass);


--
-- Name: patient_tooth_status tooth_status_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patient_tooth_status ALTER COLUMN tooth_status_id SET DEFAULT nextval('public.patient_tooth_status_tooth_status_id_seq'::regclass);


--
-- Name: patient_tooth_status_history history_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patient_tooth_status_history ALTER COLUMN history_id SET DEFAULT nextval('public.patient_tooth_status_history_history_id_seq'::regclass);


--
-- Name: patient_treatment_plans plan_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patient_treatment_plans ALTER COLUMN plan_id SET DEFAULT nextval('public.patient_treatment_plans_plan_id_seq'::regclass);


--
-- Name: patient_unban_audit_logs audit_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patient_unban_audit_logs ALTER COLUMN audit_id SET DEFAULT nextval('public.patient_unban_audit_logs_audit_id_seq'::regclass);


--
-- Name: patients patient_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patients ALTER COLUMN patient_id SET DEFAULT nextval('public.patients_patient_id_seq'::regclass);


--
-- Name: payment_transactions transaction_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payment_transactions ALTER COLUMN transaction_id SET DEFAULT nextval('public.payment_transactions_transaction_id_seq'::regclass);


--
-- Name: payments payment_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payments ALTER COLUMN payment_id SET DEFAULT nextval('public.payments_payment_id_seq'::regclass);


--
-- Name: plan_audit_logs log_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.plan_audit_logs ALTER COLUMN log_id SET DEFAULT nextval('public.plan_audit_logs_log_id_seq'::regclass);


--
-- Name: procedure_material_usage usage_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.procedure_material_usage ALTER COLUMN usage_id SET DEFAULT nextval('public.procedure_material_usage_usage_id_seq'::regclass);


--
-- Name: service_categories category_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.service_categories ALTER COLUMN category_id SET DEFAULT nextval('public.service_categories_category_id_seq'::regclass);


--
-- Name: service_consumables link_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.service_consumables ALTER COLUMN link_id SET DEFAULT nextval('public.service_consumables_link_id_seq'::regclass);


--
-- Name: service_dependencies dependency_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.service_dependencies ALTER COLUMN dependency_id SET DEFAULT nextval('public.service_dependencies_dependency_id_seq'::regclass);


--
-- Name: services service_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.services ALTER COLUMN service_id SET DEFAULT nextval('public.services_service_id_seq'::regclass);


--
-- Name: storage_transaction_items transaction_item_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.storage_transaction_items ALTER COLUMN transaction_item_id SET DEFAULT nextval('public.storage_transaction_items_transaction_item_id_seq'::regclass);


--
-- Name: storage_transactions transaction_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.storage_transactions ALTER COLUMN transaction_id SET DEFAULT nextval('public.storage_transactions_transaction_id_seq'::regclass);


--
-- Name: supplier_items supplier_item_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.supplier_items ALTER COLUMN supplier_item_id SET DEFAULT nextval('public.supplier_items_supplier_item_id_seq'::regclass);


--
-- Name: suppliers supplier_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.suppliers ALTER COLUMN supplier_id SET DEFAULT nextval('public.suppliers_supplier_id_seq'::regclass);


--
-- Name: template_phase_services phase_service_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.template_phase_services ALTER COLUMN phase_service_id SET DEFAULT nextval('public.template_phase_services_phase_service_id_seq'::regclass);


--
-- Name: template_phases phase_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.template_phases ALTER COLUMN phase_id SET DEFAULT nextval('public.template_phases_phase_id_seq'::regclass);


--
-- Name: treatment_plan_templates template_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.treatment_plan_templates ALTER COLUMN template_id SET DEFAULT nextval('public.treatment_plan_templates_template_id_seq'::regclass);


--
-- Name: vital_signs_reference reference_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.vital_signs_reference ALTER COLUMN reference_id SET DEFAULT nextval('public.vital_signs_reference_reference_id_seq'::regclass);


--
-- Name: warehouse_audit_logs log_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.warehouse_audit_logs ALTER COLUMN log_id SET DEFAULT nextval('public.warehouse_audit_logs_log_id_seq'::regclass);


--
-- Name: account_verification_tokens account_verification_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.account_verification_tokens
    ADD CONSTRAINT account_verification_tokens_pkey PRIMARY KEY (token_id);


--
-- Name: account_verification_tokens account_verification_tokens_token_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.account_verification_tokens
    ADD CONSTRAINT account_verification_tokens_token_key UNIQUE (token);


--
-- Name: accounts accounts_account_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.accounts
    ADD CONSTRAINT accounts_account_code_key UNIQUE (account_code);


--
-- Name: accounts accounts_email_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.accounts
    ADD CONSTRAINT accounts_email_key UNIQUE (email);


--
-- Name: accounts accounts_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.accounts
    ADD CONSTRAINT accounts_pkey PRIMARY KEY (account_id);


--
-- Name: accounts accounts_username_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.accounts
    ADD CONSTRAINT accounts_username_key UNIQUE (username);


--
-- Name: appointment_audit_logs appointment_audit_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.appointment_audit_logs
    ADD CONSTRAINT appointment_audit_logs_pkey PRIMARY KEY (log_id);


--
-- Name: appointment_participants appointment_participants_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.appointment_participants
    ADD CONSTRAINT appointment_participants_pkey PRIMARY KEY (appointment_id, employee_id);


--
-- Name: appointment_plan_items appointment_plan_items_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.appointment_plan_items
    ADD CONSTRAINT appointment_plan_items_pkey PRIMARY KEY (appointment_id, item_id);


--
-- Name: appointment_services appointment_services_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.appointment_services
    ADD CONSTRAINT appointment_services_pkey PRIMARY KEY (appointment_id, service_id);


--
-- Name: appointments appointments_appointment_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.appointments
    ADD CONSTRAINT appointments_appointment_code_key UNIQUE (appointment_code);


--
-- Name: appointments appointments_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.appointments
    ADD CONSTRAINT appointments_pkey PRIMARY KEY (appointment_id);


--
-- Name: base_roles base_roles_base_role_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.base_roles
    ADD CONSTRAINT base_roles_base_role_name_key UNIQUE (base_role_name);


--
-- Name: base_roles base_roles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.base_roles
    ADD CONSTRAINT base_roles_pkey PRIMARY KEY (base_role_id);


--
-- Name: blacklisted_tokens blacklisted_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.blacklisted_tokens
    ADD CONSTRAINT blacklisted_tokens_pkey PRIMARY KEY (token_hash);


--
-- Name: chatbot_knowledge chatbot_knowledge_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chatbot_knowledge
    ADD CONSTRAINT chatbot_knowledge_pkey PRIMARY KEY (knowledge_id);


--
-- Name: clinical_prescription_items clinical_prescription_items_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.clinical_prescription_items
    ADD CONSTRAINT clinical_prescription_items_pkey PRIMARY KEY (prescription_item_id);


--
-- Name: clinical_prescriptions clinical_prescriptions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.clinical_prescriptions
    ADD CONSTRAINT clinical_prescriptions_pkey PRIMARY KEY (prescription_id);


--
-- Name: clinical_record_attachments clinical_record_attachments_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.clinical_record_attachments
    ADD CONSTRAINT clinical_record_attachments_pkey PRIMARY KEY (attachment_id);


--
-- Name: clinical_record_procedures clinical_record_procedures_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.clinical_record_procedures
    ADD CONSTRAINT clinical_record_procedures_pkey PRIMARY KEY (procedure_id);


--
-- Name: clinical_records clinical_records_appointment_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.clinical_records
    ADD CONSTRAINT clinical_records_appointment_id_key UNIQUE (appointment_id);


--
-- Name: clinical_records clinical_records_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.clinical_records
    ADD CONSTRAINT clinical_records_pkey PRIMARY KEY (clinical_record_id);


--
-- Name: contact_history contact_history_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.contact_history
    ADD CONSTRAINT contact_history_pkey PRIMARY KEY (history_id);


--
-- Name: customer_contacts customer_contacts_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.customer_contacts
    ADD CONSTRAINT customer_contacts_pkey PRIMARY KEY (contact_id);


--
-- Name: dashboard_preferences dashboard_preferences_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dashboard_preferences
    ADD CONSTRAINT dashboard_preferences_pkey PRIMARY KEY (id);


--
-- Name: dashboard_preferences dashboard_preferences_user_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dashboard_preferences
    ADD CONSTRAINT dashboard_preferences_user_id_key UNIQUE (user_id);


--
-- Name: dashboard_saved_views dashboard_saved_views_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dashboard_saved_views
    ADD CONSTRAINT dashboard_saved_views_pkey PRIMARY KEY (id);


--
-- Name: employee_leave_balances employee_leave_balances_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_leave_balances
    ADD CONSTRAINT employee_leave_balances_pkey PRIMARY KEY (balance_id);


--
-- Name: employee_shift_registrations employee_shift_registrations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_shift_registrations
    ADD CONSTRAINT employee_shift_registrations_pkey PRIMARY KEY (registration_id);


--
-- Name: employee_shifts employee_shifts_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_shifts
    ADD CONSTRAINT employee_shifts_pkey PRIMARY KEY (employee_shift_id);


--
-- Name: employee_specializations employee_specializations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_specializations
    ADD CONSTRAINT employee_specializations_pkey PRIMARY KEY (employee_id, specialization_id);


--
-- Name: employees employees_account_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employees
    ADD CONSTRAINT employees_account_id_key UNIQUE (account_id);


--
-- Name: employees employees_employee_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employees
    ADD CONSTRAINT employees_employee_code_key UNIQUE (employee_code);


--
-- Name: employees employees_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employees
    ADD CONSTRAINT employees_pkey PRIMARY KEY (employee_id);


--
-- Name: fixed_registration_days fixed_registration_days_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fixed_registration_days
    ADD CONSTRAINT fixed_registration_days_pkey PRIMARY KEY (registration_id, day_of_week);


--
-- Name: fixed_shift_registrations fixed_shift_registrations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fixed_shift_registrations
    ADD CONSTRAINT fixed_shift_registrations_pkey PRIMARY KEY (registration_id);


--
-- Name: holiday_dates holiday_dates_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.holiday_dates
    ADD CONSTRAINT holiday_dates_pkey PRIMARY KEY (holiday_date, definition_id);


--
-- Name: holiday_definitions holiday_definitions_holiday_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.holiday_definitions
    ADD CONSTRAINT holiday_definitions_holiday_name_key UNIQUE (holiday_name);


--
-- Name: holiday_definitions holiday_definitions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.holiday_definitions
    ADD CONSTRAINT holiday_definitions_pkey PRIMARY KEY (definition_id);


--
-- Name: invoice_items invoice_items_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.invoice_items
    ADD CONSTRAINT invoice_items_pkey PRIMARY KEY (item_id);


--
-- Name: invoices invoices_invoice_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.invoices
    ADD CONSTRAINT invoices_invoice_code_key UNIQUE (invoice_code);


--
-- Name: invoices invoices_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.invoices
    ADD CONSTRAINT invoices_pkey PRIMARY KEY (invoice_id);


--
-- Name: item_batches item_batches_item_master_id_lot_number_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.item_batches
    ADD CONSTRAINT item_batches_item_master_id_lot_number_key UNIQUE (item_master_id, lot_number);


--
-- Name: item_batches item_batches_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.item_batches
    ADD CONSTRAINT item_batches_pkey PRIMARY KEY (batch_id);


--
-- Name: item_categories item_categories_category_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.item_categories
    ADD CONSTRAINT item_categories_category_code_key UNIQUE (category_code);


--
-- Name: item_categories item_categories_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.item_categories
    ADD CONSTRAINT item_categories_pkey PRIMARY KEY (category_id);


--
-- Name: item_masters item_masters_item_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.item_masters
    ADD CONSTRAINT item_masters_item_code_key UNIQUE (item_code);


--
-- Name: item_masters item_masters_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.item_masters
    ADD CONSTRAINT item_masters_pkey PRIMARY KEY (item_master_id);


--
-- Name: item_price_history item_price_history_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.item_price_history
    ADD CONSTRAINT item_price_history_pkey PRIMARY KEY (history_id);


--
-- Name: item_units item_units_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.item_units
    ADD CONSTRAINT item_units_pkey PRIMARY KEY (unit_id);


--
-- Name: leave_balance_history leave_balance_history_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.leave_balance_history
    ADD CONSTRAINT leave_balance_history_pkey PRIMARY KEY (history_id);


--
-- Name: notifications notifications_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT notifications_pkey PRIMARY KEY (notification_id);


--
-- Name: overtime_requests overtime_requests_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.overtime_requests
    ADD CONSTRAINT overtime_requests_pkey PRIMARY KEY (request_id);


--
-- Name: part_time_registrations part_time_registrations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.part_time_registrations
    ADD CONSTRAINT part_time_registrations_pkey PRIMARY KEY (registration_id);


--
-- Name: part_time_slots part_time_slots_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.part_time_slots
    ADD CONSTRAINT part_time_slots_pkey PRIMARY KEY (slot_id);


--
-- Name: password_reset_tokens password_reset_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.password_reset_tokens
    ADD CONSTRAINT password_reset_tokens_pkey PRIMARY KEY (token_id);


--
-- Name: password_reset_tokens password_reset_tokens_token_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.password_reset_tokens
    ADD CONSTRAINT password_reset_tokens_token_key UNIQUE (token);


--
-- Name: patient_image_comments patient_image_comments_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patient_image_comments
    ADD CONSTRAINT patient_image_comments_pkey PRIMARY KEY (comment_id);


--
-- Name: patient_images patient_images_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patient_images
    ADD CONSTRAINT patient_images_pkey PRIMARY KEY (image_id);


--
-- Name: patient_plan_items patient_plan_items_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patient_plan_items
    ADD CONSTRAINT patient_plan_items_pkey PRIMARY KEY (item_id);


--
-- Name: patient_plan_phases patient_plan_phases_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patient_plan_phases
    ADD CONSTRAINT patient_plan_phases_pkey PRIMARY KEY (patient_phase_id);


--
-- Name: patient_tooth_status_history patient_tooth_status_history_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patient_tooth_status_history
    ADD CONSTRAINT patient_tooth_status_history_pkey PRIMARY KEY (history_id);


--
-- Name: patient_tooth_status patient_tooth_status_patient_id_tooth_number_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patient_tooth_status
    ADD CONSTRAINT patient_tooth_status_patient_id_tooth_number_key UNIQUE (patient_id, tooth_number);


--
-- Name: patient_tooth_status patient_tooth_status_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patient_tooth_status
    ADD CONSTRAINT patient_tooth_status_pkey PRIMARY KEY (tooth_status_id);


--
-- Name: patient_treatment_plans patient_treatment_plans_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patient_treatment_plans
    ADD CONSTRAINT patient_treatment_plans_pkey PRIMARY KEY (plan_id);


--
-- Name: patient_treatment_plans patient_treatment_plans_plan_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patient_treatment_plans
    ADD CONSTRAINT patient_treatment_plans_plan_code_key UNIQUE (plan_code);


--
-- Name: patient_unban_audit_logs patient_unban_audit_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patient_unban_audit_logs
    ADD CONSTRAINT patient_unban_audit_logs_pkey PRIMARY KEY (audit_id);


--
-- Name: patients patients_account_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patients
    ADD CONSTRAINT patients_account_id_key UNIQUE (account_id);


--
-- Name: patients patients_patient_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patients
    ADD CONSTRAINT patients_patient_code_key UNIQUE (patient_code);


--
-- Name: patients patients_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patients
    ADD CONSTRAINT patients_pkey PRIMARY KEY (patient_id);


--
-- Name: payment_transactions payment_transactions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payment_transactions
    ADD CONSTRAINT payment_transactions_pkey PRIMARY KEY (transaction_id);


--
-- Name: payments payments_payment_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT payments_payment_code_key UNIQUE (payment_code);


--
-- Name: payments payments_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT payments_pkey PRIMARY KEY (payment_id);


--
-- Name: permissions permissions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.permissions
    ADD CONSTRAINT permissions_pkey PRIMARY KEY (permission_id);


--
-- Name: plan_audit_logs plan_audit_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.plan_audit_logs
    ADD CONSTRAINT plan_audit_logs_pkey PRIMARY KEY (log_id);


--
-- Name: procedure_material_usage procedure_material_usage_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.procedure_material_usage
    ADD CONSTRAINT procedure_material_usage_pkey PRIMARY KEY (usage_id);


--
-- Name: refresh_tokens refresh_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT refresh_tokens_pkey PRIMARY KEY (id);


--
-- Name: refresh_tokens refresh_tokens_token_hash_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT refresh_tokens_token_hash_key UNIQUE (token_hash);


--
-- Name: role_permissions role_permissions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.role_permissions
    ADD CONSTRAINT role_permissions_pkey PRIMARY KEY (permission_id, role_id);


--
-- Name: roles roles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (role_id);


--
-- Name: room_services room_services_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.room_services
    ADD CONSTRAINT room_services_pkey PRIMARY KEY (service_id, room_id);


--
-- Name: rooms rooms_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rooms
    ADD CONSTRAINT rooms_pkey PRIMARY KEY (room_id);


--
-- Name: rooms rooms_room_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rooms
    ADD CONSTRAINT rooms_room_code_key UNIQUE (room_code);


--
-- Name: service_categories service_categories_category_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.service_categories
    ADD CONSTRAINT service_categories_category_code_key UNIQUE (category_code);


--
-- Name: service_categories service_categories_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.service_categories
    ADD CONSTRAINT service_categories_pkey PRIMARY KEY (category_id);


--
-- Name: service_consumables service_consumables_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.service_consumables
    ADD CONSTRAINT service_consumables_pkey PRIMARY KEY (link_id);


--
-- Name: service_consumables service_consumables_service_id_item_master_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.service_consumables
    ADD CONSTRAINT service_consumables_service_id_item_master_id_key UNIQUE (service_id, item_master_id);


--
-- Name: service_dependencies service_dependencies_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.service_dependencies
    ADD CONSTRAINT service_dependencies_pkey PRIMARY KEY (dependency_id);


--
-- Name: services services_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.services
    ADD CONSTRAINT services_pkey PRIMARY KEY (service_id);


--
-- Name: services services_service_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.services
    ADD CONSTRAINT services_service_code_key UNIQUE (service_code);


--
-- Name: shift_renewal_requests shift_renewal_requests_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.shift_renewal_requests
    ADD CONSTRAINT shift_renewal_requests_pkey PRIMARY KEY (renewal_id);


--
-- Name: specializations specializations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.specializations
    ADD CONSTRAINT specializations_pkey PRIMARY KEY (specialization_id);


--
-- Name: specializations specializations_specialization_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.specializations
    ADD CONSTRAINT specializations_specialization_code_key UNIQUE (specialization_code);


--
-- Name: storage_transaction_items storage_transaction_items_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.storage_transaction_items
    ADD CONSTRAINT storage_transaction_items_pkey PRIMARY KEY (transaction_item_id);


--
-- Name: storage_transactions storage_transactions_invoice_number_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.storage_transactions
    ADD CONSTRAINT storage_transactions_invoice_number_key UNIQUE (invoice_number);


--
-- Name: storage_transactions storage_transactions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.storage_transactions
    ADD CONSTRAINT storage_transactions_pkey PRIMARY KEY (transaction_id);


--
-- Name: storage_transactions storage_transactions_transaction_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.storage_transactions
    ADD CONSTRAINT storage_transactions_transaction_code_key UNIQUE (transaction_code);


--
-- Name: supplier_items supplier_items_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.supplier_items
    ADD CONSTRAINT supplier_items_pkey PRIMARY KEY (supplier_item_id);


--
-- Name: supplier_items supplier_items_supplier_id_item_master_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.supplier_items
    ADD CONSTRAINT supplier_items_supplier_id_item_master_id_key UNIQUE (supplier_id, item_master_id);


--
-- Name: suppliers suppliers_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.suppliers
    ADD CONSTRAINT suppliers_pkey PRIMARY KEY (supplier_id);


--
-- Name: suppliers suppliers_supplier_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.suppliers
    ADD CONSTRAINT suppliers_supplier_code_key UNIQUE (supplier_code);


--
-- Name: template_phase_services template_phase_services_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.template_phase_services
    ADD CONSTRAINT template_phase_services_pkey PRIMARY KEY (phase_service_id);


--
-- Name: template_phases template_phases_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.template_phases
    ADD CONSTRAINT template_phases_pkey PRIMARY KEY (phase_id);


--
-- Name: time_off_requests time_off_requests_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.time_off_requests
    ADD CONSTRAINT time_off_requests_pkey PRIMARY KEY (request_id);


--
-- Name: time_off_types time_off_types_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.time_off_types
    ADD CONSTRAINT time_off_types_pkey PRIMARY KEY (type_id);


--
-- Name: time_off_types time_off_types_type_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.time_off_types
    ADD CONSTRAINT time_off_types_type_code_key UNIQUE (type_code);


--
-- Name: treatment_plan_templates treatment_plan_templates_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.treatment_plan_templates
    ADD CONSTRAINT treatment_plan_templates_pkey PRIMARY KEY (template_id);


--
-- Name: treatment_plan_templates treatment_plan_templates_template_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.treatment_plan_templates
    ADD CONSTRAINT treatment_plan_templates_template_code_key UNIQUE (template_code);


--
-- Name: employee_shifts uk_employee_date_shift; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_shifts
    ADD CONSTRAINT uk_employee_date_shift UNIQUE (employee_id, work_date, work_shift_id);


--
-- Name: employee_leave_balances uk_employee_leave_balance; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_leave_balances
    ADD CONSTRAINT uk_employee_leave_balance UNIQUE (employee_id, time_off_type_id, cycle_year);


--
-- Name: overtime_requests uk_overtime_employee_date_shift; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.overtime_requests
    ADD CONSTRAINT uk_overtime_employee_date_shift UNIQUE (employee_id, work_date, work_shift_id);


--
-- Name: template_phase_services uk_phase_service; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.template_phase_services
    ADD CONSTRAINT uk_phase_service UNIQUE (phase_id, service_id);


--
-- Name: template_phases uk_template_phase_number; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.template_phases
    ADD CONSTRAINT uk_template_phase_number UNIQUE (template_id, phase_number);


--
-- Name: vital_signs_reference vital_signs_reference_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.vital_signs_reference
    ADD CONSTRAINT vital_signs_reference_pkey PRIMARY KEY (reference_id);


--
-- Name: warehouse_audit_logs warehouse_audit_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.warehouse_audit_logs
    ADD CONSTRAINT warehouse_audit_logs_pkey PRIMARY KEY (log_id);


--
-- Name: work_shifts work_shifts_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.work_shifts
    ADD CONSTRAINT work_shifts_pkey PRIMARY KEY (work_shift_id);


--
-- Name: idx_audit_created_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_audit_created_at ON public.warehouse_audit_logs USING btree (created_at);


--
-- Name: idx_audit_item_action; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_audit_item_action ON public.warehouse_audit_logs USING btree (item_master_id, action_type);


--
-- Name: idx_audit_patient; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_audit_patient ON public.patient_unban_audit_logs USING btree (patient_id);


--
-- Name: idx_audit_performed_by; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_audit_performed_by ON public.patient_unban_audit_logs USING btree (performed_by);


--
-- Name: idx_audit_timestamp; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_audit_timestamp ON public.patient_unban_audit_logs USING btree ("timestamp");


--
-- Name: idx_clinical_record_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_clinical_record_id ON public.patient_images USING btree (clinical_record_id);


--
-- Name: idx_created_by; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_created_by ON public.patient_image_comments USING btree (created_by);


--
-- Name: idx_employee_workdate; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_employee_workdate ON public.employee_shifts USING btree (employee_id, work_date);


--
-- Name: idx_fixed_shift_employee; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fixed_shift_employee ON public.fixed_shift_registrations USING btree (employee_id, work_shift_id, is_active);


--
-- Name: idx_image_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_image_id ON public.patient_image_comments USING btree (image_id);


--
-- Name: idx_image_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_image_type ON public.patient_images USING btree (image_type);


--
-- Name: idx_overtime_employee; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_overtime_employee ON public.overtime_requests USING btree (employee_id);


--
-- Name: idx_overtime_requested_by; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_overtime_requested_by ON public.overtime_requests USING btree (requested_by);


--
-- Name: idx_overtime_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_overtime_status ON public.overtime_requests USING btree (status);


--
-- Name: idx_overtime_work_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_overtime_work_date ON public.overtime_requests USING btree (work_date);


--
-- Name: idx_patient_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_patient_id ON public.patient_images USING btree (patient_id);


--
-- Name: idx_patient_image_comment_created_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_patient_image_comment_created_at ON public.patient_image_comments USING btree (created_at);


--
-- Name: idx_patient_image_created_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_patient_image_created_at ON public.patient_images USING btree (created_at);


--
-- Name: idx_renewal_employee_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_renewal_employee_status ON public.shift_renewal_requests USING btree (employee_id, status);


--
-- Name: idx_renewal_expires_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_renewal_expires_at ON public.shift_renewal_requests USING btree (expires_at);


--
-- Name: idx_service_deps_dependent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_service_deps_dependent ON public.service_dependencies USING btree (dependent_service_id);


--
-- Name: idx_service_deps_service; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_service_deps_service ON public.service_dependencies USING btree (service_id, rule_type);


--
-- Name: idx_work_shift_active_category; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_work_shift_active_category ON public.work_shifts USING btree (is_active, category);


--
-- Name: idx_work_shift_category; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_work_shift_category ON public.work_shifts USING btree (category);


--
-- Name: idx_work_shift_is_active; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_work_shift_is_active ON public.work_shifts USING btree (is_active);


--
-- Name: idx_work_shift_start_time; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_work_shift_start_time ON public.work_shifts USING btree (start_time);


--
-- Name: idx_workdate_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_workdate_status ON public.employee_shifts USING btree (work_date, status);


--
-- Name: clinical_record_attachments fk17erqg5m14hgp0r9pmtyh0m4k; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.clinical_record_attachments
    ADD CONSTRAINT fk17erqg5m14hgp0r9pmtyh0m4k FOREIGN KEY (uploaded_by) REFERENCES public.employees(employee_id);


--
-- Name: patient_treatment_plans fk1jvc9s12279mmsak8mcc2f823; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patient_treatment_plans
    ADD CONSTRAINT fk1jvc9s12279mmsak8mcc2f823 FOREIGN KEY (patient_id) REFERENCES public.patients(patient_id);


--
-- Name: patient_tooth_status fk1l2bpkyvvviju2ha43six9n11; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patient_tooth_status
    ADD CONSTRAINT fk1l2bpkyvvviju2ha43six9n11 FOREIGN KEY (patient_id) REFERENCES public.patients(patient_id);


--
-- Name: patient_treatment_plans fk22xqwmhdrvnu8amb661b9tmhb; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patient_treatment_plans
    ADD CONSTRAINT fk22xqwmhdrvnu8amb661b9tmhb FOREIGN KEY (template_id) REFERENCES public.treatment_plan_templates(template_id);


--
-- Name: patient_treatment_plans fk28eg253mqv7ftvj0h698ofcji; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patient_treatment_plans
    ADD CONSTRAINT fk28eg253mqv7ftvj0h698ofcji FOREIGN KEY (approved_by) REFERENCES public.employees(employee_id);


--
-- Name: plan_audit_logs fk2akmwm4e5ubr3yxmrtuw3s55v; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.plan_audit_logs
    ADD CONSTRAINT fk2akmwm4e5ubr3yxmrtuw3s55v FOREIGN KEY (plan_id) REFERENCES public.patient_treatment_plans(plan_id);


--
-- Name: patient_tooth_status_history fk2flo7d5er6v34ng2dobrv8wem; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patient_tooth_status_history
    ADD CONSTRAINT fk2flo7d5er6v34ng2dobrv8wem FOREIGN KEY (patient_id) REFERENCES public.patients(patient_id);


--
-- Name: overtime_requests fk2xalx6i1kksbjt9wm5yu36waq; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.overtime_requests
    ADD CONSTRAINT fk2xalx6i1kksbjt9wm5yu36waq FOREIGN KEY (requested_by) REFERENCES public.employees(employee_id);


--
-- Name: overtime_requests fk30ui3g8apytrg9r0t8nnjvqy8; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.overtime_requests
    ADD CONSTRAINT fk30ui3g8apytrg9r0t8nnjvqy8 FOREIGN KEY (work_shift_id) REFERENCES public.work_shifts(work_shift_id);


--
-- Name: appointment_participants fk3c7ofbr1kebgtt377eo1rdapg; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.appointment_participants
    ADD CONSTRAINT fk3c7ofbr1kebgtt377eo1rdapg FOREIGN KEY (appointment_id) REFERENCES public.appointments(appointment_id);


--
-- Name: time_off_requests fk3j49edgkahbbsp0v211m9rf3; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.time_off_requests
    ADD CONSTRAINT fk3j49edgkahbbsp0v211m9rf3 FOREIGN KEY (employee_id) REFERENCES public.employees(employee_id);


--
-- Name: employee_shifts fk3ogimsoo4uq6xs1rkn81lgdpu; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_shifts
    ADD CONSTRAINT fk3ogimsoo4uq6xs1rkn81lgdpu FOREIGN KEY (work_shift_id) REFERENCES public.work_shifts(work_shift_id);


--
-- Name: template_phase_services fk41b1vy8w5cvmqxyp192b55pci; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.template_phase_services
    ADD CONSTRAINT fk41b1vy8w5cvmqxyp192b55pci FOREIGN KEY (phase_id) REFERENCES public.template_phases(phase_id);


--
-- Name: employee_specializations fk41i97rxmv2gd0vuftr2ypv26i; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_specializations
    ADD CONSTRAINT fk41i97rxmv2gd0vuftr2ypv26i FOREIGN KEY (employee_id) REFERENCES public.employees(employee_id);


--
-- Name: storage_transaction_items fk43a36kayyg96wo4qptb86cvdx; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.storage_transaction_items
    ADD CONSTRAINT fk43a36kayyg96wo4qptb86cvdx FOREIGN KEY (unit_id) REFERENCES public.item_units(unit_id);


--
-- Name: invoice_items fk46ae0lhu1oqs7cv91fn6y9n7w; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.invoice_items
    ADD CONSTRAINT fk46ae0lhu1oqs7cv91fn6y9n7w FOREIGN KEY (invoice_id) REFERENCES public.invoices(invoice_id);


--
-- Name: item_units fk4gco92by1nxi5s1di8213srbe; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.item_units
    ADD CONSTRAINT fk4gco92by1nxi5s1di8213srbe FOREIGN KEY (item_master_id) REFERENCES public.item_masters(item_master_id);


--
-- Name: patient_images fk4xjlrddkjuu42tktcv9ie0ris; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patient_images
    ADD CONSTRAINT fk4xjlrddkjuu42tktcv9ie0ris FOREIGN KEY (uploaded_by) REFERENCES public.employees(employee_id);


--
-- Name: part_time_registrations fk5491w9che5bxfhjvymekrc08j; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.part_time_registrations
    ADD CONSTRAINT fk5491w9che5bxfhjvymekrc08j FOREIGN KEY (part_time_slot_id) REFERENCES public.part_time_slots(slot_id);


--
-- Name: item_batches fk5byu7hvsecu25b2i9op7afsj8; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.item_batches
    ADD CONSTRAINT fk5byu7hvsecu25b2i9op7afsj8 FOREIGN KEY (item_master_id) REFERENCES public.item_masters(item_master_id);


--
-- Name: storage_transactions fk5ep4ls8gkaei3ewu7no05v3an; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.storage_transactions
    ADD CONSTRAINT fk5ep4ls8gkaei3ewu7no05v3an FOREIGN KEY (related_appointment_id) REFERENCES public.appointments(appointment_id);


--
-- Name: contact_history fk5k119wn49wkbpi25effpubv35; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.contact_history
    ADD CONSTRAINT fk5k119wn49wkbpi25effpubv35 FOREIGN KEY (employee_id) REFERENCES public.employees(employee_id);


--
-- Name: patient_images fk5p9p5fo5jq7hi5rtu8566xdd; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patient_images
    ADD CONSTRAINT fk5p9p5fo5jq7hi5rtu8566xdd FOREIGN KEY (patient_id) REFERENCES public.patients(patient_id);


--
-- Name: appointment_services fk68fbfnf0iy7uq0tfb5mjmm2hx; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.appointment_services
    ADD CONSTRAINT fk68fbfnf0iy7uq0tfb5mjmm2hx FOREIGN KEY (service_id) REFERENCES public.services(service_id);


--
-- Name: clinical_prescription_items fk6ea4irax1ta9nfe2150e4skev; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.clinical_prescription_items
    ADD CONSTRAINT fk6ea4irax1ta9nfe2150e4skev FOREIGN KEY (item_master_id) REFERENCES public.item_masters(item_master_id);


--
-- Name: holiday_dates fk6ev54weipvaxplbgnkdgkog1a; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.holiday_dates
    ADD CONSTRAINT fk6ev54weipvaxplbgnkdgkog1a FOREIGN KEY (definition_id) REFERENCES public.holiday_definitions(definition_id);


--
-- Name: template_phases fk6evjyipgggjj11tsvt4onn9sh; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.template_phases
    ADD CONSTRAINT fk6evjyipgggjj11tsvt4onn9sh FOREIGN KEY (template_id) REFERENCES public.treatment_plan_templates(template_id);


--
-- Name: refresh_tokens fk6fm1gdbsrg5h8r5e3voiu6bo9; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT fk6fm1gdbsrg5h8r5e3voiu6bo9 FOREIGN KEY (account_id) REFERENCES public.accounts(account_id);


--
-- Name: storage_transactions fk6h9ubgi830aoocit8yhrf2wmt; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.storage_transactions
    ADD CONSTRAINT fk6h9ubgi830aoocit8yhrf2wmt FOREIGN KEY (rejected_by_id) REFERENCES public.employees(employee_id);


--
-- Name: patient_image_comments fk6rl6yyjtu0l5imf5ikcfvs4sp; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patient_image_comments
    ADD CONSTRAINT fk6rl6yyjtu0l5imf5ikcfvs4sp FOREIGN KEY (created_by) REFERENCES public.employees(employee_id);


--
-- Name: employee_shifts fk6wol1934mgshd2u61dbg55q1d; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_shifts
    ADD CONSTRAINT fk6wol1934mgshd2u61dbg55q1d FOREIGN KEY (employee_id) REFERENCES public.employees(employee_id);


--
-- Name: roles fk7f29dqoh0pp18arhlev16ih8m; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT fk7f29dqoh0pp18arhlev16ih8m FOREIGN KEY (base_role_id) REFERENCES public.base_roles(base_role_id);


--
-- Name: contact_history fk7njrmj2blx2f5v0h17dsfxl3b; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.contact_history
    ADD CONSTRAINT fk7njrmj2blx2f5v0h17dsfxl3b FOREIGN KEY (contact_id) REFERENCES public.customer_contacts(contact_id);


--
-- Name: appointment_services fk7smp9csy21h26g51aii9gvfn8; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.appointment_services
    ADD CONSTRAINT fk7smp9csy21h26g51aii9gvfn8 FOREIGN KEY (appointment_id) REFERENCES public.appointments(appointment_id);


--
-- Name: item_price_history fk7ud3af9xu76obfn2rt2gflosd; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.item_price_history
    ADD CONSTRAINT fk7ud3af9xu76obfn2rt2gflosd FOREIGN KEY (item_master_id) REFERENCES public.item_masters(item_master_id);


--
-- Name: employees fk8dwhb0qmor08fl06pde1bef3c; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employees
    ADD CONSTRAINT fk8dwhb0qmor08fl06pde1bef3c FOREIGN KEY (account_id) REFERENCES public.accounts(account_id);


--
-- Name: appointments fk8exap5wmg8kmb1g1rx3by21yt; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.appointments
    ADD CONSTRAINT fk8exap5wmg8kmb1g1rx3by21yt FOREIGN KEY (patient_id) REFERENCES public.patients(patient_id);


--
-- Name: fixed_shift_registrations fk8j5s36okbediuwegaal9njeco; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fixed_shift_registrations
    ADD CONSTRAINT fk8j5s36okbediuwegaal9njeco FOREIGN KEY (work_shift_id) REFERENCES public.work_shifts(work_shift_id);


--
-- Name: leave_balance_history fk9582yo6hjspig6ug1wk4fr1c9; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.leave_balance_history
    ADD CONSTRAINT fk9582yo6hjspig6ug1wk4fr1c9 FOREIGN KEY (balance_id) REFERENCES public.employee_leave_balances(balance_id);


--
-- Name: time_off_requests fk96a7mdsghieplrhe7vgrw2pov; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.time_off_requests
    ADD CONSTRAINT fk96a7mdsghieplrhe7vgrw2pov FOREIGN KEY (approved_by) REFERENCES public.employees(employee_id);


--
-- Name: appointment_audit_logs fk_audit_appointment; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.appointment_audit_logs
    ADD CONSTRAINT fk_audit_appointment FOREIGN KEY (appointment_id) REFERENCES public.appointments(appointment_id);


--
-- Name: appointment_audit_logs fk_audit_employee; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.appointment_audit_logs
    ADD CONSTRAINT fk_audit_employee FOREIGN KEY (changed_by_employee_id) REFERENCES public.employees(employee_id);


--
-- Name: clinical_record_procedures fk_procedure_storage_tx; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.clinical_record_procedures
    ADD CONSTRAINT fk_procedure_storage_tx FOREIGN KEY (storage_transaction_id) REFERENCES public.storage_transactions(transaction_id) ON DELETE SET NULL;


--
-- Name: services fk_service_category; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.services
    ADD CONSTRAINT fk_service_category FOREIGN KEY (category_id) REFERENCES public.service_categories(category_id);


--
-- Name: services fk_service_specialization; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.services
    ADD CONSTRAINT fk_service_specialization FOREIGN KEY (specialization_id) REFERENCES public.specializations(specialization_id);


--
-- Name: fixed_shift_registrations fkafu5m71k0680f5v3fw8d3uolt; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fixed_shift_registrations
    ADD CONSTRAINT fkafu5m71k0680f5v3fw8d3uolt FOREIGN KEY (employee_id) REFERENCES public.employees(employee_id);


--
-- Name: shift_renewal_requests fkahiiactjqoa8a1k7r7l2l5opn; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.shift_renewal_requests
    ADD CONSTRAINT fkahiiactjqoa8a1k7r7l2l5opn FOREIGN KEY (employee_id) REFERENCES public.employees(employee_id);


--
-- Name: patient_plan_items fkajhhc2oavr2j9u77rymlk1bsc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patient_plan_items
    ADD CONSTRAINT fkajhhc2oavr2j9u77rymlk1bsc FOREIGN KEY (phase_id) REFERENCES public.patient_plan_phases(patient_phase_id);


--
-- Name: password_reset_tokens fkakpd9ro0vywgwa7hqmpbdurma; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.password_reset_tokens
    ADD CONSTRAINT fkakpd9ro0vywgwa7hqmpbdurma FOREIGN KEY (account_id) REFERENCES public.accounts(account_id);


--
-- Name: storage_transaction_items fkbgalcx120gc3xgup1q7xugqdm; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.storage_transaction_items
    ADD CONSTRAINT fkbgalcx120gc3xgup1q7xugqdm FOREIGN KEY (batch_id) REFERENCES public.item_batches(batch_id);


--
-- Name: patient_plan_items fkbj3txddhb9kll8gikwuevnf1d; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patient_plan_items
    ADD CONSTRAINT fkbj3txddhb9kll8gikwuevnf1d FOREIGN KEY (price_updated_by) REFERENCES public.employees(employee_id);


--
-- Name: appointments fkbsma6x4pnujct0e6xkycu9864; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.appointments
    ADD CONSTRAINT fkbsma6x4pnujct0e6xkycu9864 FOREIGN KEY (room_id) REFERENCES public.rooms(room_id);


--
-- Name: warehouse_audit_logs fkbt1538qqpumqwm2cittmdcuvq; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.warehouse_audit_logs
    ADD CONSTRAINT fkbt1538qqpumqwm2cittmdcuvq FOREIGN KEY (performed_by) REFERENCES public.employees(employee_id);


--
-- Name: item_batches fkbyx8hryjou47dapdmdvhou936; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.item_batches
    ADD CONSTRAINT fkbyx8hryjou47dapdmdvhou936 FOREIGN KEY (parent_batch_id) REFERENCES public.item_batches(batch_id);


--
-- Name: patient_treatment_plans fkcutkshpl3128muaq5dlm3rg5i; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patient_treatment_plans
    ADD CONSTRAINT fkcutkshpl3128muaq5dlm3rg5i FOREIGN KEY (created_by) REFERENCES public.employees(employee_id);


--
-- Name: account_verification_tokens fkd65calywteb2v1dv1q8geafcw; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.account_verification_tokens
    ADD CONSTRAINT fkd65calywteb2v1dv1q8geafcw FOREIGN KEY (account_id) REFERENCES public.accounts(account_id);


--
-- Name: employee_shift_registrations fkd8leg2pa0um6oniu6u0i4hkbe; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_shift_registrations
    ADD CONSTRAINT fkd8leg2pa0um6oniu6u0i4hkbe FOREIGN KEY (part_time_slot_id) REFERENCES public.part_time_slots(slot_id);


--
-- Name: patient_treatment_plans fkdkh6oe20mlqs6a2yjk1m40per; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patient_treatment_plans
    ADD CONSTRAINT fkdkh6oe20mlqs6a2yjk1m40per FOREIGN KEY (specialization_id) REFERENCES public.specializations(specialization_id);


--
-- Name: warehouse_audit_logs fke7cq8yiocj8hk7mi96amc9076; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.warehouse_audit_logs
    ADD CONSTRAINT fke7cq8yiocj8hk7mi96amc9076 FOREIGN KEY (transaction_id) REFERENCES public.storage_transactions(transaction_id);


--
-- Name: treatment_plan_templates fkea0llj5rlnsaa1qxhrnpu98vf; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.treatment_plan_templates
    ADD CONSTRAINT fkea0llj5rlnsaa1qxhrnpu98vf FOREIGN KEY (specialization_id) REFERENCES public.specializations(specialization_id);


--
-- Name: template_phase_services fkeb23prawjc2fm16ap0n3091ll; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.template_phase_services
    ADD CONSTRAINT fkeb23prawjc2fm16ap0n3091ll FOREIGN KEY (service_id) REFERENCES public.services(service_id);


--
-- Name: patient_unban_audit_logs fkedgguy8dxr74cfeq04m706vrl; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patient_unban_audit_logs
    ADD CONSTRAINT fkedgguy8dxr74cfeq04m706vrl FOREIGN KEY (patient_id) REFERENCES public.patients(patient_id);


--
-- Name: role_permissions fkegdk29eiy7mdtefy5c7eirr6e; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.role_permissions
    ADD CONSTRAINT fkegdk29eiy7mdtefy5c7eirr6e FOREIGN KEY (permission_id) REFERENCES public.permissions(permission_id);


--
-- Name: clinical_prescription_items fkel7gxi9to24wc90ypojtmrmc5; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.clinical_prescription_items
    ADD CONSTRAINT fkel7gxi9to24wc90ypojtmrmc5 FOREIGN KEY (prescription_id) REFERENCES public.clinical_prescriptions(prescription_id);


--
-- Name: patient_plan_phases fkeqycgrilelw6shu9e096jevqg; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patient_plan_phases
    ADD CONSTRAINT fkeqycgrilelw6shu9e096jevqg FOREIGN KEY (plan_id) REFERENCES public.patient_treatment_plans(plan_id);


--
-- Name: room_services fkewq1euu8r5i0c2f1ejfout7ty; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.room_services
    ADD CONSTRAINT fkewq1euu8r5i0c2f1ejfout7ty FOREIGN KEY (room_id) REFERENCES public.rooms(room_id);


--
-- Name: service_dependencies fkff06lpsul01hja3kiebbjpgu8; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.service_dependencies
    ADD CONSTRAINT fkff06lpsul01hja3kiebbjpgu8 FOREIGN KEY (dependent_service_id) REFERENCES public.services(service_id);


--
-- Name: patients fkflgrec6bbs3jrbf93o9fixjma; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patients
    ADD CONSTRAINT fkflgrec6bbs3jrbf93o9fixjma FOREIGN KEY (account_id) REFERENCES public.accounts(account_id);


--
-- Name: patient_image_comments fkfm7yjswwlsy0r0cr1elammo4k; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patient_image_comments
    ADD CONSTRAINT fkfm7yjswwlsy0r0cr1elammo4k FOREIGN KEY (image_id) REFERENCES public.patient_images(image_id);


--
-- Name: employee_specializations fkfot1n1vx9ue3sak3rk6jwkcbp; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_specializations
    ADD CONSTRAINT fkfot1n1vx9ue3sak3rk6jwkcbp FOREIGN KEY (specialization_id) REFERENCES public.specializations(specialization_id);


--
-- Name: clinical_records fkg5pqt9k6r8yeal5x70dr34cqf; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.clinical_records
    ADD CONSTRAINT fkg5pqt9k6r8yeal5x70dr34cqf FOREIGN KEY (appointment_id) REFERENCES public.appointments(appointment_id);


--
-- Name: overtime_requests fkg5wygdf15wnj0dhc2mgquajh5; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.overtime_requests
    ADD CONSTRAINT fkg5wygdf15wnj0dhc2mgquajh5 FOREIGN KEY (employee_id) REFERENCES public.employees(employee_id);


--
-- Name: clinical_record_procedures fkgl7ecd6gwr64f4jo1waovlcpn; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.clinical_record_procedures
    ADD CONSTRAINT fkgl7ecd6gwr64f4jo1waovlcpn FOREIGN KEY (patient_plan_item_id) REFERENCES public.patient_plan_items(item_id);


--
-- Name: overtime_requests fkgqoukhteuovrbhmmmdw5s9wys; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.overtime_requests
    ADD CONSTRAINT fkgqoukhteuovrbhmmmdw5s9wys FOREIGN KEY (approved_by) REFERENCES public.employees(employee_id);


--
-- Name: employee_shift_registrations fkgqqny9jnb22xc3j7e7qg88m1i; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_shift_registrations
    ADD CONSTRAINT fkgqqny9jnb22xc3j7e7qg88m1i FOREIGN KEY (employee_id) REFERENCES public.employees(employee_id);


--
-- Name: payment_transactions fkgu8q4u0cjr8aljtknj557g2i8; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payment_transactions
    ADD CONSTRAINT fkgu8q4u0cjr8aljtknj557g2i8 FOREIGN KEY (payment_id) REFERENCES public.payments(payment_id);


--
-- Name: clinical_record_procedures fkh0re4ktbavaqoj55h9ts8k305; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.clinical_record_procedures
    ADD CONSTRAINT fkh0re4ktbavaqoj55h9ts8k305 FOREIGN KEY (service_id) REFERENCES public.services(service_id);


--
-- Name: storage_transactions fkh7almrtpwfxmn5ea1uavd72xj; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.storage_transactions
    ADD CONSTRAINT fkh7almrtpwfxmn5ea1uavd72xj FOREIGN KEY (approved_by_id) REFERENCES public.employees(employee_id);


--
-- Name: patient_images fkil8fncq0usbin397yferle81t; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patient_images
    ADD CONSTRAINT fkil8fncq0usbin397yferle81t FOREIGN KEY (clinical_record_id) REFERENCES public.clinical_records(clinical_record_id);


--
-- Name: customer_contacts fkj76vyo39v8khrloeu2dwqjmqj; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.customer_contacts
    ADD CONSTRAINT fkj76vyo39v8khrloeu2dwqjmqj FOREIGN KEY (assigned_to) REFERENCES public.employees(employee_id);


--
-- Name: procedure_material_usage fkjp56g28qcvq9g19sr9mv4l9dj; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.procedure_material_usage
    ADD CONSTRAINT fkjp56g28qcvq9g19sr9mv4l9dj FOREIGN KEY (item_master_id) REFERENCES public.item_masters(item_master_id);


--
-- Name: clinical_record_attachments fkjqk39wwhdh7y1791j99pqa6hj; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.clinical_record_attachments
    ADD CONSTRAINT fkjqk39wwhdh7y1791j99pqa6hj FOREIGN KEY (clinical_record_id) REFERENCES public.clinical_records(clinical_record_id);


--
-- Name: supplier_items fkjxawqjs3rj3yawuwwiw1vmfqw; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.supplier_items
    ADD CONSTRAINT fkjxawqjs3rj3yawuwwiw1vmfqw FOREIGN KEY (item_master_id) REFERENCES public.item_masters(item_master_id);


--
-- Name: clinical_record_procedures fkk46glws68070vb2nnd7av4w3n; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.clinical_record_procedures
    ADD CONSTRAINT fkk46glws68070vb2nnd7av4w3n FOREIGN KEY (clinical_record_id) REFERENCES public.clinical_records(clinical_record_id);


--
-- Name: warehouse_audit_logs fkk8wcgee0o0nyos172manw0rse; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.warehouse_audit_logs
    ADD CONSTRAINT fkk8wcgee0o0nyos172manw0rse FOREIGN KEY (item_master_id) REFERENCES public.item_masters(item_master_id);


--
-- Name: patient_tooth_status_history fkkhjewei3iusf3hr6qrfc3ejug; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patient_tooth_status_history
    ADD CONSTRAINT fkkhjewei3iusf3hr6qrfc3ejug FOREIGN KEY (changed_by) REFERENCES public.employees(employee_id);


--
-- Name: blacklisted_tokens fkkjxoxt3415dhlkhe8osvdqh02; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.blacklisted_tokens
    ADD CONSTRAINT fkkjxoxt3415dhlkhe8osvdqh02 FOREIGN KEY (account_id) REFERENCES public.accounts(account_id);


--
-- Name: item_masters fkl5k3c3mcwrjw6fj1bv182mekg; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.item_masters
    ADD CONSTRAINT fkl5k3c3mcwrjw6fj1bv182mekg FOREIGN KEY (category_id) REFERENCES public.item_categories(category_id);


--
-- Name: storage_transactions fklxks0b3lwrp7mfy3v1pvqrc2j; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.storage_transactions
    ADD CONSTRAINT fklxks0b3lwrp7mfy3v1pvqrc2j FOREIGN KEY (cancelled_by_id) REFERENCES public.employees(employee_id);


--
-- Name: supplier_items fkm2t6dgtc9r1a39fop5375dtma; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.supplier_items
    ADD CONSTRAINT fkm2t6dgtc9r1a39fop5375dtma FOREIGN KEY (supplier_id) REFERENCES public.suppliers(supplier_id);


--
-- Name: storage_transactions fkm7rqff8u7165e0xa0vqyqq5rx; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.storage_transactions
    ADD CONSTRAINT fkm7rqff8u7165e0xa0vqyqq5rx FOREIGN KEY (created_by) REFERENCES public.employees(employee_id);


--
-- Name: part_time_registration_dates fkmgasng55unfl2phsf0owcg6g0; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.part_time_registration_dates
    ADD CONSTRAINT fkmgasng55unfl2phsf0owcg6g0 FOREIGN KEY (registration_id) REFERENCES public.part_time_registrations(registration_id);


--
-- Name: role_permissions fkn5fotdgk8d1xvo8nav9uv3muc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.role_permissions
    ADD CONSTRAINT fkn5fotdgk8d1xvo8nav9uv3muc FOREIGN KEY (role_id) REFERENCES public.roles(role_id);


--
-- Name: appointments fkn9hov7hdkmkdpveiujpmq4sgh; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.appointments
    ADD CONSTRAINT fkn9hov7hdkmkdpveiujpmq4sgh FOREIGN KEY (employee_id) REFERENCES public.employees(employee_id);


--
-- Name: employee_leave_balances fkna2wpd1rpr71q3tljsmsjm1jn; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_leave_balances
    ADD CONSTRAINT fkna2wpd1rpr71q3tljsmsjm1jn FOREIGN KEY (employee_id) REFERENCES public.employees(employee_id);


--
-- Name: shift_renewal_requests fkncr2j6efhntt0pk4icaao6ih4; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.shift_renewal_requests
    ADD CONSTRAINT fkncr2j6efhntt0pk4icaao6ih4 FOREIGN KEY (expiring_registration_id) REFERENCES public.fixed_shift_registrations(registration_id);


--
-- Name: patient_plan_items fko7frdk5uy178dv98767g1mv4e; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patient_plan_items
    ADD CONSTRAINT fko7frdk5uy178dv98767g1mv4e FOREIGN KEY (service_id) REFERENCES public.services(service_id);


--
-- Name: patient_plan_items fkocg85x8p1kd7xujsrkhnc8deo; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patient_plan_items
    ADD CONSTRAINT fkocg85x8p1kd7xujsrkhnc8deo FOREIGN KEY (assigned_doctor_id) REFERENCES public.employees(employee_id);


--
-- Name: item_price_history fkolhy1ytna8sigbai45v8jutal; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.item_price_history
    ADD CONSTRAINT fkolhy1ytna8sigbai45v8jutal FOREIGN KEY (supplier_id) REFERENCES public.suppliers(supplier_id);


--
-- Name: service_consumables fkotl1cj30icmc113lw2nwqf03v; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.service_consumables
    ADD CONSTRAINT fkotl1cj30icmc113lw2nwqf03v FOREIGN KEY (item_master_id) REFERENCES public.item_masters(item_master_id);


--
-- Name: service_dependencies fkovopbib4xy8ynrv0o6lt8e0gt; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.service_dependencies
    ADD CONSTRAINT fkovopbib4xy8ynrv0o6lt8e0gt FOREIGN KEY (service_id) REFERENCES public.services(service_id);


--
-- Name: storage_transactions fkow98a4xx5oxsgeivgmbrsnjjt; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.storage_transactions
    ADD CONSTRAINT fkow98a4xx5oxsgeivgmbrsnjjt FOREIGN KEY (supplier_id) REFERENCES public.suppliers(supplier_id);


--
-- Name: leave_balance_history fkp31vsi6ty3ug5ow7874rx27u5; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.leave_balance_history
    ADD CONSTRAINT fkp31vsi6ty3ug5ow7874rx27u5 FOREIGN KEY (changed_by) REFERENCES public.employees(employee_id);


--
-- Name: employee_leave_balances fkp4equ7plic3x8scjh1tg04r87; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_leave_balances
    ADD CONSTRAINT fkp4equ7plic3x8scjh1tg04r87 FOREIGN KEY (time_off_type_id) REFERENCES public.time_off_types(type_id);


--
-- Name: storage_transaction_items fkp9eeejmsih3rjbts5e1bpmdc4; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.storage_transaction_items
    ADD CONSTRAINT fkp9eeejmsih3rjbts5e1bpmdc4 FOREIGN KEY (transaction_id) REFERENCES public.storage_transactions(transaction_id);


--
-- Name: clinical_prescriptions fkpn7ifigquqr0exwh5l39hu8u4; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.clinical_prescriptions
    ADD CONSTRAINT fkpn7ifigquqr0exwh5l39hu8u4 FOREIGN KEY (clinical_record_id) REFERENCES public.clinical_records(clinical_record_id);


--
-- Name: fixed_registration_days fkq7no3do6viouhy6e86mi9pulf; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fixed_registration_days
    ADD CONSTRAINT fkq7no3do6viouhy6e86mi9pulf FOREIGN KEY (registration_id) REFERENCES public.fixed_shift_registrations(registration_id);


--
-- Name: item_categories fkq8qknackwsg7oxam82pmshtr8; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.item_categories
    ADD CONSTRAINT fkq8qknackwsg7oxam82pmshtr8 FOREIGN KEY (parent_category_id) REFERENCES public.item_categories(category_id);


--
-- Name: item_batches fkqtgftfnqn382d9p28wu8al84f; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.item_batches
    ADD CONSTRAINT fkqtgftfnqn382d9p28wu8al84f FOREIGN KEY (supplier_id) REFERENCES public.suppliers(supplier_id);


--
-- Name: time_off_requests fkqy09t9bsvuxvnhfuxq316aapi; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.time_off_requests
    ADD CONSTRAINT fkqy09t9bsvuxvnhfuxq316aapi FOREIGN KEY (requested_by) REFERENCES public.employees(employee_id);


--
-- Name: service_consumables fkr3p7g5gxn0g95rh97kkt22ms7; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.service_consumables
    ADD CONSTRAINT fkr3p7g5gxn0g95rh97kkt22ms7 FOREIGN KEY (unit_id) REFERENCES public.item_units(unit_id);


--
-- Name: payments fkrbqec6be74wab8iifh8g3i50i; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT fkrbqec6be74wab8iifh8g3i50i FOREIGN KEY (invoice_id) REFERENCES public.invoices(invoice_id);


--
-- Name: room_services fkrvoqlh9yqrup1v41ejfevv6po; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.room_services
    ADD CONSTRAINT fkrvoqlh9yqrup1v41ejfevv6po FOREIGN KEY (service_id) REFERENCES public.services(service_id);


--
-- Name: appointments fkrye5bav6bpraflmatmsoe0mvl; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.appointments
    ADD CONSTRAINT fkrye5bav6bpraflmatmsoe0mvl FOREIGN KEY (rescheduled_to_appointment_id) REFERENCES public.appointments(appointment_id);


--
-- Name: plan_audit_logs fksixohp3danuip5ae5y53th6v; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.plan_audit_logs
    ADD CONSTRAINT fksixohp3danuip5ae5y53th6v FOREIGN KEY (performed_by) REFERENCES public.employees(employee_id);


--
-- Name: appointment_participants fkslhami0fu7hf92rpkcl487eny; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.appointment_participants
    ADD CONSTRAINT fkslhami0fu7hf92rpkcl487eny FOREIGN KEY (employee_id) REFERENCES public.employees(employee_id);


--
-- Name: appointments fksrj14pej73iuvaddesj2lm4m1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.appointments
    ADD CONSTRAINT fksrj14pej73iuvaddesj2lm4m1 FOREIGN KEY (created_by) REFERENCES public.employees(employee_id);


--
-- Name: permissions fkt19537okhho643n2k2rm7hndm; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.permissions
    ADD CONSTRAINT fkt19537okhho643n2k2rm7hndm FOREIGN KEY (parent_permission_id) REFERENCES public.permissions(permission_id);


--
-- Name: appointment_plan_items fkt2lc11xym9jc2hvdowlxp4kkv; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.appointment_plan_items
    ADD CONSTRAINT fkt2lc11xym9jc2hvdowlxp4kkv FOREIGN KEY (appointment_id) REFERENCES public.appointments(appointment_id);


--
-- Name: accounts fkt3wava8ssfdspnh3hg4col3m1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.accounts
    ADD CONSTRAINT fkt3wava8ssfdspnh3hg4col3m1 FOREIGN KEY (role_id) REFERENCES public.roles(role_id);


--
-- Name: procedure_material_usage fkt6173ujk3lq6u2r3md07090d3; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.procedure_material_usage
    ADD CONSTRAINT fkt6173ujk3lq6u2r3md07090d3 FOREIGN KEY (unit_id) REFERENCES public.item_units(unit_id);


--
-- Name: procedure_material_usage fktd2b4243j63pau8mkaf9bpmkw; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.procedure_material_usage
    ADD CONSTRAINT fktd2b4243j63pau8mkaf9bpmkw FOREIGN KEY (procedure_id) REFERENCES public.clinical_record_procedures(procedure_id);


--
-- Name: part_time_slots fktelqols1hhqkj82e96gf4jtx0; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.part_time_slots
    ADD CONSTRAINT fktelqols1hhqkj82e96gf4jtx0 FOREIGN KEY (work_shift_id) REFERENCES public.work_shifts(work_shift_id);


--
-- Name: appointment_plan_items fktln80juhqea06cl5g1hkd8hlh; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.appointment_plan_items
    ADD CONSTRAINT fktln80juhqea06cl5g1hkd8hlh FOREIGN KEY (item_id) REFERENCES public.patient_plan_items(item_id);


--
-- Name: warehouse_audit_logs fktnl82ks7fo5qt4qrsnsx730hw; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.warehouse_audit_logs
    ADD CONSTRAINT fktnl82ks7fo5qt4qrsnsx730hw FOREIGN KEY (batch_id) REFERENCES public.item_batches(batch_id);


--
-- Name: customer_contacts fktqyj7nws0yv5oxa32j2nrug4n; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.customer_contacts
    ADD CONSTRAINT fktqyj7nws0yv5oxa32j2nrug4n FOREIGN KEY (converted_patient_id) REFERENCES public.patients(patient_id);


--
-- Table: appointment_feedbacks
-- Đánh giá lịch hẹn (Appointment Feedback Module)
--

CREATE TABLE public.appointment_feedbacks (
    feedback_id BIGSERIAL PRIMARY KEY,
    appointment_code VARCHAR(50) NOT NULL UNIQUE,
    patient_id INTEGER NOT NULL,
    rating SMALLINT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    tags JSON,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

--
-- Indexes for appointment_feedbacks
--

CREATE INDEX idx_feedback_appointment ON public.appointment_feedbacks USING btree (appointment_code);
CREATE INDEX idx_feedback_patient ON public.appointment_feedbacks USING btree (patient_id);
CREATE INDEX idx_feedback_rating ON public.appointment_feedbacks USING btree (rating);

--
-- Foreign Keys for appointment_feedbacks
--

ALTER TABLE ONLY public.appointment_feedbacks
    ADD CONSTRAINT fk_feedback_appointment FOREIGN KEY (appointment_code) REFERENCES public.appointments(appointment_code) ON DELETE CASCADE;

ALTER TABLE ONLY public.appointment_feedbacks
    ADD CONSTRAINT fk_feedback_patient FOREIGN KEY (patient_id) REFERENCES public.patients(patient_id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

