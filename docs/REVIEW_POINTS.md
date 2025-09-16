# 리뷰 포인트

> Financial Freedom 프로젝트의 핵심 구현을 정리했습니다.
> 각 항목은 **기존 문제 → 고도화 → 적용 방식 → 성과** 순서로 설명합니다.

##  멀티 모듈 아키텍처 설계

### 기존 문제

* 모든 기능이 하나의 서버에 통합된 **모놀리식 구조**
* CPU 집약적(AI 퀴즈), 네트워크 집약적(뉴스 수집), 배치 처리(FSS 동기화) 등이 사용자 API와 충돌
* 결과적으로 **응답 지연·장애 전파·배포 리스크** 발생

### 고도화 방향

* 초기 단계에서 서버 자체를 분리하면 비용 낭비 → 현실적으로 불가능
* 따라서 **포트 기반 모듈 분리**로 먼저 사용자/관리자 책임을 나누고, 추후 서버 분리로 확장할 수 있는 구조를 선택

### 적용 방식

* `main-server (8080)` : 사용자 서비스 전담
* `admin-server (8081)` : 관리자·배치·AI 연산 전담
* **도메인 중심 패키징**으로 향후 **MSA 전환 대비**

**구현 위치**: [`settings.gradle`](https://github.com/9oormthon-univ/2025_SEASONTHON_TEAM_25_BE/blob/main/settings.gradle)

### 성과

* **리소스·장애 격리** 실현
* 배포·확장성 확보
* “통합 구조 → 모듈 구조”로 고도화하여 **점진적 서버 분리 전략** 마련

---

## 성능 최적화 (비동기 처리)

### 기존 문제

* 홈 대시보드에서 캐릭터명·출석·지갑·퀴즈 데이터를 **동기식 순차 호출**
* 불필요하게 누적 대기시간 발생 → **조회 속도 저하**

### 고도화 방향

* **캐싱 vs 비동기** 고민

    * 인메모리 캐싱: 서버 메모리에 부담
    * Redis: 별도 서버 비용 발생
* 현재는 **CompletableFuture 기반 비동기 처리**를 선택 → 메모리·비용 부담 없이 성능 향상
* 추후 자원 확보 시 **캐싱과 조합**하여 확장 예정

### 적용 방식

* 4개 도메인 서비스 호출을 **CompletableFuture 병렬 실행**
* 타임아웃 + Fallback으로 안정성 보장
* 퀘스트 업데이트도 **미완료만 병렬 처리**

**구현 위치**:

* [`HomeFacadeService.java`](https://github.com/9oormthon-univ/2025_SEASONTHON_TEAM_25_BE/blob/main/main-server/src/main/java/com/freedom/home/application/HomeFacadeService.java)
* [`QuestFacade.java`](https://github.com/9oormthon-univ/2025_SEASONTHON_TEAM_25_BE/blob/main/main-server/src/main/java/com/freedom/quest/application/QuestFacade.java)

### 성과

* 평균 응답시간 **40% 단축** (10.89ms → 6.52ms)
* 최대 응답시간 **78% 개선** (54.14ms → 11.95ms)
* 기존 동기식 조회 구조를 고도화하여 **UX와 서버 처리량 동시 개선**

[`HomePerformanceTest`](https://github.com/9oormthon-univ/2025_SEASONTHON_TEAM_25_BE/blob/main/main-server/src/test/java/com/freedom/performance/HomeFacadePerformanceTest.java) - 테스트 컨테이너/더미데이터 기반 성능 테스트 (100회 실행)
> 2025-09-16 20:14:26.323 [Test worker] INFO  c.f.p.HomeFacadePerformanceTest - ⚡ Async : avg=6.52ms | min=3.78ms | max=11.95ms (n=100)  
> 2025-09-16 20:14:26.323 [Test worker] INFO  c.f.p.HomeFacadePerformanceTest - 🐢 Sync  : avg=10.89ms | min=6.35ms | max=54.14ms (n=100)  
> 2025-09-16 20:14:26.323 [Test worker] INFO  c.f.p.HomeFacadePerformanceTest - 🚀 Result: 평균 40.10% 개선 (Sync→Async)
---

## 멱등성 & 동시성 제어

### 기존 문제

* 지갑/적금 처리에서 **락이 없는 구조** →

    * 버튼 연타 시 중복 거래 발생 위험
    * 자동 납입·퀘스트 보상 동작 시 동시성 충돌 가능성

### 고도화 방향

* **락 없음 → 보수적 동시성 제어 적용**
* 낙관적 락도 고려했으나, 금융 도메인은 실패 자체가 치명적 → **비관적 락(PESSIMISTIC\_WRITE)** 채택
* 동시에 **requestId 기반 멱등성**을 도입하여 중복 요청 완전 차단

### 적용 방식

1. **requestId unique 제약** → 동일 요청은 1회만 처리
2. **비관적 락**으로 지갑 조회 시 동시성 충돌 방지
3. **납입 정책 준수** : 하루 1회 제한 + 거래 이력 저장

**구현 위치**:

* [`WalletTransactionJpaRepository.java`](https://github.com/9oormthon-univ/2025_SEASONTHON_TEAM_25_BE/blob/main/main-server/src/main/java/com/freedom/wallet/infra/WalletTransactionJpaRepository.java)
* [`SavingTransactionService.java`](https://github.com/9oormthon-univ/2025_SEASONTHON_TEAM_25_BE/blob/main/main-server/src/main/java/com/freedom/wallet/application/SavingTransactionService.java)
* [`SavingPaymentCommandService.java`](https://github.com/9oormthon-univ/2025_SEASONTHON_TEAM_25_BE/blob/main/main-server/src/main/java/com/freedom/saving/application/SavingPaymentCommandService.java)

### 성과

* 버튼 연타·중복 요청으로 인한 **이중 거래 차단**
* 자동납입 시에도 **안정적인 동시성 제어**
* 기존 락이 없던 구조를 고도화하여 **금융 서비스에 적합한 안정성 확보**

---

## 도메인 서비스 모듈화 & 파사드 패턴

### 선택 배경

* 단일 서비스 클래스는 단위 테스트와 유지보수에 취약
* **서비스 통합 vs 단일 책임 분리** 중 → 단일 책임 원칙을 지키고 확장성을 위해 **도메인 서비스 + 파사드 패턴**을 선택

### 적용 방식

* 회원가입/검증/조회 등 **단일 책임 도메인 서비스** 구현
* 파사드에서 여러 도메인 서비스를 조합 → 트랜잭션을 한 번에 관리

**구현 위치**:

* [SignUpUserService.java](https://github.com/9oormthon-univ/2025_SEASONTHON_TEAM_25_BE/blob/main/main-server/src/main/java/com/freedom/auth/domain/service/SignUpUserService.java)
* [ValidateUserService.java](https://github.com/9oormthon-univ/2025_SEASONTHON_TEAM_25_BE/blob/main/main-server/src/main/java/com/freedom/auth/domain/service/ValidateUserService.java)
* [FindUserService.java](https://github.com/9oormthon-univ/2025_SEASONTHON_TEAM_25_BE/blob/main/main-server/src/main/java/com/freedom/auth/domain/service/FindUserService.java)
* [AuthFacade.java](https://github.com/9oormthon-univ/2025_SEASONTHON_TEAM_25_BE/blob/main/main-server/src/main/java/com/freedom/auth/application/AuthFacade.java)

### 성과

* **단위 테스트 용이성 확보**
* 기능 확장 시 독립적 관리 가능
* 파사드 레벨에서 **안정적인 트랜잭션 경계 보장**

---

## 테스트 환경 (Testcontainers)

* **도입 이유**: H2로는 MySQL과 차이가 있어 버그 조기 발견 어려움
* MySQL 8.4.5 컨테이너로 프로덕션과 동일한 환경을 구성
* UTF8MB4·KST 설정으로 한국어/이모지/날짜 처리 안정성 확보

**구현 위치**: [`TestContainerConfig.java`](https://github.com/9oormthon-univ/2025_SEASONTHON_TEAM_25_BE/blob/main/main-server/src/test/java/com/freedom/common/test/TestContainerConfig.java)

---

## 글로벌 에러 핸들링

* **도입 이유**: 예외 처리 방식 통일 + 도메인별 ErrorCode 체계화
* Custom Exception 설계로 구체성 확보
* GlobalExceptionHandler로 일관 응답 제공

**구현 위치**:

* [`ErrorCode.java`](https://github.com/9oormthon-univ/2025_SEASONTHON_TEAM_25_BE/blob/main/main-server/src/main/java/com/freedom/common/exception/ErrorCode.java)
* [`GlobalExceptionHandler.java`](https://github.com/9oormthon-univ/2025_SEASONTHON_TEAM_25_BE/blob/main/main-server/src/main/java/com/freedom/common/exception/GlobalExceptionHandler.java)

---

## 운영 모니터링 (Discord 연동)

* **도입 이유**: 심각한 오류를 실시간으로 감지하고 대응 속도를 높이기 위함
* KST 기준 타임스탬프와 Embed 포맷으로 가독성 강화
* 알림 실패가 서비스 동작에 영향을 주지 않는 안전한 구조

**구현 위치**: [`DiscordWebhookClient.java`](https://github.com/9oormthon-univ/2025_SEASONTHON_TEAM_25_BE/blob/main/main-server/src/main/java/com/freedom/common/notification/DiscordWebhookClient.java)

---

## Key Points

1. **모놀리식 고도화** : 서버 비용 최소화 + 포트 분리로 책임 분리
2. **성능 고도화** : 동기식 조회를 병렬 처리로 개선 (40% 속도 향상)
3. **안정성 고도화** : 락 없는 구조를 멱등성 + 비관적 락으로 보강
4. **확장성 고려** : 단일 책임 서비스 + 파사드로 유지보수성 강화 + 모듈화를 통한 재사용성 극대화
5. **운영 품질** : Testcontainers / 글로벌 에러 핸들링 / Discord 알림
