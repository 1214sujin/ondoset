package com.ondoset.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "metrics")
public class Metrics extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resultId;

    private String type;
    private Integer epoch;
    private Double value;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "model_id")
    private Model model;

}
