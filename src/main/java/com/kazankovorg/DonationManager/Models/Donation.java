package com.kazankovorg.DonationManager.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "donations")
public class Donation {
    @Id
    private int id;
    @Column(columnDefinition = "nvarchar(255)")
    private String username;
    @Column(columnDefinition = "nvarchar(300)")
    private String message;
    private double amount;
    private String currency;
    private String created_at;
    @OneToOne(mappedBy = "donation")
    private Note note;
    @ManyToOne
    @JoinColumn(name = "donater_id")
    private Donater donater;
}
