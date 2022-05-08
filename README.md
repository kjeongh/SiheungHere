# <여기있시흥> SiheungHere

안드로이드 팀프로젝트



## 참여자

- 김정현 [kjeongh](https://github.com/kjeongh)
- 김하린 [kimhalin](https://github.com/kimhalin)
- 박찬민 [p-chanmin](https://github.com/p-chanmin)



## Git Rule

#### Fork

1. Fork 생성해서 각자 본인 저장소에서 작업
2. 소스트리에서 원격 저장소 추가
   - 소스트리 - 저장소 - 원격 저장소 추가 - 추가
   - 원격 이름 = 다른 참여자 이름
   - URL/경로 = 다른 참여자가 Fork한 저장소의 HTTPS
   - 확인 - 패치
3. 각자 저장소에서 다른 참여자의 작업 진행 상황 수시로 확인



#### issue

- [원본 저장소](https://github.com/kimhalin/SiheungHere)에 feature 별로 issue 생성
  - Issues - New issue - 타이틀, 내용 작성 후 추가
  - 이슈 번호 확인 (#이슈번호)
- 각 issue가 끝나면 close 하기



#### Branch

1. 각자 Fork 한 저장소에서 Branch 생성하여 작업
2. 기능별로 issue 생성 후 Branch 생성
   - feature/{이슈번호}-{기능명} 브랜치 생성 후 작업
   - commit message는 이슈번호를 붙여서 작성 (ex. #1 README.md Git Rule 추가)



#### Pull requests

1. 원본 저장소에 병합할 경우
   - 소스트리의 작업한 Branch에서 원본 저장소 위치와 병합
   - 충돌 없으면 github 에서 main <- branch 로 Pull requests 작성 및 merge
   - 충돌 있으면 소스트리에서 수정하라고 나오는데, 각자가 현재 원본 저장소와 작성 코드 충돌 해결
   - 충돌 해결 후 커밋, github 에서 main <- branch 로 Pull requests 작성 및 merge
2. 다른 참여자의 branch에 병합할 경우
   - 소스트리의 작업한 Branch에서 다른 참여자의 branch에 병합
   - 충돌 없으면 github 에서 branch <- branch 로 Pull requests 작성
   - 충돌 있으면 소스트리에서 수정하라고 나오는데, 각자가 현재 원본 저장소와 작성 코드 충돌 해결
   - 충돌 해결 후 커밋, github 에서 branch <- branch 로 Pull requests 작성

