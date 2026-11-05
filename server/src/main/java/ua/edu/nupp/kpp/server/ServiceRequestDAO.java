package ua.edu.nupp.kpp.server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceRequestDAO {

    public void add(ServiceRequest r) throws SQLException {
        String sql = "INSERT INTO service_request(room_id,technician_id,description,guest_impact,severity,"
                + "downtime_hours,state,f1,f2,f3,priority_index,created_date) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, r);
            p.executeUpdate();
        }
    }

    public List<ServiceRequest> getAll() throws SQLException {
        List<ServiceRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM service_request ORDER BY id";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql);
             ResultSet rs = p.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public ServiceRequest getById(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT * FROM service_request WHERE id=?")) {
            p.setLong(1, id);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public void update(ServiceRequest r) throws SQLException {
        String sql = "UPDATE service_request SET room_id=?,technician_id=?,description=?,guest_impact=?,severity=?,"
                + "downtime_hours=?,state=?,f1=?,f2=?,f3=?,priority_index=?,created_date=? WHERE id=?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, r);
            p.setLong(13, r.getId());
            p.executeUpdate();
        }
    }

    // окрема операція для життєвого циклу: змінюється лише стан і, за потреби, фахівець
    public void updateState(long id, RequestState state, Long technicianId) throws SQLException {
        String sql = "UPDATE service_request SET state=?, technician_id=? WHERE id=?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, state.name());
            setNullableLong(p, 2, technicianId);
            p.setLong(3, id);
            p.executeUpdate();
        }
    }

    public int countByRoom(long roomId) throws SQLException {
        return count("SELECT COUNT(*) FROM service_request WHERE room_id=?", roomId);
    }

    public int countByTechnician(long technicianId) throws SQLException {
        return count("SELECT COUNT(*) FROM service_request WHERE technician_id=?", technicianId);
    }

    public void delete(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM service_request WHERE id=?")) {
            p.setLong(1, id);
            p.executeUpdate();
        }
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

    private void bind(PreparedStatement p, ServiceRequest r) throws SQLException {
        p.setLong(1, r.getRoomId());
        setNullableLong(p, 2, r.getTechnicianId());
        p.setString(3, r.getDescription());
        p.setInt(4, r.getGuestImpact());
        p.setInt(5, r.getSeverity());
        p.setDouble(6, r.getDowntimeHours());
        p.setString(7, r.getState().name());
        p.setDouble(8, r.getF1());
        p.setDouble(9, r.getF2());
        p.setDouble(10, r.getF3());
        p.setDouble(11, r.getPriorityIndex());
        p.setString(12, r.getCreatedDate());
    }

    private void setNullableLong(PreparedStatement p, int index, Long value) throws SQLException {
        if (value == null) {
            p.setNull(index, Types.BIGINT);
        } else {
            p.setLong(index, value);
        }
    }

    private ServiceRequest map(ResultSet rs) throws SQLException {
        long technician = rs.getLong("technician_id");
        Long technicianId = rs.wasNull() ? null : technician;
        return new ServiceRequest(rs.getLong("id"), rs.getLong("room_id"), technicianId,
                rs.getString("description"), rs.getInt("guest_impact"), rs.getInt("severity"),
                rs.getDouble("downtime_hours"), RequestState.valueOf(rs.getString("state")),
                rs.getDouble("f1"), rs.getDouble("f2"), rs.getDouble("f3"),
                rs.getDouble("priority_index"), rs.getString("created_date"));
    }
}
