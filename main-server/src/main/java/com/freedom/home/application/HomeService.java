package com.freedom.home.application;

import com.freedom.character.domain.Character;
import com.freedom.character.infra.CharacterRepository;
import com.freedom.home.api.dto.HomeResponse;
import com.freedom.quiz.application.dto.UserQuizDto;
import com.freedom.quiz.domain.entity.UserQuiz;
import com.freedom.quiz.domain.service.FindUserQuizService;
import com.freedom.wallet.application.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {

    private final CharacterRepository characterRepository;
    private final WalletService walletService;
    private final FindUserQuizService findUserQuizService;

    public HomeResponse getHome(Long userId) {
        String nickname = characterRepository.findByUserId(userId)
                .map(Character::getCharacterName)
                .orElse("");

        BigDecimal balance = walletService.getWalletByUserId(userId).getBalance();

        LocalDate today = LocalDate.now();
        List<UserQuizDto> todays = findUserQuizService.findDailyQuizzes(userId, today);
        int quizCount = (int) todays.stream()
                .filter(uq -> uq.getIsCorrect() != null) // answered (correct or wrong)
                .count();

        return HomeResponse.of(nickname, balance, today, quizCount);
    }
}



