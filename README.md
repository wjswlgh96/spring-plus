# SPRING PLUS

## 12. AWS 활용

### ✅ EC2

![Image](https://github.com/user-attachments/assets/f2d88b95-1cd1-4b1c-b5b3-2933b553654b)

### ✅ RDS

![Image](https://github.com/user-attachments/assets/cdb67034-d3ae-45c0-8893-c63edbdd626e)

### ✅ S3

![Image](https://github.com/user-attachments/assets/ffe7c588-78a3-47ce-b486-0e4c34e29a39)

***

## 13. 대용량 데이터 처리

### 과제 요구사항

- 테스트 코드에서 유저 데이터 100만건 생성하기
  - 데이터 생성 시 닉네임은 랜덤으로 지정해주세요.
  - 가급적 동일한 닉네임이 들어가지 않도록 방법을 생각해보세요.

<br/>

#### 데이터 생성 최적화 이전 속도

![Image](https://github.com/user-attachments/assets/304a5e5c-b6aa-4ebc-b2d1-7ca464513c4a)

무려 2분이 넘는 어마어마한 시간이 소요된다.

과제는 조회만 최적화하라고 나와있었지만 어느정도 이 시간 소요를 줄이기 위해서 나름대로의 방법을 사용해보았음

해결 방법으로는 insert_batch_size를 1000으로 설정해서 insert query를 최적화 하였다.

MySQL DB는 Identity 전략으로 했을때 batch 설정이 먹히지 않는다고 하여 User Entity의 PK를 UUID로 바꾸어서 적용하니까 잘되었다.

아래는 그 결과이다.

> 100만건의 유저 데이터를 생성하는 코드는 `AuthServiceTest` 파일에 있다.

<br/>

#### 데이터 생성 최적화 이후 속도

![Image](https://github.com/user-attachments/assets/52e63a5b-8929-456b-b414-d5bbf6155cd5)

29초까지 줄였다.

아직 그래도 좀 느린감이 있지만 그래도 이정도의 속도에 만족하고 넘어가겠다.
데이터 100만건 / 1000 으로 batch 횟수가 총 1000번으로 정상적으로 작동함을 확인할 수 있었다.

실제 데이터가 저장되었는지 MySQL 인텔리제이 내부에서의 Console로 User의 갯수를 확인했다.

![Image](https://github.com/user-attachments/assets/4b9bba4c-55c3-4adc-9f5f-03abe40579a7)

정상적으로 100만건이 조회되는것을 확인할 수 있었다.

<br/>

### 과제 요구사항

- 닉네임을 조건으로 유저 목록을 검색하는 API를 만들어주세요.
    - 닉네임은 정확히 일치해야 검색이 가능해요.
- 여러가지 아이디어로 유저 검색 속도를 줄여주세요.
    - 조회 속도를 개선할 수 있는 여러 방법을 고민하고, 각각의 방법들을 실행해보세요.
    - `README.md` 에 각 방법별 실행 결과를 비교할 수 있도록 최초 조회 속도와 개선 과정 별 조회 속도를 확인할 수 있는 표 혹은 이미지를 첨부해주세요.

<br/>

#### 조회 최적화 이전

일단 매우 단순하게 JPA 의 기능만을 이용했을때의 조회 결과이다.

```java
public UserResponse getUsersByNickname(String nickname) {
  User findUser = userRepository.findAllByNickname(nickname);
  return new UserResponse(findUser.getId(), findUser.getEmail());
}
```

위의 코드는 최적화 이전 UserService 내부의 코드이다.

결과를 확인해보면

![Image](https://github.com/user-attachments/assets/c7d8e42f-dd28-44db-a704-4bcb6c919152)

100만건의 데이터를 모두 탐색한뒤에 찾아오느라 858ms 약 0.8초의 시간이 소요된다.

<br/>

#### 조회 최적화 이후(인덱싱)

정말 단순하게 최적화를 했다.
nickname에 인덱싱을 걸어주었고 일단 결과부터 확인해보면

![Image](https://github.com/user-attachments/assets/a10f55b0-e462-401e-ba4b-802bf5e08d86)

11ms로 엄청나게 줄어든것을 확인할 수 있다.
그러나 내가 인덱싱에 대해 제대로 알고 있는것도 아니고 그냥 검색해서 찾아본 결과만 반영한 것이기 때문에 인덱싱이 어떤 식으로 동작하는지 살짝 읽어보기만 했다.

일단 인덱싱을 인텔리제이 DB 콘솔에서 직접 지정해주었다.

```mysql
CREATE INDEX idx_nickname ON users (nickname);
```

인덱싱을 지정해주게 되면 B-트리 형태로 `nickname`의 값과 해당 레코드의 위치를 저장한다고 한다.

ex) "user-1a2b3c4d" -> 레코드 500번

위치는 `users.ibd` 파일 인덱스 데이터 섹션에 저장된다고 하는데 MySQL 에서 내부 관리하기 때문에 사용자는 직접 보지 못한다고 한다.
그래도 얼마만큼의 용량이 저장되었는지 확인할 수 있는데

```mysql
SHOW VARIABLES LIKE 'datadir';
SELECT ROUND((data_length + index_length) / 1024 / 1024, 2) AS "Size (MB)"
FROM information_schema.tables
WHERE table_schema = 'sparta_plus' AND table_name = 'users';
```

위의 코드를 인텔리제이 내부 콘솔에서 실행해보았고, 결과는 무려 `254.69MB` 였다...

지금은 로컬환경에서 DB를 조작하기 때문에 상관이 없지만 이걸 RDS에서 적용 시켜준다면..?, 만약 100만건이 아니라 더 커진다면??
으로 생각해보았을때 확실히 부담되긴한다.

만약에 내가 Redis를 공부해보았다면 Redis의 캐싱을 사용하여 최적화를 해보았겠지만 아직 공부하지 않았고 배우지 않았기에 인덱싱으로만 적용시키고
추후에 비슷한 내용으로 조회를 최적화 해야한다면 Redis를 사용해서 결과를 비교해보고 싶다.

위의 최적화에 대한 내용들은 RDS에는 적용시키지 않을 생각이다.
