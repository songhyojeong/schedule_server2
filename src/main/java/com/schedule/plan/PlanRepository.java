package com.schedule.plan;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanRepository extends JpaRepository<PlanEntity,Integer> {

	@Query("""
			select p from PlanEntity p
			where p.email = :email
				and p.start_date <= :dayEnd
				and p.end_date >= :dayStart
			order by p.start_date asc
			""")


	List<PlanEntity> findByPlanOfDay(
			@Param("email") String email,
			@Param("dayStart") LocalDateTime dayStart,
			@Param("dayEnd") LocalDateTime dayEnd);


	@Query("""
			select p from PlanEntity p
			where p.email = :email
				and p.start_date <= :rangeEnd
				and p.end_date >= :rangeStart
			order by p.start_date asc
			""")

	List<PlanEntity> findPlansOverlapping(
			@Param("email") String email,
			@Param("rangeStart") LocalDateTime rangeStart,
			@Param("rangeEnd") LocalDateTime rangeEnd
			);
	
	Optional<PlanEntity> findById(int s_id);


}
