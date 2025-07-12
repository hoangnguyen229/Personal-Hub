-- Role
INSERT INTO roles (roleID, role_name) VALUES (1, 'ADMIN');
INSERT INTO roles (roleID, role_name) VALUES (2, 'USER');

-- Category
INSERT INTO category (categoryID, category_name, slug) VALUES (1, 'Technology', 'technology');
INSERT INTO category (categoryID, category_name, slug) VALUES (2, 'Lifestyle', 'lifestyle');
INSERT INTO category (categoryID, category_name, slug) VALUES (3, 'Education', 'education');
INSERT INTO category (categoryID, category_name, slug) VALUES (4, 'Health', 'health');
INSERT INTO category (categoryID, category_name, slug) VALUES (5, 'Travel', 'travel');
INSERT INTO category (categoryID, category_name, slug) VALUES (6, 'Finance', 'finance');
INSERT INTO category (categoryID, category_name, slug) VALUES (7, 'Entertainment', 'entertainment');
INSERT INTO category (categoryID, category_name, slug) VALUES (8, 'Food', 'food');
INSERT INTO category (categoryID, category_name, slug) VALUES (9, 'Sports', 'sports');
INSERT INTO category (categoryID, category_name, slug) VALUES (10, 'Science', 'science');
INSERT INTO category (categoryID, category_name, slug) VALUES (11, 'Art', 'art');
INSERT INTO category (categoryID, category_name, slug) VALUES (12, 'Business', 'business');
INSERT INTO category (categoryID, category_name, slug) VALUES (13, 'Politics', 'politics');
INSERT INTO category (categoryID, category_name, slug) VALUES (14, 'Gaming', 'gaming');
INSERT INTO category (categoryID, category_name, slug) VALUES (15, 'Productivity', 'productivity');

COMMIT;
