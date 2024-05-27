package com.kazankovorg.DonationManager.Models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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
    @JsonIgnore
    @OneToOne(mappedBy = "donation")
    private Note note;
    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "donater_id")
    private Donater donater;
}
