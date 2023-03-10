package searchengine.services;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import russianmorphology.morph.src.main.java.org.apache.lucene.morphology.LuceneMorphology;

import java.util.*;

@Service
public class MorphologyService {

    private String NOT_A_WORD_PATTERN;
    private LuceneMorphology russianLuceneMorph;
    private LuceneMorphology englishLuceneMorph;

    public MorphologyService(
            @Qualifier("notAWord")
            String NOT_A_WORD_PATTERN,
            @Qualifier("russianMorphology")
            LuceneMorphology russianLuceneMorph,
            @Qualifier("englishMorphology")
            LuceneMorphology englishLuceneMorph) {
        this.NOT_A_WORD_PATTERN = NOT_A_WORD_PATTERN;
        this.russianLuceneMorph = russianLuceneMorph;
        this.englishLuceneMorph = englishLuceneMorph;
    }

    public List<String> getNormalFormOfAWord(String word) {
        word = word.replaceAll("ё", "е");
        if (russianLuceneMorph.checkString(word) && !isRussianGarbage(russianLuceneMorph.getMorphInfo(word))) {
            return russianLuceneMorph.getNormalForms(word);
        } else if (englishLuceneMorph.checkString(word) && !isEnglishGarbage(englishLuceneMorph.getMorphInfo(word))) {
            return englishLuceneMorph.getNormalForms(word);
        } else if (word.chars().allMatch(Character::isDigit)){
            return Collections.singletonList(word);
        }
        return new ArrayList<>();
    }

    public String[] splitStringToLowercaseWords(String input) {
        return Arrays.stream(input.toLowerCase(Locale.ROOT)
                        .replaceAll(NOT_A_WORD_PATTERN, " ")
                        .trim()
                        .split(" "))
                .filter(s -> !s.isBlank()).toArray(String[]::new);
    }

    public String[] splitStringToWords(String sentence) {
        return Arrays.stream(sentence.replaceAll(NOT_A_WORD_PATTERN, " ")
                        .trim()
                        .split(" "))
                .filter(s -> !s.isBlank()).toArray(String[]::new);
    }

    boolean isRussianGarbage(List<String> morphInfos) {
        for(String variant : morphInfos) {
            if (variant.contains(" СОЮЗ") || variant.contains(" МЕЖД") ||
                    variant.contains(" ПРЕДЛ") || variant.contains(" ЧАСТ")) {
                return true;
            }
        }
        return false;
    }

    boolean isEnglishGarbage (List<String> morphInfos) {
        for(String variant : morphInfos) {
            if (variant.contains(" CONJ") || variant.contains(" INT") ||
                    variant.contains(" PREP") || variant.contains(" PART") ||  variant.contains(" ARTICLE")) {
                return true;
            }
        }
        return false;
    }
}