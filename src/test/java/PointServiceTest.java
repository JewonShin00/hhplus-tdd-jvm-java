import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.UserPoint;

public class PointServiceTest {

	@Mock
	private UserPointTable userPointTable;  // UserPointTable 모킹

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

	// 특정 유저의 포인트 충전/이용 내역을 조회하는 기능 테스트
	@Test
	void testHistory() {

		// given

		// when

		// then

	}

	@DisplayName("특정 유저의 포인트를 충전하는 기능 테스트_기본")
	@Test
	void testCharge() {
		// given: userId 1번이 50 포인트를 충전하려고 함
		long userId = 1L;
		long chargeAmt = 50L;

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

	// 특정 유저의 포인트를 사용하는 기능 테스트
	@Test
	void testUse() {

		// given

		// when

		// then

	}
}
