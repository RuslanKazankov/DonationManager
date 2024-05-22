package com.kazankovorg.DonationManager.Models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "users")
@ToString
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(columnDefinition = "nvarchar(100)")
    @Size(min = 3, max = 100, message = "Длина имени 3-100 символов")
    private String username;
    @Size(max = 100, message = "Некорректная длина почты")
    @Pattern(regexp = ".*@.*", message = "Некорректный адрес почты")
    private String email;
    @Column(columnDefinition = "nvarchar(100)")
    @Size(min = 8, max = 100, message = "Длина пароля 8-100 символов")
    private String password;
    @Column(name = "datoken", length = 1200)
    private String datoken;
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
            name = "users_roles",
            joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "role_id", referencedColumnName = "id")}
    )
    private List<Role> roles;
    @OneToMany(mappedBy = "user")
    private List<Note> notes;
    @OneToMany(mappedBy = "user")
    private List<Donater> donaters;
}