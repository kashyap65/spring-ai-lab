-- Runs automatically on first docker compose up
-- Enables the pgvector extension needed for Spring AI vector store

CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Confirm extensions loaded
SELECT 'pgvector extension ready: ' || extversion
FROM pg_extension WHERE extname = 'vector';
