-- Product Data
insert into products (id, title, slug, description, created_at, updated_at) values
    ('2d02b402-570f-4c4b-932a-d5a42eae4c34', 'Pixel Pro', 'pixel-pro', '', '2024-07-24 15:01:00 +02:00', '2024-07-24 15:01:00 +02:00'),
    ('632a34d9-13fb-47f7-a324-d0e6ee160858', 'Data Dynamo', 'data-dynamo', '', '2024-07-24 15:01:00 +02:00', '2024-07-24 15:01:00 +02:00'),
    ('8a293c02-33f9-4bdb-96b9-3e7d4f753666', 'Virtual Vault', 'virtual-vault', '', '2024-07-24 15:01:00 +02:00', '2024-07-24 15:01:00 +02:00'),
    ('c40fb118-7639-4f5a-ada8-58ddb342aa72', 'Code Craft', 'code-craft', '', '2024-07-25 17:01:00 +02:00', '2025-07-25 17:01:00 +02:00'),
    ('f3b3b3b3-3b3b-3b3b-3b3b-3b3b3b3b3b3b', 'Cyber Sphere', 'cyber-sphere', '', '2024-07-25 15:01:00 +02:00', '2024-07-25 15:01:00 +02:00'),
    ('dab6f314-eb2e-4663-92b3-4d31791b6de0', 'Data Pro', 'data-pro', '', '2024-07-25 15:01:00 +02:00', '2024-07-25 15:01:00 +02:00'),
    ('cb68d159-edea-404d-89eb-338344295274', 'super Vault', 'super-vault', '', '2024-07-25 17:01:00 +02:00', '2024-07-25 17:01:00 +02:00'),
    ('fb081f7d-988e-47ef-a8ac-217d3803479b', 'Byte Wave', 'byte-wave', '', '2024-07-25 21:00:00 +02:00', '2024-07-25 21:00:00 +02:00'),
    ('5550cf3b-5a51-469b-aa99-e0d0fe3805d3', 'Nex Tech', 'nex-tech', '', '2024-07-25 21:00:00 +02:00', '2024-07-25 21:00:00 +02:00'),
    ('f99d65d8-f263-4ebf-bcb2-d7185fb60f01', 'Quantum Desk', 'quantum-desk', '', '2024-07-25 21:00:00 +02:00', '2024-07-25 21:00:00 +02:00'),
    ('062a179c-ea99-43ab-9d31-4f0968de49f9', 'Sync Fusion', 'sync-fusion', '', '2024-07-25 21:00:00 +02:00', '2024-07-25 21:00:00 +02:00'),
    ('7b7bba23-2e12-43ed-b36d-578c497ea962', 'Code Pulse', 'cold-pulse', '', '2024-07-25 21:00:00 +02:00', '2024-07-25 21:00:00 +02:00'),
    ('cfea5274-84a4-42f8-aff5-97ef8474d0e9', 'Vertex Cloud', 'vertex-cloud', '', '2024-07-25 21:00:00 +02:00', '2024-07-25 21:00:00 +02:00'),
    ('2b45edab-9d8c-4f5b-9876-8a8ae2e0a372', 'Alpha Stream', 'alpha-stream', '', '2024-07-25 21:00:00 +02:00', '2024-07-25 21:00:00 +02:00'),
    ('9f28a98d-262d-4abe-81ed-4ee9e4a05b51', 'Data Haven', 'data-haven', '', '2024-07-25 21:00:00 +02:00', '2024-07-25 21:00:00 +02:00'),
    ('fc76e391-b769-461b-bfb4-242166cddb84', 'Opti Core', 'opti-core', '', '2024-07-25 21:00:00 +02:00', '2024-07-25 21:00:00 +02:00'),
    ('8774a0f4-cf5d-448b-9080-430110b2266b', 'Tech Verse', 'tech-verse', '', '2024-07-25 21:00:00 +02:00', '2024-07-25 21:00:00 +02:00'),
    ('7c28c8bd-cee5-420a-89ea-4c635c31117d', 'Logic Flow', 'logic-flow', '', '2024-07-25 21:00:00 +02:00', '2024-07-25 21:00:00 +02:00'),
    ('9d93ae66-3be7-45f2-8a87-bd780bdf5f01', 'Virtu Sync', 'virtu-sync', '', '2024-07-25 21:00:00 +02:00', '2024-07-25 21:00:00 +02:00'),
    ('28f1e509-0648-45e2-9cda-a92b20b0ae45', 'Pixel Forge', 'pixel-forge', '', '2024-07-25 21:00:00 +02:00', '2024-07-25 21:00:00 +02:00');

-- Tag Data
insert into tags (type_id, tags) values
    ('632a34d9-13fb-47f7-a324-d0e6ee160858', 'big-data'),
    ('632a34d9-13fb-47f7-a324-d0e6ee160858', 'integration'),
    ('632a34d9-13fb-47f7-a324-d0e6ee160858', 'enterprise-solutions'),
    ('2d02b402-570f-4c4b-932a-d5a42eae4c34', 'web-application'),
    ('2d02b402-570f-4c4b-932a-d5a42eae4c34', 'user-experience'),
    ('2d02b402-570f-4c4b-932a-d5a42eae4c34', 'ux'),
    ('2d02b402-570f-4c4b-932a-d5a42eae4c34', 'big-data'),
    ('2d02b402-570f-4c4b-932a-d5a42eae4c34', 'ecommerce'),
    ('2d02b402-570f-4c4b-932a-d5a42eae4c34', 'e-commerce'),
    ('2d02b402-570f-4c4b-932a-d5a42eae4c34', 'saas'),
    ('2d02b402-570f-4c4b-932a-d5a42eae4c34', 'software-as-a-service'),
    ('2d02b402-570f-4c4b-932a-d5a42eae4c34', 'digital-transformation'),
    ('2d02b402-570f-4c4b-932a-d5a42eae4c34', 'automation'),
    ('2d02b402-570f-4c4b-932a-d5a42eae4c34', 'innovation'),
    ('2d02b402-570f-4c4b-932a-d5a42eae4c34', 'productivity'),
    ('8a293c02-33f9-4bdb-96b9-3e7d4f753666', 'mobile-app')
