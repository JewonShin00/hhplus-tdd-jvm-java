package io.hhplus.tdd.point;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/point")
public class PointController {

	private static final Logger log = LoggerFactory.getLogger(PointController.class);
	private final PointService pointService;

	public PointController(PointService pointService) {
		this.pointService = pointService;
	}

	/**
	 * TODO - 특정 유저의 포인트를 조회하는 기능을 작성해주세요.
	 */
	@GetMapping("{id}")
	public UserPoint point(
		@PathVariable long id
	) {
		log.info("포인트 조회 요청 - 유저 ID: {}", id);
		return pointService.getPoint(id);  // 서비스 호출하여 포인트 조회
	}

	/**
	 * TODO - 특정 유저의 포인트 충전/이용 내역을 조회하는 기능을 작성해주세요.
	 */
	@GetMapping("{id}/histories")
	public List<PointHistory> history(
		@PathVariable long id
	) {
		log.info("포인트 내역 조회 요청 - 유저 ID: {}", id);
		return pointService.getPointHistories(id);  // 서비스 호출하여 포인트 내역 조회
	}

	/**
	 * TODO - 특정 유저의 포인트를 충전하는 기능을 작성해주세요.
	 */
	@PatchMapping("{id}/charge")
	public UserPoint charge(
		@PathVariable long id,
		@RequestBody long amount
	) {
		log.info("포인트 충전 요청 - 유저 ID: {}, 충전 금액: {}", id, amount);
		return pointService.chargePoint(id, amount);  // 서비스 호출하여 포인트 충전
	}

	/**
	 * TODO - 특정 유저의 포인트를 사용하는 기능을 작성해주세요.
	 */
	@PatchMapping("{id}/use")
	public UserPoint use(
		@PathVariable long id,
		@RequestBody long amount
	) {
		log.info("포인트 사용 요청 - 유저 ID: {}, 사용 금액: {}", id, amount);
		return pointService.usePoint(id, amount);  // 서비스 호출하여 포인트 사용
	}
}
