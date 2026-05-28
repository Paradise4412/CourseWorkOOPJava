package persistence;

import domain.RecordEntity;
import domain.RecordStatus;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RecordRepository {

    public long insert(RecordEntity entity) throws SQLException {
        String sql = """
                INSERT INTO detentions (full_name, birth_date, detention_datetime, reason, article,
                    officer_name, cell_number, status, release_datetime, notes)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = DatabaseConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(statement, entity);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    entity.setId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Не удалось получить id новой записи");
    }

    public List<RecordEntity> findAll() throws SQLException {
        String sql = "SELECT * FROM detentions ORDER BY detention_datetime DESC";
        try (Connection connection = DatabaseConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return mapList(resultSet);
        }
    }

    public Optional<RecordEntity> findById(long id) throws SQLException {
        String sql = "SELECT * FROM detentions WHERE id = ?";
        try (Connection connection = DatabaseConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    public List<RecordEntity> findByName(String namePart) throws SQLException {
        String sql = "SELECT * FROM detentions WHERE LOWER(full_name) LIKE LOWER(?) ORDER BY detention_datetime DESC";
        try (Connection connection = DatabaseConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "%" + namePart.trim() + "%");
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapList(resultSet);
            }
        }
    }

    public List<RecordEntity> findByStatus(RecordStatus status) throws SQLException {
        String sql = "SELECT * FROM detentions WHERE status = ? ORDER BY detention_datetime DESC";
        try (Connection connection = DatabaseConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapList(resultSet);
            }
        }
    }

    public boolean update(RecordEntity entity) throws SQLException {
        String sql = """
                UPDATE detentions SET full_name=?, birth_date=?, detention_datetime=?, reason=?,
                    article=?, officer_name=?, cell_number=?, status=?, release_datetime=?, notes=?
                WHERE id=?
                """;
        try (Connection connection = DatabaseConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillStatement(statement, entity);
            statement.setLong(11, entity.getId());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete(long id) throws SQLException {
        String sql = "DELETE FROM detentions WHERE id = ?";
        try (Connection connection = DatabaseConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    public int countByStatus(RecordStatus status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM detentions WHERE status = ?";
        try (Connection connection = DatabaseConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM detentions";
        try (Connection connection = DatabaseConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }

    private void fillStatement(PreparedStatement statement, RecordEntity entity) throws SQLException {
        statement.setString(1, entity.getFullName());
        statement.setDate(2, Date.valueOf(entity.getBirthDate()));
        statement.setTimestamp(3, Timestamp.valueOf(entity.getDetentionDateTime()));
        statement.setString(4, entity.getReason());
        statement.setString(5, entity.getArticle());
        statement.setString(6, entity.getOfficerName());
        statement.setString(7, entity.getCellNumber());
        statement.setString(8, entity.getStatus().name());
        if (entity.getReleaseDateTime() != null) {
            statement.setTimestamp(9, Timestamp.valueOf(entity.getReleaseDateTime()));
        } else {
            statement.setNull(9, Types.TIMESTAMP);
        }
        statement.setString(10, entity.getNotes());
    }

    private List<RecordEntity> mapList(ResultSet resultSet) throws SQLException {
        List<RecordEntity> list = new ArrayList<>();
        while (resultSet.next()) {
            list.add(mapRow(resultSet));
        }
        return list;
    }

    private RecordEntity mapRow(ResultSet resultSet) throws SQLException {
        RecordEntity entity = new RecordEntity();
        entity.setId(resultSet.getLong("id"));
        entity.setFullName(resultSet.getString("full_name"));
        Date birthDate = resultSet.getDate("birth_date");
        if (birthDate != null) {
            entity.setBirthDate(birthDate.toLocalDate());
        }
        Timestamp detentionDateTime = resultSet.getTimestamp("detention_datetime");
        if (detentionDateTime != null) {
            entity.setDetentionDateTime(detentionDateTime.toLocalDateTime());
        }
        entity.setReason(resultSet.getString("reason"));
        entity.setArticle(resultSet.getString("article"));
        entity.setOfficerName(resultSet.getString("officer_name"));
        entity.setCellNumber(resultSet.getString("cell_number"));
        entity.setStatus(RecordStatus.valueOf(resultSet.getString("status")));
        Timestamp releaseDateTime = resultSet.getTimestamp("release_datetime");
        if (releaseDateTime != null) {
            entity.setReleaseDateTime(releaseDateTime.toLocalDateTime());
        }
        entity.setNotes(resultSet.getString("notes"));
        return entity;
    }
}
