package com.empOnboarding.api.entity;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.*;

import com.sun.istack.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "e_questions")
@AllArgsConstructor
@NoArgsConstructor
public class EQuestions implements java.io.Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "questions")
    private String questions;

    @Column(name = "response_type")
    private String responseType;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level", referencedColumnName = "item_key")
    private LookupItems level;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_time", nullable = true, length = 19)
    private Date createdTime;


}