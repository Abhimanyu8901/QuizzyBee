# QuizzyBee

QuizzyBee is a Java-based interactive quiz system built with JavaFX, Core Java, MySQL, and JDBC using MVC architecture.

## Features

- User registration and login
- Separate admin login
- Dashboard with categories, history, stats, and leaderboard
- Timed one-question-at-a-time quiz flow
- Randomized questions and shuffled options
- Automatic scoring and performance feedback
- Admin panel for question CRUD and category creation
- Per-category quiz timer control from the admin panel
- Session-based retake prevention
- Dark mode toggle
- Simple sound feedback for correct answers

## Project Structure

```text
quizzybee/
├── database/
│   ├── schema.sql
│   └── seed.sql
├── src/main/java/com/quizzybee/
│   ├── controller/
│   ├── dao/
│   ├── database/
│   ├── main/
│   ├── model/
│   └── util/
└── src/main/resources/view/
    ├── styles/
    └── *.fxml
```

## MySQL Setup

1. Run [`database/schema.sql`](/C:/Users/Abhimanyu/Documents/New%20project1/database/schema.sql).
2. Run [`database/seed.sql`](/C:/Users/Abhimanyu/Documents/New%20project1/database/seed.sql).
3. Update DB credentials if needed using JVM system properties:

```powershell
mvn javafx:run "-Dquizzybee.db.url=jdbc:mysql://localhost:3306/quizzybee_db?useSSL=false&serverTimezone=UTC" "-Dquizzybee.db.user=root" "-Dquizzybee.db.password=root"
```

Default admin account:

- Email: `admin@quizzybee.com`
- Password: `Admin123`

If you already created the database before the timer feature was added, run this once:

```sql
ALTER TABLE categories ADD COLUMN quiz_duration_minutes INT NOT NULL DEFAULT 10;
```

## Run

```powershell
mvn clean javafx:run
```

## Notes

- Passwords are stored as plain text here to keep the JDBC/MVC example simple and aligned with the requested stack. For production, replace this with password hashing.
- If you want to use a different MySQL schema or credentials, pass them through JVM properties or edit [`DatabaseConnection.java`](/C:/Users/Abhimanyu/Documents/New%20project1/src/main/java/com/quizzybee/database/DatabaseConnection.java).

## Excel Import Format

Admin can import questions from Excel using the `Import Excel` button in the admin panel.
Admin can also export the full question bank using the `Export Excel` button under the Question Bank table.
Admin can set the timer for a category using the `Set Timer` button next to export after selecting a question from that category.

Expected columns in the first row:

```text
question_text | option_a | option_b | option_c | option_d | correct_answer | category | difficulty_level
```

`correct_answer` can contain either the full option text or one of these values:

```text
Option A / Option B / Option C / Option D
```

A sample import file is available at [`question_import_template.csv`](/C:/Users/Abhimanyu/Documents/New%20project1/templates/question_import_template.csv). You can open and edit it in Excel, then import it directly as `.csv`, or save it as `.xlsx` and import that instead.
