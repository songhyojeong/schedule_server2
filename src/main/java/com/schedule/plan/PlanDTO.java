package com.schedule.plan;

import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanDTO {
	private Integer s_id;
	private String email;
	private LocalDateTime start_date;
	private LocalDateTime end_date;
	
	@NotBlank(message = "제목은 필수입니다.")
	private String title;
	
	private String content;
    private LocalTime start_time;
    private LocalTime end_time;
    private Integer color;

    public PlanDTO(PlanEntity entity) {
        this.s_id = entity.getS_id();
        this.email = entity.getEmail();
        this.title = entity.getTitle();
        this.content = entity.getContent();
        this.start_date = entity.getStart_date();
        this.end_date = entity.getEnd_date();
        this.start_time = entity.getStart_time();
        this.end_time = entity.getEnd_time();
        this.color=entity.getColor();
    }
}
