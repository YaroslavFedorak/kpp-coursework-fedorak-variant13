package ua.edu.nupp.kpp.server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

    public void add(Room r) throws SQLException {
        String sql = "INSERT INTO room(number,floor,category,capacity,equipment) VALUES(?,?,?,?,?)";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, r);
            p.executeUpdate();
        }
    }

    public List<Room> getAll() throws SQLException {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT * FROM room ORDER BY id";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql);
             ResultSet rs = p.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public Room getById(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT * FROM room WHERE id=?")) {
            p.setLong(1, id);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public Room getByNumber(String number) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT * FROM room WHERE number=?")) {
            p.setString(1, number);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public void update(Room r) throws SQLException {
        String sql = "UPDATE room SET number=?,floor=?,category=?,capacity=?,equipment=? WHERE id=?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, r);
            p.setLong(6, r.getId());
            p.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM room WHERE id=?")) {
            p.setLong(1, id);
            p.executeUpdate();
        }
    }

    private void bind(PreparedStatement p, Room r) throws SQLException {
        p.setString(1, r.getNumber());
        p.setInt(2, r.getFloor());
        p.setString(3, r.getCategory());
        p.setInt(4, r.getCapacity());
        p.setString(5, r.getEquipment());
    }

    private Room map(ResultSet rs) throws SQLException {
        return new Room(rs.getLong("id"), rs.getString("number"), rs.getInt("floor"),
                rs.getString("category"), rs.getInt("capacity"), rs.getString("equipment"));
    }
}
