-- profiles: use standard "id" column referencing auth.users
create table if not exists public.profiles (
  id uuid not null primary key references auth.users (id) on delete cascade,
  display_name text,
  avatar_url text,
  settings_json jsonb default '{}'::jsonb,
  created_at timestamptz default now(),
  updated_at timestamptz default now()
);

alter table public.profiles enable row level security;
drop policy if exists profiles_select on public.profiles;
drop policy if exists profiles_insert on public.profiles;
drop policy if exists profiles_update on public.profiles;
drop policy if exists profiles_delete on public.profiles;
create policy profiles_select on public.profiles for select using (id = auth.uid());
create policy profiles_insert on public.profiles for insert with check (id = auth.uid());
create policy profiles_update on public.profiles for update using (id = auth.uid());
create policy profiles_delete on public.profiles for delete using (id = auth.uid());

-- steps
create table if not exists public.steps (
  id bigint generated always as identity primary key,
  user_id uuid not null references auth.users (id) on delete cascade,
  at date not null,
  count int not null,
  created_at timestamptz default now()
);
alter table public.steps enable row level security;
drop policy if exists steps_select on public.steps;
drop policy if exists steps_insert on public.steps;
drop policy if exists steps_update on public.steps;
drop policy if exists steps_delete on public.steps;
create policy steps_select on public.steps for select using (user_id = auth.uid());
create policy steps_insert on public.steps for insert with check (user_id = auth.uid());
create policy steps_update on public.steps for update using (user_id = auth.uid());
create policy steps_delete on public.steps for delete using (user_id = auth.uid());

-- heart_rates
create table if not exists public.heart_rates (
  id bigint generated always as identity primary key,
  user_id uuid not null references auth.users (id) on delete cascade,
  at timestamptz not null,
  bpm int not null,
  created_at timestamptz default now()
);
alter table public.heart_rates enable row level security;
drop policy if exists heart_rates_select on public.heart_rates;
drop policy if exists heart_rates_insert on public.heart_rates;
drop policy if exists heart_rates_update on public.heart_rates;
drop policy if exists heart_rates_delete on public.heart_rates;
create policy heart_rates_select on public.heart_rates for select using (user_id = auth.uid());
create policy heart_rates_insert on public.heart_rates for insert with check (user_id = auth.uid());
create policy heart_rates_update on public.heart_rates for update using (user_id = auth.uid());
create policy heart_rates_delete on public.heart_rates for delete using (user_id = auth.uid());

-- sleep_sessions
create table if not exists public.sleep_sessions (
  id bigint generated always as identity primary key,
  user_id uuid not null references auth.users (id) on delete cascade,
  start_at timestamptz not null,
  end_at timestamptz not null,
  score int,
  stages_json jsonb,
  created_at timestamptz default now()
);
alter table public.sleep_sessions enable row level security;
drop policy if exists sleep_sessions_select on public.sleep_sessions;
drop policy if exists sleep_sessions_insert on public.sleep_sessions;
drop policy if exists sleep_sessions_update on public.sleep_sessions;
drop policy if exists sleep_sessions_delete on public.sleep_sessions;
create policy sleep_sessions_select on public.sleep_sessions for select using (user_id = auth.uid());
create policy sleep_sessions_insert on public.sleep_sessions for insert with check (user_id = auth.uid());
create policy sleep_sessions_update on public.sleep_sessions for update using (user_id = auth.uid());
create policy sleep_sessions_delete on public.sleep_sessions for delete using (user_id = auth.uid());

-- meals
create table if not exists public.meals (
  id bigint generated always as identity primary key,
  user_id uuid not null references auth.users (id) on delete cascade,
  at timestamptz not null,
  items_json jsonb,
  calories int,
  created_at timestamptz default now()
);
alter table public.meals enable row level security;
drop policy if exists meals_select on public.meals;
drop policy if exists meals_insert on public.meals;
drop policy if exists meals_update on public.meals;
drop policy if exists meals_delete on public.meals;
create policy meals_select on public.meals for select using (user_id = auth.uid());
create policy meals_insert on public.meals for insert with check (user_id = auth.uid());
create policy meals_update on public.meals for update using (user_id = auth.uid());
create policy meals_delete on public.meals for delete using (user_id = auth.uid());

-- water_intakes
create table if not exists public.water_intakes (
  id bigint generated always as identity primary key,
  user_id uuid not null references auth.users (id) on delete cascade,
  at timestamptz not null,
  ml int not null,
  created_at timestamptz default now()
);
alter table public.water_intakes enable row level security;
drop policy if exists water_intakes_select on public.water_intakes;
drop policy if exists water_intakes_insert on public.water_intakes;
drop policy if exists water_intakes_update on public.water_intakes;
drop policy if exists water_intakes_delete on public.water_intakes;
create policy water_intakes_select on public.water_intakes for select using (user_id = auth.uid());
create policy water_intakes_insert on public.water_intakes for insert with check (user_id = auth.uid());
create policy water_intakes_update on public.water_intakes for update using (user_id = auth.uid());
create policy water_intakes_delete on public.water_intakes for delete using (user_id = auth.uid());

-- moods
create table if not exists public.moods (
  id bigint generated always as identity primary key,
  user_id uuid not null references auth.users (id) on delete cascade,
  at timestamptz not null,
  mood text,
  score int,
  created_at timestamptz default now()
);
alter table public.moods enable row level security;
drop policy if exists moods_select on public.moods;
drop policy if exists moods_insert on public.moods;
drop policy if exists moods_update on public.moods;
drop policy if exists moods_delete on public.moods;
create policy moods_select on public.moods for select using (user_id = auth.uid());
create policy moods_insert on public.moods for insert with check (user_id = auth.uid());
create policy moods_update on public.moods for update using (user_id = auth.uid());
create policy moods_delete on public.moods for delete using (user_id = auth.uid());

-- exercise_sessions
create table if not exists public.exercise_sessions (
  id bigint generated always as identity primary key,
  user_id uuid not null references auth.users (id) on delete cascade,
  start_at timestamptz not null,
  end_at timestamptz,
  type text,
  calories int,
  created_at timestamptz default now()
);
alter table public.exercise_sessions enable row level security;
drop policy if exists exercise_sessions_select on public.exercise_sessions;
drop policy if exists exercise_sessions_insert on public.exercise_sessions;
drop policy if exists exercise_sessions_update on public.exercise_sessions;
drop policy if exists exercise_sessions_delete on public.exercise_sessions;
create policy exercise_sessions_select on public.exercise_sessions for select using (user_id = auth.uid());
create policy exercise_sessions_insert on public.exercise_sessions for insert with check (user_id = auth.uid());
create policy exercise_sessions_update on public.exercise_sessions for update using (user_id = auth.uid());
create policy exercise_sessions_delete on public.exercise_sessions for delete using (user_id = auth.uid());

-- imports
create table if not exists public.imports (
  id bigint generated always as identity primary key,
  user_id uuid not null references auth.users (id) on delete cascade,
  filename text,
  source text,
  rows_count int,
  status text,
  created_at timestamptz default now()
);
alter table public.imports enable row level security;
drop policy if exists imports_select on public.imports;
drop policy if exists imports_insert on public.imports;
drop policy if exists imports_update on public.imports;
drop policy if exists imports_delete on public.imports;
create policy imports_select on public.imports for select using (user_id = auth.uid());
create policy imports_insert on public.imports for insert with check (user_id = auth.uid());
create policy imports_update on public.imports for update using (user_id = auth.uid());
create policy imports_delete on public.imports for delete using (user_id = auth.uid());

