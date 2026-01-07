# Schedule Manager Backend

## 프로젝트 소개
일정 관리 및 친구와의 일정 공유를 위한 캘린더 애플리케이션의 백엔드 API 서버입니다.
사용자는 개인 일정을 등록하고 관리하며, 친구를 추가하여 일정을 공유할 수 있습니다.

## 주요 기능
- **회원 관리**: JWT 기반 인증/인가, 이메일 인증을 통한 비밀번호 찾기
- **일정 관리**: 일정 등록, 조회, 수정, 삭제 및 기간별 일정 조회
- **친구 기능**: 친구 추가 및 검색 기능
- **보안**: Spring Security를 활용한 인증 처리 및 비밀번호 암호화

## 기술 스택
- **Backend**: Java 17, Spring Boot 3.5.4
- **Database**: MySQL 8.x
- **Infra**: AWS (EC2, RDS)
- **Security**: JWT, Spring Security
- **Build Tool**: Gradle

## ERD
-<img width="749" height="700" alt="scheduleApp erd" src="https://github.com/user-attachments/assets/1df532a5-0554-4990-9bb6-b49ceb069033" />

## API 명세

### 회원 관리

#### 회원가입
Endpoint: POST /api/signup
Request:
json{
  "email": "test@example.com",
  "pw": "password123",
  "nickname": "홍길동"
}
Response:
json{
  "result": true,
  "message": "회원가입 성공",
  "data": null
}
```

**Status Code:**
- 200: 회원가입 성공
- 400: 중복된 이메일 또는 닉네임

**구현 상세:**
- 이메일과 닉네임은 trim() 처리 후 소문자 변환하여 저장
- 이메일/닉네임 중복 체크를 사전에 수행
- 비밀번호는 **PasswordEncoder(BCrypt)**로 암호화하여 저장
- DB의 unique 제약조건으로 이중 검증

---

### **2. 이메일 중복 체크**

**Endpoint:** `GET /api/checkEmail?value={email}`

**Request:**
```
GET /api/checkEmail?value=test@example.com
Response:
json{
  "result": true,
  "message": "사용 가능한 이메일입니다.",
  "data": null
}
```

**Status Code:**
- 200: 사용 가능
- 400: 이미 사용 중

**구현 상세:**
- 이메일 공백 및 빈 값 검증
- DB 조회로 중복 여부 확인

---

### **3. 닉네임 중복 체크**

**Endpoint:** `GET /api/checkNickname?value={nickname}`

**Request:**
```
GET /api/checkNickname?value=홍길동
Response:
json{
  "result": true,
  "message": "사용 가능한 닉네임입니다.",
  "data": null
}
Status Code:

200: 사용 가능
400: 이미 사용 중

구현 상세:

닉네임 trim 처리
DB 조회로 중복 여부 확인


4. 로그인
Endpoint: POST /api/login
Request:
json{
  "email": "test@example.com",
  "pw": "password123"
}
Response:
json{
  "result": true,
  "message": "로그인 성공",
  "data": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
Status Code:

200: 로그인 성공 (JWT 토큰 반환)
400: 이메일 또는 비밀번호 불일치

구현 상세:

이메일로 사용자 조회
PasswordEncoder로 비밀번호 검증
JWT 토큰 생성 (HS256 알고리즘, 만료시간 3600초=1시간)
토큰을 응답 data에 포함하여 반환


5. 비밀번호 재설정 요청 (이메일 전송)
Endpoint: POST /api/forgot
Request:
json{
  "email": "test@example.com"
}
Response:
json{
  "result": true,
  "message": "해당 이메일로 인증코드를 전송했습니다.",
  "data": null
}
Status Code:

200: 이메일 전송 성공

구현 상세:

6자리 랜덤 OTP 생성 (SecureRandom 사용)
OTP를 BCrypt로 해싱하여 DB에 저장
기존 재설정 요청이 있으면 삭제 후 새로 생성
만료시간 10분, 최대 시도 횟수 5회 설정
JavaMailSender로 이메일 전송


6. 인증코드 검증
Endpoint: POST /api/verify
Request:
json{
  "email": "test@example.com",
  "code": "123456"
}
Response:
json{
  "result": true,
  "message": "인증에 성공했습니다.",
  "data": null
}
Status Code:

200: 인증 성공
400: 코드 불일치, 코드 만료
429: 시도 횟수 초과 (5회)

구현 상세:

만료시간 검증 (10분 초과 시 실패)
시도 횟수 검증 (5회 초과 시 실패 및 삭제)
PasswordEncoder로 코드 해시 비교
실패 시 시도 횟수 증가


7. 비밀번호 재설정
Endpoint: POST /api/reset
Request:
json{
  "email": "test@example.com",
  "code": "123456",
  "pw": "newPassword123"
}
Response:
json{
  "result": true,
  "message": "비밀번호가 변경되었습니다.",
  "data": null
}
Status Code:

200: 비밀번호 변경 성공
400: 코드 불일치, 사용자 없음

구현 상세:

코드 검증 (만료시간, 시도횟수, 일치 여부)
새 비밀번호를 BCrypt로 암호화하여 저장
재설정 요청 정보 DB에서 삭제 (1회용)


보안 처리:

비밀번호: BCrypt 단방향 해싱
JWT: HS256 알고리즘, 1시간 만료
OTP: 6자리, 10분 만료, 5회 시도 제한
모든 인증 관련 정보는 암호화하여 저장


이 정도면 회원 관리 API 명세 완성! 어때? 수정하고 싶은 부분 있어?api 랑 상세 설명 어떻게 적는 게 좋을까오후 2:15추천 구조:
API 하나당 박스 형태로 명확히 구분!
markdown## 📡 API 명세

### 회원 관리

---

#### 회원가입

**Endpoint**
```
POST /api/signup
```

**Request Body**
```json
{
  "email": "test@example.com",
  "pw": "password123",
  "nickname": "홍길동"
}
```

**Response**
```json
{
  "result": true,
  "message": "회원가입 성공",
  "data": null
}
```

**Status Code**
- `200` : 회원가입 성공
- `400` : 중복된 이메일 또는 닉네임

**구현 내용**
- 이메일과 닉네임은 trim() 처리 후 소문자 변환
- 이메일/닉네임 중복 체크 선행
- 비밀번호는 BCrypt로 암호화하여 저장
- DB unique 제약조건으로 이중 검증

---

#### 로그인

**Endpoint**
```
POST /api/login
```

**Request Body**
...
장점:

구분선(---)으로 API 분리 → 한눈에 보기 쉬움
일관된 순서 → Endpoint → Request → Response → Status → 구현
코드블록 활용 → JSON은 json, URL은  
간결한 구현 내용 → 핵심만 3-5줄

vs 내가 준 버전:

너무 길고 설명이 산만함
구분이 덜 명확함

이 구조로 다시 정리해줄까? 훨씬 깔끔해질 거야!응응 다시 정리해줘오후 2:16오케이! 깔끔하게 다시 정리할게!

📡 API 명세
회원 관리

회원가입
Endpoint
POST /api/signup
Request Body
json{
  "email": "test@example.com",
  "pw": "password123",
  "nickname": "홍길동"
}
Response
json{
  "result": true,
  "message": "회원가입 성공",
  "data": null
}
```

**Status Code**
- `200` : 회원가입 성공
- `400` : 중복된 이메일 또는 닉네임

**구현 내용**
- 이메일과 닉네임은 trim() 처리 후 소문자 변환
- 이메일/닉네임 중복 체크 선행
- 비밀번호는 BCrypt로 암호화하여 저장
- DB unique 제약조건으로 이중 검증

---

#### 이메일 중복 체크

**Endpoint**
```
GET /api/checkEmail?value={email}
```

**Request**
```
GET /api/checkEmail?value=test@example.com
Response
json{
  "result": true,
  "message": "사용 가능한 이메일입니다.",
  "data": null
}
```

**Status Code**
- `200` : 사용 가능한 이메일
- `400` : 이미 사용 중인 이메일

**구현 내용**
- 이메일 공백 및 빈 값 검증
- DB 조회로 중복 여부 확인

---

#### 닉네임 중복 체크

**Endpoint**
```
GET /api/checkNickname?value={nickname}
```

**Request**
```
GET /api/checkNickname?value=홍길동
Response
json{
  "result": true,
  "message": "사용 가능한 닉네임입니다.",
  "data": null
}
```

**Status Code**
- `200` : 사용 가능한 닉네임
- `400` : 이미 사용 중인 닉네임

**구현 내용**
- 닉네임 trim 처리
- DB 조회로 중복 여부 확인

---

#### 로그인

**Endpoint**
```
POST /api/login
Request Body
json{
  "email": "test@example.com",
  "pw": "password123"
}
Response
json{
  "result": true,
  "message": "로그인 성공",
  "data": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Status Code**
- `200` : 로그인 성공 (JWT 토큰 반환)
- `400` : 이메일 또는 비밀번호 불일치

**구현 내용**
- 이메일로 사용자 조회 후 비밀번호 검증
- PasswordEncoder(BCrypt)로 비밀번호 매칭
- JWT 토큰 생성 (HS256 알고리즘, 만료시간 1시간)
- 생성된 토큰을 data 필드에 담아 반환

---

#### 비밀번호 재설정 요청

**Endpoint**
```
POST /api/forgot
Request Body
json{
  "email": "test@example.com"
}
Response
json{
  "result": true,
  "message": "해당 이메일로 인증코드를 전송했습니다.",
  "data": null
}
```

**Status Code**
- `200` : 인증코드 전송 성공

**구현 내용**
- 6자리 랜덤 OTP 생성 (SecureRandom)
- OTP를 BCrypt로 해싱하여 DB 저장
- 기존 재설정 요청 삭제 후 새로 생성
- 만료시간 10분, 최대 시도 횟수 5회 설정
- JavaMailSender로 이메일 전송

---

#### 인증코드 검증

**Endpoint**
```
POST /api/verify
Request Body
json{
  "email": "test@example.com",
  "code": "123456"
}
Response
json{
  "result": true,
  "message": "인증에 성공했습니다.",
  "data": null
}
```

**Status Code**
- `200` : 인증 성공
- `400` : 코드 불일치 또는 만료
- `429` : 시도 횟수 초과 (5회)

**구현 내용**
- 만료시간 검증 (10분 초과 시 삭제 및 실패)
- 시도 횟수 검증 (5회 초과 시 삭제 및 실패)
- PasswordEncoder로 코드 해시 비교
- 실패 시 시도 횟수 1 증가

---

#### 비밀번호 재설정

**Endpoint**
```
POST /api/reset
Request Body
json{
  "email": "test@example.com",
  "code": "123456",
  "pw": "newPassword123"
}
Response
json{
  "result": true,
  "message": "비밀번호가 변경되었습니다.",
  "data": null
}
Status Code

200 : 비밀번호 변경 성공
400 : 코드 불일치 또는 사용자 없음

구현 내용

코드 검증 (만료시간, 시도횟수, 일치여부)
새 비밀번호를 BCrypt로 암호화하여 업데이트
재설정 요청 정보 DB에서 삭제 (1회용)


보안 처리

비밀번호: BCrypt 단방향 해싱
JWT: HS256 알고리즘, 1시간 만료
OTP: 6자리, 10분 만료, 5회 시도 제한
모든 인증 정보는 암호화하여 저장


### 일정 관리
(다음에 추가)

### 친구 관리
(다음에 추가)

## 🏗 AWS 아키텍처
(나중에 추가)

## 💡 트러블슈팅
(나중에 추가)

## 🚀 실행 방법
(나중에 추가)
지금까지 완성된 거:

