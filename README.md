# Financial Freedom Project

[![Java](https://img.shields.io/badge/Java-21-red.svg)](https://openjdk.java.net/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-green.svg)](https://spring.io/projects/spring-boot)


## [리뷰 포인트 - 바로가기](./docs/REVIEW_POINTS.md)
**본선 평가 고도화 핵심 기술 구현 사례들을 코드링크와 함께 정리**
- 멀티 모듈 아키텍처 설계
- 모듈화를 통한 재사용성 극대화
- 비동기 처리를 통한 성능 최적화
- 멱등성 보장 및 동시성 제어  
- Testcontainers 도커 기반 테스트

---

## 핵심 성과

### 정량적 지표
- **API 응답 시간**: 비동기 처리로 응답시간 단축
  - 평균 응답시간 40% 단축 (10.89ms → 6.52ms)
  - 최대 응답시간 78% 개선 (54.14ms → 11.95ms)

- **시스템 안정성**: 멱등성 보장으로 금융 사고 0건 목표
- **장애 대응**: Discord 알림으로 즉시 대응 체계 구축

### 기술적 완성도와 확장성
- **도메인 주도 설계**: 11개 도메인별 완전 분리
- **멱등성 보장**: requestId 기반 중복 처리 방지
- **비동기 최적화**: CompletableFuture + Fallback 처리
- **AI 활용**: GPT-4 기반 콘텐츠 생성

### 현실적 확장성
모놀리식 구조이지만 추후 서버 분리까지 고려해 개발을 진행했습니다. 현재는 비용 효율적으로, 미래는 확장 가능하게 설계된 균형잡힌 아키텍처입니다.

- **포트 분리**: 현재 비용 효율 + 추후 서버 분리 용이
- **모듈화**: 도메인별 독립 개발 및 배포 가능
- **단일 책임 원칙**: 각 모듈이 하나의 책임만 담당

### 핵심 가치

- **🤖 AI 기반 콘텐츠 제작**: OpenAI GPT-4를 활용한 맞춤형 퀴즈 힌트 및 콘텐츠 생성
- **⚡ 완전 자동화**: 뉴스 수집, 스케줄링까지 무인 운영
- **🏗️ 엔터프라이즈 아키텍처**: 도메인 분리, 멱등성 보장, 확장 가능한 설계
- **📊 게임화**: 출석, 퀴즈, 업적 시스템으로 재미있는 금융 학습
- **🔒 안정성 확보**: 트랜잭션, 동시성, 멱등성 보장으로 안전한 서비스 설계

## 시스템 아키텍처

### <img width="1090" height="562" alt="파프 아키텍처" src="https://github.com/user-attachments/assets/6046e837-92f0-43b5-92ae-dc9ec8abdaef" />

## 👥 팀 정보

**2025 SEASONTHON TEAM 25**
- 이연우(기획)
- 장서휘(디자인) [GitHub](https://github.com/oesreen)
- 최영빈(앱/플러터) [GitHub](https://github.com/yb0x00)
- 김기현(서버/백엔드) [GitHub](https://github.com/gihhyeon)
- 노영오(서버/백엔드) [GitHub](https://github.com/NohYeongO)

---

> 💡 **"현재는 비용 효율적으로, 미래는 확장 가능하게"**
>
> Financial Freedom은 스타트업의 현실적 제약을 고려하면서도 엔터프라이즈급 확장성을 동시에 달성한 균형잡힌 플랫폼입니다.
