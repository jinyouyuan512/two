-- WARNING: This schema is for context only and is not meant to be run.
-- Table order and constraints may not be valid for execution.

CREATE TABLE public.daily_news (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  news_date date NOT NULL,
  title text NOT NULL,
  summary text,
  content text,
  url text,
  source text,
  created_at timestamp with time zone DEFAULT now(),
  published_at timestamp with time zone,
  CONSTRAINT daily_news_pkey PRIMARY KEY (id)
);
CREATE TABLE public.daily_tips (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  content text NOT NULL,
  tip_date date NOT NULL DEFAULT CURRENT_DATE UNIQUE,
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT daily_tips_pkey PRIMARY KEY (id)
);
CREATE TABLE public.heart_rates (
  id bigint NOT NULL DEFAULT nextval('heart_rates_id_seq'::regclass),
  user_id uuid NOT NULL,
  at timestamp with time zone NOT NULL,
  bpm integer NOT NULL,
  CONSTRAINT heart_rates_pkey PRIMARY KEY (id),
  CONSTRAINT heart_rates_user_id_fkey FOREIGN KEY (user_id) REFERENCES auth.users(id)
);
CREATE TABLE public.imports (
  id bigint NOT NULL DEFAULT nextval('imports_id_seq'::regclass),
  filename text,
  source text,
  rows_count integer,
  status text,
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT imports_pkey PRIMARY KEY (id)
);
CREATE TABLE public.mood_logs (
  id bigint NOT NULL DEFAULT nextval('mood_logs_id_seq'::regclass),
  user_id uuid NOT NULL,
  at timestamp with time zone NOT NULL,
  mood text NOT NULL,
  note text,
  score integer,
  CONSTRAINT mood_logs_pkey PRIMARY KEY (id),
  CONSTRAINT mood_logs_user_id_fkey FOREIGN KEY (user_id) REFERENCES auth.users(id)
);
CREATE TABLE public.profiles (
  id uuid NOT NULL,
  display_name text,
  avatar_url text,
  created_at timestamp with time zone DEFAULT now(),
  updated_at timestamp with time zone DEFAULT now(),
  CONSTRAINT profiles_pkey PRIMARY KEY (id),
  CONSTRAINT profiles_id_fkey FOREIGN KEY (id) REFERENCES auth.users(id)
);
CREATE TABLE public.sleep_sessions (
  id bigint NOT NULL DEFAULT nextval('sleep_sessions_id_seq'::regclass),
  user_id uuid NOT NULL,
  at date NOT NULL,
  hours numeric NOT NULL,
  CONSTRAINT sleep_sessions_pkey PRIMARY KEY (id),
  CONSTRAINT sleep_sessions_user_id_fkey FOREIGN KEY (user_id) REFERENCES auth.users(id)
);
CREATE TABLE public.steps (
  id bigint NOT NULL DEFAULT nextval('steps_id_seq'::regclass),
  user_id uuid NOT NULL,
  at date NOT NULL,
  count integer NOT NULL,
  CONSTRAINT steps_pkey PRIMARY KEY (id),
  CONSTRAINT steps_user_id_fkey FOREIGN KEY (user_id) REFERENCES auth.users(id)
);
CREATE TABLE public.water_intake (
  id bigint NOT NULL DEFAULT nextval('water_intake_id_seq'::regclass),
  user_id uuid NOT NULL,
  at date NOT NULL,
  ml integer NOT NULL,
  CONSTRAINT water_intake_pkey PRIMARY KEY (id),
  CONSTRAINT water_intake_user_id_fkey FOREIGN KEY (user_id) REFERENCES auth.users(id)
);
CREATE TABLE public.weights (
  id bigint NOT NULL DEFAULT nextval('weights_id_seq'::regclass),
  user_id uuid NOT NULL,
  at date NOT NULL,
  kg numeric NOT NULL,
  CONSTRAINT weights_pkey PRIMARY KEY (id),
  CONSTRAINT weights_user_id_fkey FOREIGN KEY (user_id) REFERENCES auth.users(id)
);