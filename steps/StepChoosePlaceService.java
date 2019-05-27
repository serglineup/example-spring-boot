package ua.com.lineup.enrollmentSystem.service.sms.steps;

import org.springframework.stereotype.Service;
import ua.com.lineup.enrollmentSystem.model.entity.organizationalstructure.Place;
import ua.com.lineup.enrollmentSystem.model.entity.organizationalstructure.SpecificLocation;
import ua.com.lineup.enrollmentSystem.model.entity.statistic.RegistrationStatisticSms;
import ua.com.lineup.enrollmentSystem.model.entity.statistic.SessionStatistic;
import ua.com.lineup.enrollmentSystem.model.entity.statistic.enumeration.StepSms;
import ua.com.lineup.enrollmentSystem.properties.TranslationProperties;
import ua.com.lineup.enrollmentSystem.service.client.OzekiApiCaller;
import ua.com.lineup.enrollmentSystem.service.organizationalstructure.LocationService;
import ua.com.lineup.enrollmentSystem.service.statistic.RegistrationStatisticSmsService;
import ua.com.lineup.enrollmentSystem.service.statistic.SessionStatisticService;
import ua.com.lineup.enrollmentSystem.service.userbiometric.CitizenService;

import java.util.List;

@Service
public class StepChoosePlaceService extends StepChooseGeographicalObjectService {

    public StepChoosePlaceService(RegistrationStatisticSmsService registrationStatisticSmsService, OzekiApiCaller ozekiApiCaller, CitizenService citizenService, LocationService locationService,
                                  SessionStatisticService sessionStatisticService, TranslationProperties translationProperties) {
        super(registrationStatisticSmsService, ozekiApiCaller, citizenService, locationService, sessionStatisticService, translationProperties);
        nextStepName = StepSms.CHOOSE_SPECIFIC_LOCATION;
        entityName = translationProperties.getPLACE();
    }

    public String processSmsRequest() {

        if (!isInteger(smsText)) {
            return errorDetect();
        }
        int answer = Integer.parseInt(smsText);
        List<Place> allPlaces = locationService.getAllPlaceByVillageId(registrationStatisticSms.getVillageId());
        if (answer == allPlaces.size() + 1) {
            // step BACK detected
            return back();
        }
        Place place = getLocationsByUserAnswer(allPlaces, answer);
        if (place == null) {
            return errorDetect();
        }
        SessionStatistic sessionStatistic = sessionStatisticService.findSession(registrationStatisticSms.getSessionStatisticId());
        sessionStatistic.setPlaceId(place.getId());
        sessionStatisticService.updateSession(sessionStatistic);
        registrationStatisticSms.setPlaceId(place.getId());
        return showNextStepSms();
    }


    public String getSmsText() {
        List<SpecificLocation> allSpecificLocations = locationService.getAllLocationByPlaceId(registrationStatisticSms.getPlaceId());
        return displayOptionList(allSpecificLocations,allSmsStepsService.getStepServiceByName(nextStepName).getEntityName());
    }

    public StepChooseGeographicalObjectService initStep(String smsText, String senderNumber, RegistrationStatisticSms registrationStatisticSms) {

        if(registrationStatisticSms.isSummaryConfirm()){
            stepBackDisplayMsg = StepSms.PROCESS_CNIB_PASSPORT_VERIFICATION;
            stepBackName = StepSms.IDENTIFICATION_SUMMARY;
        }else{
            stepBackDisplayMsg = StepSms.CHOOSE_DISTRICT;
            stepBackName = StepSms.CHOOSE_SECTOR_OR_VILLAGE;
        }
        return super.initStep( smsText,  senderNumber,  registrationStatisticSms);
    }

}
