-- Group Member Data (both actor usernames and nested groups)

-- Engineering group members (actors)
INSERT INTO group_members (id, group_id, actor_username, member_group_id) VALUES
    ('11111111-1111-1111-1111-111111111111', '8393296b-32f2-4429-bb8f-2b8e8687ee1f', 'john.doe', NULL),
    ('22222222-2222-2222-2222-222222222222', '8393296b-32f2-4429-bb8f-2b8e8687ee1f', 'jane.smith', NULL),
    ('33333333-3333-3333-3333-333333333333', '8393296b-32f2-4429-bb8f-2b8e8687ee1f', 'bob.wilson', NULL);

-- Product Management group members (actors)
INSERT INTO group_members (id, group_id, actor_username, member_group_id) VALUES
    ('44444444-4444-4444-4444-444444444444', '588c8c76-9815-45fe-8f03-dec1dd5b9254', 'alice.johnson', NULL),
    ('55555555-5555-5555-5555-555555555555', '588c8c76-9815-45fe-8f03-dec1dd5b9254', 'charlie.brown', NULL);

-- Operations group members (actors)
INSERT INTO group_members (id, group_id, actor_username, member_group_id) VALUES
    ('66666666-6666-6666-6666-666666666666', 'd5f701e7-afd0-43cd-aad9-646a9b4da1ef', 'david.lee', NULL),
    ('77777777-7777-7777-7777-777777777777', 'd5f701e7-afd0-43cd-aad9-646a9b4da1ef', 'emma.davis', NULL),
    ('88888888-8888-8888-8888-888888888888', 'd5f701e7-afd0-43cd-aad9-646a9b4da1ef', 'frank.miller', NULL);

-- Administrators group members (mix of actors and nested groups)
INSERT INTO group_members (id, group_id, actor_username, member_group_id) VALUES
    ('99999999-9999-9999-9999-999999999999', '32779848-78b5-4ba2-bdc3-fc97fc7f0c8a', 'admin.user', NULL),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '32779848-78b5-4ba2-bdc3-fc97fc7f0c8a', NULL, '8393296b-32f2-4429-bb8f-2b8e8687ee1f'), -- Engineering as nested group
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '32779848-78b5-4ba2-bdc3-fc97fc7f0c8a', NULL, 'd5f701e7-afd0-43cd-aad9-646a9b4da1ef'); -- Operations as nested group

-- Marketing group members (actors)
INSERT INTO group_members (id, group_id, actor_username, member_group_id) VALUES
    ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'a1234567-89ab-cdef-0123-456789abcdef', 'grace.taylor', NULL),
    ('dddddddd-dddd-dddd-dddd-dddddddddddd', 'a1234567-89ab-cdef-0123-456789abcdef', 'henry.anderson', NULL);

-- Sales group with nested Product Management group
INSERT INTO group_members (id, group_id, actor_username, member_group_id) VALUES
    ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'b2345678-9abc-def0-1234-56789abcdef0', 'irene.thomas', NULL),
    ('ffffffff-ffff-ffff-ffff-ffffffffffff', 'b2345678-9abc-def0-1234-56789abcdef0', NULL, '588c8c76-9815-45fe-8f03-dec1dd5b9254'); -- Product Management as nested group

-- DevOps group members (actors)
INSERT INTO group_members (id, group_id, actor_username, member_group_id) VALUES
    ('10101010-1010-1010-1010-101010101010', '12345678-f012-3456-789a-bcdef0123456', 'jack.white', NULL),
    ('20202020-2020-2020-2020-202020202020', '12345678-f012-3456-789a-bcdef0123456', 'karen.martin', NULL),
    ('30303030-3030-3030-3030-303030303030', '12345678-f012-3456-789a-bcdef0123456', 'leo.garcia', NULL);

