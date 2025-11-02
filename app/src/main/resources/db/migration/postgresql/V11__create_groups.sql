-- Creates groups and group_members tables (phase 0-1 group management)
CREATE TABLE IF NOT EXISTS groups (
  id UUID PRIMARY KEY,
  name VARCHAR(128) NOT NULL UNIQUE,
  description TEXT,
  created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
  updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
  created_by VARCHAR(250),
  updated_by VARCHAR(250)
);

CREATE TABLE IF NOT EXISTS group_members (
  id UUID PRIMARY KEY,
  group_id UUID NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
  actor_username VARCHAR(250),
  member_group_id UUID REFERENCES groups(id) ON DELETE CASCADE,
  CONSTRAINT chk_group_member_oneof CHECK (
    (actor_username IS NOT NULL AND member_group_id IS NULL) OR
    (actor_username IS NULL AND member_group_id IS NOT NULL)
  )
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_group_members_actor ON group_members(group_id, actor_username) WHERE actor_username IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS ux_group_members_group ON group_members(group_id, member_group_id) WHERE member_group_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_group_members_group_id ON group_members(group_id);
