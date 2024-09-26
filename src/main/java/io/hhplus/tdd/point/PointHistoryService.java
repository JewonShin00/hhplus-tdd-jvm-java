package io.hhplus.tdd.point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class PointHistoryService {
	// 포인트 내역을 관리하는 Map (유저 ID -> 포인트 내역 리스트)
	private final Map<Long, List<PointHistory>> historyTable = new HashMap<>();

	// 포인트 내역 조회
	public List<PointHistory> getHistoriesByUserId(long userId) {
		return historyTable.getOrDefault(userId, new ArrayList<>());
	}

	// 포인트 내역 추가
	public void addHistory(long userId, PointHistory history) {
		historyTable.computeIfAbsent(userId, k -> new ArrayList<>()).add(history);
	}
}
