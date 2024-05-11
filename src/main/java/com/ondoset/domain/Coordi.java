package com.ondoset.domain;

import com.ondoset.domain.Enum.Satisfaction;
import com.ondoset.domain.Enum.Weather;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
public class Coordi extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coordi_id", columnDefinition = "int unsigned")
    private Long id;

    // 코디 날짜
    @Column(columnDefinition = "bigint")
    private Long date;

    // 코디 만족도
    @Enumerated(EnumType.STRING)
    private Satisfaction satisfaction;

    private String region;

    // 나간 시간
    @Column(columnDefinition = "bigint")
    private Long departTime;

    // 들어온 시간
    @Column(columnDefinition = "bigint")
    private Long arrivalTime;

	// 날씨
    @Enumerated(EnumType.STRING)
    private Weather weather;

    // 최저 기온
    private Integer lowestTemp;

    // 최고 기온
    private Integer highestTemp;

    // 코디 이미지
    @Column(name = "image_url")
    private String imageURL;

    @OneToMany(mappedBy = "coordi")
    private List<Consisting> consistings = new ArrayList<>();
}
