package com.kazankovorg.DonationManager.Models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DaUser {
    private int id;
    private String code;
    private String name;
    private String avatar;
    private String email;
    private String socket_connection_token;
}
