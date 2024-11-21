package org.example.expert.aop.log;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
public class LoginLog {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long requesterId;

	private Long registeringId;

	@Setter
	private String status; // "SUCCESS" or "FAILURE"

	private LocalDateTime timestamp;

	private LoginLog(Long requesterId, Long registeringId, LocalDateTime time) {
		this.requesterId = requesterId;
		this.registeringId = registeringId;
		this.timestamp = time;
	}

	public static LoginLog createLogOf(Long requesterId, Long registeringId, LocalDateTime time) {
		return new LoginLog(requesterId, registeringId, time);
	}
}
