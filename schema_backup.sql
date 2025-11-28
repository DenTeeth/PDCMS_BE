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
    'CHECKED_IN',
    'COMPLETED',
    'IN_PROGRESS',
    'NO_SHOW',
    'SCHEDULED'
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


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: account_verification_tokens; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.account_verification_tokens (
    token_id character varying(50) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    expires_at timestamp(6) without time zone NOT NULL,
    token character varying(100) NOT NULL,
    verified_at timestamp(6) without time zone,
    account_id integer NOT NULL
);


--
-- Name: accounts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.accounts (
    account_id integer NOT NULL,
    account_code character varying(20),
    created_at timestamp(6) without time zone,
    email character varying(100) NOT NULL,
    is_email_verified boolean,
    must_change_password boolean,
    password character varying(255) NOT NULL,
    password_changed_at timestamp(6) without time zone,
    status character varying(255),
    username character varying(50) NOT NULL,
    role_id character varying(50) NOT NULL,
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
    actual_end_time timestamp(6) without time zone,
    actual_start_time timestamp(6) without time zone,
    appointment_code character varying(20) NOT NULL,
    appointment_end_time timestamp(6) without time zone NOT NULL,
    appointment_start_time timestamp(6) without time zone NOT NULL,
    created_at timestamp(6) without time zone,
    created_by integer,
    employee_id integer NOT NULL,
    expected_duration_minutes integer NOT NULL,
    notes text,
    patient_id integer NOT NULL,
    room_id character varying(50) NOT NULL,
    status character varying(255) NOT NULL,
    updated_at timestamp(6) without time zone,
    rescheduled_to_appointment_id integer,
    CONSTRAINT appointments_status_check CHECK (((status)::text = ANY ((ARRAY['SCHEDULED'::character varying, 'CHECKED_IN'::character varying, 'IN_PROGRESS'::character varying, 'COMPLETED'::character varying, 'CANCELLED'::character varying, 'NO_SHOW'::character varying])::text[])))
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
    base_role_name character varying(50) NOT NULL,
    created_at timestamp(6) without time zone,
    description text,
    is_active boolean,
    updated_at timestamp(6) without time zone
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
    token_hash character varying(512) NOT NULL,
    blacklisted_at timestamp(6) without time zone NOT NULL,
    expires_at timestamp(6) without time zone NOT NULL,
    reason character varying(50),
    account_id integer
);


--
-- Name: contact_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.contact_history (
    history_id character varying(20) NOT NULL,
    action character varying(20) NOT NULL,
    contact_id character varying(20) NOT NULL,
    content text NOT NULL,
    created_at timestamp(6) without time zone,
    employee_id integer,
    CONSTRAINT contact_history_action_check CHECK (((action)::text = ANY ((ARRAY['CALL'::character varying, 'MESSAGE'::character varying, 'NOTE'::character varying])::text[])))
);


--
-- Name: customer_contacts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.customer_contacts (
    contact_id character varying(20) NOT NULL,
    assigned_to integer,
    converted_patient_id integer,
    created_at timestamp(6) without time zone,
    email character varying(100),
    full_name character varying(100) NOT NULL,
    message text,
    notes text,
    phone character varying(15) NOT NULL,
    service_interested character varying(100),
    source character varying(20),
    status character varying(20),
    updated_at timestamp(6) without time zone,
    CONSTRAINT customer_contacts_source_check CHECK (((source)::text = ANY ((ARRAY['WEBSITE'::character varying, 'FACEBOOK'::character varying, 'ZALO'::character varying, 'WALK_IN'::character varying, 'REFERRAL'::character varying])::text[]))),
    CONSTRAINT customer_contacts_status_check CHECK (((status)::text = ANY ((ARRAY['NEW'::character varying, 'CONTACTED'::character varying, 'APPOINTMENT_SET'::character varying, 'NOT_INTERESTED'::character varying, 'CONVERTED'::character varying])::text[])))
);


--
-- Name: employee_leave_balances; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.employee_leave_balances (
    balance_id bigint NOT NULL,
    employee_id integer NOT NULL,
    notes text,
    time_off_type_id character varying(50) NOT NULL,
    total_days_allowed double precision NOT NULL,
    updated_at timestamp(6) without time zone,
    days_taken double precision NOT NULL,
    cycle_year integer NOT NULL
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
    registration_id character varying(20) NOT NULL,
    effective_from date NOT NULL,
    effective_to date,
    employee_id integer NOT NULL,
    is_active boolean DEFAULT true,
    part_time_slot_id bigint NOT NULL
);


--
-- Name: employee_shifts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.employee_shifts (
    employee_shift_id character varying(20) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    created_by integer,
    is_overtime boolean NOT NULL,
    notes text,
    source character varying(20) NOT NULL,
    source_off_request_id character varying(20),
    source_ot_request_id character varying(20),
    source_registration_id bigint,
    status character varying(20) NOT NULL,
    updated_at timestamp(6) without time zone,
    work_date date NOT NULL,
    employee_id integer NOT NULL,
    work_shift_id character varying(20) NOT NULL,
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
    employee_id integer NOT NULL,
    address text,
    created_at timestamp(6) without time zone,
    date_of_birth date,
    employee_code character varying(20),
    employment_type character varying(255),
    first_name character varying(50) NOT NULL,
    is_active boolean,
    last_name character varying(50) NOT NULL,
    phone character varying(15),
    account_id integer NOT NULL,
    CONSTRAINT employees_employment_type_check CHECK (((employment_type)::text = ANY ((ARRAY['FULL_TIME'::character varying, 'PART_TIME_FIXED'::character varying, 'PART_TIME_FLEX'::character varying])::text[])))
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
    day_of_week character varying(10) NOT NULL,
    registration_id integer NOT NULL
);


--
-- Name: fixed_shift_registrations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.fixed_shift_registrations (
    registration_id integer NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    effective_from date NOT NULL,
    effective_to date,
    is_active boolean NOT NULL,
    updated_at timestamp(6) without time zone,
    employee_id integer NOT NULL,
    work_shift_id character varying(20) NOT NULL
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
    definition_id character varying(20) NOT NULL,
    holiday_date date NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    description character varying(500),
    updated_at timestamp(6) without time zone
);


--
-- Name: holiday_definitions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.holiday_definitions (
    definition_id character varying(20) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    description character varying(500),
    holiday_name character varying(100) NOT NULL,
    holiday_type character varying(20) NOT NULL,
    updated_at timestamp(6) without time zone,
    CONSTRAINT holiday_definitions_holiday_type_check CHECK (((holiday_type)::text = ANY ((ARRAY['NATIONAL'::character varying, 'COMPANY'::character varying])::text[])))
);


--
-- Name: item_batches; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.item_batches (
    batch_id bigint NOT NULL,
    bin_location character varying(50),
    created_at timestamp(6) without time zone NOT NULL,
    expiry_date date NOT NULL,
    imported_at timestamp(6) without time zone NOT NULL,
    initial_quantity integer,
    is_unpacked boolean,
    lot_number character varying(100) NOT NULL,
    quantity_on_hand integer NOT NULL,
    unpacked_at timestamp(6) without time zone,
    unpacked_by_transaction_id bigint,
    updated_at timestamp(6) without time zone,
    item_master_id bigint NOT NULL,
    parent_batch_id bigint,
    supplier_id bigint
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
    category_id bigint NOT NULL,
    category_code character varying(50) NOT NULL,
    category_name character varying(255) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    description text,
    is_active boolean NOT NULL,
    updated_at timestamp(6) without time zone,
    parent_category_id bigint
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
    item_master_id bigint NOT NULL,
    cached_last_import_date timestamp(6) without time zone,
    cached_last_updated timestamp(6) without time zone,
    cached_total_quantity integer,
    created_at timestamp(6) without time zone NOT NULL,
    current_market_price numeric(15,2),
    default_shelf_life_days integer,
    description text,
    is_active boolean NOT NULL,
    is_prescription_required boolean NOT NULL,
    is_tool boolean NOT NULL,
    item_code character varying(50) NOT NULL,
    item_name character varying(255) NOT NULL,
    max_stock_level integer NOT NULL,
    min_stock_level integer NOT NULL,
    unit_of_measure character varying(50),
    updated_at timestamp(6) without time zone,
    warehouse_type character varying(255) NOT NULL,
    category_id bigint,
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
    history_id bigint NOT NULL,
    effective_date date NOT NULL,
    new_import_price numeric(15,2),
    notes text,
    old_import_price numeric(15,2),
    item_master_id bigint NOT NULL,
    supplier_id bigint NOT NULL
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
    unit_id bigint NOT NULL,
    conversion_rate integer NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    display_order integer,
    is_active boolean NOT NULL,
    is_base_unit boolean NOT NULL,
    is_default_export_unit boolean NOT NULL,
    is_default_import_unit boolean NOT NULL,
    unit_name character varying(50) NOT NULL,
    updated_at timestamp(6) without time zone,
    item_master_id bigint NOT NULL
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
    history_id bigint NOT NULL,
    balance_id bigint NOT NULL,
    change_amount double precision NOT NULL,
    changed_by integer NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    notes text,
    reason character varying(50) NOT NULL,
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
-- Name: overtime_requests; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.overtime_requests (
    request_id character varying(12) NOT NULL,
    approved_at timestamp(6) without time zone,
    cancellation_reason text,
    created_at timestamp(6) without time zone NOT NULL,
    reason text NOT NULL,
    rejected_reason text,
    status character varying(20) NOT NULL,
    work_date date NOT NULL,
    approved_by integer,
    employee_id integer NOT NULL,
    requested_by integer NOT NULL,
    work_shift_id character varying(20) NOT NULL,
    CONSTRAINT overtime_requests_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying, 'CANCELLED'::character varying])::text[])))
);


--
-- Name: part_time_registration_dates; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.part_time_registration_dates (
    registration_id integer NOT NULL,
    registered_date date
);


--
-- Name: part_time_registrations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.part_time_registrations (
    registration_id integer NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    effective_from date NOT NULL,
    effective_to date NOT NULL,
    employee_id integer NOT NULL,
    is_active boolean NOT NULL,
    part_time_slot_id bigint NOT NULL,
    processed_at timestamp(6) without time zone,
    processed_by integer,
    reason text,
    status character varying(20) NOT NULL,
    updated_at timestamp(6) without time zone,
    version bigint NOT NULL,
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
    slot_id bigint NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    day_of_week character varying(255) NOT NULL,
    effective_from date NOT NULL,
    effective_to date NOT NULL,
    is_active boolean NOT NULL,
    quota integer NOT NULL,
    updated_at timestamp(6) without time zone,
    work_shift_id character varying(20) NOT NULL
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
    token_id character varying(50) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    expires_at timestamp(6) without time zone NOT NULL,
    token character varying(100) NOT NULL,
    used_at timestamp(6) without time zone,
    account_id integer NOT NULL
);


--
-- Name: patient_plan_items; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.patient_plan_items (
    item_id bigint NOT NULL,
    completed_at timestamp(6) without time zone,
    created_at timestamp(6) without time zone NOT NULL,
    estimated_time_minutes integer,
    item_name character varying(255) NOT NULL,
    price numeric(10,2),
    sequence_number integer NOT NULL,
    status character varying(30) NOT NULL,
    price_update_reason character varying(500),
    price_updated_at timestamp(6) without time zone,
    service_id integer NOT NULL,
    phase_id bigint NOT NULL,
    price_updated_by integer,
    CONSTRAINT patient_plan_items_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'READY_FOR_BOOKING'::character varying, 'SCHEDULED'::character varying, 'IN_PROGRESS'::character varying, 'COMPLETED'::character varying])::text[])))
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
    patient_phase_id bigint NOT NULL,
    completion_date date,
    created_at timestamp(6) without time zone NOT NULL,
    phase_name character varying(255) NOT NULL,
    phase_number integer NOT NULL,
    start_date date,
    status character varying(20),
    estimated_duration_days integer,
    plan_id bigint NOT NULL,
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
-- Name: patient_treatment_plans; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.patient_treatment_plans (
    plan_id bigint NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    created_by integer,
    expected_end_date date,
    patient_id integer NOT NULL,
    plan_code character varying(50) NOT NULL,
    plan_name character varying(255) NOT NULL,
    start_date date,
    status character varying(20),
    template_id bigint,
    total_price numeric(12,2),
    approval_status character varying(20) NOT NULL,
    approved_at timestamp(6) without time zone,
    discount_amount numeric(12,2),
    final_cost numeric(12,2),
    patient_consent_date timestamp(6) without time zone,
    payment_type character varying(20),
    rejection_reason text,
    approved_by integer,
    specialization_id integer,
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
-- Name: patients; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.patients (
    patient_id integer NOT NULL,
    address text,
    allergies text,
    created_at timestamp(6) without time zone,
    date_of_birth date,
    email character varying(100),
    emergency_contact_name character varying(100),
    emergency_contact_phone character varying(15),
    first_name character varying(50) NOT NULL,
    gender character varying(10),
    is_active boolean,
    last_name character varying(50) NOT NULL,
    medical_history text,
    patient_code character varying(20),
    phone character varying(15),
    updated_at timestamp(6) without time zone,
    account_id integer,
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
-- Name: permissions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.permissions (
    permission_id character varying(30) NOT NULL,
    created_at timestamp(6) without time zone,
    description text,
    display_order integer,
    is_active boolean,
    module character varying(20) NOT NULL,
    permission_name character varying(100) NOT NULL,
    parent_permission_id character varying(30)
);


--
-- Name: plan_audit_logs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.plan_audit_logs (
    log_id bigint NOT NULL,
    action_type character varying(50) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    new_approval_status character varying(20),
    notes text,
    old_approval_status character varying(20),
    performed_by integer NOT NULL,
    plan_id bigint NOT NULL,
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
-- Name: refresh_tokens; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.refresh_tokens (
    id character varying(36) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    expires_at timestamp(6) without time zone NOT NULL,
    is_active boolean NOT NULL,
    token_hash character varying(512) NOT NULL,
    updated_at timestamp(6) without time zone,
    account_id integer NOT NULL
);


--
-- Name: role_permissions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.role_permissions (
    role_id character varying(50) NOT NULL,
    permission_id character varying(30) NOT NULL
);


--
-- Name: roles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.roles (
    role_id character varying(50) NOT NULL,
    created_at timestamp(6) without time zone,
    description text,
    is_active boolean,
    requires_specialization boolean,
    role_name character varying(50) NOT NULL,
    base_role_id integer NOT NULL
);


--
-- Name: room_services; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.room_services (
    created_at timestamp(6) without time zone NOT NULL,
    room_id character varying(255) NOT NULL,
    service_id integer NOT NULL
);


--
-- Name: rooms; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.rooms (
    room_id character varying(50) NOT NULL,
    created_at timestamp(6) without time zone,
    is_active boolean,
    room_code character varying(20) NOT NULL,
    room_name character varying(100) NOT NULL,
    room_type character varying(50)
);


--
-- Name: service_categories; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.service_categories (
    category_id bigint NOT NULL,
    category_code character varying(50) NOT NULL,
    category_name character varying(255) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    description text,
    display_order integer NOT NULL,
    is_active boolean NOT NULL,
    updated_at timestamp(6) without time zone
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
    link_id bigint NOT NULL,
    notes text,
    quantity_per_service numeric(10,2) NOT NULL,
    service_id bigint NOT NULL,
    item_master_id bigint NOT NULL,
    unit_id bigint NOT NULL
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
    dependency_id bigint NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    min_days_apart integer,
    receptionist_note text,
    rule_type character varying(30) NOT NULL,
    dependent_service_id bigint NOT NULL,
    service_id bigint NOT NULL,
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
    service_id integer NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    default_buffer_minutes integer NOT NULL,
    default_duration_minutes integer NOT NULL,
    description text,
    display_order integer NOT NULL,
    is_active boolean NOT NULL,
    price numeric(15,2) NOT NULL,
    service_code character varying(50) NOT NULL,
    service_name character varying(255) NOT NULL,
    updated_at timestamp(6) without time zone,
    category_id bigint,
    specialization_id integer,
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
    renewal_id character varying(20) NOT NULL,
    confirmed_at timestamp(6) without time zone,
    created_at timestamp(6) without time zone NOT NULL,
    decline_reason text,
    expires_at timestamp(6) without time zone NOT NULL,
    status character varying(20) NOT NULL,
    updated_at timestamp(6) without time zone,
    employee_id integer NOT NULL,
    expiring_registration_id integer NOT NULL,
    CONSTRAINT shift_renewal_requests_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING_ACTION'::character varying, 'CONFIRMED'::character varying, 'FINALIZED'::character varying, 'DECLINED'::character varying, 'EXPIRED'::character varying])::text[])))
);


--
-- Name: specializations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.specializations (
    specialization_id integer NOT NULL,
    created_at timestamp(6) without time zone,
    description text,
    is_active boolean,
    specialization_code character varying(20) NOT NULL,
    specialization_name character varying(100) NOT NULL
);


--
-- Name: storage_transaction_items; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.storage_transaction_items (
    transaction_item_id bigint NOT NULL,
    item_code character varying(50),
    notes text,
    price numeric(15,2),
    quantity_change integer NOT NULL,
    total_line_value numeric(15,2),
    batch_id bigint NOT NULL,
    transaction_id bigint NOT NULL,
    unit_id bigint
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
    transaction_id bigint NOT NULL,
    approval_status character varying(20),
    approved_at timestamp(6) without time zone,
    cancellation_reason text,
    cancelled_at timestamp(6) without time zone,
    created_at timestamp(6) without time zone NOT NULL,
    department_name character varying(200),
    due_date date,
    expected_delivery_date date,
    export_type character varying(20),
    invoice_number character varying(100),
    notes text,
    paid_amount numeric(15,2),
    payment_status character varying(20),
    reference_code character varying(100),
    rejected_at timestamp(6) without time zone,
    rejection_reason text,
    remaining_debt numeric(15,2),
    requested_by character varying(200),
    status character varying(20),
    total_value numeric(15,2),
    transaction_code character varying(50) NOT NULL,
    transaction_date timestamp(6) without time zone NOT NULL,
    transaction_type character varying(255) NOT NULL,
    approved_by_id integer,
    cancelled_by_id integer,
    created_by integer,
    rejected_by_id integer,
    related_appointment_id integer,
    supplier_id bigint,
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
    supplier_item_id bigint NOT NULL,
    is_preferred boolean NOT NULL,
    last_purchase_date timestamp(6) without time zone,
    supplier_item_code character varying(100),
    item_master_id bigint NOT NULL,
    supplier_id bigint NOT NULL
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
    supplier_id bigint NOT NULL,
    address text,
    created_at timestamp(6) without time zone NOT NULL,
    email character varying(100),
    is_active boolean NOT NULL,
    last_order_date date,
    notes text,
    phone_number character varying(20),
    rating_score numeric(3,1),
    supplier_code character varying(50) NOT NULL,
    supplier_name character varying(255) NOT NULL,
    tier_level character varying(255) NOT NULL,
    total_orders integer,
    updated_at timestamp(6) without time zone,
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
    phase_service_id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    estimated_time_minutes integer,
    quantity integer NOT NULL,
    sequence_number integer NOT NULL,
    updated_at timestamp(6) without time zone,
    service_id bigint NOT NULL,
    phase_id bigint NOT NULL
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
    phase_id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    estimated_duration_days integer,
    phase_name character varying(255) NOT NULL,
    phase_number integer NOT NULL,
    updated_at timestamp(6) without time zone,
    template_id bigint NOT NULL
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
    request_id character varying(50) NOT NULL,
    approved_at timestamp(6) without time zone,
    approved_by integer,
    cancellation_reason text,
    employee_id integer NOT NULL,
    end_date date NOT NULL,
    reason text,
    rejected_reason text,
    requested_at timestamp(6) without time zone NOT NULL,
    requested_by integer NOT NULL,
    start_date date NOT NULL,
    status character varying(20) NOT NULL,
    time_off_type_id character varying(50) NOT NULL,
    work_shift_id character varying(50),
    CONSTRAINT time_off_requests_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying, 'CANCELLED'::character varying])::text[])))
);


--
-- Name: time_off_types; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.time_off_types (
    type_id character varying(50) NOT NULL,
    default_days_per_year double precision,
    description text,
    is_active boolean NOT NULL,
    is_paid boolean NOT NULL,
    requires_approval boolean NOT NULL,
    requires_balance boolean NOT NULL,
    type_code character varying(50) NOT NULL,
    type_name character varying(100) NOT NULL
);


--
-- Name: treatment_plan_templates; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.treatment_plan_templates (
    template_id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    description text,
    estimated_duration_days integer,
    is_active boolean,
    template_code character varying(50) NOT NULL,
    template_name character varying(255) NOT NULL,
    total_price numeric(12,2),
    updated_at timestamp(6) without time zone,
    specialization_id integer
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
-- Name: warehouse_audit_logs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.warehouse_audit_logs (
    log_id bigint NOT NULL,
    action_type character varying(255) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    new_value text,
    old_value text,
    reason text,
    batch_id bigint,
    item_master_id bigint,
    performed_by integer,
    transaction_id bigint,
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
    work_shift_id character varying(20) NOT NULL,
    category character varying(20) NOT NULL,
    end_time time(6) without time zone NOT NULL,
    is_active boolean NOT NULL,
    shift_name character varying(100) NOT NULL,
    start_time time(6) without time zone NOT NULL,
    CONSTRAINT work_shifts_category_check CHECK (((category)::text = ANY ((ARRAY['NORMAL'::character varying, 'NIGHT'::character varying])::text[])))
);


--
-- Name: accounts account_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.accounts ALTER COLUMN account_id SET DEFAULT nextval('public.accounts_account_id_seq'::regclass);


--
-- Name: appointments appointment_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.appointments ALTER COLUMN appointment_id SET DEFAULT nextval('public.appointments_appointment_id_seq'::regclass);


--
-- Name: base_roles base_role_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.base_roles ALTER COLUMN base_role_id SET DEFAULT nextval('public.base_roles_base_role_id_seq'::regclass);


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
-- Name: part_time_registrations registration_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.part_time_registrations ALTER COLUMN registration_id SET DEFAULT nextval('public.part_time_registrations_registration_id_seq'::regclass);


--
-- Name: part_time_slots slot_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.part_time_slots ALTER COLUMN slot_id SET DEFAULT nextval('public.part_time_slots_slot_id_seq'::regclass);


--
-- Name: patient_plan_items item_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patient_plan_items ALTER COLUMN item_id SET DEFAULT nextval('public.patient_plan_items_item_id_seq'::regclass);


--
-- Name: patient_plan_phases patient_phase_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patient_plan_phases ALTER COLUMN patient_phase_id SET DEFAULT nextval('public.patient_plan_phases_patient_phase_id_seq'::regclass);


--
-- Name: patient_treatment_plans plan_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patient_treatment_plans ALTER COLUMN plan_id SET DEFAULT nextval('public.patient_treatment_plans_plan_id_seq'::regclass);


--
-- Name: patients patient_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patients ALTER COLUMN patient_id SET DEFAULT nextval('public.patients_patient_id_seq'::regclass);


--
-- Name: plan_audit_logs log_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.plan_audit_logs ALTER COLUMN log_id SET DEFAULT nextval('public.plan_audit_logs_log_id_seq'::regclass);


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
-- Name: warehouse_audit_logs log_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.warehouse_audit_logs ALTER COLUMN log_id SET DEFAULT nextval('public.warehouse_audit_logs_log_id_seq'::regclass);


--
-- Name: account_verification_tokens account_verification_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.account_verification_tokens
    ADD CONSTRAINT account_verification_tokens_pkey PRIMARY KEY (token_id);


--
-- Name: accounts accounts_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.accounts
    ADD CONSTRAINT accounts_pkey PRIMARY KEY (account_id);


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
-- Name: appointments appointments_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.appointments
    ADD CONSTRAINT appointments_pkey PRIMARY KEY (appointment_id);


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
-- Name: employees employees_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employees
    ADD CONSTRAINT employees_pkey PRIMARY KEY (employee_id);


--
-- Name: fixed_registration_days fixed_registration_days_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fixed_registration_days
    ADD CONSTRAINT fixed_registration_days_pkey PRIMARY KEY (day_of_week, registration_id);


--
-- Name: fixed_shift_registrations fixed_shift_registrations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fixed_shift_registrations
    ADD CONSTRAINT fixed_shift_registrations_pkey PRIMARY KEY (registration_id);


--
-- Name: holiday_dates holiday_dates_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.holiday_dates
    ADD CONSTRAINT holiday_dates_pkey PRIMARY KEY (definition_id, holiday_date);


--
-- Name: holiday_definitions holiday_definitions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.holiday_definitions
    ADD CONSTRAINT holiday_definitions_pkey PRIMARY KEY (definition_id);


--
-- Name: item_batches item_batches_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.item_batches
    ADD CONSTRAINT item_batches_pkey PRIMARY KEY (batch_id);


--
-- Name: item_categories item_categories_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.item_categories
    ADD CONSTRAINT item_categories_pkey PRIMARY KEY (category_id);


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
-- Name: patient_treatment_plans patient_treatment_plans_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patient_treatment_plans
    ADD CONSTRAINT patient_treatment_plans_pkey PRIMARY KEY (plan_id);


--
-- Name: patients patients_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patients
    ADD CONSTRAINT patients_pkey PRIMARY KEY (patient_id);


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
-- Name: refresh_tokens refresh_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT refresh_tokens_pkey PRIMARY KEY (id);


--
-- Name: role_permissions role_permissions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.role_permissions
    ADD CONSTRAINT role_permissions_pkey PRIMARY KEY (role_id, permission_id);


--
-- Name: roles roles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (role_id);


--
-- Name: room_services room_services_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.room_services
    ADD CONSTRAINT room_services_pkey PRIMARY KEY (room_id, service_id);


--
-- Name: rooms rooms_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rooms
    ADD CONSTRAINT rooms_pkey PRIMARY KEY (room_id);


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
-- Name: storage_transaction_items storage_transaction_items_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.storage_transaction_items
    ADD CONSTRAINT storage_transaction_items_pkey PRIMARY KEY (transaction_item_id);


--
-- Name: storage_transactions storage_transactions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.storage_transactions
    ADD CONSTRAINT storage_transactions_pkey PRIMARY KEY (transaction_id);


--
-- Name: supplier_items supplier_items_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.supplier_items
    ADD CONSTRAINT supplier_items_pkey PRIMARY KEY (supplier_item_id);


--
-- Name: suppliers suppliers_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.suppliers
    ADD CONSTRAINT suppliers_pkey PRIMARY KEY (supplier_id);


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
-- Name: treatment_plan_templates treatment_plan_templates_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.treatment_plan_templates
    ADD CONSTRAINT treatment_plan_templates_pkey PRIMARY KEY (template_id);


--
-- Name: supplier_items uk7iydyfqgsfkd172dm0uw53jxj; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.supplier_items
    ADD CONSTRAINT uk7iydyfqgsfkd172dm0uw53jxj UNIQUE (supplier_id, item_master_id);


--
-- Name: item_masters uk_194fmu6u594r1b3forn13eot4; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.item_masters
    ADD CONSTRAINT uk_194fmu6u594r1b3forn13eot4 UNIQUE (item_code);


--
-- Name: storage_transactions uk_5dpr2q29n8okg90fcwyhbmmdr; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.storage_transactions
    ADD CONSTRAINT uk_5dpr2q29n8okg90fcwyhbmmdr UNIQUE (invoice_number);


--
-- Name: password_reset_tokens uk_71lqwbwtklmljk3qlsugr1mig; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.password_reset_tokens
    ADD CONSTRAINT uk_71lqwbwtklmljk3qlsugr1mig UNIQUE (token);


--
-- Name: base_roles uk_7u3dri0cxmwel1xkqugsyih7e; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.base_roles
    ADD CONSTRAINT uk_7u3dri0cxmwel1xkqugsyih7e UNIQUE (base_role_name);


--
-- Name: appointments uk_88w59a3uq8pvoxypr2ejldy0h; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.appointments
    ADD CONSTRAINT uk_88w59a3uq8pvoxypr2ejldy0h UNIQUE (appointment_code);


--
-- Name: treatment_plan_templates uk_a11nbk68pswirkk5dgntlm2og; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.treatment_plan_templates
    ADD CONSTRAINT uk_a11nbk68pswirkk5dgntlm2og UNIQUE (template_code);


--
-- Name: rooms uk_ejc4trkinbxtajwetru2o8kdo; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rooms
    ADD CONSTRAINT uk_ejc4trkinbxtajwetru2o8kdo UNIQUE (room_code);


--
-- Name: employee_shifts uk_employee_date_shift; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_shifts
    ADD CONSTRAINT uk_employee_date_shift UNIQUE (employee_id, work_date, work_shift_id);


--
-- Name: employees uk_etqhw9qqnad1kyjq3ks1glw8x; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employees
    ADD CONSTRAINT uk_etqhw9qqnad1kyjq3ks1glw8x UNIQUE (employee_code);


--
-- Name: time_off_types uk_gqd3dggybwsufrnug74pgub35; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.time_off_types
    ADD CONSTRAINT uk_gqd3dggybwsufrnug74pgub35 UNIQUE (type_code);


--
-- Name: employees uk_guw5r3c5brob94evptgkjl9hr; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employees
    ADD CONSTRAINT uk_guw5r3c5brob94evptgkjl9hr UNIQUE (account_id);


--
-- Name: specializations uk_gy8dakj283hqsmg0hneowcylv; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.specializations
    ADD CONSTRAINT uk_gy8dakj283hqsmg0hneowcylv UNIQUE (specialization_code);


--
-- Name: account_verification_tokens uk_i7agin0w2kpuysbdad72x1b68; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.account_verification_tokens
    ADD CONSTRAINT uk_i7agin0w2kpuysbdad72x1b68 UNIQUE (token);


--
-- Name: service_categories uk_iy8o91sdp1j4yf5not3ri1ai; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.service_categories
    ADD CONSTRAINT uk_iy8o91sdp1j4yf5not3ri1ai UNIQUE (category_code);


--
-- Name: accounts uk_k8h1bgqoplx0rkngj01pm1rgp; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.accounts
    ADD CONSTRAINT uk_k8h1bgqoplx0rkngj01pm1rgp UNIQUE (username);


--
-- Name: patients uk_k9r3mhukb7ml6ownhkekh1htj; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patients
    ADD CONSTRAINT uk_k9r3mhukb7ml6ownhkekh1htj UNIQUE (account_id);


--
-- Name: patient_treatment_plans uk_kebtlup89fbk9qbbqa6km0an6; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patient_treatment_plans
    ADD CONSTRAINT uk_kebtlup89fbk9qbbqa6km0an6 UNIQUE (plan_code);


--
-- Name: item_categories uk_kq3psrj0btnokxg1xoh2k4d4b; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.item_categories
    ADD CONSTRAINT uk_kq3psrj0btnokxg1xoh2k4d4b UNIQUE (category_code);


--
-- Name: holiday_definitions uk_ktr3d1k8am4fpie5fjrpa7y15; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.holiday_definitions
    ADD CONSTRAINT uk_ktr3d1k8am4fpie5fjrpa7y15 UNIQUE (holiday_name);


--
-- Name: storage_transactions uk_mpnrbn8h7a4i2953yww9wl39s; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.storage_transactions
    ADD CONSTRAINT uk_mpnrbn8h7a4i2953yww9wl39s UNIQUE (transaction_code);


--
-- Name: accounts uk_n7ihswpy07ci568w34q0oi8he; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.accounts
    ADD CONSTRAINT uk_n7ihswpy07ci568w34q0oi8he UNIQUE (email);


--
-- Name: refresh_tokens uk_o2mlirhldriil2y7krapq4frt; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT uk_o2mlirhldriil2y7krapq4frt UNIQUE (token_hash);


--
-- Name: accounts uk_ohk73u483aagsew869bjtem5j; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.accounts
    ADD CONSTRAINT uk_ohk73u483aagsew869bjtem5j UNIQUE (account_code);


--
-- Name: overtime_requests uk_overtime_employee_date_shift; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.overtime_requests
    ADD CONSTRAINT uk_overtime_employee_date_shift UNIQUE (employee_id, work_date, work_shift_id);


--
-- Name: patients uk_pdu5f0e015icwwcx7otn46rv8; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patients
    ADD CONSTRAINT uk_pdu5f0e015icwwcx7otn46rv8 UNIQUE (patient_code);


--
-- Name: template_phase_services uk_phase_service; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.template_phase_services
    ADD CONSTRAINT uk_phase_service UNIQUE (phase_id, service_id);


--
-- Name: suppliers uk_qlclyj0vn5vwtb86objyhmlkx; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.suppliers
    ADD CONSTRAINT uk_qlclyj0vn5vwtb86objyhmlkx UNIQUE (supplier_code);


--
-- Name: services uk_rm9rfu0ekvjb9y1ff8blnaf0i; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.services
    ADD CONSTRAINT uk_rm9rfu0ekvjb9y1ff8blnaf0i UNIQUE (service_code);


--
-- Name: template_phases uk_template_phase_number; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.template_phases
    ADD CONSTRAINT uk_template_phase_number UNIQUE (template_id, phase_number);


--
-- Name: item_batches ukrw8u3oep5a4b4kkxlsmeyfdjv; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.item_batches
    ADD CONSTRAINT ukrw8u3oep5a4b4kkxlsmeyfdjv UNIQUE (item_master_id, lot_number);


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
-- Name: idx_employee_workdate; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_employee_workdate ON public.employee_shifts USING btree (employee_id, work_date);


--
-- Name: idx_fixed_shift_employee; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fixed_shift_employee ON public.fixed_shift_registrations USING btree (employee_id, work_shift_id, is_active);


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
-- Name: patient_treatment_plans fk1jvc9s12279mmsak8mcc2f823; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patient_treatment_plans
    ADD CONSTRAINT fk1jvc9s12279mmsak8mcc2f823 FOREIGN KEY (patient_id) REFERENCES public.patients(patient_id);


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
-- Name: item_units fk4gco92by1nxi5s1di8213srbe; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.item_units
    ADD CONSTRAINT fk4gco92by1nxi5s1di8213srbe FOREIGN KEY (item_master_id) REFERENCES public.item_masters(item_master_id);


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
-- Name: appointment_services fk68fbfnf0iy7uq0tfb5mjmm2hx; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.appointment_services
    ADD CONSTRAINT fk68fbfnf0iy7uq0tfb5mjmm2hx FOREIGN KEY (service_id) REFERENCES public.services(service_id);


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
-- Name: role_permissions fkegdk29eiy7mdtefy5c7eirr6e; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.role_permissions
    ADD CONSTRAINT fkegdk29eiy7mdtefy5c7eirr6e FOREIGN KEY (permission_id) REFERENCES public.permissions(permission_id);


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
-- Name: employee_specializations fkfot1n1vx9ue3sak3rk6jwkcbp; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_specializations
    ADD CONSTRAINT fkfot1n1vx9ue3sak3rk6jwkcbp FOREIGN KEY (specialization_id) REFERENCES public.specializations(specialization_id);


--
-- Name: overtime_requests fkg5wygdf15wnj0dhc2mgquajh5; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.overtime_requests
    ADD CONSTRAINT fkg5wygdf15wnj0dhc2mgquajh5 FOREIGN KEY (employee_id) REFERENCES public.employees(employee_id);


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
-- Name: storage_transactions fkh7almrtpwfxmn5ea1uavd72xj; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.storage_transactions
    ADD CONSTRAINT fkh7almrtpwfxmn5ea1uavd72xj FOREIGN KEY (approved_by_id) REFERENCES public.employees(employee_id);


--
-- Name: customer_contacts fkj76vyo39v8khrloeu2dwqjmqj; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.customer_contacts
    ADD CONSTRAINT fkj76vyo39v8khrloeu2dwqjmqj FOREIGN KEY (assigned_to) REFERENCES public.employees(employee_id);


--
-- Name: supplier_items fkjxawqjs3rj3yawuwwiw1vmfqw; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.supplier_items
    ADD CONSTRAINT fkjxawqjs3rj3yawuwwiw1vmfqw FOREIGN KEY (item_master_id) REFERENCES public.item_masters(item_master_id);


--
-- Name: warehouse_audit_logs fkk8wcgee0o0nyos172manw0rse; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.warehouse_audit_logs
    ADD CONSTRAINT fkk8wcgee0o0nyos172manw0rse FOREIGN KEY (item_master_id) REFERENCES public.item_masters(item_master_id);


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
-- PostgreSQL database dump complete
--

