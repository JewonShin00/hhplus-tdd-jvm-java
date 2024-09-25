package io.hhplus.tdd.point;

import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import io.hhplus.tdd.database.UserPointTable;

@Service
public class PointService {
	private final UserPointTable userPointTable;

	public PointService(UserPointTable userPointTable) {
		this.userPointTable = userPointTable;
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
		//기존 포인트 조회
		UserPoint userPoint = userPointTable.selectById(userId);

		//포인트 합산(기존 + 충전)
		long sumPoint = userPoint.point() + chargeAmt;

		//보유포인트를 합산포인트로 변경
		UserPoint updateUserPoint = userPointTable.insertOrUpdate(userId, sumPoint);

		return updateUserPoint;
	}
}
