package io.hhplus.tdd.point;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import io.hhplus.tdd.database.UserPointTable;

@Service
public class PointService {
	private final UserPointTable userPointTable;
	private final PointHistoryService pointHistoryService;  // 포인트 내역 관리 서비스

	public PointService(UserPointTable userPointTable, PointHistoryService pointHistoryService) {
		this.userPointTable = userPointTable;
		this.pointHistoryService = pointHistoryService;
	}

	public UserPoint getPoint(long userId) {
		//유저 포인트 조회(기본테스트에서는 리턴할 때 그냥 바로 던졌으나 유저없는 경우 테스트를 위해 한 번 담음)
		UserPoint userPoint = userPointTable.selectById(userId);

		//유저가 없을 경우 예외처리
		if (userPoint == null) {
			throw new NoSuchElementException("UserId : " + userId + " not found");
		}

		return userPoint;
	}

	public UserPoint chargePoint(long userId, long chargeAmt) {
		//충전 금액이 0 또는 음수인 경우 예외 발생(testInvalidCharge() 테스트진행하면서 추가)
		if (chargeAmt <= 0) {
			throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다.");
		}

		//기존 포인트 조회
		UserPoint userPoint = userPointTable.selectById(userId);

		//포인트 합산(기존 + 충전)
		long sumPoint = userPoint.point() + chargeAmt;

		//보유포인트를 합산포인트로 변경
		UserPoint updateUserPoint = userPointTable.insertOrUpdate(userId, sumPoint);

		// 충전 내역 기록(특정 유저의 포인트 충전/이용 내역을 조회하는 기능 테스트)
		PointHistory history = new PointHistory(System.currentTimeMillis(), userId, chargeAmt, TransactionType.CHARGE,
			System.currentTimeMillis());

		pointHistoryService.addHistory(userId, history);

		return updateUserPoint;
	}

	public UserPoint usePoint(long userId, long amount) {
		// 잘못된 금액 입력 처리
		if (amount <= 0) {
			throw new IllegalArgumentException("사용 금액은 0보다 커야 합니다.");
		}
		// 유저의 현재 포인트를 조회
		UserPoint userPoint = userPointTable.selectById(userId);

		// 포인트가 충분한지 확인 (잔고 부족 시 예외 발생)
		if (userPoint.point() < amount) {
			throw new IllegalArgumentException("포인트가 부족합니다.");
		}

		// 기존 포인트에서 사용 금액을 차감하여 새로운 포인트 계산
		long newPoint = userPoint.point() - amount;

		// 새로운 포인트로 업데이트
		UserPoint updatedUserPoint = userPointTable.insertOrUpdate(userId, newPoint);

		// 사용 내역 기록(특정 유저의 포인트 충전/이용 내역을 조회하는 기능 테스트)
		PointHistory history = new PointHistory(System.currentTimeMillis(), userId, amount, TransactionType.USE,
			System.currentTimeMillis());
		pointHistoryService.addHistory(userId, history);

		// 업데이트된 포인트 값을 반환
		return updatedUserPoint;
	}

	// 포인트 내역 조회
	public List<PointHistory> getPointHistories(long userId) {
		return pointHistoryService.getHistoriesByUserId(userId);
	}

}
