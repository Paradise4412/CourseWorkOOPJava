package ru.istu.police.dao;

import ru.istu.police.db.Database;
import ru.istu.police.model.DetentionRecord;
import ru.istu.police.model.DetentionStatus;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Доступ к данным о задержаниях (JDBC).
 */
public class DetentionDao {

    public long insert(DetentionRecord record) throws SQLException {
        String sql = """
                INSERT INTO detentions (full_name, birth_date, detention_datetime, reason, article,
                    officer_name, cell_number, status, release_datetime, notes)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(ps, record);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    record.setId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Не удалось получить id новой записи");
    }

    public List<DetentionRecord> findAll() throws SQLException {
        String sql = "SELECT * FROM detentions ORDER BY detention_datetime DESC";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return mapList(rs);
        }
    }

    public Optional<DetentionRecord> findById(long id) throws SQLException {
        String sql = "SELECT * FROM detentions WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<DetentionRecord> findByName(String namePart) throws SQLException {
        String sql = "SELECT * FROM detentions WHERE LOWER(full_name) LIKE LOWER(?) ORDER BY detention_datetime DESC";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + namePart.trim() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                return mapList(rs);
            }
        }
    }

    public List<DetentionRecord> findByStatus(DetentionStatus status) throws SQLException {
        String sql = "SELECT * FROM detentions WHERE status = ? ORDER BY detention_datetime DESC";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                return mapList(rs);
            }
        }
    }

    public boolean update(DetentionRecord record) throws SQLException {
        String sql = """
                UPDATE detentions SET full_name=?, birth_date=?, detention_datetime=?, reason=?,
                    article=?, officer_name=?, cell_number=?, status=?, release_datetime=?, notes=?
                WHERE id=?
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            fillStatement(ps, record);
            ps.setLong(11, record.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(long id) throws SQLException {
        String sql = "DELETE FROM detentions WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public int countByStatus(DetentionStatus status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM detentions WHERE status = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM detentions";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private void fillStatement(PreparedStatement ps, DetentionRecord r) throws SQLException {
        ps.setString(1, r.getFullName());
        ps.setDate(2, Date.valueOf(r.getBirthDate()));
        ps.setTimestamp(3, Timestamp.valueOf(r.getDetentionDateTime()));
        ps.setString(4, r.getReason());
        ps.setString(5, r.getArticle());
        ps.setString(6, r.getOfficerName());
        ps.setString(7, r.getCellNumber());
        ps.setString(8, r.getStatus().name());
        if (r.getReleaseDateTime() != null) {
            ps.setTimestamp(9, Timestamp.valueOf(r.getReleaseDateTime()));
        } else {
            ps.setNull(9, Types.TIMESTAMP);
        }
        ps.setString(10, r.getNotes());
    }

    private List<DetentionRecord> mapList(ResultSet rs) throws SQLException {
        List<DetentionRecord> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapRow(rs));
        }
        return list;
    }

    private DetentionRecord mapRow(ResultSet rs) throws SQLException {
        DetentionRecord r = new DetentionRecord();
        r.setId(rs.getLong("id"));
        r.setFullName(rs.getString("full_name"));
        Date birth = rs.getDate("birth_date");
        if (birth != null) {
            r.setBirthDate(birth.toLocalDate());
        }
        Timestamp detained = rs.getTimestamp("detention_datetime");
        if (detained != null) {
            r.setDetentionDateTime(detained.toLocalDateTime());
        }
        r.setReason(rs.getString("reason"));
        r.setArticle(rs.getString("article"));
        r.setOfficerName(rs.getString("officer_name"));
        r.setCellNumber(rs.getString("cell_number"));
        r.setStatus(DetentionStatus.valueOf(rs.getString("status")));
        Timestamp released = rs.getTimestamp("release_datetime");
        if (released != null) {
            r.setReleaseDateTime(released.toLocalDateTime());
        }
        r.setNotes(rs.getString("notes"));
        return r;
    }
}
