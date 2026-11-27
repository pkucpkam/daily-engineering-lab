CREATE TABLE ledger_entry (
    id BIGSERIAL PRIMARY KEY,
    external_ref VARCHAR(64) NOT NULL UNIQUE,
    amount_cents BIGINT NOT NULL CHECK (amount_cents > 0),
    metadata JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ledger_channel ON ledger_entry ((metadata ->> 'channel'));
