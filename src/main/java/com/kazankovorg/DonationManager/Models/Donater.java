package com.kazankovorg.DonationManager.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
    @JsonIgnore
    @OneToMany(mappedBy = "donater")
    private List<Donation> donations;

}
