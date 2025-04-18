# SPRING ADVANCED
## 1. [문제 인식 및 정의]
- `GlobalExceptionHandler`에서 예외 처리 시 `Map<String, Object>`를 반복적으로 생성하여 응답하는 방식은 중복 코드가 많고, 유지보수에 불리했음.
- 응답 포맷도 일관성이 떨어지고, 확장성 부족.

---

## 2. [해결 방안]

### 2-1. [의사결정 과정]
- 이전 팀 프로젝트에서도 `ErrorResponse` DTO를 도입하여 예외 응답 포맷을 일관되게 관리했던 경험이 있었음.
- 공통 응답 구조를 분리하면 `예외 타입별 처리 로직은 최소화`, `응답 포맷은 통일` 가능하다는 장점이 있어 동일한 방식 적용 결정.

### 2-2. [해결 과정]
1. `common.response.ErrorResponse` 클래스 작성:

```java
@Getter
@AllArgsConstructor
public class ErrorResponse {
    private int code;
    private String status;
    private String message;

    public static ErrorResponse of(HttpStatus status, String message) {
        return new ErrorResponse(status.value(), status.name(), message);
    }
}
```

## 3. [해결 완료]
### 3-1. [회고]
작은 리팩토링이지만, 예외 처리의 가독성과 재사용성이 크게 향상됨.

향후 로그 추적용 필드(timestamp, path 등) 추가도 DTO에서 간편히 확장 가능하다는 장점이 있음.

다른 도메인 예외도 쉽게 통합 가능.

### 3-2. [전후 데이터 비교]
Map<String, Object> 에서 ErrorResponse DTO로 변경
중복 코드는 buildResponse()로 통일
또한 변경후에는 필드 추가가 용이함