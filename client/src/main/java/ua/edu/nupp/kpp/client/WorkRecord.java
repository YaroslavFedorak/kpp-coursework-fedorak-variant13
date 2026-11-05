package ua.edu.nupp.kpp.client;

public class WorkRecord {
    private Long id;
    private long requestId;
    private long technicianId;
    private String workDate;
    private String description;
    private double hoursSpent;

    public WorkRecord() {
    }

    public WorkRecord(Long id, long requestId, long technicianId, String workDate,
                      String description, double hoursSpent) {
        this.id = id;
        this.requestId = requestId;
        this.technicianId = technicianId;
        this.workDate = workDate;
        this.description = description;
        this.hoursSpent = hoursSpent;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public long getRequestId() { return requestId; }
    public void setRequestId(long requestId) { this.requestId = requestId; }
    public long getTechnicianId() { return technicianId; }
    public void setTechnicianId(long technicianId) { this.technicianId = technicianId; }
    public String getWorkDate() { return workDate; }
    public void setWorkDate(String workDate) { this.workDate = workDate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getHoursSpent() { return hoursSpent; }
    public void setHoursSpent(double hoursSpent) { this.hoursSpent = hoursSpent; }
}
