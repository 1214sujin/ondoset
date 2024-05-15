package com.ondoset.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;

import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "Model")
public class Model extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long modelId;

    private Double modelVersion;
    private Boolean adapt;
    private Boolean trainStatus;
    private Long dataCount;
    private Integer numFeatures;
    private Integer iterations;
    private Double learningRate;
    private Double lambda;
    private Double countWeight;
    private Double loss;
    @Column(name = "precision_K")
    private Double precisionK;
    @Column(name = "recall_K")
    private Double recallK;
    @Column(name = "f1_score_K")
    private Double f1ScoreK;

}