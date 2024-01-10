ShortURL
=
---

## 프로젝트 설명
해당 프로젝트는 긴 URL을 단축, 요약 해주는 서비스입니다.
단축된 URL을 이용한 횟수 등을 확인 할 수 있게 제작하고 추후 확장성을 고려중입니다.

## 기술 스택
- java 17
- spring boot 2.7
- mysql
- redis

## 인프라 구조
![shorti](https://github.com/Ji-Hoon24/ShortURL/assets/36688619/bb9a6b2f-7a9b-4b42-bf3d-40dc48054d6e)

1. git push
2. github Actions
3. gradle build
4. docker build
5. ECR Upload
6. ECS에서 EC2에 해당 도커 이미지 실행

## 주안점
1. 자동 배포 프로세스 구축 경험
   1. git push만으로 자동으로 배포시킬 수 있는 인프라 구축 경험
2. 빠른 응답성
   1. Redis(Inmemory DB)를 이용하여 RDS대비 빠른 응답성 구축