
-- Role
INSERT INTO roles (roleID, role_name) VALUES (1, 'ADMIN');
INSERT INTO roles (roleID, role_name) VALUES (2, 'USER');

-- Category
INSERT INTO category (categoryID, category_name, slug) VALUES (1, 'Technology', 'technology');
INSERT INTO category (categoryID, category_name, slug) VALUES (2, 'Lifestyle', 'lifestyle');
INSERT INTO category (categoryID, category_name, slug) VALUES (3, 'Education', 'education');

COMMIT;