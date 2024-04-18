package com.ondoset.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Consisting extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "consisting_id", columnDefinition = "int unsigned")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "clothes_id")
    private Clothes clothes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coordi_id")
    private Coordi coordi;

    public void setConsistings(Coordi coordi, Clothes clothes) {

        this.coordi = coordi;
        coordi.getConsistings().add(this);

        this.clothes = clothes;
        // clothes에서 consisting을 찾을 일이 없기 때문에 설정하지 않음
    }
}