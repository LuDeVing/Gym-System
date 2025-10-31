package org.example.model;

public class Trainer extends User {

    private String specialization;
    private Long userId;

    public Trainer() {}

    public Trainer(Long userId, String firstName, String lastName, String username, String password, boolean isActive, String specialization) {
        super(firstName, lastName, username, password, isActive);
        this.userId = userId;
        this.specialization = specialization;
    }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

}
