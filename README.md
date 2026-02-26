# spring-gift-refactoring

## 1단계 - 리팩터링 준비하기

> 핵심 목표: 작동을 바꾸기 쉬운 상태를 만든다. 구조 변경을 통해 변경 난이도를 낮추되 작동은 유지한다.

---

## 기능 목록

### 1. 스타일 정리 (작동 변경 없음)

- [ ] `@Autowired` 제거 통일 — 단일 생성자 빈에서 불필요한 `@Autowired` 제거
  - 대상: `MemberController`, `AdminMemberController`, `AuthenticationResolver`, `JwtProvider`
- [ ] `@RequestMapping` 속성 스타일 통일 — `path = "/..."` 대신 `"/..."` 축약형으로 통일
  - 대상: `KakaoAuthController`, `OptionController`
- [ ] 변수 선언 스타일 통일 — `var` 또는 명시적 타입 중 하나로 통일, 불필요한 `final` 제거
  - `var` 사용: `OrderController`, `WishController`
  - 명시적 타입 + `final` 사용: `MemberController`, `AdminMemberController`, `AuthenticationResolver`
- [ ] 주석/Javadoc 스타일 통일 — Javadoc(`/** */`) 또는 블록 주석(`/* */`) 중 하나로 통일
  - Javadoc 사용: `member` 패키지, `auth` 패키지 일부
  - 블록 주석 사용: `OptionController`, `KakaoAuthController`, `OptionNameValidator`
  - 주석 없음: `category`, `product`, `order`, `wish` 대부분
- [ ] HTTP 상태 코드 표현 통일 — 매직 넘버(`401`, `403`) 대신 `HttpStatus` 열거형 사용
  - 대상: `WishController`, `OrderController`
- [ ] Stream 종결 연산 통일 — `.collect(Collectors.toList())` → `.toList()`로 통일
  - 대상: `OptionController`
- [ ] 에러 메시지 언어 통일 — 한국어 또는 영어 중 하나로 통일
  - 한국어: `AdminProductController`, `OptionController`, `Member`, 각 Validator
  - 영어: `MemberController`, `AdminMemberController`, `Member` 일부
- [ ] `ResponseEntity` 제네릭 타입 통일 — `ResponseEntity<?>` 대신 구체적 타입 사용
  - 대상: `OrderController`

### 2. 불필요한 코드 제거 (작동 변경 없음)

- [ ] `OrderController`에서 미사용 `WishRepository` 의존성 제거
  - 필드, 생성자 파라미터, import 모두 제거 (주석 "6. cleanup wish"도 함께 제거)
- [ ] `OptionController`에서 `import java.util.stream.Collectors` 제거 (`.toList()` 전환 후)
- [ ] 각 Controller의 중복 `@ExceptionHandler` 제거 대비 확인
  - `ProductController`, `OptionController`, `MemberController`에 동일한 `IllegalArgumentException` 핸들러 존재

### 3. 서비스 계층 추출 (구조 변경, 작동 변경 없음)

- [ ] **ProductService** 추출
  - `ProductController`의 상품 CRUD 로직 (이름 검증, 카테고리 조회, 생성/수정/삭제)
  - `AdminProductController`의 상품 CRUD 로직 (동일 로직 중복 제거)
- [ ] **CategoryService** 추출
  - `CategoryController`의 카테고리 CRUD 로직 (조회, 생성, 수정, 삭제)
- [ ] **MemberService** 추출
  - `MemberController`의 회원 가입/로그인 로직
  - `AdminMemberController`의 회원 관리/포인트 충전 로직
- [ ] **OptionService** 추출
  - `OptionController`의 옵션 CRUD 로직 (이름 검증, 중복 확인, 최소 1개 보장 규칙)
- [ ] **OrderService** 추출 (`@Transactional` 적용)
  - `OrderController`의 주문 생성 로직 (재고 차감 → 포인트 차감 → 주문 저장 → 카카오 메시지)
- [ ] **WishService** 추출
  - `WishController`의 위시리스트 조회/추가/삭제 로직 (중복 확인, 소유권 검증)
- [ ] 각 Controller가 요청 검증 + Service 위임만 수행하는지 최종 확인

---

## 구현 전략

### 작업 순서

작동 변경 없이 구조만 개선하므로, 안전한 순서대로 진행한다.

```
스타일 정리 → 불필요한 코드 제거 → 서비스 계층 추출
```

### 단계별 전략

**1단계: 스타일 정리**

- KtLint/포매터를 먼저 적용하여 자동으로 잡을 수 있는 항목을 처리한다.
- 이후 수동으로 `@Autowired`, `var`/명시적 타입, 주석 스타일 등을 통일한다.
- 한 가지 스타일 항목씩 커밋한다 (예: "style: remove unnecessary @Autowired annotations").

**2단계: 불필요한 코드 제거**

- IDE 정적 분석(미사용 import, 미사용 필드)을 활용하여 제거 대상을 식별한다.
- `OrderController`의 `WishRepository`는 주석에 의도("cleanup wish")가 있으나 미구현 상태이므로, 이 단계에서는 미사용 코드로 판단하여 제거한다.
- 제거 전 `git blame`으로 추가 의도를 확인한다.

**3단계: 서비스 계층 추출**

- 도메인별로 하나씩 Service를 추출하며, 각 Service 추출마다 별도 커밋한다.
- 추출 순서: 의존이 적은 것부터 → 의존이 많은 것 순서로 진행한다.
  1. `CategoryService` (의존 없음, 가장 단순)
  2. `ProductService` (Category 의존)
  3. `MemberService` (독립적)
  4. `OptionService` (Product 의존)
  5. `WishService` (Member, Product 의존)
  6. `OrderService` (Member, Option, KakaoMessageClient 의존 — 가장 복잡, `@Transactional` 필수)
- API Controller와 Admin Controller가 동일 Service를 공유하도록 하여 중복 로직을 제거한다.
- 각 추출 후 전체 테스트를 실행하여 작동이 유지되는지 확인한다.

### 커밋 컨벤션

[AngularJS Git Commit Message Conventions](https://gist.github.com/stephenparish/9941e89d80e2bc58a153) 을 따른다.

| 접두사 | 용도 |
|---|---|
| `style` | 스타일 정리 (코드 포맷, 네이밍 통일 등) |
| `refactor` | 구조 변경 (서비스 계층 추출 등) |
| `chore` | 불필요한 코드 제거, 빌드 설정 등 |
| `docs` | README 등 문서 작성 |

### 검증 방법

- 매 커밋 전 `./gradlew test` 전체 테스트 통과 확인
- `./gradlew ktlintCheck`로 스타일 위반 확인
- 구조 변경 커밋에 작동 변경이 섞이지 않았는지 `git diff`로 확인

---

## AI 활용 기록

- Claude Code를 활용하여 프로젝트 전체 코드를 분석하고 스타일 불일치, 미사용 코드, 서비스 추출 대상을 식별함
- 분석 결과를 바탕으로 기능 목록과 구현 전략을 직접 정리하고, 작업 순서와 커밋 단위를 설계함
