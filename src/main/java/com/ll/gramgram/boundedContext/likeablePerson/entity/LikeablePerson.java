package com.ll.gramgram.boundedContext.likeablePerson.entity;

import com.ll.gramgram.base.appConfig.AppConfig;
import com.ll.gramgram.base.baseEntity.BaseEntity;
import com.ll.gramgram.base.rsData.RsData;
import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.standard.util.Ut;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.cglib.core.Local;
import org.springframework.scheduling.annotation.Scheduled;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

@Entity
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class LikeablePerson extends BaseEntity {
    private LocalDateTime modifyUnlockDate;
    @ManyToOne
    @ToString.Exclude
    private InstaMember fromInstaMember; // 호감을 표시한 사람(인스타 멤버)
    private String fromInstaMemberUsername; // 혹시 몰라서 기록
    @ManyToOne
    @ToString.Exclude
    private InstaMember toInstaMember; // 호감을 받은 사람(인스타 멤버)
    private String toInstaMemberUsername; // 혹시 몰라서 기록
    @Setter
    private int attractiveTypeCode; // 매력포인트(1=외모, 2=성격, 3=능력)

    public String getAttractiveTypeDisplayName() {
        return switch (attractiveTypeCode) {
            case 1 -> "외모";
            case 2 -> "성격";
            default -> "능력";
        };
    }

    public boolean isModifyUnlocked() {
        return modifyUnlockDate.isBefore(LocalDateTime.now());
    }

    // 수정 가능 시간 보여주기
    public String getModifyUnlockDateRemainStrHuman() {
        long timeOut=ChronoUnit.MINUTES.between(LocalDateTime.now(),modifyUnlockDate);
        String modifyUnlockTime="";

        // 남은 시간 출력(타이머)
        if(timeOut<60) {
           modifyUnlockTime = timeOut+"분";
       }else if(timeOut>60){
            Long hour=timeOut/60;
            modifyUnlockTime=hour+"시간 "+(timeOut%60)+"분";
        }

        // 타이머 형식이 아닌 수정할 수 있는 시간을 보여줌
        //String modifyUnlockTime=modifyUnlockDate.format(DateTimeFormatter.ofPattern("HH시 mm분 ss초"));
        return modifyUnlockTime;
    }

    public RsData updateAttractionTypeCode(int attractiveTypeCode) {
        if (this.attractiveTypeCode == attractiveTypeCode) {
            return RsData.of("F-1", "이미 설정되었습니다.");
        }
        this.modifyUnlockDate = AppConfig.genLikeablePersonModifyUnlockDate();
        this.attractiveTypeCode = attractiveTypeCode;
        return RsData.of("S-1", "성공");
    }
    public String getAttractiveTypeDisplayNameWithIcon() {
        return switch (attractiveTypeCode) {
            case 1 -> "<i class=\"fa-solid fa-person-rays\"></i>";
            case 2 -> "<i class=\"fa-regular fa-face-smile\"></i>";
            default -> "<i class=\"fa-solid fa-people-roof\"></i>";
        } + "&nbsp;" + getAttractiveTypeDisplayName();
    }
}
