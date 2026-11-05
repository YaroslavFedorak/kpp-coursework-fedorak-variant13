package ua.edu.nupp.kpp.server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TechnicianDAO {

    public void add(Technician t) throws SQLException {
        String sql = "INSERT INTO technician(full_name,specialization,phone) VALUES(?,?,?)";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, t);
            p.executeUpdate();
        }
    }

    public List<Technician> getAll() throws SQLException {
        List<Technician> list = new ArrayList<>();
        String sql = "SELECT * FROM technician ORDER BY id";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql);
             ResultSet rs = p.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public Technician getById(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT * FROM technician WHERE id=?")) {
            p.setLong(1, id);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public void update(Technician t) throws SQLException {
        String sql = "UPDATE technician SET full_name=?,specialization=?,phone=? WHERE id=?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, t);
            p.setLong(4, t.getId());
            p.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM technician WHERE id=?")) {
            p.setLong(1, id);
            p.executeUpdate();
        }
    }

    private void bind(PreparedStatement p, Technician t) throws SQLException {
        p.setString(1, t.getFullName());
        p.setString(2, t.getSpecialization());
        p.setString(3, t.getPhone());
    }

    private Technician map(ResultSet rs) throws SQLException {
        return new Technician(rs.getLong("id"), rs.getString("full_name"),
                rs.getString("specialization"), rs.getString("phone"));
    }
}
