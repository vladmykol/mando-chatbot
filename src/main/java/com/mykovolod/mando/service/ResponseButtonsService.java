package com.mykovolod.mando.service;

import com.mykovolod.mando.entity.ResponseButton;
import com.mykovolod.mando.entity.ResponseButtonMapping;
import com.mykovolod.mando.repository.ResponseButtonMappingRepository;
import com.mykovolod.mando.repository.ResponseButtonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResponseButtonsService {
    private final ResponseButtonRepository responseButtonRepository;
    private final ResponseButtonMappingRepository responseButtonMappingRepository;

    public List<ResponseButtonMapping> findButtonsAttachedToIntentDataId(String intentDataId) {
        return responseButtonMappingRepository.findAllByIntentDataId(intentDataId);
    }

    public String createNewButton(String botId, String buttonName) {
        final var optionalIntentDataResponseButton = responseButtonRepository.findByButtonNameAndBotId(buttonName, botId);

        ResponseButton responseButton;
        if (optionalIntentDataResponseButton.isPresent()) {
            return optionalIntentDataResponseButton.get().getId();
        } else {
            final var button = ResponseButton.builder()
                    .buttonName(buttonName)
                    .botId(botId)
                    .build();
            responseButton = responseButtonRepository.save(button);
        }
        return responseButton.getId();
    }

    public void setTargetIntentDataId(String targetButtonId, String targetIntentDataId) {
        final var optionalIntentDataResponseButton = responseButtonRepository.findById(targetButtonId);

        optionalIntentDataResponseButton.ifPresent(responseButton -> {
            responseButton.setTargetIntentDataId(targetIntentDataId);

            responseButtonRepository.save(responseButton);
        });
    }

    public Optional<ResponseButton> getById(String newButtonId) {
        return responseButtonRepository.findById(newButtonId);
    }

    public void deleteById(String id) {
        responseButtonMappingRepository.deleteAllByButtonId(id);
        responseButtonRepository.deleteById(id);
    }

    public List<ResponseButton> findButtonsByBotId(String botId) {
        return responseButtonRepository.findByBotId(botId);
    }

    public Optional<ResponseButton> getButtonByNameAndBotId(String buttonName, String botId) {
        return responseButtonRepository.findByButtonNameAndBotId(buttonName, botId);
    }

    public void assignButtonToIntentData(String intentResponseButtonId, String intentDataId) {
        if (!responseButtonMappingRepository.existsByButtonIdAndIntentDataId(intentResponseButtonId, intentDataId)) {
            final var buttonMapping = ResponseButtonMapping.builder()
                    .buttonId(intentResponseButtonId)
                    .intentDataId(intentDataId)
                    .build();

            responseButtonMappingRepository.save(buttonMapping);
        }
    }

    public String getIntendDataIdByMappingId(String buttonMappingId) {
        final var responseButtonMapping = responseButtonMappingRepository.findById(buttonMappingId);

        if (responseButtonMapping.isPresent()) {
            return responseButtonMapping.get().getIntentDataId();
        } else {
            return null;
        }
    }

    public Optional<ResponseButtonMapping> findResponseButtonMappingById(String buttonMappingId) {
        return responseButtonMappingRepository.findById(buttonMappingId);
    }

    public void deleteAssignMapping(String buttonMappingId) {
        responseButtonMappingRepository.deleteById(buttonMappingId);
    }

    public List<String> findButtonNamesByIntentDataId(String intentDataId) {
        final var buttonList = findButtonsAttachedToIntentDataId(intentDataId);
        if (buttonList.isEmpty()) {
            return new ArrayList<>(0);
        } else {
            List<String> buttonNames = new ArrayList<>();
            for (ResponseButtonMapping responseButtonMapping : buttonList) {
                final var optionalResponseButton = getById(responseButtonMapping.getButtonId());
                optionalResponseButton.ifPresent(responseButton -> {
                    buttonNames.add(responseButton.getButtonName());
                });
            }
            return buttonNames;
        }
    }

    public void deleteButtonsByBotIdAndIntentDataIds(String botId, List<String> intentDataIdList) {
        responseButtonRepository.deleteAllByBotIdAndTargetIntentDataIdIn(botId, intentDataIdList);
        responseButtonMappingRepository.deleteAllByIntentDataIdIn(intentDataIdList);

        deleteButtonsWithNoMapping(botId);
    }

    private void deleteButtonsWithNoMapping(String botId) {
        final var buttonList = responseButtonRepository.findByBotId(botId);
        buttonList.forEach(responseButton -> {
            if (!responseButtonMappingRepository.existsByButtonId(responseButton.getId())) {
                responseButtonRepository.deleteById(responseButton.getId());
            }
        });
    }

    public boolean renameButton(String id, String newButtonName, String botId) {
        final var optionalResponseButton = responseButtonRepository.findById(id);

        if (optionalResponseButton.isPresent()) {
            final var optionalResponseButtonWithSameName = responseButtonRepository.findByButtonNameAndBotId(newButtonName, botId);
            if (optionalResponseButtonWithSameName.isPresent()) {
                return false;
            } else {
                optionalResponseButton.get().setButtonName(newButtonName);
                responseButtonRepository.save(optionalResponseButton.get());
                return true;
            }

        } else {
            return false;
        }


    }

    public List<String> findAssignedIntentDataIdsByButtonId(String buttonId) {
        final var buttonMappingList = responseButtonMappingRepository.findAllByButtonId(buttonId);
        return buttonMappingList.stream()
                .map(ResponseButtonMapping::getIntentDataId).collect(Collectors.toList());
    }

    public void deleteAllByBot(String botId) {
        final var buttonList = responseButtonRepository.findByBotId(botId);

        final var buttonIds = buttonList.stream()
                .map(ResponseButton::getId)
                .collect(Collectors.toList());
        responseButtonMappingRepository.deleteByButtonIdIn(buttonIds);
        responseButtonRepository.deleteAllByBotId(botId);
    }
}
