package ua.edu.nupp.kpp.server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WorkRecordDAO {

    public void add(WorkRecord w) throws SQLException {
        String sql = "INSERT INTO work_record(request_id,technician_id,work_date,description,hours_spent) VALUES(?,?,?,?,?)";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, w);
            p.executeUpdate();
        }
    }

    public List<WorkRecord> getAll() throws SQLException {
        List<WorkRecord> list = new ArrayList<>();
        String sql = "SELECT * FROM work_record ORDER BY id";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql);
             ResultSet rs = p.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public WorkRecord getById(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT * FROM work_record WHERE id=?")) {
            p.setLong(1, id);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public void update(WorkRecord w) throws SQLException {
        String sql = "UPDATE work_record SET request_id=?,technician_id=?,work_date=?,description=?,hours_spent=? WHERE id=?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, w);
            p.setLong(6, w.getId());
            p.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM work_record WHERE id=?")) {
            p.setLong(1, id);
            p.executeUpdate();
        }
    }

    public int countByRequest(long requestId) throws SQLException {
        return count("SELECT COUNT(*) FROM work_record WHERE request_id=?", requestId);
    }

    public int countByTechnician(long technicianId) throws SQLException {
        return count("SELECT COUNT(*) FROM work_record WHERE technician_id=?", technicianId);
    }

    private int count(String sql, long id) throws SQLException {
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, id);
            try (ResultSet rs = p.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private void bind(PreparedStatement p, WorkRecord w) throws SQLException {
        p.setLong(1, w.getRequestId());
        p.setLong(2, w.getTechnicianId());
        p.setString(3, w.getWorkDate());
        p.setString(4, w.getDescription());
        p.setDouble(5, w.getHoursSpent());
    }

    private WorkRecord map(ResultSet rs) throws SQLException {
        return new WorkRecord(rs.getLong("id"), rs.getLong("request_id"), rs.getLong("technician_id"),
                rs.getString("work_date"), rs.getString("description"), rs.getDouble("hours_spent"));
    }
}
