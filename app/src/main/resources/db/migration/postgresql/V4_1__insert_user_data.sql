-- Populate users from dexidp
INSERT INTO users (id, created_at, updated_at, email, username, user_type, external_id) VALUES
    ('1f1de938-6c08-4435-8ef1-068f7b9c7f16', '2025-10-23 18:38:00 +02:00', '2025-10-23 18:38:00 +02:00', 'storeowner@simple-commerce.com', 'simple_commerce', 'STAFF', 'CiQxZjFkZTkzOC02YzA4LTQ0MzUtOGVmMS0wNjhmN2I5YzdmMTYSBWxvY2Fs'),
    ('93a38a0f-1c1e-4c24-87be-99b13446f73c', '2025-10-23 18:38:00 +02:00', '2025-10-23 18:38:00 +02:00', 'trinity@simple-commerce.com', 'trinity', 'STAFF', 'CiQ5M2EzOGEwZi0xYzFlLTRjMjQtODdiZS05OWIxMzQ0NmY3M2MSBWxvY2Fs'),
    ('aab278e5-5495-4706-bc0c-7aa53eecff2f', '2025-10-23 18:38:00 +02:00', '2025-10-23 18:38:00 +02:00', 'naomi@example.com', 'naomi', 'CUSTOMER', 'CiRhYWIyNzhlNS01NDk1LTQ3MDYtYmMwYy03YWE1M2VlY2ZmMmYSBWxvY2Fs'),
    ('14f6055e-cb8a-439f-939d-7a936fd24bb3', '2025-10-23 18:38:00 +02:00', '2025-10-23 18:38:00 +02:00', 'tank.ext@simple-commerce.com', 'tank', 'COLLABORATOR', 'CiQxNGY2MDU1ZS1jYjhhLTQzOWYtOTM5ZC03YTkzNmZkMjRiYjMSBWxvY2Fs');
