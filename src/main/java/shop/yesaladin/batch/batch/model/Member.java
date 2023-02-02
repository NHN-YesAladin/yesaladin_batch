package shop.yesaladin.batch.batch.model;

import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import shop.yesaladin.batch.batch.converter.MemberGenderCodeConverter;
import shop.yesaladin.batch.batch.converter.MemberGradeCodeConverter;

/**
 * 회원의 엔티티 클래스 입니다.
 *
 * @author : 송학현, 최예린
 * @since : 1.0
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "members")
@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 15, nullable = false)
    private String nickname;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(name = "login_id", unique = true, length = 15, nullable = false)
    private String loginId;

    @Column(name = "login_password", nullable = false)
    private String password;

    @Column(name = "birth_year", nullable = false)
    private Integer birthYear;

    @Column(name = "birth_month", nullable = false)
    private Integer birthMonth;

    @Column(name = "birth_day", nullable = false)
    private Integer birthDay;

    @Column(unique = true, length = 100, nullable = false)
    private String email;

    @Column(length = 11, unique = true, nullable = false)
    private String phone;

    @Column(name = "sign_up_date", nullable = false)
    private LocalDate signUpDate;

    @Column(name = "withdrawal_date")
    private LocalDate withdrawalDate;

    @Column(name = "is_withdrawal", nullable = false)
    private boolean isWithdrawal;

    @Column(name = "is_blocked", nullable = false)
    private boolean isBlocked;

    @Column(name = "member_grade_id")
    @Convert(converter = MemberGradeCodeConverter.class)
    private MemberGrade memberGrade;

    @Column(name = "gender_code")
    @Convert(converter = MemberGenderCodeConverter.class)
    private MemberGenderCode memberGenderCode;
}
