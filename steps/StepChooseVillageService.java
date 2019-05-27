package ua.com.lineup.enrollmentSystem.service.sms.steps;

import org.springframework.stereotype.Service;
import ua.com.lineup.enrollmentSystem.model.entity.organizationalstructure.Place;
import ua.com.lineup.enrollmentSystem.model.entity.organizationalstructure.Village;
import ua.com.lineup.enrollmentSystem.model.entity.statistic.RegistrationStatisticSms;
import ua.com.lineup.enrollmentSystem.model.entity.statistic.enumeration.StepSms;
import ua.com.lineup.enrollmentSystem.properties.TranslationProperties;
import ua.com.lineup.enrollmentSystem.service.client.OzekiApiCaller;
import ua.com.lineup.enrollmentSystem.service.organizationalstructure.LocationService;
import ua.com.lineup.enrollmentSystem.service.statistic.RegistrationStatisticSmsService;
import ua.com.lineup.enrollmentSystem.service.statistic.SessionStatisticService;
import ua.com.lineup.enrollmentSystem.service.userbiometric.CitizenService;

import java.util.List;

@Service
public class StepChooseVillageService extends StepChooseGeographicalObjectService {

    public StepChooseVillageService(RegistrationStatisticSmsService registrationStatisticSmsService,
                                    OzekiApiCaller ozekiApiCaller, CitizenService citizenService,
                                    LocationService locationService, SessionStatisticService sessionStatisticService,
                                    TranslationProperties translationProperties) {
        super(registrationStatisticSmsService, ozekiApiCaller, citizenService, locationService, sessionStatisticService, translationProperties);
        stepBackDisplayMsg = StepSms.CHOOSE_CITY;
        stepBackName = StepSms.CHOOSE_DISTRICT;
        nextStepName = StepSms.CHOOSE_PLACE;
        entityName = translationProperties.getVILLAGE();
    }

    public String processSmsRequest() {

        if (!isInteger(smsText)) {
            return errorDetect();
        }
        int answer = Integer.parseInt(smsText);
        List<Village> allVillages = locationService.getAllVillageByDistrictId(registrationStatisticSms.getDistrictId());
        if (answer == allVillages.size() + 1) {
            // step BACK detected
            return back();
        }
        Village village = getLocationsByUserAnswer(allVillages, answer);
        if (village == null) {
            return errorDetect();
        }
        registrationStatisticSms.setVillageId(village.getId());
        return showNextStepSms();
    }


    public String getSmsText() {
        List<Place> allPlace = locationService.getAllPlaceByVillageId(registrationStatisticSms.getVillageId());
        return displayOptionList(allPlace, allSmsStepsService.getStepServiceByName(nextStepName).getEntityName());
    }

    public StepChooseGeographicalObjectService initStep(String smsText, String senderNumber, RegistrationStatisticSms registrationStatisticSms) {

        if (CITY_IDS_WITH_DISTRICTS.contains(registrationStatisticSms.getCityId())) {
            stepBackDisplayMsg = StepSms.CHOOSE_CITY;
            stepBackName = StepSms.CHOOSE_DISTRICT;
        } else {
            stepBackDisplayMsg = StepSms.CHOOSE_PROVINCE;
            stepBackName = StepSms.CHOOSE_CITY;
        }
        return super.initStep(smsText, senderNumber, registrationStatisticSms);
    }

}
