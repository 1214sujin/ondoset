package com.ondoset.domain;

import com.ondoset.domain.Enum.Thickness;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Getter
@Setter
@DynamicInsert
public class Clothes extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "clothes_id", columnDefinition = "int unsigned")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tag_id")
    private Tag tag;

    // 옷 이름
    private String name;

    // 두께감
    @Enumerated(EnumType.STRING)
    private Thickness thickness;

    // 옷 이미지
    @Column(name = "image_url")
    private String imageURL;

    private Boolean isDeleted;
}
