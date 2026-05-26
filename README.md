Курсовая работа по дисциплине «Объектно-ориентированное программирование (Java)».Консольное приложение для ведения учёта задержанных лиц: регистрация, поиск, изменение и удаление записей, статистика по статусам. Данные хранятся в реляционной базе **H2** (файловая БД в каталоге `database/`).
- Java 17
- Maven
- JDBC
- H2 Database
## Структура проекта

```
src/main/java/
├── DetentionRegistryApplication.java   — точка входа
├── domain/                             — доменная модель
│   ├── DetentionEntity.java
│   └── DetentionStatus.java
├── persistence/                        — слой хранения данных
│   ├── DatabaseConnectionManager.java
│   └── DetentionRepository.java
└── presentation/                       — пользовательский интерфейс
    └── ConsoleApplication.java
```


| Компонент | Назначение |
|-----------|------------|
| `DetentionRegistryApplication` | Запуск приложения и корректное закрытие соединения с БД |
| `DetentionEntity` | Сущность записи о задержании |
| `DetentionStatus` | Перечисление статусов задержанного лица |
| `DatabaseConnectionManager` | Подключение к H2 и инициализация схемы |
| `DetentionRepository` | Операции CRUD и статистика (JDBC) |
| `ConsoleApplication` | Консольное меню и ввод данных |

Требуются **JDK 17+** и **Maven**.

```bash
mvn compile exec:java -Dexec.mainClass="DetentionRegistryApplication"
```
Сборка и запуск JAR:
```bash
mvn package
java -jar target/police-detention-registry-1.0.0.jar
```