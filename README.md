# Информационная система учёта задержаний

Курсовая работа по дисциплине «Объектно-ориентированное программирование (Java)».

## Описание

Консольное приложение для ведения учёта задержанных лиц: регистрация, поиск, изменение и удаление записей, статистика по статусам. Данные хранятся в реляционной базе **H2** (файловая БД в каталоге `database/`).

## Технологии

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

## Назначение компонентов

| Компонент | Назначение |
|-----------|------------|
| `DetentionRegistryApplication` | Запуск приложения и корректное закрытие соединения с БД |
| `DetentionEntity` | Сущность записи о задержании |
| `DetentionStatus` | Перечисление статусов задержанного лица |
| `DatabaseConnectionManager` | Подключение к H2 и инициализация схемы |
| `DetentionRepository` | Операции CRUD и статистика (JDBC) |
| `ConsoleApplication` | Консольное меню и ввод данных |

## Запуск

Требуются **JDK 17+** и **Maven**.

```bash
mvn compile exec:java -Dexec.mainClass="DetentionRegistryApplication"
```

Сборка и запуск JAR:

```bash
mvn package
java -jar target/police-detention-registry-1.0.0.jar
```

## Функции меню

1. Добавить запись о задержании  
2. Показать все записи  
3. Найти по ФИО  
4. Найти по статусу  
5. Просмотреть запись по номеру  
6. Изменить запись  
7. Удалить запись  
8. Статистика  
0. Выход  

## База данных

При первом запуске создаётся файл `database/detention_registry.mv.db`. Таблица `detentions` создаётся автоматически.

URL для H2 Console:

```
jdbc:h2:./database/detention_registry
```

Логин: `sa`, пароль: пустой.

## Документация

UML-диаграммы находятся в каталоге `documentation/`.

## Автор

Курсовая работа, ИГТУ.
