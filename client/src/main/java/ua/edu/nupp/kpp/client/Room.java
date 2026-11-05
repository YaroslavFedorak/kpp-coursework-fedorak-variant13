package ua.edu.nupp.kpp.client;

public class Room {
    private Long id;
    private String number;
    private int floor;
    private String category;
    private int capacity;
    private String equipment;

    public Room() {
    }

    public Room(Long id, String number, int floor, String category, int capacity, String equipment) {
        this.id = id;
        this.number = number;
        this.floor = floor;
        this.category = category;
        this.capacity = capacity;
        this.equipment = equipment;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }
    public int getFloor() { return floor; }
    public void setFloor(int floor) { this.floor = floor; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public String getEquipment() { return equipment; }
    public void setEquipment(String equipment) { this.equipment = equipment; }

    @Override
    public String toString() {
        return number + " (" + category + ")";
    }
}
