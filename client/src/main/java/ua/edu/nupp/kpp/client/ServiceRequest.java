package ua.edu.nupp.kpp.client;

public class ServiceRequest {
    private Long id;
    private long roomId;
    private Long technicianId;
    private String description;
    private int guestImpact;
    private int severity;
    private double downtimeHours;
    private RequestState state;
    private double f1;
    private double f2;
    private double f3;
    private double priorityIndex;
    private String priorityLevel;
    private String createdDate;

    public ServiceRequest() {
    }

    public ServiceRequest(Long id, long roomId, String description, int guestImpact, int severity, double downtimeHours) {
        this.id = id;
        this.roomId = roomId;
        this.description = description;
        this.guestImpact = guestImpact;
        this.severity = severity;
        this.downtimeHours = downtimeHours;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public long getRoomId() { return roomId; }
    public void setRoomId(long roomId) { this.roomId = roomId; }
    public Long getTechnicianId() { return technicianId; }
    public void setTechnicianId(Long technicianId) { this.technicianId = technicianId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getGuestImpact() { return guestImpact; }
    public void setGuestImpact(int guestImpact) { this.guestImpact = guestImpact; }
    public int getSeverity() { return severity; }
    public void setSeverity(int severity) { this.severity = severity; }
    public double getDowntimeHours() { return downtimeHours; }
    public void setDowntimeHours(double downtimeHours) { this.downtimeHours = downtimeHours; }
    public RequestState getState() { return state; }
    public void setState(RequestState state) { this.state = state; }
    public double getF1() { return f1; }
    public void setF1(double f1) { this.f1 = f1; }
    public double getF2() { return f2; }
    public void setF2(double f2) { this.f2 = f2; }
    public double getF3() { return f3; }
    public void setF3(double f3) { this.f3 = f3; }
    public double getPriorityIndex() { return priorityIndex; }
    public void setPriorityIndex(double priorityIndex) { this.priorityIndex = priorityIndex; }
    public String getPriorityLevel() { return priorityLevel; }
    public void setPriorityLevel(String priorityLevel) { this.priorityLevel = priorityLevel; }
    public String getCreatedDate() { return createdDate; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }

    @Override
    public String toString() {
        String text = description == null ? "" : description;
        if (text.length() > 35) {
            text = text.substring(0, 35) + "…";
        }
        return "№" + id + " [" + state + "] " + text;
    }
}
