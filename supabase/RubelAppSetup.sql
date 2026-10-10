create extension if not exists pgcrypto with schema extensions;

create table if not exists public.pf_exam_users (
    time_id text primary key,
    device_id text not null unique,
    exam text not null,
    mobile text not null,
    email text not null,
    name text not null default '',
    switch text not null default '',
    pay_link text not null default '',
    active_link text not null default '',
    payment_link text not null default '',
    wp_group text not null default ''
);

do $$
begin
    if exists (
        select 1
        from information_schema.columns
        where table_schema = 'public'
            and table_name = 'pf_exam_users'
            and column_name = 'switch'
    ) and not exists (
        select 1
        from information_schema.columns
        where table_schema = 'public'
            and table_name = 'pf_exam_users'
            and column_name = 'active_link'
    ) then
        alter table public.pf_exam_users rename column switch to active_link;
    end if;

    if exists (
        select 1
        from information_schema.columns
        where table_schema = 'public'
            and table_name = 'pf_exam_users'
            and column_name = 'address'
    ) and not exists (
        select 1
        from information_schema.columns
        where table_schema = 'public'
            and table_name = 'pf_exam_users'
            and column_name = 'switch'
    ) then
        alter table public.pf_exam_users rename column address to switch;
    end if;

    if exists (
        select 1
        from information_schema.columns
        where table_schema = 'public'
            and table_name = 'pf_exam_users'
            and column_name = 'reference'
    ) and not exists (
        select 1
        from information_schema.columns
        where table_schema = 'public'
            and table_name = 'pf_exam_users'
            and column_name = 'pay_link'
    ) then
        alter table public.pf_exam_users rename column reference to pay_link;
    end if;
end;
$$;

drop function if exists public.admin_update_pf_exam_user(
    text, text, text, text, text, text, text, text, text, text, text
);
drop function if exists public.admin_update_pf_exam_user(
    text, text, text, text, text, text, text, text, text, text
);
drop function if exists public.admin_update_pf_exam_user(
    text, text, text, text, text, text, text, text
);
drop function if exists public.register_pf_exam_user(
    text, text, text, text, text, text, text, text
);
alter table public.pf_exam_users drop column if exists "time";

alter table public.pf_exam_users
    alter column switch set default '',
    alter column pay_link set default '',
    alter column active_link set default '',
    alter column payment_link set default '',
    alter column wp_group set default '';

update public.pf_exam_users
set switch = 'Active'
where lower(btrim(coalesce(switch, ''))) not in ('active', 'deactive');

update public.pf_exam_users
set exam = 'QuestionList'
where lower(btrim(coalesce(exam, ''))) = 'pharmacy';

alter table public.pf_exam_users enable row level security;

revoke all on public.pf_exam_users from anon, authenticated;

drop policy if exists "Allow anonymous RubelApp registrations"
    on public.pf_exam_users;

create or replace function public.register_pf_exam_user(
    p_time_id text,
    p_device_id text,
    p_exam text,
    p_mobile text,
    p_email text,
    p_name text,
    p_switch text,
    p_pay_link text
)
returns jsonb
language plpgsql
security definer
set search_path = ''
as $$
declare
    safe_time_id text := btrim(coalesce(p_time_id, ''));
    safe_device_id text := btrim(coalesce(p_device_id, ''));
    safe_exam text := btrim(coalesce(p_exam, ''));
    safe_email text := lower(btrim(coalesce(p_email, '')));
    safe_name text := btrim(coalesce(p_name, ''));
    safe_mobile text := regexp_replace(
        btrim(coalesce(p_mobile, '')),
        '[[:space:]()\-]',
        '',
        'g'
    );
begin
    if safe_time_id !~ '^[0-9]{15}$' then
        return jsonb_build_object('success', false, 'message', 'Code must be a 15-digit number.');
    end if;
    if char_length(safe_device_id) not between 1 and 128 then
        return jsonb_build_object('success', false, 'message', 'Invalid device ID.');
    end if;
    if lower(safe_exam) <> 'questionlist' then
        return jsonb_build_object('success', false, 'message', 'Only QuestionList registration is currently available.');
    end if;
    if safe_mobile !~ '^(\+?88)?01[3-9][0-9]{8}$' then
        return jsonb_build_object(
            'success', false,
            'message', 'Mobile number must be a valid Bangladesh number.'
        );
    end if;
    if char_length(safe_email) not between 3 and 320
        or safe_email !~ '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$' then
        return jsonb_build_object('success', false, 'message', 'Please enter a valid E-Mail ID.');
    end if;
    if safe_name = '' then
        return jsonb_build_object(
            'success', false,
            'message', 'Name/Details is required.'
        );
    end if;

    insert into public.pf_exam_users (
        time_id, device_id, exam, mobile, email, name, switch, pay_link
    ) values (
        safe_time_id,
        safe_device_id,
        'QuestionList',
        btrim(coalesce(p_mobile, '')),
        safe_email,
        safe_name,
        coalesce(nullif(btrim(coalesce(p_switch, '')), ''), 'Active'),
        btrim(coalesce(p_pay_link, ''))
    );
    return jsonb_build_object('success', true, 'message', 'Profile registered in Supabase.');
exception
    when unique_violation then
        return jsonb_build_object(
            'success', false,
            'duplicate', true,
            'message', 'This device is already registered.'
        );
end;
$$;

create or replace function public.verify_pf_exam_login(
    p_device_id text,
    p_time_id text
)
returns jsonb
language plpgsql
security definer
set search_path = ''
as $$
declare
    user_record public.pf_exam_users%rowtype;
    payment_required boolean;
begin
    select u.* into user_record
    from public.pf_exam_users as u
    where u.device_id = btrim(coalesce(p_device_id, ''))
        and u.time_id = btrim(coalesce(p_time_id, ''));

    if not found then
        return jsonb_build_object('success', false, 'message', 'Invalid Code or Device ID.');
    end if;

    if lower(btrim(coalesce(user_record.switch, ''))) = 'deactive' then
        return jsonb_build_object(
            'success', false,
            'message', 'This account is deactivated. Contact the administrator.'
        );
    end if;

    payment_required := lower(btrim(coalesce(user_record.active_link, ''))) in (
        '0', 'false', 'off', 'no', 'unpaid', 'payment required', 'inactive'
    );
    if payment_required and btrim(coalesce(user_record.payment_link, '')) = '' then
        return jsonb_build_object(
            'success', false,
            'message', 'Payment is required, but no payment link is configured. Contact the administrator.'
        );
    end if;

    return jsonb_build_object(
        'success', true,
        'message', 'Login successful.',
        'paymentRequired', payment_required,
        'paymentUrl', coalesce(user_record.payment_link, ''),
        'examName', user_record.exam,
        'whatsappUrl', coalesce(user_record.wp_group, '')
    );
end;
$$;

create table if not exists public.pf_exam_code_recovery_limits (
    device_id text primary key,
    last_requested_at timestamptz not null default now()
);
alter table public.pf_exam_code_recovery_limits enable row level security;
revoke all on public.pf_exam_code_recovery_limits from public, anon, authenticated;

create or replace function public.claim_pf_exam_code_recovery(p_device_id text)
returns boolean
language plpgsql
security definer
set search_path = ''
as $$
declare
    claimed_device_id text;
begin
    insert into public.pf_exam_code_recovery_limits (device_id, last_requested_at)
    values (btrim(coalesce(p_device_id, '')), now())
    on conflict (device_id) do update
        set last_requested_at = excluded.last_requested_at
        where public.pf_exam_code_recovery_limits.last_requested_at
            < now() - interval '1 minute'
    returning device_id into claimed_device_id;
    return claimed_device_id is not null;
end;
$$;

create or replace function public.current_pf_exam_database_name()
returns text
language sql
stable
security invoker
set search_path = ''
as $$
    select current_database();
$$;

revoke all on function public.current_pf_exam_database_name() from public;
grant execute on function public.current_pf_exam_database_name() to anon, authenticated;

revoke all on function public.register_pf_exam_user(text, text, text, text, text, text, text, text)
    from public, authenticated;
grant execute on function public.register_pf_exam_user(text, text, text, text, text, text, text, text)
    to anon;
revoke all on function public.verify_pf_exam_login(text, text) from public, authenticated;
grant execute on function public.verify_pf_exam_login(text, text) to anon;
revoke all on function public.claim_pf_exam_code_recovery(text) from public, anon, authenticated;
grant execute on function public.claim_pf_exam_code_recovery(text) to service_role;

create or replace function public.admin_list_pf_exam_users()
returns setof public.pf_exam_users
language plpgsql
stable
security definer
set search_path = ''
as $$
begin
    if lower(coalesce((select auth.jwt() ->> 'email'), '')) <> 'properfixer.it@gmail.com' then
        raise exception 'Admin access required.' using errcode = '42501';
    end if;

    return query
        select u.*
        from public.pf_exam_users as u
        order by u.time_id;
end;
$$;

create or replace function public.admin_update_pf_exam_user(
    p_time_id text,
    p_exam text,
    p_mobile text,
    p_email text,
    p_name text,
    p_switch text,
    p_pay_link text,
    p_active_link text
)
returns setof public.pf_exam_users
language plpgsql
security definer
set search_path = ''
as $$
begin
    if lower(coalesce((select auth.jwt() ->> 'email'), '')) <> 'properfixer.it@gmail.com' then
        raise exception 'Admin access required.' using errcode = '42501';
    end if;

    return query
        update public.pf_exam_users as u
        set exam = p_exam,
            mobile = p_mobile,
            email = p_email,
            name = p_name,
            switch = p_switch,
            pay_link = p_pay_link,
            active_link = p_active_link
        where u.time_id = p_time_id
        returning u.*;
end;
$$;

create or replace function public.admin_delete_pf_exam_user(p_time_id text)
returns boolean
language plpgsql
security definer
set search_path = ''
as $$
declare
    affected_rows integer;
begin
    if lower(coalesce((select auth.jwt() ->> 'email'), '')) <> 'properfixer.it@gmail.com' then
        raise exception 'Admin access required.' using errcode = '42501';
    end if;

    delete from public.pf_exam_users as u
    where u.time_id = p_time_id;
    get diagnostics affected_rows = row_count;
    return affected_rows = 1;
end;
$$;

revoke all on function public.admin_list_pf_exam_users() from public, anon;
revoke all on function public.admin_update_pf_exam_user(text, text, text, text, text, text, text, text) from public, anon;
revoke all on function public.admin_delete_pf_exam_user(text) from public, anon;
grant execute on function public.admin_list_pf_exam_users() to authenticated;
grant execute on function public.admin_update_pf_exam_user(text, text, text, text, text, text, text, text) to authenticated;
grant execute on function public.admin_delete_pf_exam_user(text) to authenticated;

notify pgrst, 'reload schema';

-- GitHub OAuth is used only for administrator access; registration uses RPCs.
drop trigger if exists on_pf_exam_auth_user_created on auth.users;
