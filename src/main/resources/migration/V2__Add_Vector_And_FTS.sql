-- Enable pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- Add embedding column to note table
ALTER TABLE note ADD COLUMN IF NOT EXISTS embedding vector(768);

-- Create HNSW index for vector similarity search
-- Note: We use cosine distance (<=>) for indexing
CREATE INDEX ON note USING hnsw (embedding vector_cosine_ops);

-- Add GIN index for Full Text Search on title and content
CREATE INDEX IF NOT EXISTS note_fts_idx ON note USING GIN (to_tsvector('english', title || ' ' || content));
