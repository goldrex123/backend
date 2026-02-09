# 결제 취소 기능 통합 테스트 가이드

## 📋 개요

결제 취소 기능의 전체 플로우를 검증하기 위한 수동 테스트 가이드입니다.
Phase 4: 결제 취소/환불 구현이 완료되었으며, 다음 항목들을 검증해야 합니다.

## ✅ 검증 항목

### 1. 빌드 검증
- [x] 컴파일 에러 없이 빌드 성공
- [x] 모든 소스 코드 정상 컴파일
- [x] PaymentCancelRepository.findByPaymentId() 메서드 추가 완료

### 2. 애플리케이션 기동 확인

```bash
# 애플리케이션 실행
./gradlew bootRun

# 또는 jar 실행
java -jar build/libs/pg-0.0.1-SNAPSHOT.jar
```

**검증 포인트:**
- Spring Boot 애플리케이션이 정상적으로 시작됨
- MySQL 데이터베이스 연결 성공
- 테이블 스키마 에러 없음
- 모든 Bean이 정상적으로 주입됨

### 3. API 엔드포인트 테스트

#### 3.1 전액 취소 성공 케이스

```bash
# 1단계: 결제 준비
curl -X POST http://localhost:8080/api/v1/payments/prepare \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "TEST_ORDER_001",
    "amount": 10000,
    "method": "CARD",
    "customerName": "홍길동",
    "customerEmail": "test@example.com"
  }'

# 2단계: 결제 승인 (토스페이먼츠 테스트 API 사용)
curl -X POST http://localhost:8080/api/v1/payments/approve \
  -H "Content-Type: application/json" \
  -d '{
    "paymentKey": "test_payment_key_001",
    "orderId": "TEST_ORDER_001",
    "amount": 10000
  }'

# 3단계: 전액 취소
curl -X POST http://localhost:8080/api/v1/payments/TEST_ORDER_001/cancel \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "TEST_ORDER_001",
    "cancelAmount": 10000,
    "cancelReason": "고객 요청"
  }'
```

**예상 결과:**
```json
{
  "success": true,
  "data": {
    "paymentId": 1,
    "orderId": "TEST_ORDER_001",
    "status": "CANCELED",
    "totalAmount": 10000,
    "totalCancelAmount": 10000,
    "cancelableAmount": 0,
    "currentCancelAmount": 10000,
    "cancelReason": "고객 요청",
    "canceledAt": "2026-02-09T..."
  }
}
```

#### 3.2 부분 취소 여러 번

```bash
# 1차 부분 취소 (3,000원)
curl -X POST http://localhost:8080/api/v1/payments/TEST_ORDER_002/cancel \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "TEST_ORDER_002",
    "cancelAmount": 3000,
    "cancelReason": "부분 취소 1차"
  }'
# 예상: status="PARTIAL_CANCELED", totalCancelAmount=3000, cancelableAmount=7000

# 2차 부분 취소 (4,000원)
curl -X POST http://localhost:8080/api/v1/payments/TEST_ORDER_002/cancel \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "TEST_ORDER_002",
    "cancelAmount": 4000,
    "cancelReason": "부분 취소 2차"
  }'
# 예상: status="PARTIAL_CANCELED", totalCancelAmount=7000, cancelableAmount=3000

# 3차 부분 취소 (3,000원 - 남은 금액 전액)
curl -X POST http://localhost:8080/api/v1/payments/TEST_ORDER_002/cancel \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "TEST_ORDER_002",
    "cancelAmount": 3000,
    "cancelReason": "부분 취소 3차"
  }'
# 예상: status="CANCELED", totalCancelAmount=10000, cancelableAmount=0
```

#### 3.3 예외 케이스 테스트

```bash
# 취소 가능 금액 초과
curl -X POST http://localhost:8080/api/v1/payments/TEST_ORDER_003/cancel \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "TEST_ORDER_003",
    "cancelAmount": 15000,
    "cancelReason": "금액 초과"
  }'
# 예상: HTTP 400, errorCode="INVALID_CANCEL_AMOUNT"

# READY 상태 결제 취소 시도
curl -X POST http://localhost:8080/api/v1/payments/TEST_ORDER_READY/cancel \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "TEST_ORDER_READY",
    "cancelAmount": 10000,
    "cancelReason": "READY 상태 취소"
  }'
# 예상: HTTP 400, errorCode="INVALID_PAYMENT_STATE"

# 존재하지 않는 orderId
curl -X POST http://localhost:8080/api/v1/payments/INVALID_ORDER/cancel \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "INVALID_ORDER",
    "cancelAmount": 10000,
    "cancelReason": "존재하지 않는 주문"
  }'
# 예상: HTTP 404, errorCode="PAYMENT_NOT_FOUND"

# orderId 불일치
curl -X POST http://localhost:8080/api/v1/payments/ORDER_A/cancel \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORDER_B",
    "cancelAmount": 10000,
    "cancelReason": "orderId 불일치"
  }'
# 예상: HTTP 400, IllegalArgumentException

# cancelAmount가 0 이하
curl -X POST http://localhost:8080/api/v1/payments/TEST_ORDER_001/cancel \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "TEST_ORDER_001",
    "cancelAmount": 0,
    "cancelReason": "0원 취소"
  }'
# 예상: HTTP 400, Validation Error

# cancelReason이 null
curl -X POST http://localhost:8080/api/v1/payments/TEST_ORDER_001/cancel \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "TEST_ORDER_001",
    "cancelAmount": 10000,
    "cancelReason": null
  }'
# 예상: HTTP 400, Validation Error
```

### 4. 데이터베이스 검증

```sql
-- payments 테이블 확인
SELECT id, order_id, status, amount, payment_key, canceled_at
FROM payments
WHERE order_id = 'TEST_ORDER_001';
-- status가 CANCELED 또는 PARTIAL_CANCELED인지 확인

-- payment_cancels 테이블 확인
SELECT payment_id, cancel_amount, cancel_reason, canceled_at
FROM payment_cancels
WHERE payment_id = (SELECT id FROM payments WHERE order_id = 'TEST_ORDER_001');
-- 취소 이력이 정상 기록되었는지 확인

-- payment_histories 테이블 확인
SELECT payment_id, event_type, request_body, status_code, created_at
FROM payment_histories
WHERE payment_id = (SELECT id FROM payments WHERE order_id = 'TEST_ORDER_001')
  AND event_type = 'CANCEL';
-- CANCEL 이벤트가 기록되었는지 확인
```

### 5. 로그 확인

애플리케이션 로그에서 다음 패턴을 확인합니다:

```
[INFO] 결제 취소 Facade 호출 - orderId: TEST_ORDER_001, cancelAmount: 10000
[INFO] PG 취소 성공 - orderId: TEST_ORDER_001, cancelAmount: 10000
[INFO] 결제 취소 이력 저장 완료 - orderId: TEST_ORDER_001, cancelAmount: 10000
[INFO] 전액 취소 완료 - orderId: TEST_ORDER_001
```

### 6. 멱등성 테스트

```bash
# 동일한 전액 취소 요청을 2번 보냄
curl -X POST http://localhost:8080/api/v1/payments/TEST_ORDER_IDEMPOTENT/cancel \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "TEST_ORDER_IDEMPOTENT",
    "cancelAmount": 10000,
    "cancelReason": "멱등성 테스트"
  }'

# 같은 요청 재전송
curl -X POST http://localhost:8080/api/v1/payments/TEST_ORDER_IDEMPOTENT/cancel \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "TEST_ORDER_IDEMPOTENT",
    "cancelAmount": 10000,
    "cancelReason": "멱등성 테스트"
  }'
```

**검증:**
- 두 요청 모두 HTTP 200 반환
- 응답 데이터가 동일함
- DB에 취소 이력이 중복 생성되지 않음 (1개만 존재)

### 7. 동시성 테스트 (선택사항)

JMeter 또는 Apache Bench로 동시 요청 테스트:

```bash
# Apache Bench로 동시 10개 요청
ab -n 10 -c 5 -p cancel_request.json -T application/json \
   http://localhost:8080/api/v1/payments/TEST_ORDER_CONCURRENT/cancel
```

**검증:**
- 비관적 락으로 순차 처리됨
- DB에 중복 취소 이력 없음
- 금액 계산이 정확함

### 8. 회귀 테스트

기존 결제 준비/승인 API가 정상 작동하는지 확인:

```bash
# 결제 준비 API
curl -X POST http://localhost:8080/api/v1/payments/prepare \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "REGRESSION_TEST_001",
    "amount": 5000,
    "method": "CARD",
    "customerName": "테스트",
    "customerEmail": "regression@test.com"
  }'

# 결제 승인 API
curl -X POST http://localhost:8080/api/v1/payments/approve \
  -H "Content-Type: application/json" \
  -d '{
    "paymentKey": "regression_payment_key",
    "orderId": "REGRESSION_TEST_001",
    "amount": 5000
  }'
```

## 📊 검증 체크리스트

- [ ] 1. 애플리케이션 정상 기동
- [ ] 2. 전액 취소 API 호출 성공 (HTTP 200)
- [ ] 3. 부분 취소 여러 번 호출 성공 (금액 계산 정확)
- [ ] 4. 취소 가능 금액 초과 시 400 에러
- [ ] 5. 잘못된 상태 취소 시 400 에러
- [ ] 6. 존재하지 않는 orderId 취소 시 404 에러
- [ ] 7. DB에 payment_cancels 데이터 정상 저장
- [ ] 8. DB에 payment_histories에 CANCEL 이벤트 기록
- [ ] 9. Payment 상태가 정확히 업데이트됨 (CANCELED/PARTIAL_CANCELED)
- [ ] 10. 멱등성 테스트 통과 (전액 취소 완료 후 재요청 시 기존 결과 반환)
- [ ] 11. 로그에 모든 단계 정상 출력
- [ ] 12. 기존 preparePayment, approvePayment API 정상 작동 (회귀 테스트)

## 🔧 Postman 컬렉션

위의 테스트를 Postman으로 실행하려면:

1. Postman에서 새 컬렉션 생성
2. 각 curl 명령을 Postman 요청으로 변환
3. Environment 변수 설정:
   - `base_url`: `http://localhost:8080`
   - `order_id`: 동적으로 변경 가능

## 📝 주의사항

1. **PG API 모킹**: 실제 토스페이먼츠 API를 사용하려면 테스트 환경 Secret Key가 필요합니다.
2. **MySQL 연결**: MySQL이 실행 중이어야 하며, `application-local.yml`에 설정된 DB 정보가 올바라야 합니다.
3. **데이터 초기화**: 각 테스트 전에 데이터를 초기화하거나, 고유한 orderId를 사용하세요.

## 🎯 성공 기준

위의 12개 검증 항목이 모두 통과하면 Phase 4: 결제 취소/환불 구현이 완료된 것으로 간주합니다.
