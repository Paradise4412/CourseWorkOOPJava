
Курсовая работа по дисциплине «Объектно-ориентированное программирование (Java)».
Консольное приложение для ведения учёта задержанных лиц: регистрация, поиск, изменение и удаление записей, статистика по статусам. Данные хранятся в реляционной базе **H2** (файловая БД в каталоге `database/`).


- Java 17
- Maven
- JDBC
- H2 Database

```
src/main/java/
├── RegistryApplication.java            — точка входа
├── domain/                             — доменная модель
│   ├── RecordEntity.java
│   └── RecordStatus.java
├── persistence/                        — слой хранения данных
│   ├── DatabaseConnectionManager.java
│   └── RecordRepository.java
└── presentation/                       — пользовательский интерфейс
    └── ConsoleApplication.java
```

| Компонент | Назначение |
|-----------|------------|
| `RegistryApplication` | Запуск приложения и корректное закрытие соединения с БД |
| `RecordEntity` | Сущность записи о задержании |
| `RecordStatus` | Перечисление статусов задержанного лица |
| `DatabaseConnectionManager` | Подключение к H2 и инициализация схемы |
| `RecordRepository` | Операции CRUD и статистика (JDBC) |
| `ConsoleApplication` | Консольное меню и ввод данных |

Требуются **JDK 17+** и **Maven**.

```bash
mvn compile exec:java -Dexec.mainClass="RegistryApplication"
```

Сборка и запуск JAR:

```bash
mvn package
java -jar target/police-detention-registry-1.0.0.jar
```

## База данных

При первом запуске создаётся файл `database/detention_registry.mv.db`. Таблица `detentions` создаётся автоматически.