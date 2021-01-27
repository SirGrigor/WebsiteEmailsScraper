package com.ilgrig.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "prospects")
@Entity
public class Prospect {

    @Id
    @Column(name = "prospect_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long prospectId;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "email")
    private String prospectEmails;
}