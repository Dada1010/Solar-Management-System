USE solar_management;

INSERT INTO companies (name, industry, address, mobile_no, email_address)
VALUES ('Aditya Solar Group', 'Renewable energy', '', '', '')
ON DUPLICATE KEY UPDATE id = LAST_INSERT_ID(id);

SET @company_id = (
    SELECT id FROM companies WHERE name = 'Aditya Solar Group' LIMIT 1
);

INSERT INTO branches (name, address, mobile_no, email_address, company_id)
VALUES ('Head Office', '', '', '', @company_id)
ON DUPLICATE KEY UPDATE id = LAST_INSERT_ID(id);

SET @branch_id = (
    SELECT id FROM branches WHERE company_id = @company_id AND name = 'Head Office' LIMIT 1
);

INSERT INTO employees (
    first_name,
    last_name,
    address,
    mobile_no,
    email_address,
    password_hash,
    employee_type,
    role,
    must_change_password,
    active,
    branch_id
)
SELECT
    'System',
    'Administrator',
    '',
    '',
    'admin@adityasolar.com',
    '$2a$12$SE.ylYIcXFX/c6r.0y.jmuG4hoWYyR8dgBRPiftTKFqraO7pF.9Eu',
    'COMPANY_EMPLOYEE',
    'ADMIN',
    TRUE,
    TRUE,
    @branch_id
WHERE NOT EXISTS (
    SELECT 1 FROM employees WHERE email_address = 'admin@adityasolar.com'
);

SELECT id, email_address, role, must_change_password
FROM employees
WHERE email_address = 'admin@adityasolar.com';
