# Інформаційна система технічних заявок готельного комплексу

Курсова робота з дисципліни «Кросплатформене програмування», **варіант №13**.
Виконав: Федорак Ярослав Олександрович, група д2ТН.

Клієнт–серверна система: JavaFX-клієнт працює із сервером через HTTP/JSON, сервер (Servlet → Service → DAO → JDBC)
зберігає дані в MySQL.

```
JavaFX → HttpHotelClient → HTTP/JSON → Servlet → Service → DAO → JDBC → MySQL
```

## Предметна модель

| Сутність | Таблиця | Призначення |
|---|---|---|
| Номер (`Room`) | `room` | номер готелю та його обладнання |
| Фахівець (`Technician`) | `technician` | технічний працівник, що виконує заявки |
| Заявка (`ServiceRequest`) | `service_request` | технічна заявка щодо номера, об'єкт життєвого циклу |
| Запис виконання (`WorkRecord`) | `work_record` | виконані роботи за заявкою |

Життєвий цикл заявки: **НОВА (NEW) → ПРИЗНАЧЕНА (ASSIGNED) → В_РОБОТІ (IN_PROGRESS) → ВИКОНАНО (DONE)**.
Для переходу в ASSIGNED потрібно вибрати фахівця, для переходу в DONE — мати хоча б один запис виконання.

Інтегральний показник пріоритету заявки: `I = 0,40·F1 + 0,35·F2 + 0,25·F3`

| Фактор | Зміст | Нормування до 0–100 |
|---|---|---|
| F1 | вплив на гостя | оцінка 0–10 × 10 |
| F2 | тяжкість несправності | (рівень 1–5 − 1) × 25 |
| F3 | вплив на доступність номера | min(100, простій_год / 48 × 100) |

## Вимоги до середовища

- JDK 21, Apache Maven 3.9+
- MySQL 8 (або MySQL із XAMPP)
- Apache Tomcat 9.x (використовується `javax.servlet`, Tomcat 10+ не підходить)
- IntelliJ IDEA (рекомендовано)

## Структура проєкту

```
kpp-coursework-fedorak-variant13/
├─ database.sql                  — створення БД hotel_requests і демонстраційні дані
├─ server/                       — серверний Maven-проєкт (WAR)
│  ├─ pom.xml
│  ├─ src/main/java/ua/edu/nupp/kpp/server/   — моделі, DBUtil, DAO, Service, Servlet
│  ├─ src/main/webapp/WEB-INF/web.xml
│  └─ src/test/java/ua/edu/nupp/kpp/server/   — тести JUnit 5
└─ client/                       — JavaFX-клієнт (окремий Maven-проєкт)
   ├─ pom.xml
   ├─ sample-data/rooms.csv      — CSV з переліком номерів та обладнання
   ├─ src/main/java/ua/edu/nupp/kpp/client/   — моделі, HttpHotelClient, ImportRoomsTask, MainApp
   └─ src/test/java/ua/edu/nupp/kpp/client/   — тести розбору CSV
```

## Запуск

### 1. База даних
Запустити MySQL і виконати `database.sql` (phpMyAdmin → вкладка SQL, або в терміналі):
```
mysql -u root -p < database.sql
```
Створюється база `hotel_requests` з таблицями `room`, `technician`, `service_request`, `work_record`.

Параметри підключення за замовчуванням: `jdbc:mysql://localhost:3306/hotel_requests`, користувач `root`, порожній пароль
(як у XAMPP). Інші значення задаються змінними середовища перед запуском Tomcat, пароль у коді не зберігається:
```
set DB_URL=jdbc:mysql://localhost:3306/hotel_requests
set DB_USER=root
set DB_PASSWORD=ваш_пароль
```

### 2. Сервер
```
cd server
mvn clean package
```
Maven запускає тести і створює `server/target/hotel-requests-server.war`. Файл копіюється в `webapps` Tomcat 9,
після чого Tomcat запускається (`bin/startup.bat` або `bin/catalina.bat run`).

Перевірка у браузері: http://localhost:8080/hotel-requests-server/rooms — має повернутися JSON-масив номерів.

### 3. Клієнт
```
cd client
mvn javafx:run
```
Клієнт запускається з каталогу `client`, тому відносний шлях `sample-data/rooms.csv` вказує на файл прикладу.
Інша адреса сервера задається параметром `-Dserver.url=...`. Журнал клієнта записується у `client/client.log`.

## HTTP-маршрути

| URL | Servlet | Операції (`action` для POST) |
|---|---|---|
| `/rooms` | RoomServlet | GET — список; POST: `add`, `update`, `delete` |
| `/technicians` | TechnicianServlet | GET — список; POST: `add`, `update`, `delete` |
| `/requests` | ServiceRequestServlet | GET — список; POST: `add`, `update`, `transition`, `delete` |
| `/work-records` | WorkRecordServlet | GET — список; POST: `add`, `update`, `delete` |

POST-параметри передаються як `application/x-www-form-urlencoded`, відповідь — JSON. Помилка вхідних даних → HTTP 400
з `{"error": "..."}`, непередбачена помилка сервера → HTTP 500.

## Формат CSV (`client/sample-data/rooms.csv`)

Роздільник `;`, кодування UTF-8, перший рядок — заголовок:
```
room_number;floor;category;capacity;equipment
206;2;Напівлюкс;3;Кондиціонер, телевізор, міні-бар, сейф
```
Некоректний рядок (не 5 полів, нечислове значення, порожнє поле, номер уже існує) пропускається, а після імпорту
показується підсумок із причинами пропуску. Імпорт виконується у фоновому `javafx.concurrent.Task` з ProgressBar,
його можна скасувати.

## Тести
```
cd server
mvn test
cd ../client
mvn test
```
Сервер: 22 тести (розрахунок F1–F3 та I, життєвий цикл, валідація, правила Service без MySQL).
Клієнт: 4 тести розбору CSV.

## Сценарій перевірки
1. Виконати `database.sql`, зібрати й розгорнути сервер, перевірити `/rooms` у браузері.
2. Запустити клієнт, перевірити додавання, редагування й видалення на всіх чотирьох вкладках.
3. На вкладці «Заявки» створити заявку й перевірити автоматичний розрахунок F1–F3, I і рівня пріоритету.
4. Пройти життєвий цикл НОВА → ПРИЗНАЧЕНА (з фахівцем) → В_РОБОТІ → (додати запис виконання) → ВИКОНАНО;
   переконатися, що НОВА → ВИКОНАНО відхиляється.
5. На вкладці «Номери» виконати імпорт `sample-data/rooms.csv`: 6 рядків імпортується, 3 пропускаються.
6. Переглянути журнал Tomcat і `client/client.log`.
