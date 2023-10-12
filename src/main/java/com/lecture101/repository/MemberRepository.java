package com.lecture101.repository;


import com.lecture101.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>,
        MemberRepositoryCustom{
//        QuerydslPredicateExecutor<Member> {

    Member findByEmail(String email);

    Optional<Member> findById(Long memberId);


        //public interface MemberRepository extends JpaRepository<Member, Long> {
//
//    여러개의 쿼리 메소드 중에서, 조회 부분 보고,
//    조회하는 옵션을 하나씩 볼 예정
//    findByUserNm.
//        List<Member> findByUserNm(String userNm);
//    List<Member> findByUserDescription(String userDescription);

//        List<Member> findByUserNmOrUserDescription(String userNm, String userDescription);


// 통계, 조회 조건이 복잡하면, 쿼리 메소드로만 작업이 조금 어려움.
// JPQL 문법을 이용해서, 표준 SQL을 사용함.
// 문법이 너무 복잡하면, nativeSQL 속성을 사용해서, 그대로 사용.
// 단점. 인텔리 제이라서, 문자열 안의 문법을 체크를 해주는데 원래, 안해줌.
// 이 단점, 해결도하고, 자바 컴파일러의 문법 체크의 도움도 받기 위해서
// queryDSl 를 사용을 함.
//@Query("select m from Member m where m.userDescription like " +
//        "%:userDescription% order by m.regTime desc")
//    List<Member> findByUserDescription(@Param("userDescription") String userDescription);


// 주의사항
//nativeQuery = true 사용시,
// 실제 테이블의 컬럼을 사용을 해야함.
// 만약, 단위 테스트 중, 안되는 부분이 있으면, 실제 디비로 확인필요.
// userDescription -> user_description
// regTime -> reg_time
//@Query(value = "select * from member i where i.user_description like " +
//        "%:userDescription% order by i.reg_time desc", nativeQuery = true)
//    List<Member> findByUserDescriptionByNative(@Param("userDescription") String userDescription);

}

