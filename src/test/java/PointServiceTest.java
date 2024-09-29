import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.PointHistoryService;
import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;

public class PointServiceTest {

	@Mock
	private UserPointTable userPointTable;  // UserPointTable 모킹

	@Mock
	private PointHistoryService pointHistoryService;

	@InjectMocks
	private PointService pointService;  // 테스트할 PointService 클래스

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);  // Mock 객체 초기화, 모든 테스트에서 Mock 객체가 제대로 주입되도록 설정
	}

	@DisplayName("특정 유저의 포인트를 조회하는 기능 테스트_기본")
	@Test
	void testPoint() {
		// given: 유저 ID가 1인 경우
		long userId = 1L;

		// when: userPointTable이 해당 유저 ID로 포인트를 조회할 때, 기본 값(포인트 0)을 반환하도록 설정
		when(userPointTable.selectById(userId)).thenReturn(new UserPoint(userId, 0, System.currentTimeMillis()));

		// PointService를 통해 포인트를 조회
		UserPoint result = pointService.getPoint(userId);

		// then: 기본 포인트가 0인지 확인
		assertEquals(0, result.point());
		assertEquals(userId, result.id());  // 조회된 유저 ID가 맞는지 확인
	}

	@DisplayName("특정 유저의 포인트를 조회하는 기능 테스트_유저 없는 경우 예외처리")
	@Test
	void testPointUserNotExist() {
		//given: 존재하지 않는 유저 ID 999L
		long userId = 999L;

		//when: userPointTable에서 유저를 못찾으면 null 반환
		when(userPointTable.selectById(userId)).thenReturn(null);

		//then: 유저가 없을 때 예외발생, assertThrows: 예외처리 확인
		assertThrows(NoSuchElementException.class, () -> pointService.getPoint(userId));
	}

	@DisplayName("특정 유저의 포인트 충전/이용 내역을 조회하는 기능 테스트")
	@Test
	void testHistory() {
		// given: 유저 ID 1번의 포인트 충전 및 사용 내역이 있음
		long userId = 1L;
		List<PointHistory> mockHistories = List.of(
			new PointHistory(1L, userId, 50L, TransactionType.CHARGE, System.currentTimeMillis()),  // 충전 내역
			new PointHistory(2L, userId, 30L, TransactionType.USE, System.currentTimeMillis())       // 사용 내역
		);

		// Mock 설정: 유저의 포인트 내역이 반환되도록 설정
		when(pointHistoryService.getHistoriesByUserId(userId)).thenReturn(mockHistories);

		// when: 내역 조회 실행
		List<PointHistory> result = pointService.getPointHistories(userId);

		// then: 반환된 내역이 예상한 것과 동일한지 확인
		assertEquals(2, result.size());  // 내역이 2건 있는지 확인
		assertEquals(TransactionType.CHARGE, result.get(0).type());  // 첫 번째는 충전 내역
		assertEquals(TransactionType.USE, result.get(1).type());     // 두 번째는 사용 내역
	}

	@DisplayName("특정 유저의 포인트 충전/이용 내역을 조회하는 기능 테스트_잔고 부족")
	@Test
	void testUsePointInsufficient() {
		// given: 유저 ID 1번이 있고, 잔고가 30포인트인 상태
		long userId = 1L;
		long useAmount = 50L;  // 사용하려는 포인트가 잔고보다 많음
		UserPoint userPoint = new UserPoint(userId, 30L, System.currentTimeMillis());  // 잔고 30

		// Mock 설정: 유저 ID 1번의 현재 포인트를 30으로 설정
		when(userPointTable.selectById(userId)).thenReturn(userPoint);

		// when & then: 잔고가 부족할 경우 예외가 발생해야 함
		assertThrows(IllegalArgumentException.class, () -> pointService.usePoint(userId, useAmount));
	}

	@DisplayName("특정 유저의 포인트 충전/이용 내역을 조회하는 기능 테스트_잘못된 사용 금액")
	@Test
	void testUsePointInvalidAmount() {
		// given: 유저 ID 1번이 있고, 0 또는 음수의 금액을 사용하려고 함
		long userId = 1L;
		long invalidUseAmount = -10L;  // 잘못된 금액 (음수)

		// Mock 설정: 유저 ID 1번의 현재 포인트를 100으로 설정
		UserPoint userPoint = new UserPoint(userId, 100L, System.currentTimeMillis());
		when(userPointTable.selectById(userId)).thenReturn(userPoint);

		// when & then: 0 이하의 금액을 사용할 때 예외가 발생해야 함
		assertThrows(IllegalArgumentException.class, () -> pointService.usePoint(userId, invalidUseAmount));
	}

	@DisplayName("특정 유저의 포인트를 충전하는 기능 테스트_기본")
	@Test
	void testCharge() {
		// given: userId 1번이 50 포인트를 충전하려고 함
		long userId = 1L;
		long chargeAmt = 50L;    //충전할 포인트

		//기존에 유저가 가지고 있던 포인트가 100인 경우
		UserPoint existUserPoint = new UserPoint(userId, 100L, System.currentTimeMillis());

		// when: 충전 후 포인트가 150으로 업데이트
		when(userPointTable.selectById(userId)).thenReturn(existUserPoint);
		when(userPointTable.insertOrUpdate(userId, 150L)).thenReturn(
			new UserPoint(userId, 150L, System.currentTimeMillis()));

		// then: 충전 결과가 150인지 확인
		UserPoint result = pointService.chargePoint(userId, chargeAmt);
		assertEquals(150L, result.point());  // 충전 후 150 포인트가 되어야 함
	}

	@DisplayName("특정 유저의 포인트를 충전하는 기능 테스트_0또는 음수 충전")
	@Test
	void testInvalidCharge() {
		long userId = 1L;

		// 0포인트 충전 시도 > 예외 발생 확인
		assertThrows(IllegalArgumentException.class, () -> pointService.chargePoint(userId, 0L));
		// 음수인 포인트 충전 시도 > 예외 발생 확인
		assertThrows(IllegalArgumentException.class, () -> pointService.chargePoint(userId, -50L));
	}

	@DisplayName("특정 유저의 포인트를 사용하는 기능 테스트")
	@Test
	void testUse() {

		// given :  userId가 1인 유저가 100포인트를 가지고 있음
		long userId = 1L;
		long useAmt = 50L; //사용할 금액
		UserPoint existUserPoint = new UserPoint(userId, 100L, System.currentTimeMillis());

		// 유저 ID 1번의 현재 포인트를 100으로 설정
		when(userPointTable.selectById(userId)).thenReturn(existUserPoint);
		// 유저 ID 1번이 50 포인트 사용 후, 최종 포인트는 50으로 업데이트
		when(userPointTable.insertOrUpdate(userId, 50L)).thenReturn(
			new UserPoint(userId, 50L, System.currentTimeMillis()));
		// when : 포인트 사용 실행
		UserPoint result = pointService.usePoint(userId, useAmt);
		// then : 사용 후 포인트가 50인지 확인
		assertEquals(useAmt, result.point());

	}

	@DisplayName("특정 유저의 포인트를 사용하는 기능 테스트_포인트부족시 예외처리")
	@Test
	void testUseInsufficient() {
		// 유저 ID 1번이 있고, 100 포인트를 가지고 있음
		long userId = 1L;
		long useAmount = 150L;  // 사용할 금액이 150, 즉 잔고보다 많음
		UserPoint existingUserPoint = new UserPoint(userId, 100L, System.currentTimeMillis());  // 현재 포인트는 100

		// Mock 설정: 유저 ID 1번의 현재 포인트를 100으로 설정
		when(userPointTable.selectById(userId)).thenReturn(existingUserPoint);

		// 포인트 사용 시 잔고 부족으로 예외가 발생해야 함
		assertThrows(IllegalArgumentException.class, () -> pointService.usePoint(userId, useAmount));
	}

	@DisplayName("특정 유저의 포인트를 사용하는 기능 테스트_동시성")
	@Test
	void testSynchroUse() throws InterruptedException {
		//모킹된 객체는 상태를 유지하지 않기 때문에 동시성 테스트에서는 적합하지 않다고 함
		UserPointTable realUserPointTable = new UserPointTable();
		PointService pointService = new PointService(realUserPointTable, pointHistoryService);

		// given - 초기 포인트 설정
		long userId = 1L;
		long initialPoints = 500L;
		long useAmount = 100L;
		int threadCount = 5; // 스레드의 수는 5개

		// 유저 포인트 초기화 (test 실행 전에 반드시 초기 데이터 설정)
		realUserPointTable.insertOrUpdate(userId, initialPoints);

		// when - 스레드를 통해 포인트를 차감하는 작업 수행
		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
		for (int i = 0; i < threadCount; i++) {
			executorService.execute(() -> {
				pointService.usePoint(userId, useAmount);
			});
		}

		executorService.shutdown();
		executorService.awaitTermination(30, TimeUnit.SECONDS); // 모든 스레드가 작업을 끝낼 때까지 대기

		// then - 최종 포인트가 정확히 차감되었는지 검증
		UserPoint finalPoint = realUserPointTable.selectById(userId); // 최종 포인트 확인
		assertEquals(0L, finalPoint.point(), "모든 포인트가 정상적으로 차감되었는지 확인");
	}
}
