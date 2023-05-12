package com.ll.gramgram.boundedContext.likeablePerson.controller;

import com.ll.gramgram.base.rq.Rq;
import com.ll.gramgram.base.rsData.RsData;
import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.ll.gramgram.boundedContext.likeablePerson.service.LikeablePersonService;
import com.ll.gramgram.boundedContext.member.entity.Member;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/usr/likeablePerson")
@RequiredArgsConstructor
public class LikeablePersonController {
    private final Rq rq;
    private final LikeablePersonService likeablePersonService;

    @PreAuthorize("isAuthenticated()") //로그인 여부 확인
    @GetMapping("/like")
    public String showLike() {
        return "usr/likeablePerson/like";
    }

    @AllArgsConstructor
    @Getter
    public static class LikeForm {
        @NotBlank
        @Size(min = 3, max = 30)
        private final String username;

        @NotNull
        @Min(1)
        @Max(3)
        private final int attractiveTypeCode;
    }

    @PreAuthorize("isAuthenticated()") //로그인 여부 확인
    @PostMapping("/like")
    public String like(@Valid LikeForm likeForm) {
        RsData<LikeablePerson> rsData = likeablePersonService.like(rq.getMember(),
                likeForm.getUsername(), likeForm.getAttractiveTypeCode());
        if (rsData.isFail()) {
            return rq.historyBack(rsData);
        }
        return rq.redirectWithMsg("/usr/likeablePerson/list", rsData);
    }

    @PreAuthorize("isAuthenticated()") //로그인 여부 확인
    @GetMapping("/list")
    public String showList(Model model) {
        InstaMember instaMember = rq.getMember().getInstaMember();

        // 인스타인증을 했는지 체크
        if (instaMember != null) {
            List<LikeablePerson> likeablePeople = instaMember.getFromLikeablePeople();
            model.addAttribute("likeablePeople", likeablePeople);
        }

        return "usr/likeablePerson/list";
    }

    @PreAuthorize("isAuthenticated()") //로그인 여부 확인
    @DeleteMapping("/{id}")
    public String cancel(@PathVariable Long id) {

        LikeablePerson likeablePerson = likeablePersonService.findById(id).orElse(null);

        RsData canDeleteRsData = likeablePersonService.canCancel(rq.getMember(), likeablePerson);

        if (canDeleteRsData.isFail()) return rq.historyBack(canDeleteRsData);

        RsData deleteRsData = likeablePersonService.cancel(likeablePerson);

        if (deleteRsData.isFail()) return rq.historyBack(deleteRsData);

        return rq.redirectWithMsg("/usr/likeablePerson/list", deleteRsData);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String showModify(@PathVariable Long id, Model model) {
        LikeablePerson likeablePerson = likeablePersonService.findById(id).orElseThrow();

        RsData canModifyRsData = likeablePersonService.canModify(rq.getMember(), likeablePerson);

        if (canModifyRsData.isFail()) return rq.historyBack(canModifyRsData);

        model.addAttribute("likeablePerson", likeablePerson);

        return "usr/likeablePerson/modify";
    }

    @AllArgsConstructor
    @Getter
    public static class ModifyForm {
        @NotNull
        @Min(1)
        @Max(3)
        private final int attractiveTypeCode;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String modify(@PathVariable Long id, @Valid ModifyForm modifyForm) {
        RsData<LikeablePerson> rsData = likeablePersonService.modifyAttractive(rq.getMember(), id, modifyForm.getAttractiveTypeCode());

        if (rsData.isFail()) {
            return rq.historyBack(rsData);
        }

        return rq.redirectWithMsg("/usr/likeablePerson/list", rsData);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/toList")
    public String showToList(Model model, @RequestParam(defaultValue = "") String gender, @RequestParam(defaultValue = "0") int attractiveTypeCode, @RequestParam(defaultValue = "1") int sortCode) {
        InstaMember instaMember = rq.getMember().getInstaMember();

        // 인스타인증을 했는지 체크
        if (instaMember != null) {
            //당신을 좋아하는 사람들 목록
            Stream<LikeablePerson> likeablePeopleStream = instaMember.getToLikeablePeople().stream();

            if (!gender.isEmpty()) {
                likeablePeopleStream = likeablePeopleStream
                        .filter(likeablePerson -> likeablePerson.getFromInstaMember().getGender().equals(gender));
            }

            if (attractiveTypeCode != 0) {
                likeablePeopleStream = likeablePeopleStream
                        .filter(likeablePerson -> likeablePerson.getAttractiveTypeCode() == attractiveTypeCode);
            }

            switch (sortCode) {
                case 2:
                    likeablePeopleStream = likeablePeopleStream.sorted(
                            Comparator.comparing(LikeablePerson::getId)
                    );
                    break;
                case 3:
                    likeablePeopleStream = likeablePeopleStream.sorted(
                            Comparator.comparing((LikeablePerson lp) -> lp.getFromInstaMember().getLikes()).reversed()
                    );
                    break;
                case 4:
                    likeablePeopleStream = likeablePeopleStream.sorted(
                            Comparator.comparing(lp -> lp.getFromInstaMember().getLikes())
                    );
                    break;
                case 5:
                    likeablePeopleStream = likeablePeopleStream
                            .sorted(Comparator.comparing((LikeablePerson lp) -> lp.getFromInstaMember().getGender()).reversed()
                                    .thenComparing(Comparator.comparing(LikeablePerson::getId).reversed()));
                    break;
                case 6:
                    likeablePeopleStream = likeablePeopleStream
                            .sorted(Comparator.comparing(LikeablePerson::getAttractiveTypeCode)
                                    .thenComparing(Comparator.comparing(LikeablePerson::getId).reversed()));
                    break;

            }

            List<LikeablePerson> likeablePeople = likeablePeopleStream.collect(Collectors.toList());
            model.addAttribute("likeablePeople", likeablePeople);
        }
        return "usr/likeablePerson/toList";
    }
}