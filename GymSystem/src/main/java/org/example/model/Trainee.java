package org.example.model;

import java.time.LocalDate;
import java.util.Date;

public class Trainee extends User{

    private LocalDate dateOfBirth;
    private String address;
    private Long userId;


    public Trainee(Long userId, String firstName, String lastName, String username, String password, boolean isActive,
                   LocalDate dateOfBirth, String address) {
        super(firstName, lastName, username, password, isActive);
        this.userId = userId;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
    }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

}
