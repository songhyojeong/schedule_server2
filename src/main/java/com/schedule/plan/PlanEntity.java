package com.schedule.plan;

import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table( name="user_schedule")

public class PlanEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer s_id;

	@Column(length=50)
	private String email;

	@Column(name="start_date")
	private LocalDateTime start_date;

	@Column(name="end_date")
	private LocalDateTime end_date;

	@Column(length = 255)
	private String title;

	@Column(columnDefinition = "TEXT")
	private String content;

	@Column(name = "start_time")
    private LocalTime start_time;

    @Column(name = "end_time")
    private LocalTime end_time;
    
    @Column
    private Integer color;

    public PlanEntity(PlanDTO planDto){
    	this.email=planDto.getEmail();
    	this.start_date=planDto.getStart_date();
    	this.end_date=planDto.getEnd_date();
    	this.title=planDto.getTitle();
    	this.content=planDto.getContent();
    	this.start_time=planDto.getStart_time();
    	this.end_time=planDto.getEnd_time();
    	this.color=planDto.getColor();
    	}

    }

