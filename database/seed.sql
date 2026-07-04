USE quizzybee_db;

INSERT INTO users (registration_number, name, email, password, role) VALUES
(1000, 'Quiz Admin', 'admin@quizzybee.com', 'Admin123', 'ADMIN')
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO categories (name, description, quiz_duration_minutes) VALUES
('Java Basics', 'Core Java fundamentals, OOP, syntax and collections', 10),
('DBMS', 'Database concepts, SQL, normalization and transactions', 12),
('Web Tech', 'HTML, CSS, JavaScript and web architecture', 8)
ON DUPLICATE KEY UPDATE description = VALUES(description);

INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, correct_answer, category, difficulty_level) VALUES
('Which keyword is used to inherit a class in Java?', 'implements', 'extends', 'inherits', 'super', 'extends', 'Java Basics', 'Easy'),
('Which collection does not allow duplicate elements?', 'ArrayList', 'Vector', 'HashSet', 'LinkedList', 'HashSet', 'Java Basics', 'Medium'),
('Which SQL command is used to remove all rows from a table but keep the structure?', 'DELETE', 'DROP', 'TRUNCATE', 'REMOVE', 'TRUNCATE', 'DBMS', 'Easy'),
('Which normal form removes transitive dependency?', '1NF', '2NF', '3NF', 'BCNF', '3NF', 'DBMS', 'Medium'),
('Which protocol is primarily used to transfer web pages?', 'FTP', 'HTTP', 'SMTP', 'SSH', 'HTTP', 'Web Tech', 'Easy'),
('Which JavaScript method converts JSON text into an object?', 'JSON.stringify()', 'JSON.convert()', 'JSON.parse()', 'JSON.object()', 'JSON.parse()', 'Web Tech', 'Medium')
ON DUPLICATE KEY UPDATE question_text = VALUES(question_text);
