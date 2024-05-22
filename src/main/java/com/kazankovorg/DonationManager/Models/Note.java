package com.kazankovorg.DonationManager.Models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "notes")
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Size(max = 300, message = "Попробуйте уложиться в 300 символов")
    @Column(columnDefinition = "nvarchar(300)", name = "text", length = 1200)
    private String text;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "donation_id", referencedColumnName = "id")
    private Donation donation;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
}
