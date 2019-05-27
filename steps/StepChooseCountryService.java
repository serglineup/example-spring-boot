package ua.com.lineup.enrollmentSystem.service.sms.steps;

import org.springframework.stereotype.Service;
import ua.com.lineup.enrollmentSystem.model.dto.request.SMSRequest;
import ua.com.lineup.enrollmentSystem.model.entity.organizationalstructure.Country;
import ua.com.lineup.enrollmentSystem.model.entity.organizationalstructure.Region;
import ua.com.lineup.enrollmentSystem.model.entity.statistic.enumeration.StepSms;
import ua.com.lineup.enrollmentSystem.properties.TranslationProperties;
import ua.com.lineup.enrollmentSystem.service.client.OzekiApiCaller;
import ua.com.lineup.enrollmentSystem.service.organizationalstructure.LocationService;
import ua.com.lineup.enrollmentSystem.service.statistic.RegistrationStatisticSmsService;
import ua.com.lineup.enrollmentSystem.service.statistic.SessionStatisticService;
import ua.com.lineup.enrollmentSystem.service.userbiometric.CitizenService;

import java.util.List;

@Service
public class StepChooseCountryService extends StepChooseGeographicalObjectService {

    public StepChooseCountryService(RegistrationStatisticSmsService registrationStatisticSmsService,
                                    OzekiApiCaller ozekiApiCaller, CitizenService citizenService,
                                    LocationService locationService, SessionStatisticService sessionStatisticService, TranslationProperties translationProperties) {
        super(registrationStatisticSmsService, ozekiApiCaller, citizenService, locationService, sessionStatisticService, translationProperties);
        stepBackDisplayMsg = StepSms.PROCESS_CNIB_PASSPORT_VERIFICATION;
        stepBackName = StepSms.IDENTIFICATION_SUMMARY;
        nextStepName = StepSms.CHOOSE_REGION;
        entityName = translationProperties.getCOUNTRY();
    }

    public String processSmsRequest() {

        if (!isInteger(smsText)) {
            return errorDetect();
        }
        int answer = Integer.parseInt(smsText);
        List<Country> allCountry = locationService.getAllCountry();
        if (answer == allCountry.size() + 1) {
            // step BACK detected
            return back();
        }
        Country country = getLocationsByUserAnswer(allCountry, answer);
        if (country == null) {
            return errorDetect();
        }
        registrationStatisticSms.setCountryCode(country.getCode());
        return showNextStepSms();
    }

    protected String errorDetectAction(String finalErrorText) {
        String msgText;
        SMSRequest smsRequest;
        if (registrationStatisticSms.getWrongAttempts() == 2) {
            msgText = finalErrorText;
            registrationStatisticSms.setIsProcessFinished(true);
            registrationStatisticSms.getSteps().add(StepSms.ENDED_FAILED_ATTEMPTS);
            registrationStatisticSmsService.updateStatisticSms(registrationStatisticSms);
            smsRequest = new SMSRequest(senderNumber, msgText);
            ozekiApiCaller.sendMessage(smsRequest);
            return msgText;
        }

        StepChooseGeographicalObjectService stepIdentificationSummaryService = allSmsStepsService.getStepServiceByName(stepBackName);
        stepIdentificationSummaryService.initStep(smsText, senderNumber, registrationStatisticSms);
        msgText = stepIdentificationSummaryService.getSmsTextModifyData();

        registrationStatisticSms.setWrongAttempts(registrationStatisticSms.getWrongAttempts() + 1);
        registrationStatisticSmsService.updateStatisticSms(registrationStatisticSms);
        smsRequest = new SMSRequest(senderNumber, msgText);
        ozekiApiCaller.sendMessage(smsRequest);
        return msgText;
    }

    public String getSmsText() {
        List<Region> allRegions = locationService.getAllRegionByCountryCode(registrationStatisticSms.getCountryCode());
        return displayOptionList(allRegions, allSmsStepsService.getStepServiceByName(nextStepName).getEntityName());
    }

}
