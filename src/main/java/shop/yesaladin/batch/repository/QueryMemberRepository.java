package shop.yesaladin.batch.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import shop.yesaladin.batch.dto.MemberCouponDto;

/**
 * 회원 조회 관련 repository interface 입니다.
 *
 * @author : 서민지
 * @since : 1.0
 */
public interface QueryMemberRepository {

    @Query("select m.id from Member as m where m.birthMonth = ?1 and m.birthDay = ?2")
    List<MemberCouponDto> findMemberIdsByBirthday(int month, int date);
}
