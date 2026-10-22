ALTER TABLE tasks
    ADD COLUMN priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM';

ALTER TABLE tasks
    ADD COLUMN assignee_id BIGINT;

UPDATE tasks
SET assignee_id = projects.owner_id
FROM projects
WHERE tasks.project_id = projects.id;

ALTER TABLE tasks
    ADD CONSTRAINT fk_tasks_assignee
    FOREIGN KEY (assignee_id) REFERENCES users (id);

CREATE INDEX idx_tasks_assignee_id ON tasks (assignee_id);