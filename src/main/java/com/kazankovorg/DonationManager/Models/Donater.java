package com.kazankovorg.DonationManager.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity(name = "donaters")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Donater {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(columnDefinition = "nvarchar(100)")
    private String username;
    @Column(columnDefinition = "nvarchar(100)")
    private String status;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
    @OneToMany(mappedBy = "donater")
    private List<Donation> donations;
}
